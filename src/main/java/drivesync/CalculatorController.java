package drivesync;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.time.LocalDate;

public class CalculatorController {

    @FXML private TextField distanceField;
    @FXML private TextField consumptionField;
    @FXML private TextField priceField;
    @FXML private Label resultLabel;
    @FXML private Button calcButton;

    // Gépjárműadó kalkulátor változói
    @FXML private TextField HorsePower;
    @FXML private TextField ProductionYear;
    @FXML private Label resultLabel1;
    @FXML private Button calcButton1;


    //Kötelező bizt kalkulátor
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

    private void calculateVehicleTax(){
        try {
            double power = Double.parseDouble(HorsePower.getText());
            int prodyear = Integer.parseInt(ProductionYear.getText());
            double result=0;
            if(prodyear<=3){
                result = power*345;
            }else if (prodyear>=4 && prodyear<=7){
                result=power*300;
            }else if (prodyear>=8 && prodyear<=11) {
                result=power*230;
            }else if (prodyear>=12 && prodyear<=15) {
                result=power*185;
            }else{
                result=power*140;
            }
            resultLabel1.setText(String.format("Költség: %.2f Ft/Év", result));
        } catch (NumberFormatException ex) {
            resultLabel1.setText("❌ Kérlek adj meg helyes számokat!");
        }
    }

    private void calculateMandatoryInsurance() {
        double bm = 0;
        try {

            double base = Double.parseDouble(BaseFee.getText());
            int power = Integer.parseInt(PowerFactor.getText());
            int prodyear = Integer.parseInt(ProductionYear1.getText());



            switch (BonusMalus.getText()) {
                case "M4":
                    bm = 4.0;
                    break;
                case "M3":
                    bm = 3.5;
                    break;
                case "M2":
                    bm = 3.0;
                    break;
                case "M1":
                    bm = 2.5;
                    break;
                case "A00":
                    bm = 1.0;
                    break;
                case "B1":
                    bm = 0.95;
                    break;
                case "B2":
                    bm = 0.90;
                    break;
                case "B3":
                    bm = 0.85;
                    break;
                case "B4":
                    bm = 0.80;
                    break;
                case "B5":
                    bm = 0.75;
                    break;
                case "B6":
                    bm = 0.70;
                    break;
                case "B7":
                    bm = 0.65;
                    break;
                case "B8":
                    bm = 0.60;
                    break;
                case "B9":
                    bm = 0.55;
                    break;
                case "B10":
                    bm = 0.5;
                    break;
            } //Bónusz Manusz kategóriak definiálása

            //Lóerő szerinti szorzó

            double powerMuliplier=1;
            if(power<=37){
                powerMuliplier=0.6;
            } else if (power>=38 && power<=50) {
                powerMuliplier=0.8;
            }else if (power>=51 && power<=70) {
                powerMuliplier=1.0;
            }else if (power>=71 && power<=90) {
                powerMuliplier=1.2;
            }else if (power>=91 && power<=120) {
                powerMuliplier=1.4;
            }else if (power>=121 && power<=150) {
                powerMuliplier=1.6;
            }else if (power>=151 && power<=180) {
                powerMuliplier=1.8;
            }else if (power>=181 && power<=200) {
                powerMuliplier=2.0;
            }else if (power>=201 && power<=250) {
                powerMuliplier=2.2;
            }else if (power>250) {
                powerMuliplier=2.4;
            }


            //Gyártási év szerinti szorzó
            double prodyearMultiplier=1;
            LocalDate now = LocalDate.now();
            int currentYear = now.getYear();
            if(prodyear-currentYear<=3){
                prodyearMultiplier=1.2;
            } else if (prodyear-currentYear>=4&&prodyear-currentYear<=7) {
                prodyearMultiplier=1.0;
            }else if (prodyear-currentYear>=8&&prodyear-currentYear<=12) {
                prodyearMultiplier=0.9;
            }else if (prodyear-currentYear>=13&&prodyear-currentYear<=20) {
                prodyearMultiplier=0.8;
            }else if (prodyear-currentYear>20) {
                prodyearMultiplier=0.7;
            }

            double result= base*powerMuliplier*prodyearMultiplier*bm; //Díj=Alapdíj×Teljesítméní faktor×Gyártási év×Bónusz Manusz szorzo

            resultLabel2.setText(String.format("Költség: %.2f Ft/Év", result));


        }catch (NumberFormatException ex) {
            resultLabel1.setText("❌ Kérlek adj meg helyes számokat!");
        }

    }


}
