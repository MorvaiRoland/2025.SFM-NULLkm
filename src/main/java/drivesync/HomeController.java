package drivesync;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
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

    @FXML
    private void handleLogout() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        Preferences prefs = Preferences.userNodeForPackage(HomeController.class);
        prefs.remove("username");
        stage.setScene(App.getLoginScene());
        stage.setTitle("DriveSync");
    }

    // Menü gombok
    @FXML private void showHome() { loadFXMLToContent("/drivesync/HomeDashboard.fxml"); }
    @FXML private void showCars() { loadFXMLToContent("/drivesync/SajatAutok.fxml"); }
    @FXML private void showBudget() { loadFXMLToContent("/drivesync/Budget.fxml"); }
    @FXML private void showLinks() { loadFXMLToContent("/drivesync/Links.fxml"); }
    @FXML private void showCalculator() { loadFXMLToContent("/drivesync/CalculatorController.fxml"); }

    /**
     * Betölti az FXML-t a fő tartalom területre
     */
    private void loadFXMLToContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent pane = loader.load();  // <-- itt most már bármilyen gyökérelem jó (VBox, ScrollPane, AnchorPane, stb.)

            contentContainer.getChildren().clear();
            contentContainer.getChildren().add(pane);

            // Ha az autók oldal, átadjuk a felhasználónevet
            Object controller = loader.getController();
            if (controller instanceof SajatAutokController) {
                ((SajatAutokController) controller).setUsername(username);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
