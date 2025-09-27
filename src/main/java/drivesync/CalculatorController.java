package drivesync;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class CalculatorController {

    @FXML private TextField distanceField;
    @FXML private TextField consumptionField;
    @FXML private TextField priceField;
    @FXML private Label resultLabel;
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

            double cost = (distance / 100.0) * consumption * price;
            resultLabel.setText(String.format("Költség: %.2f Ft", cost));
        } catch (NumberFormatException ex) {
            resultLabel.setText("❌ Kérlek adj meg helyes számokat!");
        }
    }
}
