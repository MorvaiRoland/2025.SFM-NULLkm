package drivesync.Beállítások;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.application.Platform;
import javafx.scene.control.skin.ChoiceBoxSkin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.prefs.Preferences;

public class SettingsController {

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label regDateLabel;

    // UI beállítások és értesítések
    @FXML private CheckBox emailNotifications;
    @FXML private CheckBox smsNotifications;
    @FXML private CheckBox pushNotifications;

    @FXML private ChoiceBox<String> themeChoiceBox;
    @FXML private Slider fontSizeSlider;

    @FXML private CheckBox twoFactorAuth;
    @FXML private CheckBox autoUpdate;
    @FXML private CheckBox sendUsageStats;
    @FXML private CheckBox enableLogging;

    private int currentUserId;
    private Connection conn;
    private final Preferences prefs = Preferences.userNodeForPackage(SettingsController.class);

    public void setConnection(Connection connection, int userId) {
        this.conn = connection;
        this.currentUserId = userId;
        loadUserData();

    }

    @FXML
    private void initialize() {
        loadPreferencesToControls();

        Platform.runLater(() -> {
            applyTheme(prefs.get("theme", "Rendszer"));
            applyFontSize(prefs.getDouble("fontSize", 14.0));
        });

        themeChoiceBox.showingProperty().addListener((obs, wasShowing, isNowShowing) -> {
            if (isNowShowing) {
                var skin = themeChoiceBox.getSkin();
                if (skin instanceof ChoiceBoxSkin<?> choiceBoxSkin) {
                    try {
                        // Reflection helyett próbáljuk meg a popupot CSS-sel vagy közvetlen stílussal kezelni
                        var popupNode = themeChoiceBox.lookup(".context-menu");
                        if (popupNode != null) {
                            popupNode.getStyleClass().add("theme-dark");
                        }
                    } catch (Exception e) {
                        System.err.println("Nem sikerült stílust alkalmazni a ChoiceBox popupra: " + e.getMessage());
                    }
                }
            }
        });

    }


    private void loadUserData() {
        if (conn == null) return;

        String sql = "SELECT username, email, reg_date FROM users WHERE id=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                usernameField.setText(rs.getString("username"));
                emailField.setText(rs.getString("email"));
                regDateLabel.setText(rs.getString("reg_date"));
                passwordField.clear(); // Jelszót nem töltünk be biztonsági okokból
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Hiba", "Nem sikerült betölteni az adatokat.");
        }
    }

    @FXML
    private void handleSave() {
        // 1) Preferenciák mentése
        String theme = themeChoiceBox != null && themeChoiceBox.getValue() != null ? themeChoiceBox.getValue() : "Rendszer";
        double fontSize = fontSizeSlider != null ? fontSizeSlider.getValue() : 14.0;
        prefs.put("theme", theme);
        prefs.putDouble("fontSize", fontSize);
        prefs.putBoolean("emailNotifications", emailNotifications != null && emailNotifications.isSelected());
        prefs.putBoolean("smsNotifications", smsNotifications != null && smsNotifications.isSelected());
        prefs.putBoolean("pushNotifications", pushNotifications != null && pushNotifications.isSelected());
        prefs.putBoolean("twoFactorAuth", twoFactorAuth != null && twoFactorAuth.isSelected());
        prefs.putBoolean("autoUpdate", autoUpdate != null && autoUpdate.isSelected());
        prefs.putBoolean("sendUsageStats", sendUsageStats != null && sendUsageStats.isSelected());
        prefs.putBoolean("enableLogging", enableLogging != null && enableLogging.isSelected());

        // Azonnali alkalmazás
        applyTheme(theme);
        applyFontSize(fontSize);

        // 2) Felhasználói adatok mentése DB-be (ha van kapcsolat)
        if (conn != null) {
            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();
            String password = passwordField.getText().trim();

            if (username.isEmpty() || email.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Hiányzó adat", "A felhasználónév és email kötelező!");
                return;
            }

            try {
                String sql;
                if (password.isEmpty()) {
                    sql = "UPDATE users SET username=?, email=? WHERE id=?";
                } else {
                    sql = "UPDATE users SET username=?, email=?, password=? WHERE id=?";
                }

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, username);
                    stmt.setString(2, email);

                    if (password.isEmpty()) {
                        stmt.setInt(3, currentUserId);
                    } else {
                        stmt.setString(3, password); // TODO: hash jelszó
                        stmt.setInt(4, currentUserId);
                    }

                    int rows = stmt.executeUpdate();
                    if (rows > 0) {
                        showAlert(Alert.AlertType.INFORMATION, "Siker", "Beállítások és adatok mentve.");
                        passwordField.clear();
                    } else {
                        showAlert(Alert.AlertType.INFORMATION, "Siker", "Beállítások mentve.");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Hiba", "Nem sikerült menteni az adatbázis adatait, de a beállítások elmentésre kerültek.");
            }
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Siker", "Beállítások mentve.");
        }
    }

    @FXML
    private void handleReset() {
        // Prefek visszatöltése és alkalmazása
        loadPreferencesToControls();
        applyTheme(prefs.get("theme", "Rendszer"));
        applyFontSize(prefs.getDouble("fontSize", 14.0));

        // DB adatok friss betöltése
        loadUserData();
        if (passwordField != null) passwordField.clear();
        showAlert(Alert.AlertType.INFORMATION, "Visszaállítás", "Beállítások visszaállítva.");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        // Dialógus témázása és stíluslap csatolása
        drivesync.App.styleDialog(alert);
        alert.showAndWait();
    }
    @FXML
    private void handleChangePassword() {
        // Például egy alert, később ide jöhet a jelszóváltoztatás logikája
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Jelszó módosítás");
        alert.setHeaderText(null);
        alert.setContentText("Itt lehet a jelszó megváltoztatását kezelni.");
        drivesync.App.styleDialog(alert);
        alert.showAndWait();
    }

    private void loadPreferencesToControls() {
        if (themeChoiceBox != null) {
            String theme = prefs.get("theme", "Rendszer");
            // Ha nincs az opciók között, állítsuk be biztonságos értékre
            themeChoiceBox.setValue(theme.matches("Világos|Sötét|Rendszer alapértelmezett|Rendszer") ? normalizeThemeLabel(theme) : "Rendszer alapértelmezett");
        }
        if (fontSizeSlider != null) {
            fontSizeSlider.setValue(prefs.getDouble("fontSize", 14.0));
        }

        if (emailNotifications != null) emailNotifications.setSelected(prefs.getBoolean("emailNotifications", false));
        if (smsNotifications != null) smsNotifications.setSelected(prefs.getBoolean("smsNotifications", false));
        if (pushNotifications != null) pushNotifications.setSelected(prefs.getBoolean("pushNotifications", true));
        if (twoFactorAuth != null) twoFactorAuth.setSelected(prefs.getBoolean("twoFactorAuth", false));
        if (autoUpdate != null) autoUpdate.setSelected(prefs.getBoolean("autoUpdate", true));
        if (sendUsageStats != null) sendUsageStats.setSelected(prefs.getBoolean("sendUsageStats", false));
        if (enableLogging != null) enableLogging.setSelected(prefs.getBoolean("enableLogging", false));
    }

    private void applyTheme(String label) {
        String normalized = normalizeThemeLabel(label);
        Platform.runLater(() -> {
            if (usernameField == null || usernameField.getScene() == null || usernameField.getScene().getRoot() == null) return;
            var root = usernameField.getScene().getRoot();
            if ("sötét".equalsIgnoreCase(normalized)) {
                root.getStyleClass().add("theme-dark");
            } else {
                root.getStyleClass().remove("theme-dark");
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

    private String normalizeThemeLabel(String input) {
        if (input == null) return "Rendszer alapértelmezett";
        if (input.equalsIgnoreCase("Rendszer") || input.equalsIgnoreCase("Rendszer alapértelmezett")) {
            return "Rendszer alapértelmezett";
        }
        return input;
    }

}
