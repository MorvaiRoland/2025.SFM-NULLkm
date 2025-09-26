package drivesync;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.File;

public class HomeController {

    @FXML private Label usernameLabel;
    @FXML private Button logoutButton;
    @FXML private StackPane contentPane;
    @FXML private Label contentLabel;

    @FXML private Button homeBtn;
    @FXML private Button carsBtn;
    @FXML private Button budgetBtn;
    @FXML private Button linksBtn;
    @FXML private Button calculatorBtn;

    @FXML
    private void initialize() {
        setupHover(homeBtn);
        setupHover(carsBtn);
        setupHover(budgetBtn);
        setupHover(linksBtn);
        setupHover(calculatorBtn);
    }

    private void setupHover(Button btn) {
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: #FFD700; -fx-text-fill: black; -fx-background-radius: 12; -fx-font-size: 16px; -fx-font-weight: bold;"));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #FFD700; -fx-background-radius: 0; -fx-font-size: 16px; -fx-font-weight: bold;"));
    }

    public void setUsername(String username) {
        usernameLabel.setText(username);
    }

    @FXML
    private void handleLogout() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();

        // Remember me törlése
        File rememberFile = new File("remember_me.txt");
        if(rememberFile.exists()) rememberFile.delete();

        stage.setScene(App.getLoginScene());
        stage.setTitle("DriveSync");

        javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
        stage.setX((screenBounds.getWidth() - 900) / 2);
        stage.setY((screenBounds.getHeight() - 600) / 2);
    }

    @FXML private void showHome() { contentLabel.setText("Főoldal tartalom"); }
    @FXML private void showCars() { contentLabel.setText("Saját autók tartalom"); }
    @FXML private void showBudget() { contentLabel.setText("Költségvetés tartalom"); }
    @FXML private void showLinks() { contentLabel.setText("Hasznos linkek tartalom"); }
    @FXML private void showCalculator() { contentLabel.setText("Kalkulátor modul: ide jön a számítási felület."); }
}
