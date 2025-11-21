package drivesync.Kalkulátor;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.util.concurrent.CountDownLatch;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.*;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

public class CalculatorControllerUnitTest {
    private CalculatorController controller;

    @BeforeAll
    public static  void initJavaFX(){
        new JFXPanel();
    }

    @BeforeEach
    public void setup() throws Exception {
        controller = new CalculatorController();

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                // minden @FXML mező reflexióval kerül beállításra
                setField(controller, "avgDistance", new TextField());
                setField(controller, "avgFuelUsed", new TextField());
                setField(controller, "resultLabel9", new Label());
                setField(controller, "calcButton9", new Button());

                setField(controller, "distanceField", new TextField());
                setField(controller, "consumptionField", new TextField());
                setField(controller, "priceField", new TextField());
                setField(controller, "resultLabel", new Label());
                setField(controller, "calcButton", new Button());

                setField(controller, "HorsePower", new TextField());
                setField(controller, "ProductionYear", new TextField());
                setField(controller, "resultLabel1", new Label());
                setField(controller, "calcButton1", new Button());

                setField(controller, "BaseFee", new TextField());
                setField(controller, "PowerFactor", new TextField());
                setField(controller, "ProductionYear1", new TextField());
                setField(controller, "BonusMalus", new ComboBox<String>());
                setField(controller, "resultLabel2", new Label());
                setField(controller, "calcButton2", new Button());

                setField(controller, "vehiclePrice", new TextField());
                setField(controller, "downPayment", new TextField());
                setField(controller, "leaseTerm", new TextField());
                setField(controller, "interestRate", new TextField());
                setField(controller, "resultLabel3", new Label());
                setField(controller, "calcButton3", new Button());

                setField(controller, "purchasePrice", new TextField());
                setField(controller, "currentAge", new TextField());
                setField(controller, "lifespan", new TextField());
                setField(controller, "resultLabel4", new Label());
                setField(controller, "calcButton4", new Button());

                setField(controller, "annualKm", new TextField());
                setField(controller, "fuelCostYear", new TextField());
                setField(controller, "insuranceCost", new TextField());
                setField(controller, "taxCost", new TextField());
                setField(controller, "maintenanceCost", new TextField());
                setField(controller, "resultLabel5", new Label());
                setField(controller, "calcButton5", new Button());

                setField(controller, "vehicleCategory", new ComboBox<String>());
                setField(controller, "vignetteType", new ComboBox<String>());
                setField(controller, "resultLabel6", new Label());
                setField(controller, "calcButton6", new Button());

                setField(controller, "priceDifference", new TextField());
                setField(controller, "petrolConsumption", new TextField());
                setField(controller, "dieselConsumption", new TextField());
                setField(controller, "petrolPrice", new TextField());
                setField(controller, "dieselPrice", new TextField());
                setField(controller, "resultLabel7", new Label());
                setField(controller, "calcButton7", new Button());

                setField(controller, "tireSetPrice", new TextField());
                setField(controller, "tireLifespan", new TextField());
                setField(controller, "annualKmTire", new TextField());
                setField(controller, "resultLabel8", new Label());
                setField(controller, "calcButton8", new Button());

                // invoke private initialize() if present
                try {
                    Method init = controller.getClass().getDeclaredMethod("initialize");
                    init.setAccessible(true);
                    init.invoke(controller);
                } catch (NoSuchMethodException ignored) {
                    // ha nincs initialize, folytatjuk - nem kritikus
                }

                latch.countDown();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        latch.await();
    }

    // Reflexiós segédfüggvények
    private static void setField(Object target, String name, Object value) {
        try {
            Field f = findField(target.getClass(), name);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("setField failed for: " + name, e);
        }
    }

    private static <T> T getField(Object target, String name, Class<T> cls) {
        try {
            Field f = findField(target.getClass(), name);
            f.setAccessible(true);
            return (T) f.get(target);
        } catch (Exception e) {
            throw new RuntimeException("getField failed for: " + name, e);
        }
    }

    // keresi a mezőt az osztályláncban (biztosabb, ha örökölt mezők lennének)
    private static Field findField(Class<?> cls, String name) throws NoSuchFieldException {
        Class<?> cur = cls;
        while (cur != null) {
            try {
                return cur.getDeclaredField(name);
            } catch (NoSuchFieldException ex) {
                cur = cur.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    // ------------------- Tesztek -------------------

    @Test
    public void testAverageConsumption() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            TextField avgDistance = getField(controller, "avgDistance", TextField.class);
            TextField avgFuelUsed = getField(controller, "avgFuelUsed", TextField.class);
            Label resultLabel9 = getField(controller, "resultLabel9", Label.class);
            Button calcButton9 = getField(controller, "calcButton9", Button.class);

            avgDistance.setText("200");
            avgFuelUsed.setText("12");
            calcButton9.fire();
            // Feltételezzük, hogy a controller kimenete a "liter/100km" mértékegységet is tartalmazza
            String expectedComma = "Átlagfogyasztás: 6,00 l/100km";
            String expectedDot = "Átlagfogyasztás: 6.00 l/100km";

            // Ellenőrizd, hogy a kimenet pontosan tartalmazza-e az elvárt szöveget (ponttal vagy vesszővel)
            String actualText = resultLabel9.getText();
            assertTrue(actualText.contains(expectedComma) || actualText.contains(expectedDot),
                    "A fogyasztás formázása nem egyezik. Várt: 6,00/100km vagy 6.00/100km. Aktuális: " + actualText);
            latch.countDown();
        });
        latch.await();
    }

    @Test
    public void testFuelCost() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            TextField distanceField = getField(controller, "distanceField", TextField.class);
            TextField consumptionField = getField(controller, "consumptionField", TextField.class);
            TextField priceField = getField(controller, "priceField", TextField.class);
            Label resultLabel = getField(controller, "resultLabel", Label.class);
            Button calcButton = getField(controller, "calcButton", Button.class);

            distanceField.setText("100");
            consumptionField.setText("7");
            priceField.setText("600");
            calcButton.fire();

            assertTrue(resultLabel.getText().contains("Költség:"));
            latch.countDown();
        });
        latch.await();
    }

    @Test
    public void testTollFee() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            ComboBox vehicleCategory = getField(controller, "vehicleCategory", ComboBox.class);
            ComboBox vignetteType = getField(controller, "vignetteType", ComboBox.class);
            Label resultLabel6 = getField(controller, "resultLabel6", Label.class);
            Button calcButton6 = getField(controller, "calcButton6", Button.class);

            vehicleCategory.setValue("D1 - Személyautó");
            vignetteType.setValue("10 napos");
            calcButton6.fire();

            assertTrue(resultLabel6.getText().contains("Matrica díj:"));
            latch.countDown();
        });
        latch.await();
    }

    // további tesztek átvehetők ugyanezzel a mintával (a korábbi full tesztkészletet beillesztheted)
}

