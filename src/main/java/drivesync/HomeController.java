package drivesync;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.prefs.Preferences;

public class HomeController {

    @FXML private Label usernameLabel;
    @FXML private Button logoutButton;
    @FXML private VBox contentContainer;

    @FXML private Button homeBtn, carsBtn, budgetBtn, linksBtn, calculatorBtn;

    private String username;

    public void setUsername(String username) {
        this.username = username;
        usernameLabel.setText(username);
        showHome();
    }

    @FXML private void handleLogout() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        Preferences prefs = Preferences.userNodeForPackage(HomeController.class);
        prefs.remove("username");
        stage.setScene(App.getLoginScene());
        stage.setTitle("DriveSync");
    }

    @FXML private void showHome() { loadFXMLToContent("HomeDashboard.fxml"); }
    @FXML private void showCars() { loadFXMLToContent("SajatAutok.fxml"); }
    @FXML private void showBudget() { loadFXMLToContent("Budget.fxml"); }
    @FXML private void showLinks() { loadFXMLToContent("Links.fxml"); }
    @FXML private void showCalculator() { loadFXMLToContent("CalculatorController.fxml"); }

    private void loadFXMLToContent(String fxmlFileName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
            VBox pane = loader.load();
            contentContainer.getChildren().clear();
            contentContainer.getChildren().add(pane);

            Object controller = loader.getController();
            if (controller instanceof SajatAutokController) {
                ((SajatAutokController) controller).setUsername(username);
            }

        } catch (IOException e) { e.printStackTrace(); }
    }
}
