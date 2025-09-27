package drivesync;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class HomeController {

    @FXML private Label usernameLabel;
    @FXML private Button logoutButton;

    @FXML private Button homeBtn, carsBtn, budgetBtn, linksBtn, calculatorBtn;
    @FXML private FlowPane contentFlow;

    private String username;

    public void setUsername(String username) {
        this.username = username;
        usernameLabel.setText(username);
        showHome();
    }

    @FXML
    private void handleLogout() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(HomeController.class);
        prefs.remove("username");
        stage.setScene(App.getLoginScene());
        stage.setTitle("DriveSync");
    }

    // ----------------- Navigáció -----------------
    @FXML private void showHome() {
        loadFXMLToContent("HomeDashboard.fxml");
    }

    @FXML private void showCars() {
        loadFXMLToContent("SajatAutok.fxml");
    }

    @FXML private void showBudget() {
        loadFXMLToContent("Budget.fxml");
    }

    @FXML private void showLinks() {
        loadFXMLToContent("Links.fxml");
    }

    @FXML private void showCalculator() {
        loadFXMLToContent("CalculatorController.fxml");
    }

    // FXML betöltés segédmetódus
    private void loadFXMLToContent(String fxmlFileName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
            VBox pane = loader.load();
            contentFlow.getChildren().clear();
            contentFlow.getChildren().add(pane);

            // Ha szükséges, itt átadhatjuk a username-t a child controllernek
            Object controller = loader.getController();
            if (controller instanceof HomeDashboardController) {
                ((HomeDashboardController) controller).setUsername(username);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
