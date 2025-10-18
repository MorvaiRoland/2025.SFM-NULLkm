package drivesync;

import drivesync.SajátAutók.SajatAutokController;
import drivesync.Főoldal.HomeDashboardController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

public class HomeController {

    @FXML private Label usernameLabel;
    @FXML private Button logoutButton;

    @FXML private VBox contentContainer;

    @FXML private Button homeBtn, carsBtn, budgetBtn, linksBtn, calculatorBtn, settingsBtn;

    private String username;
    private final List<Button> menuButtons = new ArrayList<>();

    @FXML
    public void initialize() {
        menuButtons.add(homeBtn);
        menuButtons.add(carsBtn);
        menuButtons.add(budgetBtn);
        menuButtons.add(linksBtn);
        menuButtons.add(calculatorBtn);
        menuButtons.add(settingsBtn);

        setActiveMenu(homeBtn);
        addHoverEffectToMenu();
    }

    public void setUsername(String username) {
        this.username = username;
        if (usernameLabel != null) usernameLabel.setText(username);
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

    @FXML private void showHome() { loadFXML("/drivesync/Főoldal/HomeDashboard.fxml"); setActiveMenu(homeBtn); }
    @FXML private void showCars() { loadFXML("/drivesync/SajátAutók/SajatAutok.fxml"); setActiveMenu(carsBtn); }
    @FXML private void showBudget() { loadFXML("/drivesync/Költségvetés/Budget.fxml"); setActiveMenu(budgetBtn); }
    @FXML private void showLinks() { loadFXML("/drivesync/Linkek/Links.fxml"); setActiveMenu(linksBtn); }
    @FXML private void showCalculator() { loadFXML("/drivesync/Kalkulátor/Calculator.fxml"); setActiveMenu(calculatorBtn); }
    @FXML private void showSettings() { loadFXML("/drivesync/Beállítások/Settings.fxml"); setActiveMenu(settingsBtn); }

    private void loadFXML(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent pane = loader.load();
            contentContainer.getChildren().setAll(pane);

            Object controller = loader.getController();
            if (controller instanceof SajatAutokController sa) sa.setUsername(username);
            else if (controller instanceof HomeDashboardController hd) hd.setUsername(username);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setActiveMenu(Button activeBtn) {
        for (Button btn : menuButtons) {
            if (btn == activeBtn) {
                btn.setStyle("-fx-background-color: #FFD700; -fx-text-fill: black; -fx-font-weight: bold; -fx-background-radius: 12; -fx-font-size: 16px; -fx-alignment: CENTER_LEFT; -fx-padding: 10 20 10 20;");
            } else {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: gold; -fx-font-weight: bold; -fx-background-radius: 12; -fx-font-size: 16px; -fx-alignment: CENTER_LEFT; -fx-padding: 10 20 10 20;");
            }
        }
    }

    private void addHoverEffectToMenu() {
        for (Button btn : menuButtons) {
            btn.setOnMouseEntered(e -> {
                if (!btn.getStyle().contains("#FFD700")) btn.setStyle(btn.getStyle() + "-fx-background-color: rgba(255,215,0,0.3); -fx-text-fill: white;");
            });
            btn.setOnMouseExited(e -> {
                if (!btn.getStyle().contains("#FFD700")) btn.setStyle(btn.getStyle().replace("-fx-background-color: rgba(255,215,0,0.3); -fx-text-fill: white;", ""));
            });
        }
    }
}
