package drivesync;

import drivesync.Költségvetés.BudgetController;
import drivesync.SajátAutók.SajatAutokController;
import drivesync.Főoldal.HomeDashboardController;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
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

        // Tooltip-ek a jobb használhatóságért, különösen összecsukott oldalsávnál
        setupTooltips();

        // Oldalsáv állapot visszatöltése
        Preferences prefs = Preferences.userNodeForPackage(HomeController.class);
        boolean collapsed = prefs.getBoolean("sidebarCollapsed", false);
        applySidebarCollapsed(collapsed);
        if (menuToggle != null) {
            menuToggle.setSelected(collapsed);
        }
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

            // Tartalomváltás finom átmenettel
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
            if (controller != null) {
                if (controller instanceof SajatAutokController) {
                    ((SajatAutokController) controller).setUsername(username);
                } else if (controller instanceof HomeDashboardController) {
                    ((HomeDashboardController) controller).setUsername(username);
                }
            }
            if (controller instanceof BudgetController) {
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
        if (activeBtn != null) {
            if (!activeBtn.getStyleClass().contains("menu-button-active")) {
                activeBtn.getStyleClass().add("menu-button-active");
            }
        }
    }

    private void addHoverEffectToMenu() {
        // Hover effektek CSS-ből érkeznek (:hover). Itt nincs további teendő.
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
            // Csak ikonok megjelenítése összecsukva
            setMenuTextsCollapsed(true);
        } else {
            sidebarBox.setPrefWidth(250);
            sidebarBox.setMinWidth(250);
            sidebarBox.setMaxWidth(250);
            sidebarBox.getStyleClass().remove("sidebar-collapsed");
            // Eredeti szövegek visszaállítása
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
                        new KeyValue(sidebarBox.maxWidthProperty(), from)
                ),
                new KeyFrame(Duration.millis(180), kvPref, kvMin, kvMax)
        );

        // Felhasználónév finom áttűnése, gombok maradjanak láthatóak (ikon mód)
        if (usernameLabel != null) {
            double targetOpacity = collapse ? 0.0 : 1.0;
            FadeTransition ftUser = new FadeTransition(Duration.millis(140), usernameLabel);
            ftUser.setToValue(targetOpacity);
            ftUser.play();
        }

        tl.setOnFinished(e -> applySidebarCollapsed(collapse));
        tl.play();
    }

    // --------- Ikon/label szöveg kezelés az összecsukott menühöz ---------
    private final List<String> originalMenuTexts = new ArrayList<>();

    private void cacheOriginalMenuTextsIfNeeded() {
        if (!originalMenuTexts.isEmpty()) return;
        for (Button b : menuButtons) {
            originalMenuTexts.add(b.getText());
        }
    }

    private void setMenuTextsCollapsed(boolean collapsed) {
        cacheOriginalMenuTextsIfNeeded();
        for (int i = 0; i < menuButtons.size(); i++) {
            Button b = menuButtons.get(i);
            String full = originalMenuTexts.get(i);
            if (collapsed) {
                // Próbáljuk az első "ikon" karaktert megtartani (általában emoji), egy szóköz után kezdődik a szöveg
                String iconOnly = extractLeadingIcon(full);
                b.setText(iconOnly);
            } else {
                b.setText(full);
            }
        }
    }

    private String extractLeadingIcon(String text) {
        if (text == null || text.isEmpty()) return "";
        // Ha az első karakter egy emoji + szóköz, vágjuk le az első token-t
        int spaceIdx = text.indexOf(' ');
        if (spaceIdx > 0) {
            return text.substring(0, spaceIdx); // pl. "🏠"
        }
        // Ellenkező esetben hagyjuk az első Unicode kódpontot
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
