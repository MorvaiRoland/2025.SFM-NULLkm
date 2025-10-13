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

    private int currentUserId;
    private Connection conn;

    public void setConnection(Connection connection, int userId) {
        this.conn = connection;
        this.currentUserId = userId;
        loadUserData();
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
        if (conn == null) return;

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
                // Ha a jelszó mező üres, ne változtassuk meg a jelszót
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
                    stmt.setString(3, password); // Itt érdemes lenne hash-elni a jelszót!
                    stmt.setInt(4, currentUserId);
                }

                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Siker", "Adatok sikeresen mentve.");
                    passwordField.clear(); // Jelszó mező törlése mentés után
                } else {
                    showAlert(Alert.AlertType.WARNING, "Figyelem", "Nem történt változtatás.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Hiba", "Nem sikerült menteni az adatokat.");
        }
    }

    @FXML
    private void handleReset() {
        loadUserData();
        passwordField.clear();
        showAlert(Alert.AlertType.INFORMATION, "Visszaállítás", "Az adatok visszaállítva.");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private void handleChangePassword() {
        // Például egy alert, később ide jöhet a jelszóváltoztatás logikája
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Jelszó módosítás");
        alert.setHeaderText(null);
        alert.setContentText("Itt lehet a jelszó megváltoztatását kezelni.");
        alert.showAndWait();
    }

}
