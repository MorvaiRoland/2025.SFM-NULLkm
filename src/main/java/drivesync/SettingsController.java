package drivesync;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SettingsController {

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label regDateLabel;

    private int currentUserId; // Aktuális felhasználó ID
    private Connection conn;   // SQL kapcsolat

    // Beállítja az SQL kapcsolatot és az aktuális felhasználót
    public void setConnection(Connection connection, int userId) {
        this.conn = connection;
        this.currentUserId = userId;
        loadUserData();
    }

    // Adatok betöltése a users táblából
    private void loadUserData() {
        if (conn == null) return;
        String sql = "SELECT username, email, password, reg_date FROM users WHERE id=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                usernameField.setText(rs.getString("username"));
                emailField.setText(rs.getString("email"));
                passwordField.setText(rs.getString("password"));
                regDateLabel.setText(rs.getString("reg_date"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Hiba", "Nem sikerült betölteni az adatokat.");
        }
    }

    // Mentés gomb kezelése
    @FXML
    private void handleSave() {
        if (conn == null) return;
        String sql = "UPDATE users SET username=?, email=?, password=? WHERE id=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usernameField.getText());
            stmt.setString(2, emailField.getText());
            stmt.setString(3, passwordField.getText());
            stmt.setInt(4, currentUserId);
            stmt.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Siker", "Adatok sikeresen mentve.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Hiba", "Nem sikerült menteni az adatokat.");
        }
    }

    // Visszaállítás gomb kezelése
    @FXML
    private void handleReset() {
        loadUserData();
        showAlert(Alert.AlertType.INFORMATION, "Visszaállítás", "Az adatok visszaállítva.");
    }

    // Segédmetódus alert megjelenítéséhez
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
