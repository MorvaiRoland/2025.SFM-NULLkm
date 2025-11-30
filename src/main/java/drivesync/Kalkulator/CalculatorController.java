package drivesync.Kalkulator;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.time.LocalDate;

public class CalculatorController {
    // Átlagfogyasztás kalkulátor
    @FXML private TextField avgDistance;
    @FXML private TextField avgFuelUsed;
    @FXML private Label resultLabel9;
    @FXML private Button calcButton9;

    public TextField getAvgDistance() {
        return avgDistance;
    }

    public TextField getAvgFuelUsed() {
        return avgFuelUsed;
    }

    public Label getResultLabel9() {
        return resultLabel9;
    }

    public Button getCalcButton9() {
        return calcButton9;
    }

    // Üzemanyag kalkulátor
    @FXML private TextField distanceField;
    @FXML private TextField consumptionField;
    @FXML private TextField priceField;
    @FXML private Label resultLabel;
    @FXML private Button calcButton;

    public TextField getDistanceField() {
        return distanceField;
    }

    public TextField getConsumptionField() {
        return consumptionField;
    }

    public TextField getPriceField() {
        return priceField;
    }

    public Label getResultLabel() {
        return resultLabel;
    }

    public Button getCalcButton() {
        return calcButton;
    }

    // Gépjárműadó kalkulátor
    @FXML private TextField HorsePower;
    @FXML private TextField ProductionYear;
    @FXML private Label resultLabel1;
    @FXML private Button calcButton1;

    public TextField getHorsePower() {
        return HorsePower;
    }

    public TextField getProductionYear() {
        return ProductionYear;
    }

    public Label getResultLabel1() {
        return resultLabel1;
    }

    public Button getCalcButton1() {
        return calcButton1;
    }

    // Kötelező biztosítás kalkulátor
    @FXML private TextField BaseFee;
    @FXML private TextField PowerFactor;
    @FXML private TextField ProductionYear1;
    @FXML private ComboBox<String> BonusMalus;
    @FXML private Label resultLabel2;
    @FXML private Button calcButton2;

    public TextField getBaseFee() {
        return BaseFee;
    }

    public TextField getPowerFactor() {
        return PowerFactor;
    }

    public TextField getProductionYear1() {
        return ProductionYear1;
    }

    public ComboBox<String> getBonusMalus() {
        return BonusMalus;
    }

    public Label getResultLabel2() {
        return resultLabel2;
    }

    public Button getCalcButton2() {
        return calcButton2;
    }

    // Lízing kalkulátor
    @FXML private TextField vehiclePrice;
    @FXML private TextField downPayment;
    @FXML private TextField leaseTerm;
    @FXML private TextField interestRate;
    @FXML private Label resultLabel3;
    @FXML private Button calcButton3;

    public TextField getVehiclePrice() {
        return vehiclePrice;
    }

    public TextField getDownPayment() {
        return downPayment;
    }

    public TextField getLeaseTerm() {
        return leaseTerm;
    }

    public TextField getInterestRate() {
        return interestRate;
    }

    public Label getResultLabel3() {
        return resultLabel3;
    }

    public Button getCalcButton3() {
        return calcButton3;
    }

    // Amortizáció kalkulátor
    @FXML private TextField purchasePrice;
    @FXML private TextField currentAge;
    @FXML private TextField lifespan;
    @FXML private Label resultLabel4;
    @FXML private Button calcButton4;

    public TextField getPurchasePrice() {
        return purchasePrice;
    }

    public TextField getCurrentAge() {
        return currentAge;
    }

    public TextField getLifespan() {
        return lifespan;
    }

    public Label getResultLabel4() {
        return resultLabel4;
    }

    public Button getCalcButton4() {
        return calcButton4;
    }

    // Költség per kilométer kalkulátor
    @FXML private TextField annualKm;
    @FXML private TextField fuelCostYear;
    @FXML private TextField insuranceCost;
    @FXML private TextField taxCost;
    @FXML private TextField maintenanceCost;
    @FXML private Label resultLabel5;
    @FXML private Button calcButton5;

    public TextField getAnnualKm() {
        return annualKm;
    }

    public TextField getFuelCostYear() {
        return fuelCostYear;
    }

    public TextField getInsuranceCost() {
        return insuranceCost;
    }

    public TextField getTaxCost() {
        return taxCost;
    }

    public TextField getMaintenanceCost() {
        return maintenanceCost;
    }

    public Label getResultLabel5() {
        return resultLabel5;
    }

    public Button getCalcButton5() {
        return calcButton5;
    }

    // Útdíj kalkulátor
    @FXML private ComboBox<String> vehicleCategory;
    @FXML private ComboBox<String> vignetteType;
    @FXML private Label resultLabel6;
    @FXML private Button calcButton6;

    public ComboBox<String> getVehicleCategory() {
        return vehicleCategory;
    }

    public ComboBox<String> getVignetteType() {
        return vignetteType;
    }

    public Label getResultLabel6() {
        return resultLabel6;
    }

    public Button getCalcButton6() {
        return calcButton6;
    }

    // Benzin vs Dízel kalkulátor
    @FXML private TextField priceDifference;
    @FXML private TextField petrolConsumption;
    @FXML private TextField dieselConsumption;
    @FXML private TextField petrolPrice;
    @FXML private TextField dieselPrice;
    @FXML private Label resultLabel7;
    @FXML private Button calcButton7;

    public TextField getPriceDifference() {
        return priceDifference;
    }

    public TextField getPetrolConsumption() {
        return petrolConsumption;
    }

    public TextField getDieselConsumption() {
        return dieselConsumption;
    }

    public TextField getPetrolPrice() {
        return petrolPrice;
    }

    public TextField getDieselPrice() {
        return dieselPrice;
    }

    public Label getResultLabel7() {
        return resultLabel7;
    }

    public Button getCalcButton7() {
        return calcButton7;
    }

    // Gumiabroncs kalkulátor
    @FXML private TextField tireSetPrice;
    @FXML private TextField tireLifespan;
    @FXML private TextField annualKmTire;
    @FXML private Label resultLabel8;
    @FXML private Button calcButton8;

    public TextField getTireSetPrice() {
        return tireSetPrice;
    }

    public TextField getTireLifespan() {
        return tireLifespan;
    }

    public TextField getAnnualKmTire() {
        return annualKmTire;
    }

    public Label getResultLabel8() {
        return resultLabel8;
    }

    public Button getCalcButton8() {
        return calcButton8;
    }

    @FXML
    private void initialize() {
        calcButton9.setOnAction(e -> calculateAverageConsumption());

        // Gomb események beállítása
        calcButton.setOnAction(e -> calculateFuelCost());
        calcButton1.setOnAction(e -> calculateVehicleTax());
        calcButton2.setOnAction(e -> calculateMandatoryInsurance());
        calcButton3.setOnAction(e -> calculateLeasing());
        calcButton4.setOnAction(e -> calculateDepreciation());
        calcButton5.setOnAction(e -> calculateCostPerKm());
        calcButton6.setOnAction(e -> calculateTollFee());
        calcButton7.setOnAction(e -> calculatePetrolVsDiesel());
        calcButton8.setOnAction(e -> calculateTireCost());

        // ComboBox-ok feltöltése
        initializeBonusMalusComboBox();
        initializeVehicleCategoryComboBox();
        initializeVignetteTypeComboBox();
    }


    private void initializeBonusMalusComboBox() {
        BonusMalus.getItems().addAll(
                "M4", "M3", "M2", "M1", "A00",
                "B1", "B2", "B3", "B4", "B5",
                "B6", "B7", "B8", "B9", "B10"
        );
    }

    private void initializeVehicleCategoryComboBox() {
        vehicleCategory.getItems().addAll(
                "D1 - Személyautó",
                "D1M - Motorkerékpár",
                "D2 - Kisteher"
        );
    }

    private void initializeVignetteTypeComboBox() {
        vignetteType.getItems().addAll(
                "10 napos",
                "Havi",
                "Éves"
        );
    }

    // 10. Átlagfogyasztás kalkulátor
    private void calculateAverageConsumption() {
        try {
            double distance = Double.parseDouble(avgDistance.getText());
            double fuel = Double.parseDouble(avgFuelUsed.getText());

            if (distance <= 0 || fuel <= 0) {
                resultLabel9.setText("❌ Érvénytelen adatok!");
                return;
            }

            double avgConsumption = (fuel / distance) * 100.0;
            resultLabel9.setText(String.format("Átlagfogyasztás: %.2f l/100km", avgConsumption));
        } catch (NumberFormatException ex) {
            resultLabel9.setText("❌ Hibás adatok!");
        }
    }



    // 1. Üzemanyag kalkulátor
    private void calculateFuelCost() {
        try {
            double distance = Double.parseDouble(distanceField.getText());
            double consumption = Double.parseDouble(consumptionField.getText());
            double price = Double.parseDouble(priceField.getText());

            double cost = (distance / 100.0) * consumption * price;
            double liters = (distance / 100.0) * consumption;

            resultLabel.setText(String.format("Költség: %,d Ft | %.2f liter", (int)cost, liters));
        } catch (NumberFormatException ex) {
            resultLabel.setText("❌ Hibás adatok!");
        }
    }

    // 2. Gépjárműadó kalkulátor (javított életkor számítással)
    private void calculateVehicleTax() {
        try {
            double power = Double.parseDouble(HorsePower.getText());
            int prodYear = Integer.parseInt(ProductionYear.getText());
            int currentYear = LocalDate.now().getYear();
            int age = currentYear - prodYear;

            double ratePerKw;
            if (age <= 3) {
                ratePerKw = 345;
            } else if (age <= 7) {
                ratePerKw = 300;
            } else if (age <= 11) {
                ratePerKw = 230;
            } else if (age <= 15) {
                ratePerKw = 185;
            } else {
                ratePerKw = 140;
            }

            double result = power * ratePerKw;
            resultLabel1.setText(String.format("Adó: %,d Ft/év | Kora: %d év", (int)result, age));
        } catch (NumberFormatException ex) {
            resultLabel1.setText("❌ Hibás adatok!");
        }
    }

    // 3. Kötelező biztosítás kalkulátor (javított)
    private void calculateMandatoryInsurance() {
        try {
            double base = Double.parseDouble(BaseFee.getText());
            double power = Double.parseDouble(PowerFactor.getText());
            int prodYear = Integer.parseInt(ProductionYear1.getText());
            String bmValue = BonusMalus.getValue();

            if (bmValue == null || bmValue.isEmpty()) {
                resultLabel2.setText("❌ Válassz B/M fokozatot!");
                return;
            }

            // B/M szorzó
            double bm = switch (bmValue) {
                case "M4" -> 4.0;
                case "M3" -> 3.5;
                case "M2" -> 3.0;
                case "M1" -> 2.5;
                case "A00" -> 1.0;
                case "B1" -> 0.95;
                case "B2" -> 0.9;
                case "B3" -> 0.85;
                case "B4" -> 0.8;
                case "B5" -> 0.75;
                case "B6" -> 0.7;
                case "B7" -> 0.65;
                case "B8" -> 0.6;
                case "B9" -> 0.55;
                case "B10" -> 0.5;
                default -> 1.0;
            };

            // Teljesítmény szorzó
            double powerMultiplier;
            if (power <= 37) powerMultiplier = 0.6;
            else if (power <= 50) powerMultiplier = 0.8;
            else if (power <= 70) powerMultiplier = 1.0;
            else if (power <= 90) powerMultiplier = 1.2;
            else if (power <= 120) powerMultiplier = 1.4;
            else if (power <= 150) powerMultiplier = 1.6;
            else if (power <= 180) powerMultiplier = 1.8;
            else if (power <= 200) powerMultiplier = 2.0;
            else if (power <= 250) powerMultiplier = 2.2;
            else powerMultiplier = 2.4;

            // Gyártási év szorzó
            int currentYear = LocalDate.now().getYear();
            int age = currentYear - prodYear;
            double ageMultiplier;
            if (age <= 3) ageMultiplier = 1.2;
            else if (age <= 7) ageMultiplier = 1.0;
            else if (age <= 12) ageMultiplier = 0.9;
            else if (age <= 20) ageMultiplier = 0.8;
            else ageMultiplier = 0.7;

            double result = base * powerMultiplier * ageMultiplier * bm;
            resultLabel2.setText(String.format("Biztosítás: %,d Ft/év", (int)result));

        } catch (NumberFormatException ex) {
            resultLabel2.setText("❌ Hibás adatok!");
        }
    }

    // 4. Lízing kalkulátor
    private void calculateLeasing() {
        try {
            double price = Double.parseDouble(vehiclePrice.getText());
            double downPct = Double.parseDouble(downPayment.getText());
            int months = Integer.parseInt(leaseTerm.getText());
            double annualRate = Double.parseDouble(interestRate.getText());

            double downAmount = price * (downPct / 100.0);
            double loanAmount = price - downAmount;
            double monthlyRate = (annualRate / 100.0) / 12.0;

            // Annuitás számítás
            double monthlyPayment = loanAmount *
                    (monthlyRate * Math.pow(1 + monthlyRate, months)) /
                    (Math.pow(1 + monthlyRate, months) - 1);

            double totalPayment = monthlyPayment * months + downAmount;
            double totalInterest = totalPayment - price;

            resultLabel3.setText(String.format("Havi: %,d Ft | Össz: %,d Ft | Kamat: %,d Ft",
                    (int)monthlyPayment, (int)totalPayment, (int)totalInterest));
        } catch (NumberFormatException ex) {
            resultLabel3.setText("❌ Hibás adatok!");
        }
    }

    // 5. Amortizáció kalkulátor
    private void calculateDepreciation() {
        try {
            double purchase = Double.parseDouble(purchasePrice.getText());
            int age = Integer.parseInt(currentAge.getText());
            int totalLife = Integer.parseInt(lifespan.getText());

            if (age >= totalLife) {
                resultLabel4.setText("❌ A jármű elérte élettartamát!");
                return;
            }

            // Lineáris amortizáció
            double annualDepreciation = purchase / totalLife;
            double currentValue = purchase - (annualDepreciation * age);
            double remainingValue = currentValue;
            double depreciationPct = (age / (double)totalLife) * 100;

            resultLabel4.setText(String.format("Jelenlegi érték: %,d Ft | Évente: %,d Ft | %.1f%%",
                    (int)remainingValue, (int)annualDepreciation, depreciationPct));
        } catch (NumberFormatException ex) {
            resultLabel4.setText("❌ Hibás adatok!");
        }
    }

    // 6. Költség per kilométer kalkulátor
    private void calculateCostPerKm() {
        try {
            double km = Double.parseDouble(annualKm.getText());
            double fuel = Double.parseDouble(fuelCostYear.getText());
            double insurance = Double.parseDouble(insuranceCost.getText());
            double tax = Double.parseDouble(taxCost.getText());
            double maintenance = Double.parseDouble(maintenanceCost.getText());

            double totalAnnual = fuel + insurance + tax + maintenance;
            double costPerKm = totalAnnual / km;

            resultLabel5.setText(String.format("Össz éves: %,d Ft | Per km: %.2f Ft",
                    (int)totalAnnual, costPerKm));
        } catch (NumberFormatException ex) {
            resultLabel5.setText("❌ Hibás adatok!");
        }
    }

    // 7. Útdíj kalkulátor (2024-es magyar árak)
    private void calculateTollFee() {
        try {
            String category = vehicleCategory.getValue();
            String type = vignetteType.getValue();

            if (category == null || type == null) {
                resultLabel6.setText("❌ Válassz kategóriát és típust!");
                return;
            }

            int cost = 0;

            // 2025-es magyar e-matrica árak
            if (category.contains("D1 -")) { // Személyautó
                cost = switch (type) {
                    case "10 napos" -> 6620;
                    case "Havi" -> 10710;
                    case "Éves" -> 59210;
                    default -> 0;
                };
            } else if (category.contains("D1M")) { // Motorkerékpár
                cost = switch (type) {
                    case "10 napos" -> 3310;
                    case "Havi" -> 5360;
                    case "Éves" -> 59210;
                    default -> 0;
                };
            } else if (category.contains("D2")) { // Kisteher
                cost = switch (type) {
                    case "10 napos" -> 9630;
                    case "Havi" -> 15170;
                    case "Éves" -> 84040;
                    default -> 0;
                };
            }

            resultLabel6.setText(String.format("Matrica díj: %,d Ft", cost));
        } catch (Exception ex) {
            resultLabel6.setText("❌ Hiba a számításban!");
        }
    }


    // 8. Benzin vs Dízel megtérülési kalkulátor
    private void calculatePetrolVsDiesel() {
        try {
            double priceDiff = Double.parseDouble(priceDifference.getText());
            double petrolCons = Double.parseDouble(petrolConsumption.getText());
            double dieselCons = Double.parseDouble(dieselConsumption.getText());
            double petrolPr = Double.parseDouble(petrolPrice.getText());
            double dieselPr = Double.parseDouble(dieselPrice.getText());

            // Költség különbség 100 km-en
            double petrolCost100 = (petrolCons * petrolPr);
            double dieselCost100 = (dieselCons * dieselPr);
            double savings100km = petrolCost100 - dieselCost100;

            if (savings100km <= 0) {
                resultLabel7.setText("❌ A dízel nem spórolósabb!");
                return;
            }

            // Megtérülési kilométer
            double breakEvenKm = priceDiff / (savings100km / 100);
            double breakEvenYears = breakEvenKm / 15000; // átlag 15000 km/év

            resultLabel7.setText(String.format("Megtérülés: %,d km | %.1f év | Spórol: %.0f Ft/100km",
                    (int)breakEvenKm, breakEvenYears, savings100km));
        } catch (NumberFormatException ex) {
            resultLabel7.setText("❌ Hibás adatok!");
        }
    }

    // 9. Gumiabroncs költség kalkulátor
    private void calculateTireCost() {
        try {
            double setPrice = Double.parseDouble(tireSetPrice.getText());
            double lifespan = Double.parseDouble(tireLifespan.getText());
            double annualKm = Double.parseDouble(annualKmTire.getText());

            double costPerKm = setPrice / lifespan;
            double annualCost = costPerKm * annualKm;
            double yearsToReplace = lifespan / annualKm;

            resultLabel8.setText(String.format("Évente: %,d Ft | Per km: %.2f Ft | Csere: %.1f év",
                    (int)annualCost, costPerKm, yearsToReplace));
        } catch (NumberFormatException ex) {
            resultLabel8.setText("❌ Hibás adatok!");
        }
    }
}