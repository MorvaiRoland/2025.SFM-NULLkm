package drivesync.SajatAutok;

import drivesync.Adatbazis.Database;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.*;
import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class SajatAutokControllerTest {

    // Belső osztály: Elnyomja a felugró ablakokat
    static class TestableSajatAutokController extends SajatAutokController {
        @Override
        protected void showAlert(String title, String message) {
            System.out.println("TEST ALERT [SUPPRESSED]: " + title + " - " + message);
        }
    }

    private TestableSajatAutokController controller;
    private MockedStatic<Database> mockedDatabase;
    private Connection mockConnection;
    private PreparedStatement mockStatement;
    private ResultSet mockResultSet;

    @BeforeAll
    static void initJfx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // JavaFX már fut
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        controller = new TestableSajatAutokController();

        // --- UI Elemek Injektálása ---
        injectField(controller, "licenseField", new TextField());
        injectField(controller, "kmField", new TextField());
        injectField(controller, "brandCombo", new ComboBox<String>());
        injectField(controller, "typeCombo", new ComboBox<String>());
        injectField(controller, "engineTypeCombo", new ComboBox<String>());
        injectField(controller, "fuelTypeField", new ChoiceBox<String>());
        injectField(controller, "vintageField", new ChoiceBox<Integer>());
        injectField(controller, "oilTypeCombo", new ComboBox<String>());
        injectField(controller, "oilQuantityChoice", new ChoiceBox<String>());
        injectField(controller, "tireSizeCombo", new ComboBox<String>());
        injectField(controller, "colorPicker", new ColorPicker(Color.WHITE));
        injectField(controller, "insuranceDatePicker", new DatePicker());
        injectField(controller, "inspection_date", new DatePicker());
        injectField(controller, "notesField", new TextArea());
        injectField(controller, "carsList", new FlowPane());

        // --- HIÁNYZÓ ELEMEK PÓTLÁSA (EZEK OKOZTÁK A HIBÁT) ---
        injectField(controller, "servicesContainer", new VBox()); // Ez volt a NULL
        injectField(controller, "carDetailsPane", new TitledPane()); // Ez is kellhet a részletekhez
        injectField(controller, "carDetailsLabel", new Label());     // Ez is
        injectField(controller, "selectedCarLabel", new Label());    // Ez is
        // -----------------------------------------------------

        injectField(controller, "serviceTypeCombo", new ComboBox<String>());
        injectField(controller, "serviceKmField", new TextField());
        injectField(controller, "servicePriceField", new TextField());
        injectField(controller, "replacedPartsField", new TextField());
        injectField(controller, "serviceDatePicker", new DatePicker());
        injectField(controller, "serviceListView", new ListView<String>());

        // Listák inicializálása
        Platform.runLater(() -> {
            ((ComboBox<String>) getFieldSilently(controller, "brandCombo")).setItems(FXCollections.observableArrayList());
            ((ComboBox<String>) getFieldSilently(controller, "typeCombo")).setItems(FXCollections.observableArrayList());
            ((ComboBox<String>) getFieldSilently(controller, "engineTypeCombo")).setItems(FXCollections.observableArrayList());
        });
        waitForRunLater();

        // --- Adatbázis Mockolás ---
        mockConnection = mock(Connection.class);
        mockStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        mockedDatabase = Mockito.mockStatic(Database.class);
        mockedDatabase.when(Database::getConnection).thenReturn(mockConnection);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockConnection.prepareStatement(anyString(), anyInt())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockStatement.getGeneratedKeys()).thenReturn(mockResultSet);
    }

    @AfterEach
    void tearDown() {
        // Mock bezárása nagyon fontos!
        mockedDatabase.close();
    }

    @Test

    void testAddCar() throws Exception {
        // GIVEN - Adatok beállítása (UI update mehet runLaterben)
        Platform.runLater(() -> {
            ((TextField) getFieldSilently(controller, "licenseField")).setText("ABC-123");
            ((TextField) getFieldSilently(controller, "kmField")).setText("150000");
            ((ComboBox<String>) getFieldSilently(controller, "brandCombo")).getEditor().setText("BMW");
            ((ComboBox<String>) getFieldSilently(controller, "typeCombo")).getEditor().setText("X5");
            ((ComboBox<String>) getFieldSilently(controller, "engineTypeCombo")).getEditor().setText("3.0 Diesel");
            ((ChoiceBox<String>) getFieldSilently(controller, "fuelTypeField")).setValue("Dízel");
            ((ChoiceBox<Integer>) getFieldSilently(controller, "vintageField")).setValue(2020);
        });
        waitForRunLater();

        injectField(controller, "username", "testuser");

        // WHEN
        controller.addCar();

        // THEN
        // JAVÍTÁS: times(2)-t használunk, mert az addCar és a loadUserCars is beállítja a nevet
        verify(mockStatement, times(2)).setString(eq(1), eq("testuser"));

        // A többi verify maradhat, vagy azokat is igazítani kell, ha azok is duplázódnak:
        verify(mockStatement).setString(eq(2), eq("ABC-123")); // Rendszám (ez csak egyszer van)
        verify(mockStatement).setString(eq(3), eq("BMW"));     // Márka (ez is csak egyszer)

        verify(mockStatement, atLeastOnce()).executeUpdate();
    }

    @Test

    void testLoadEnginesForModel() throws Exception {
        // GIVEN
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getString("engine_name")).thenReturn("1.6 Benzín", "2.0 Diesel");

        Method method = SajatAutokController.class.getDeclaredMethod("loadEnginesForModel", String.class, String.class);
        method.setAccessible(true);

        // WHEN - JAVÍTÁS: Reflection hívás közvetlenül, runLater nélkül
        try {
            method.invoke(controller, "Opel", "Astra");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // THEN
        ComboBox<String> engineCombo = getFieldSilently(controller, "engineTypeCombo");
        assertEquals(2, engineCombo.getItems().size());
        assertTrue(engineCombo.getItems().contains("1.6 Benzín"));
    }

    @Test

    void testLoadUserCars() throws Exception {
        // GIVEN
        injectField(controller, "username", "Elek");

        // Mock adatok: FONTOS, hogy minden mező meglegyen, amit a createCarCard olvas!
        // A ciklus vezérlése:
        when(mockResultSet.next()).thenReturn(true, false); // 1. hívás: van adat, 2. hívás: vége

        // Oszlopok (ügyelj a típusokra!):
        when(mockResultSet.getInt("id")).thenReturn(101);
        when(mockResultSet.getString("brand")).thenReturn("Suzuki");
        when(mockResultSet.getString("type")).thenReturn("Swift");
        when(mockResultSet.getString("license")).thenReturn("AAA-000");
        when(mockResultSet.getInt("vintage")).thenReturn(2005);
        when(mockResultSet.getInt("km")).thenReturn(100000);

        Method loadMethod = SajatAutokController.class.getDeclaredMethod("loadUserCars");
        loadMethod.setAccessible(true);

        // WHEN - Közvetlen hívás a fő szálon (hogy lássa a mock adatbázist)
        try {
            loadMethod.invoke(controller);
        } catch (Exception e) {
            // Ha itt száll el, látni fogjuk a stack trace-t
            e.printStackTrace();
            throw new RuntimeException("Hiba a loadUserCars hívásakor: " + e.getMessage());
        }

        // FONTOS: Várunk egy kicsit, hogy a JavaFX szálon esetlegesen futó eseménykezelők lefussanak
        waitForRunLater();

        // THEN
        FlowPane carsList = getFieldSilently(controller, "carsList");

        // Ha ez 0, nézd meg a konzolt: Írt-e ki "TEST ALERT [SUPPRESSED]" üzenetet?
        // Ha igen, akkor a controller catch ága futott le valami hiba miatt.
        assertEquals(1, carsList.getChildren().size(), "A listának tartalmaznia kellene az autót!");

        VBox carCard = (VBox) carsList.getChildren().get(0);
        assertEquals(101, carCard.getUserData());
    }

    @Test

    void testSaveServiceWithNewType() throws Exception {
        // GIVEN
        injectField(controller, "selectedCarId", 5);

        Platform.runLater(() -> {
            ((ComboBox<String>) getFieldSilently(controller, "serviceTypeCombo")).getEditor().setText("Teljes Generál");
            ((TextField) getFieldSilently(controller, "serviceKmField")).setText("200000");
            ((TextField) getFieldSilently(controller, "servicePriceField")).setText("50000");
            ((DatePicker) getFieldSilently(controller, "serviceDatePicker")).setValue(LocalDate.now());
        });
        waitForRunLater();

        // Mock Config
        ResultSet checkRs = mock(ResultSet.class);
        ResultSet keysRs = mock(ResultSet.class);

        when(checkRs.next()).thenReturn(false); // Nincs ilyen típus
        when(keysRs.next()).thenReturn(true);   // Van generált kulcs
        when(keysRs.getInt(1)).thenReturn(99);  // Új ID

        when(mockStatement.executeQuery()).thenReturn(checkRs);
        when(mockStatement.getGeneratedKeys()).thenReturn(keysRs);

        // WHEN - JAVÍTÁS: Közvetlen hívás
        controller.saveService();

        // THEN
        verify(mockConnection).prepareStatement("INSERT INTO service_types (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
        verify(mockStatement).setInt(eq(2), eq(99)); // Használta az új ID-t
        verify(mockStatement, atLeastOnce()).executeUpdate();
    }

    // --- SEGÉD METÓDUSOK ---

    private void injectField(Object target, String fieldName, Object value) throws Exception {
        // Keresés a hierarchiában felfelé, mivel a target egy alosztály (Testable...)
        Class<?> clazz = target.getClass();
        Field field = null;
        while (clazz != null) {
            try {
                field = clazz.getDeclaredField(fieldName);
                break;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        if (field != null) {
            field.setAccessible(true);
            field.set(target, value);
        } else {
            throw new NoSuchFieldException(fieldName);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getFieldSilently(Object target, String fieldName) {
        try {
            Class<?> clazz = target.getClass();
            Field field = null;
            while (clazz != null) {
                try {
                    field = clazz.getDeclaredField(fieldName);
                    break;
                } catch (NoSuchFieldException e) {
                    clazz = clazz.getSuperclass();
                }
            }
            if (field != null) {
                field.setAccessible(true);
                return (T) field.get(target);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private void waitForRunLater() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(latch::countDown);
        if (!latch.await(2, TimeUnit.SECONDS)) {
            throw new RuntimeException("JavaFX timeout");
        }
    }
}