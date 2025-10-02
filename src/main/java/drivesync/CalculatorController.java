package drivesync;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.time.LocalDate;

public class CalculatorController {

    // Üzemanyag kalkulátor
    @FXML private TextField distanceField;
    @FXML private TextField consumptionField;
    @FXML private TextField priceField;
    @FXML private Label resultLabel;
    @FXML private Button calcButton;

    // Gépjárműadó kalkulátor
    @FXML private TextField HorsePower;
    @FXML private TextField ProductionYear;
    @FXML private Label resultLabel1;
    @FXML private Button calcButton1;

    // Kötelezőbiztosítás kalkulátor
    @FXML private TextField BaseFee;
    @FXML private TextField PowerFactor;
    @FXML private TextField ProductionYear1;
    @FXML private TextField BonusMalus;
    @FXML private Label resultLabel2;
    @FXML private Button calcButton2;

    @FXML
    private void initialize() {
        calcButton.setOnAction(e -> calculateFuelCost());
        calcButton1.setOnAction(e -> calculateVehicleTax());
        calcButton2.setOnAction(e -> calculateMandatoryInsurance());
    }

    private void calculateFuelCost() {
        try {
            double distance = Double.parseDouble(distanceField.getText());
            double consumption = Double.parseDouble(consumptionField.getText());
            double price = Double.parseDouble(priceField.getText());

            double cost = (distance / 100.0) * consumption * price;
            resultLabel.setText(String.format("Költség: %.2f Ft", cost));
        } catch (NumberFormatException ex) {
            resultLabel.setText("❌ Kérlek adj meg helyes számokat!");
        }
    }

    private void calculateVehicleTax() {
        try {
            double power = Double.parseDouble(HorsePower.getText());
            int prodyear = Integer.parseInt(ProductionYear.getText());
            double result = 0;

            if(prodyear <= 3) result = power * 345;
            else if(prodyear <= 7) result = power * 300;
            else if(prodyear <= 11) result = power * 230;
            else if(prodyear <= 15) result = power * 185;
            else result = power * 140;

            resultLabel1.setText(String.format("Költség: %.2f Ft/Év", result));
        } catch (NumberFormatException ex) {
            resultLabel1.setText("❌ Kérlek adj meg helyes számokat!");
        }
    }

    private void calculateMandatoryInsurance() {
        try {
            double base = Double.parseDouble(BaseFee.getText());
            int power = Integer.parseInt(PowerFactor.getText());
            int prodyear = Integer.parseInt(ProductionYear1.getText());

            double bm = switch (BonusMalus.getText()) {
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
            double powerMuliplier = 1;
            if(power <= 37) powerMuliplier = 0.6;
            else if(power <= 50) powerMuliplier = 0.8;
            else if(power <= 70) powerMuliplier = 1.0;
            else if(power <= 90) powerMuliplier = 1.2;
            else if(power <= 120) powerMuliplier = 1.4;
            else if(power <= 150) powerMuliplier = 1.6;
            else if(power <= 180) powerMuliplier = 1.8;
            else if(power <= 200) powerMuliplier = 2.0;
            else if(power <= 250) powerMuliplier = 2.2;
            else powerMuliplier = 2.4;

            // Gyártási év szorzó
            int currentYear = LocalDate.now().getYear();
            int age = currentYear - prodyear;
            double prodyearMultiplier = 1;
            if(age <= 3) prodyearMultiplier = 1.2;
            else if(age <= 7) prodyearMultiplier = 1.0;
            else if(age <= 12) prodyearMultiplier = 0.9;
            else if(age <= 20) prodyearMultiplier = 0.8;
            else prodyearMultiplier = 0.7;

            double result = base * powerMuliplier * prodyearMultiplier * bm;
            resultLabel2.setText(String.format("Költség: %.2f Ft/Év", result));

        } catch (NumberFormatException ex) {
            resultLabel2.setText("❌ Kérlek adj meg helyes számokat!");
        }
    }
}
