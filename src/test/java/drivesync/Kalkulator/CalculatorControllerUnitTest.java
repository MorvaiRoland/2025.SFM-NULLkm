package drivesync.Kalkulator;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.util.concurrent.CountDownLatch;

import javafx.scene.control.*;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

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
    public void testVehicleTax() throws  Exception{
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            TextField horsepower = getField(controller, "HorsePower", TextField.class);
            TextField productionYear = getField(controller, "ProductionYear", TextField.class);
            Label resultLabel1 = getField(controller, "resultLabel1", Label.class);
            Button calcButton1 = getField(controller, "calcButton1", Button.class);

            horsepower.setText("85");
            productionYear.setText("2015");
            calcButton1.fire();

            // Várt eredmény: 85 * 230 Ft = 19550 Ft. Kora: 10 év.
            String expectedPart1 = "Adó:";
            String expectedPart2 = "19"; // 19 550 vagy 19,550
            String expectedPart3 = "Kora: 10 év";

            String actualText = resultLabel1.getText();

            // Ellenőrizzük a kulcsrészeket: az Adó előtagot, a számot, és az életkort
            boolean success = actualText.contains(expectedPart1) &&
                    actualText.contains(expectedPart2) &&
                    actualText.contains(expectedPart3);

            assertTrue(success,
                    "Hiba: A járműadó számítás helytelen. Aktuális tartalom: " + actualText +
                            ". Elvárt: 'Adó: 19(xxx) Ft/év | Kora: 10 év'");

            latch.countDown();
        });
        latch.await();
    }

    @Test
    public void testMandatoryInsurance() throws Exception{
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {

            TextField BaseFee = getField(controller, "BaseFee", TextField.class);
            TextField PowerFactor = getField(controller, "PowerFactor", TextField.class);
            TextField ProductionYear1 = getField(controller, "ProductionYear1", TextField.class);
            ComboBox<String> BonusMalus = getField(controller, "BonusMalus", ComboBox.class);
            Label resultLabel2 = getField(controller, "resultLabel2", Label.class);
            Button calcButton2 = getField(controller, "calcButton2", Button.class);

            BaseFee.setText("50000");      // Alapdíj
            PowerFactor.setText("100");    // 100 kW (Teljesítmény szorzó: 1.4)
            ProductionYear1.setText("2018"); // 2025 - 2018 = 7 év (Kor szorzó: 1.0)

            BonusMalus.setValue("B10");    // Bónusz-Málusz (B10 szorzó: 0.5)

            calcButton2.fire();

            // Várt eredmény: 50000 * 1.4 * 1.0 * 0.5 = 35000 Ft
            String expectedClean = "Biztosítás:35000Ft/év"; // Várt szöveg, szóközök nélkül.
            String actualText = getField(controller, "resultLabel2", Label.class).getText();

            // 1. Tisztítás
            String actualClean = actualText.replaceAll("[\\s\\u00A0]+", "");

            // 2. Ellenőrzés a megtisztított stringeken
            assertTrue(actualClean.contains(expectedClean),
                    "Hiba: A kötelező biztosítás számítás hibás. Aktuális (tisztítva): " + actualClean +
                            ". Elvárt (tisztítva): " + expectedClean);

            latch.countDown();
        });
    }
    @Test
    public void testLeasing() throws Exception{
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            TextField vehiclePrice = getField(controller, "vehiclePrice", TextField.class);
            TextField downPayment = getField(controller, "downPayment", TextField.class);
            TextField leaseTerm = getField(controller, "leaseTerm", TextField.class);
            TextField interestRate = getField(controller, "interestRate", TextField.class);
            Label resultLabel3 = getField(controller, "resultLabel3", Label.class);
            Button calcButton3 = getField(controller, "calcButton3", Button.class);

            // Bemeneti adatok beállítása: 1M Ft, 20% önrész, 12 hónap, 12% éves kamat
            vehiclePrice.setText("1000000");
            downPayment.setText("20");
            leaseTerm.setText("12");
            interestRate.setText("12");

            calcButton3.fire();

            // VÁRT EREDMÉNY
            // 71079 Ft | 1052948 Ft | 52948 Ft
            String expectedClean = "Havi:71079Ft|Össz:1052948Ft|Kamat:52948Ft"; // A "kb." kimaradt, ami helyes!

            String actualText = resultLabel3.getText();

            String actualClean = actualText.replaceAll("[\\s\\u00A0]+", "");

            assertTrue(actualClean.contains(expectedClean),
                    "Hiba: A lízing kalkuláció hibás. Aktuális (tisztítva): " + actualClean +
                            " | Elvárt (tisztítva): " + expectedClean);

            latch.countDown();
        });
    }

    @Test
    public void testDepreciaton() throws Exception{
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {

            TextField purchasePrice = getField(controller, "purchasePrice", TextField.class);
            TextField currentAge = getField(controller, "currentAge", TextField.class);
            TextField lifespan = getField(controller, "lifespan", TextField.class);
            Label resultLabel4 = getField(controller, "resultLabel4", Label.class);
            Button calcButton4 = getField(controller, "calcButton4", Button.class);

            purchasePrice.setText("1000000");
            currentAge.setText("3");
            lifespan.setText("10");

            calcButton4.fire();

            // Várt eredmény
            // Értékek: 700000 Ft, 100000 Ft, 30.0%

            String expectedText = "Jelenlegi érték: 700000 Ft | Évente: 100000 Ft | 30.0%";
            String actualText = resultLabel4.getText();

            //Tisztítás
            String actualClean = actualText.replaceAll("[\\s\\u00A0,]+", "");
            String expectedClean = expectedText.replaceAll("[\\s\\u00A0,.]+", ""); // Eltávolítjuk a pontot, vesszőt, szóközt

            assertTrue(actualClean.contains(expectedClean),
                    "Hiba: Az amortizáció számítás hibás. Aktuális (tisztítva): " + actualClean +
                            " | Elvárt (tisztítva): " + expectedClean);

            latch.countDown();
        });
    }

    @Test
    public void testCostPerKm() throws Exception{
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {

            TextField annualKm = getField(controller, "annualKm", TextField.class);
            TextField fuelCostYear = getField(controller, "fuelCostYear", TextField.class);
            TextField insuranceCost = getField(controller, "insuranceCost", TextField.class);
            TextField taxCost = getField(controller, "taxCost", TextField.class);
            TextField maintenanceCost = getField(controller, "maintenanceCost", TextField.class);
            Label resultLabel5 = getField(controller, "resultLabel5", Label.class);
            Button calcButton5 = getField(controller, "calcButton5", Button.class);

            // Bemeneti adatok beállítása
            annualKm.setText("20000");
            fuelCostYear.setText("400000");
            insuranceCost.setText("100000");
            taxCost.setText("50000");
            maintenanceCost.setText("50000");

            calcButton5.fire();

            // Várt eredmény: Össz éves: 600 000 Ft | Per km: 30.00 Ft

            String expectedClean = "Összéves:600000Ft|Perkm:3000Ft";

            String actualText = resultLabel5.getText();

            // Tisztítás
            String actualClean = actualText.replaceAll("[\\s\\u00A0,.]+", "");

            assertTrue(actualClean.contains(expectedClean),
                    "Hiba: A km-enkénti költség számítás hibás. Aktuális (tisztítva): " + actualClean +
                            " | Elvárt (tisztítva): " + expectedClean);

            latch.countDown();
        });
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

    @Test
    public void testPetrolVsDiesel() throws  Exception{
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            // Mezők lekérése
            TextField priceDifference = getField(controller, "priceDifference", TextField.class);
            TextField petrolConsumption = getField(controller, "petrolConsumption", TextField.class);
            TextField dieselConsumption = getField(controller, "dieselConsumption", TextField.class);
            TextField petrolPrice = getField(controller, "petrolPrice", TextField.class);
            TextField dieselPrice = getField(controller, "dieselPrice", TextField.class);
            Label resultLabel7 = getField(controller, "resultLabel7", Label.class);
            Button calcButton7 = getField(controller, "calcButton7", Button.class);

            // Bemeneti adatok beállítása
            priceDifference.setText("300000"); // 300e Ft árkülönbség
            petrolConsumption.setText("7.0");
            dieselConsumption.setText("5.5");
            petrolPrice.setText("600");
            dieselPrice.setText("600");

            calcButton7.fire();

            // Várt eredmény: Megtérülés: 33 333 km | 2.2 év | Spórol: 900 Ft/100km

            // A kimenet formátuma: Megtérülés: %,d km | %.1f év | Spórol: %.0f Ft/100km

            // Várt értékek tisztítva (szóközök, tizedesvessző/pont nélkül).
            String expectedClean = "Megtérülés:33333km|22év|Spórol:900Ft/100km";

            String actualText = resultLabel7.getText();

            // Tisztítás: Eltávolítunk minden szóköz karaktert ÉS a tizedes elválasztókat (pont/vessző)
            String actualClean = actualText.replaceAll("[\\s\\u00A0,.]+", "");

            // Ellenőrzés a tisztított stringen
            assertTrue(actualClean.contains(expectedClean),
                    "Hiba: A benzin-dízel megtérülés számítás hibás. Aktuális (tisztítva): " + actualClean +
                            " | Elvárt (tisztítva): " + expectedClean);

            latch.countDown();
        });
    }

    @Test
    public void testTireCost() throws Exception{
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {

            TextField tireSetPrice = getField(controller, "tireSetPrice", TextField.class);
            TextField tireLifespan = getField(controller, "tireLifespan", TextField.class);
            TextField annualKmTire = getField(controller, "annualKmTire", TextField.class);
            Label resultLabel8 = getField(controller, "resultLabel8", Label.class);
            Button calcButton8 = getField(controller, "calcButton8", Button.class);

            // Bemeneti adatok beállítása
            tireSetPrice.setText("120000");
            tireLifespan.setText("40000");
            annualKmTire.setText("10000");

            calcButton8.fire();

            // Várt eredmény: Évente: 30 000 Ft | Per km: 3.00 Ft | Csere: 4.0 év

            String expectedClean = "Évente:30000Ft|Perkm:300Ft|Csere:40év";

            String actualText = resultLabel8.getText();

            // Tisztítás:
            String actualClean = actualText.replaceAll("[\\s\\u00A0,.]+", "");

            assertTrue(actualClean.contains(expectedClean),
                    "Hiba: A gumiabroncs költség számítás hibás. Aktuális (tisztítva): " + actualClean +
                            " | Elvárt (tisztítva): " + expectedClean);

            latch.countDown();
        });
    }

}

