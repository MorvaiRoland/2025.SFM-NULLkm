package drivesync;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class CalculatorController {

    @FXML private TextField distanceField;     // megtett km
    @FXML private TextField consumptionField;  // liter/100km
    @FXML private TextField priceField;        // Ft/liter
    @FXML private Label resultLabel;           // eredmény kiírása
    @FXML private Button calcButton;

    @FXML
    private void initialize() {
        calcButton.setOnAction(e -> calculateFuelCost());
    }

    private void calculateFuelCost() {
        try {
            double distance = Double.parseDouble(distanceField.getText());
            double consumption = Double.parseDouble(consumptionField.getText());
            double price = Double.parseDouble(priceField.getText());

            // képlet: (táv / 100) * fogyasztás * ár
            double cost = (distance / 100.0) * consumption * price;

            resultLabel.setText(String.format("Költség: %.2f Ft", cost));
        } catch (NumberFormatException ex) {
            resultLabel.setText("❌ Kérlek adj meg helyes számokat!");
        }
    }

}
