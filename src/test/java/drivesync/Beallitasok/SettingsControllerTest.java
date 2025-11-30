package drivesync.Beallitasok;

import drivesync.Adatbazis.Database;
import javafx.application.Platform;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class SettingsControllerTest {

    private SettingsController controller;
    private MockedStatic<Database> mockedDatabase;
    private Connection mockConnection;
    private PreparedStatement mockStatement;
    private ResultSet mockResultSet;

    // Egyszer fut le az összes teszt előtt: JavaFX inicializálása
    @BeforeAll
    static void initJfx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // A Platform már elindult, figyelmen kívül hagyjuk
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        controller = new SettingsController();

        // UI Elemek injektálása (Reflection segítségével, mert privátak)
        injectField(controller, "usernameField", new TextField());
        injectField(controller, "emailField", new TextField());
        injectField(controller, "passwordField", new PasswordField());
        injectField(controller, "regDateLabel", new Label());
        injectField(controller, "twoFactorAuth", new CheckBox());
        injectField(controller, "themeChoiceBox", new javafx.scene.control.ChoiceBox<String>());
        injectField(controller, "fontSizeSlider", new javafx.scene.control.Slider());
        injectField(controller, "emailNotifications", new CheckBox());
        injectField(controller, "smsNotifications", new CheckBox());
        injectField(controller, "autoUpdate", new CheckBox());
        injectField(controller, "sendUsageStats", new CheckBox());
        injectField(controller, "enableLogging", new CheckBox());

        // Adatbázis Mockolás előkészítése
        mockConnection = mock(Connection.class);
        mockStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        // A statikus Database osztály mockolása
        mockedDatabase = Mockito.mockStatic(Database.class);
        mockedDatabase.when(Database::getConnection).thenReturn(mockConnection);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
    }

    @AfterEach
    void tearDown() {
        // Fontos: A statikus mockot le kell zárni minden teszt után!
        mockedDatabase.close();
    }

    @Test

    void testLoadUserData() throws Exception {
        // GIVEN - Adatbázis válasz beállítása
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("username")).thenReturn("TesztElek");
        when(mockResultSet.getString("email")).thenReturn("elek@test.com");
        when(mockResultSet.getString("reg_date")).thenReturn("2023-01-01");
        when(mockResultSet.getBoolean("twoFactorAuth_enabled")).thenReturn(true);
        when(mockResultSet.getString("password")).thenReturn("hashed_password_string"); // Normál user

        // WHEN - Metódus hívása
        controller.setUserId(1);

        // A setUserId hívja a loadUserData-t, ami Platform.runLater-t használ.
        // Meg kell várnunk a JavaFX szálat.
        waitForRunLater();

        // THEN - Ellenőrzés, hogy a mezők frissültek-e
        TextField userField = getField(controller, "usernameField");
        TextField emailField = getField(controller, "emailField");
        CheckBox twoFactor = getField(controller, "twoFactorAuth");

        assertEquals("TesztElek", userField.getText());
        assertEquals("elek@test.com", emailField.getText());
        assertTrue(twoFactor.isSelected());

        // Ellenőrizzük, hogy a DB-t lekérdezte
        verify(mockStatement).setInt(1, 1);
        verify(mockStatement).executeQuery();
    }

    @Test

    void testGoogleUserDetection() throws Exception {
        // GIVEN - Google user (nincs jelszó hash vagy rövid)
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("username")).thenReturn("GoogleUser");
        when(mockResultSet.getString("password")).thenReturn(null); // Google login jelzése

        // WHEN
        controller.setUserId(2);
        waitForRunLater();

        // THEN
        PasswordField passField = getField(controller, "passwordField");
        assertTrue(passField.isDisabled(), "A jelszó mezőnek tiltottnak kell lennie Google user esetén");
        assertEquals("Külső azonosítással bejelentkezve", passField.getText());
    }

    @Test

    void testHandleSaveWithoutPassword() throws Exception {
        // GIVEN
        // 1. Mezők beállítása
        ((TextField) getField(controller, "usernameField")).setText("UjNev");
        ((TextField) getField(controller, "emailField")).setText("uj@email.com");
        ((PasswordField) getField(controller, "passwordField")).setText(""); // Üres jelszó = nincs csere

        // 2. FONTOS JAVÍTÁS: A ChoiceBox és Slider értékadása, hogy a savePreferences() ne szálljon el NPE-vel
        javafx.scene.control.ChoiceBox<String> themeBox = getField(controller, "themeChoiceBox");
        // JavaFX-ben a setValue előtt a UI szálon kívül biztonságosabb így:
        Platform.runLater(() -> themeBox.setValue("Rendszer alapértelmezett"));

        javafx.scene.control.Slider fontSizeSlider = getField(controller, "fontSizeSlider");
        Platform.runLater(() -> fontSizeSlider.setValue(14.0));

        // Várjuk meg, amíg a Platform.runLater beállítja az értékeket
        waitForRunLater();

        // 3. Egyéb beállítások
        injectField(controller, "currentUserId", 10);
        injectField(controller, "isGoogleUser", false);

        when(mockStatement.executeUpdate()).thenReturn(1); // Sikeres update szimulálása

        // WHEN - Privát handleSave metódus hívása
        invokePrivateMethod(controller, "handleSave");
        waitForRunLater(); // Megvárjuk a Toast üzenetet és a savePreferences lefutását

        // THEN
        // SQL paraméterek ellenőrzése
        verify(mockStatement).setString(1, "UjNev");
        verify(mockStatement).setString(2, "uj@email.com");
        verify(mockStatement).setBoolean(3, false); // 2FA (default false)
        verify(mockStatement).setInt(4, 10); // ID

        // Ellenőrizzük, hogy NEM hívta meg a jelszavas setString-et a 3-as indexen
        // (A 3-as indexen setBoolean történt, nem setString, így ez a verify never() igaz lesz)
        verify(mockStatement, never()).setString(eq(3), anyString());
    }
    // --- Segédmetódusok (Reflection & Utility) ---

    // Privát mezők írása
    private void injectField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    // Privát mezők olvasása
    @SuppressWarnings("unchecked")
    private <T> T getField(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(target);
    }

    // Privát metódus hívása
    private void invokePrivateMethod(Object target, String methodName) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName);
        method.setAccessible(true);
        method.invoke(target);
    }

    // Várakozás a JavaFX szálra (Platform.runLater miatt)
    private void waitForRunLater() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(latch::countDown);
        if (!latch.await(2, TimeUnit.SECONDS)) {
            throw new RuntimeException("Timeout waiting for JavaFX RunLater");
        }
    }
}