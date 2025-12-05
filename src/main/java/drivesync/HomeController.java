package drivesync;

import drivesync.Koltsegvetes.BudgetController;
import drivesync.SajatAutok.SajatAutokController;
import drivesync.Fooldal.HomeDashboardController;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

public class HomeController {

    @FXML private Label usernameLabel;
    @FXML private Button logoutButton;

    @FXML private VBox contentContainer;
    @FXML private ScrollPane contentScrollPane;

    @FXML private Button homeBtn, carsBtn, budgetBtn, linksBtn, calculatorBtn, settingsBtn;
    @FXML private ToggleButton menuToggle;
    @FXML private VBox sidebarBox;

    private String username;
    private final List<Button> menuButtons = new ArrayList<>();
    private final List<String> originalMenuTexts = new ArrayList<>();

    @FXML
    public void initialize() {
        // Menü gombok listába
        menuButtons.add(homeBtn);
        menuButtons.add(carsBtn);
        menuButtons.add(budgetBtn);
        menuButtons.add(linksBtn);
        menuButtons.add(calculatorBtn);
        menuButtons.add(settingsBtn);

        // Eredeti gomb szövegek cache-elése
        cacheOriginalMenuTexts();

        setActiveMenu(homeBtn);
        setupTooltips();

        // Oldalsáv állapot visszatöltése
        Preferences prefs = Preferences.userNodeForPackage(HomeController.class);
        boolean collapsed = prefs.getBoolean("sidebarCollapsed", false);
        applySidebarCollapsed(collapsed);
        if (menuToggle != null) menuToggle.setSelected(collapsed);
    }

    public void setUsername(String username) {
        this.username = username;
        if (usernameLabel != null) usernameLabel.setText(username);
        showHome();
    }

    @FXML
    private void handleLogout() {
        try {
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            Preferences prefs = Preferences.userNodeForPackage(HomeController.class);
            prefs.remove("username");
            prefs.remove("google_email"); // Google login törlése

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/drivesync/LOG/REG/main.fxml"));
            Parent root = loader.load(); // Itt a MainController automatikusan initialize() futtat

            Scene loginScene = new Scene(root, 900, 600);
            loginScene.getStylesheets().add(getClass().getResource("/drivesync/CSS/style.css").toExternalForm());
            stage.setScene(loginScene);
            stage.setTitle("DriveSync");
            stage.setResizable(false);

            // Fade animáció
            root.setOpacity(0);
            FadeTransition fade = new FadeTransition(Duration.millis(220), root);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();

        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Hiba");
            alert.setHeaderText("Hiba történt a kijelentkezés során");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }






    @FXML private void showHome()       { loadFXML("/drivesync/Fooldal/HomeDashboard.fxml"); setActiveMenu(homeBtn); }
    @FXML private void showCars()       { loadFXML("/drivesync/SajatAutok/SajatAutok.fxml"); setActiveMenu(carsBtn); }
    @FXML private void showBudget()     { loadFXML("/drivesync/Koltsegvetes/Budget.fxml"); setActiveMenu(budgetBtn); }
    @FXML private void showLinks()      { loadFXML("/drivesync/Linkek/Links.fxml"); setActiveMenu(linksBtn); }
    @FXML private void showCalculator() { loadFXML("/drivesync/Kalkulator/Calculator.fxml"); setActiveMenu(calculatorBtn); }
    @FXML private void showSettings()   { loadFXML("/drivesync/Beallitasok/Settings.fxml"); setActiveMenu(settingsBtn); }

    private void loadFXML(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent pane = loader.load();

            // Tartalomváltás animációval
            if (!contentContainer.getChildren().isEmpty()) {
                Parent old = (Parent) contentContainer.getChildren().get(0);
                FadeTransition fadeOut = new FadeTransition(Duration.millis(140), old);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(e -> {
                    contentContainer.getChildren().setAll(pane);
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(180), pane);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);
                    fadeIn.play();
                });
                fadeOut.play();
            } else {
                pane.setOpacity(0);
                contentContainer.getChildren().setAll(pane);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(180), pane);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            }

            Object controller = loader.getController();
            if (controller instanceof SajatAutokController) {
                ((SajatAutokController) controller).setUsername(username);
            } else if (controller instanceof HomeDashboardController) {
                ((HomeDashboardController) controller).setUsername(username);

                // --- Ide tesszük az automatikus popup hívást ---
                CarReminderPopup popup = new CarReminderPopup();
                popup.showReminders(username);
            } else if (controller instanceof BudgetController) {
                ((BudgetController) controller).setUsername(username);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void setActiveMenu(Button activeBtn) {
        for (Button btn : menuButtons) {
            btn.getStyleClass().remove("menu-button-active");
            if (!btn.getStyleClass().contains("menu-button")) {
                btn.getStyleClass().add("menu-button");
            }
        }
        if (activeBtn != null && !activeBtn.getStyleClass().contains("menu-button-active")) {
            activeBtn.getStyleClass().add("menu-button-active");
        }
    }

    @FXML
    private void toggleSidebar() {
        boolean collapse = menuToggle != null && menuToggle.isSelected();
        animateSidebar(collapse);
        Preferences prefs = Preferences.userNodeForPackage(HomeController.class);
        prefs.putBoolean("sidebarCollapsed", collapse);
    }

    private void applySidebarCollapsed(boolean collapse) {
        if (sidebarBox == null) return;
        if (collapse) {
            sidebarBox.setPrefWidth(72);
            sidebarBox.setMinWidth(72);
            sidebarBox.setMaxWidth(72);
            if (!sidebarBox.getStyleClass().contains("sidebar-collapsed")) {
                sidebarBox.getStyleClass().add("sidebar-collapsed");
            }
            setMenuTextsCollapsed(true);
        } else {
            sidebarBox.setPrefWidth(250);
            sidebarBox.setMinWidth(250);
            sidebarBox.setMaxWidth(250);
            sidebarBox.getStyleClass().remove("sidebar-collapsed");
            setMenuTextsCollapsed(false);
        }
    }

    private void animateSidebar(boolean collapse) {
        if (sidebarBox == null) return;

        double from = sidebarBox.getWidth() > 0 ? sidebarBox.getWidth() : (collapse ? 250 : 72);
        double to = collapse ? 72 : 250;

        KeyValue kvPref = new KeyValue(sidebarBox.prefWidthProperty(), to, Interpolator.EASE_BOTH);
        KeyValue kvMin = new KeyValue(sidebarBox.minWidthProperty(), to, Interpolator.EASE_BOTH);
        KeyValue kvMax = new KeyValue(sidebarBox.maxWidthProperty(), to, Interpolator.EASE_BOTH);

        Timeline tl = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(sidebarBox.prefWidthProperty(), from),
                        new KeyValue(sidebarBox.minWidthProperty(), from),
                        new KeyValue(sidebarBox.maxWidthProperty(), from)),
                new KeyFrame(Duration.millis(180), kvPref, kvMin, kvMax)
        );

        if (usernameLabel != null) {
            double targetOpacity = collapse ? 0.0 : 1.0;
            FadeTransition ftUser = new FadeTransition(Duration.millis(140), usernameLabel);
            ftUser.setToValue(targetOpacity);
            ftUser.play();
        }

        tl.setOnFinished(e -> applySidebarCollapsed(collapse));
        tl.play();
    }

    private void cacheOriginalMenuTexts() {
        if (!originalMenuTexts.isEmpty()) return;
        for (Button b : menuButtons) {
            originalMenuTexts.add(b.getText());
        }
    }

    private void setMenuTextsCollapsed(boolean collapsed) {
        for (int i = 0; i < menuButtons.size(); i++) {
            Button b = menuButtons.get(i);
            String full = originalMenuTexts.get(i);
            if (collapsed) {
                b.setText(extractLeadingIcon(full));
            } else {
                b.setText(full);
            }
        }
    }

    private String extractLeadingIcon(String text) {
        if (text == null || text.isEmpty()) return "";
        int spaceIdx = text.indexOf(' ');
        if (spaceIdx > 0) return text.substring(0, spaceIdx);
        int cp = text.codePointAt(0);
        return new String(Character.toChars(cp));
    }

    private void setupTooltips() {
        if (homeBtn != null) homeBtn.setTooltip(new Tooltip("Főoldal"));
        if (carsBtn != null) carsBtn.setTooltip(new Tooltip("Saját autók"));
        if (budgetBtn != null) budgetBtn.setTooltip(new Tooltip("Költségvetés"));
        if (linksBtn != null) linksBtn.setTooltip(new Tooltip("Hasznos linkek"));
        if (calculatorBtn != null) calculatorBtn.setTooltip(new Tooltip("Kalkulátor"));
        if (settingsBtn != null) settingsBtn.setTooltip(new Tooltip("Beállítások"));
        if (logoutButton != null) logoutButton.setTooltip(new Tooltip("Kijelentkezés"));
    }
}
