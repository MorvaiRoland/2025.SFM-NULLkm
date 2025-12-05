package drivesync.Beallitasok;

import drivesync.Adatbazis.Database;
import drivesync.App;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.sql.*;
import java.util.prefs.Preferences;

public class SettingsController {

    // FXML - Fiók
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label regDateLabel;
    @FXML private CheckBox twoFactorAuth;

    // FXML - Értesítések/Adatvédelem
    @FXML private CheckBox emailNotifications;
    @FXML private CheckBox smsNotifications;
    @FXML private CheckBox autoUpdate;
    @FXML private CheckBox sendUsageStats;
    @FXML private CheckBox enableLogging;

    // FXML - Megjelenés
    @FXML private ChoiceBox<String> themeChoiceBox;
    @FXML private Slider fontSizeSlider;

    // FXML - Névjegy
    @FXML private ImageView logoImageView;

    // Belső állapot
    private int currentUserId;
    private boolean isGoogleUser = false;
    private final Preferences prefs = Preferences.userNodeForPackage(SettingsController.class);

    public void setUserId(int userId) {
        this.currentUserId = userId;
        loadUserData();
    }

    @FXML
    private void initialize() {
        // Logó betöltése
        try {
            logoImageView.setImage(new Image(App.class.getResourceAsStream("/drivesync/Logok/DriveSync logo-2.png")));
        } catch (Exception e) {
            System.err.println("Logó betöltése sikertelen.");
        }

        // Betűméret Slider konverter beállítása
        fontSizeSlider.setLabelFormatter(new StringConverter<>() {
            @Override
            public String toString(Double object) { return String.format("%.0f", object); }
            @Override
            public Double fromString(String string) { return Double.valueOf(string); }
        });

        loadPreferencesToControls();

        Platform.runLater(() -> {
            applyTheme(prefs.get("theme", "Rendszer alapértelmezett"));
            applyFontSize(prefs.getDouble("fontSize", 14.0));
        });

        themeChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                applyTheme(newVal);
            }
        });

        fontSizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            applyFontSize(newVal.doubleValue());
        });
    }


    private void loadUserData() {
        if (currentUserId == 0) return;

        // SQL: Lekérdezzük a password mezőt, hogy eldönthessük, Google felhasználó-e.
        String sql = "SELECT username, email, reg_date, twoFactorAuth_enabled, password FROM users WHERE id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Változók kinyerése a TRY blokkon belül
                final String username = rs.getString("username");
                final String email = rs.getString("email");
                final String regDate = rs.getString("reg_date");
                final boolean twoFactorEnabled = rs.getBoolean("twoFactorAuth_enabled");
                final String passwordHash = rs.getString("password");

                // Ellenőrizzük, hogy Google felhasználó-e (üres, NULL, vagy nem hashelt jelszó)
                isGoogleUser = (passwordHash == null || passwordHash.isEmpty() || passwordHash.length() < 30);

                Platform.runLater(() -> {
                    // Adatok kiírása a mezőbe
                    usernameField.setText(username);
                    emailField.setText(email);
                    regDateLabel.setText(regDate);
                    twoFactorAuth.setSelected(twoFactorEnabled);

                    // Jelszó mező letiltása Google felhasználó esetén
                    if (isGoogleUser) {
                        passwordField.setText("Külső azonosítással bejelentkezve");
                        passwordField.setDisable(true);
                        passwordField.setStyle("-fx-opacity: 0.7;");
                    } else {
                        passwordField.clear(); // Jelszó mező törlése a biztonság érdekében
                        passwordField.setDisable(false);
                        passwordField.setStyle(null);
                    }
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Hiba", "Nem sikerült betölteni a felhasználói adatokat.");
        }
    }

    // SettingsController.java - handleSave() frissítés

    @FXML
    private void handleSave() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String newPassword = passwordField.getText().trim();

        // ... (Ellenőrzések) ...

        try (Connection conn = Database.getConnection()) {
            String sql;

            boolean needsPasswordUpdate = !isGoogleUser && !newPassword.isEmpty();

            if (!needsPasswordUpdate) {
                // SQL: UPDATE users SET username=?, email=?, twoFactorAuth_enabled=? WHERE id=?
                sql = "UPDATE users SET username=?, email=?, twoFactorAuth_enabled=? WHERE id=?";
            } else {
                // SQL: UPDATE users SET username=?, email=?, password=SHA2(?, 256), twoFactorAuth_enabled=? WHERE id=?";
                sql = "UPDATE users SET username=?, email=?, password=SHA2(?, 256), twoFactorAuth_enabled=? WHERE id=?";
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {

                // 1. és 2. paraméter: Mindig ugyanaz
                stmt.setString(1, username);
                stmt.setString(2, email);

                // 2FA állapot rögzítése
                boolean is2FA = twoFactorAuth.isSelected();

                if (!needsPasswordUpdate) {
                    // JELSZÓ NÉLKÜLI ÁG (3 placeholder a két beállított után)
                    // 3. paraméter: twoFactorAuth_enabled
                    // 4. paraméter: id
                    stmt.setBoolean(3, is2FA);
                    stmt.setInt(4, currentUserId);
                } else {
                    // JELSZÓS ÁG (4 placeholder a két beállított után)
                    // 3. paraméter: newPassword
                    // 4. paraméter: twoFactorAuth_enabled
                    // 5. paraméter: id
                    stmt.setString(3, newPassword);
                    stmt.setBoolean(4, is2FA);
                    stmt.setInt(5, currentUserId);
                }

                int rows = stmt.executeUpdate();

                // ... (Visszajelzés logika) ...
                if (rows > 0) {
                    savePreferences();
                    showToast(Alert.AlertType.INFORMATION, "Siker", "Beállítások és adatok mentve.", Duration.seconds(2));
                    passwordField.clear();
                } else {
                    showToast(Alert.AlertType.INFORMATION, "Siker", "Beállítások mentve (nincs változás a DB-ben).", Duration.seconds(2));
                }
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            showAlert(Alert.AlertType.ERROR, "Hiba", "A felhasználónév vagy email cím már foglalt!");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Hiba", "Nem sikerült menteni az adatbázis adatait: " + e.getMessage());
        }
    }

    // Elválasztott metódus a prefek mentésére
    private void savePreferences() {
        prefs.put("theme", themeChoiceBox.getValue());
        prefs.putDouble("fontSize", fontSizeSlider.getValue());
        prefs.putBoolean("emailNotifications", emailNotifications.isSelected());
        prefs.putBoolean("smsNotifications", smsNotifications.isSelected());
        prefs.putBoolean("autoUpdate", autoUpdate.isSelected());
        prefs.putBoolean("sendUsageStats", sendUsageStats.isSelected());
        prefs.putBoolean("enableLogging", enableLogging.isSelected());
    }


    @FXML
    private void handleReset() {
        // Visszatöltés és alkalmazás
        loadPreferencesToControls();
        applyTheme(prefs.get("theme", "Rendszer alapértelmezett"));
        applyFontSize(prefs.getDouble("fontSize", 14.0));

        // DB adatok friss betöltése
        loadUserData();
        if (passwordField != null) passwordField.clear();

        showToast(Alert.AlertType.INFORMATION, "Visszaállítás", "Beállítások visszaállítva a legutóbb mentett állapotra.", Duration.seconds(2));
    }

    private void loadPreferencesToControls() {
        if (themeChoiceBox != null) {
            String theme = prefs.get("theme", "Rendszer alapértelmezett");
            themeChoiceBox.setValue(theme.equals("Sötét") ? "Sötét" : theme.equals("Világos") ? "Világos" : "Rendszer alapértelmezett");
        }
        if (fontSizeSlider != null) {
            fontSizeSlider.setValue(prefs.getDouble("fontSize", 14.0));
        }

        if (emailNotifications != null) emailNotifications.setSelected(prefs.getBoolean("emailNotifications", true));
        if (smsNotifications != null) smsNotifications.setSelected(prefs.getBoolean("smsNotifications", false));
        if (twoFactorAuth != null) twoFactorAuth.setSelected(prefs.getBoolean("twoFactorAuth", false));
        if (autoUpdate != null) autoUpdate.setSelected(prefs.getBoolean("autoUpdate", true));
        if (sendUsageStats != null) sendUsageStats.setSelected(prefs.getBoolean("sendUsageStats", false));
        if (enableLogging != null) enableLogging.setSelected(prefs.getBoolean("enableLogging", false));
    }

    // Téma alkalmazásának logikája
    private void applyTheme(String label) {
        String normalized = label != null ? label : "Rendszer alapértelmezett";
        Platform.runLater(() -> {
            if (usernameField == null || usernameField.getScene() == null || usernameField.getScene().getRoot() == null) return;
            var root = usernameField.getScene().getRoot();

            root.getStyleClass().remove("theme-dark");

            if (normalized.equals("Sötét")) {
                root.getStyleClass().add("theme-dark");
            }
        });
        prefs.put("theme", normalized);
    }

    private void applyFontSize(double size) {
        Platform.runLater(() -> {
            if (usernameField == null || usernameField.getScene() == null) return;
            var root = usernameField.getScene().getRoot();
            if (root == null) return;
            root.setStyle(String.format("-fx-font-size: %.0fpx;", size));
        });
        prefs.putDouble("fontSize", size);
    }

    // A régi showAlert metódusok cseréje a showToast metódusra

    private void showToast(Alert.AlertType t, String title, String msg, Duration duration) {
        if (t == Alert.AlertType.ERROR || t == Alert.AlertType.WARNING) {
            showAlert(t, title, msg);
            return;
        }

        Platform.runLater(() -> {
            Stage toastStage = new Stage();
            toastStage.initStyle(StageStyle.UNDECORATED);
            toastStage.setAlwaysOnTop(true);

            Label label = new Label(msg);
            label.setStyle("-fx-padding: 10 20; -fx-background-color: rgba(50, 50, 50, 0.95); -fx-text-fill: white; -fx-background-radius: 5; -fx-font-weight: bold;");

            Scene scene = new Scene(new VBox(label));
            scene.setFill(Color.TRANSPARENT);
            toastStage.setScene(scene);

            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

            toastStage.sizeToScene();

            // Középre igazítás
            toastStage.setX((screenBounds.getWidth() - toastStage.getWidth()) / 2);
            toastStage.setY(50);

            toastStage.show();

            Timeline timeline = new Timeline(new KeyFrame(duration, e -> toastStage.close()));
            timeline.play();
        });
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            // drivesync.App.styleDialog(alert); // Ha az App.java létezik
            alert.showAndWait();
        });
    }
}