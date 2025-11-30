package drivesync.Költségvetés;

import drivesync.Adatbázis.Database;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.embed.swing.JFXPanel;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.*;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class BudgetControllerTest {

    private BudgetController controller;

    @Mock private Connection mockConnection;
    @Mock private PreparedStatement mockStatement;
    @Mock private ResultSet mockResultSet;

    private MockedStatic<Database> mockedDatabase;

    @BeforeAll
    static void initJFX() {
        // JavaFX Toolkit indítása (egyszer fut le az elején)
        new JFXPanel();
        // Kikapcsoljuk az implicit exit-et, hogy ne álljon le a toolkit az első teszt után
        Platform.setImplicitExit(false);
    }

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // 1. Adatbázis statikus mock beállítása
        mockedDatabase = mockStatic(Database.class);
        mockedDatabase.when(Database::getConnection).thenReturn(mockConnection);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockStatement.executeUpdate()).thenReturn(1);

        // Fontos: Alapértelmezetten ne legyen adat a ResultSet-ben,
        // különben a setup közbeni refreshExpenses végtelen ciklusba vagy hibába futhat.
        when(mockResultSet.next()).thenReturn(false);

        // 2. Controller és UI elemek létrehozása a JavaFX szálon
        runAndWait(() -> {
            // Használjuk a controller beépített connectionSupplier-ét, így nem igényel valódi DB-t
            BudgetController.setConnectionSupplier(() -> mockConnection);
            controller = new BudgetController();

            // UI Elemek kézi inicializálása (Mivel nincs FXML loader)
            controller.txt_what = new TextField();
            controller.txt_amount = new TextField();
            controller.txt_date = new DatePicker();
            controller.categoryBox = new ComboBox<>();
            controller.yearBox = new ComboBox<>();
            controller.filterBox = new ComboBox<>();
            controller.filterCategoryBox = new ComboBox<>();

            controller.msg = new Label();
            controller.monthlyAmount = new Label();
            controller.yearlyAmount = new Label();
            controller.saveBtn = new Button();

            // Chart-ok inicializálása elengedhetetlen a refreshExpenses hívás előtt
            controller.months = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis();
            controller.monthlyChart = new BarChart<>(controller.months, yAxis);
            controller.trendChart = new LineChart<>(new CategoryAxis(), new NumberAxis());

            controller.table = new TableView<>();
            setPrivateField(controller, "colWhat", new TableColumn<>());
            setPrivateField(controller, "colAmount", new TableColumn<>());
            setPrivateField(controller, "colCategory", new TableColumn<>());
            setPrivateField(controller, "colDate", new TableColumn<>());
            setPrivateField(controller, "colEdit", new TableColumn<>());
            setPrivateField(controller, "colDelete", new TableColumn<>());

            // 3. Controller inicializálása
            // Először initialize(), hogy a listenerek létrejöjjenek
            controller.initialize();

            // Majd setUsername(), ami elindítja az adatbetöltést
            // Mivel a mockResultSet üres (false), ez nem fog hibát dobni
            controller.setUsername("testuser");
        });
    }

    @AfterEach
    void tearDown() {
        // Mock lezárása minden teszt után
        if (mockedDatabase != null) {
            mockedDatabase.close();
        }
        // Visszaállítás: ne szivárogjon a connection supplier más tesztekre
        BudgetController.setConnectionSupplier(null);
    }

    @Test
    @DisplayName("Mentés sikeres valid adatokkal")
    void testSaveData_Success() throws Exception {
        // GIVEN
        runAndWait(() -> {
            controller.txt_what.setText("Tankolás");
            controller.txt_amount.setText("5000");
            controller.txt_date.setValue(LocalDate.now());
            controller.categoryBox.setItems(FXCollections.observableArrayList("Üzemanyag"));
            controller.categoryBox.setValue("Üzemanyag");
        });

        // WHEN
        runAndWait(() -> controller.saveData());

        // THEN
        // Ellenőrizzük, hogy meghívódott-e az adatbázis
        verify(mockStatement, times(1)).executeUpdate();
        verify(mockStatement).setString(1, "Tankolás");
        verify(mockStatement).setInt(2, 5000);

        // Ellenőrizzük UI állapotát (FX szálon a biztonság kedvéért)
        runAndWait(() -> {
            assertTrue(controller.txt_what.getText().isEmpty(), "A mezőnek törlődnie kell mentés után");
        });
    }

    @Test
    @DisplayName("Mentés sikertelen, ha hiányzik mező")
    void testSaveData_MissingField() throws Exception {
        // GIVEN
        runAndWait(() -> {
            controller.txt_what.setText(""); // Üres
            controller.txt_amount.setText("5000");
            controller.txt_date.setValue(LocalDate.now());
            controller.categoryBox.setValue("Egyéb");
        });

        // WHEN
        runAndWait(() -> controller.saveData());

        // THEN
        verify(mockStatement, never()).executeUpdate();

        runAndWait(() -> {
            assertFalse(controller.msg.getText().isEmpty(), "Hibaüzenetnek kell megjelennie");
        });
    }

    @Test
    @DisplayName("Mentés sikertelen, ha az összeg nem szám")
    void testSaveData_InvalidAmount() throws Exception {
        // GIVEN
        runAndWait(() -> {
            controller.txt_what.setText("Szervíz");
            controller.txt_amount.setText("sok pénz"); // Hiba
            controller.txt_date.setValue(LocalDate.now());
            controller.categoryBox.setValue("Szervíz");
        });

        // WHEN
        runAndWait(() -> controller.saveData());

        // THEN
        verify(mockStatement, never()).executeUpdate();
    }

    // --- Segédfüggvények ---

    /**
     * Futtat egy Runnable-t a JavaFX szálon, és megvárja amíg befejeződik.
     * Ha kivétel történik a szálon, azt továbbdobja a tesztnek.
     */
    private void runAndWait(Runnable action) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Throwable[] exceptionHolder = new Throwable[1];

        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable t) {
                exceptionHolder[0] = t;
                t.printStackTrace(); // Kiírjuk a konzolra a hibát
            } finally {
                latch.countDown();
            }
        });

        // Várunk max 5 másodpercet
        if (!latch.await(5, TimeUnit.SECONDS)) {
            throw new RuntimeException("Időtúllépés: A JavaFX szál nem válaszolt.");
        }

        // Ha hiba volt bent, dobjuk tovább
        if (exceptionHolder[0] != null) {
            throw new RuntimeException("Hiba a JavaFX szálon: " + exceptionHolder[0].getMessage(), exceptionHolder[0]);
        }
    }

    private void setPrivateField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Nem sikerült beállítani a mezőt: " + fieldName, e);
        }
    }
}