package drivesync.Regisztráció;

import drivesync.Adatbázis.Database;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.*;

public class Register {
    private Button btn;
    private TextField regUsername, regEmail;
    private PasswordField regPassword, regPasswordConfirm;

    public Register(Button btn, TextField regUsername, TextField regEmail, PasswordField regPassword, PasswordField regPasswordConfirm) {
        this.btn = btn;
        this.regUsername = regUsername;
        this.regEmail = regEmail;
        this.regPassword = regPassword;
        this.regPasswordConfirm = regPasswordConfirm;
    }

    private boolean isUserExists() {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, regUsername.getText().trim());
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private boolean isEmailExists() {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, regEmail.getText().trim());
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean registerUser() {
        if (regUsername.getText().trim().isEmpty() ||
                regEmail.getText().trim().isEmpty() ||
                regPassword.getText().trim().isEmpty() ||
                regPasswordConfirm.getText().trim().isEmpty()) {

            showAlert(Alert.AlertType.ERROR, "Hiba", "Minden mezőt ki kell tölteni!");
            return false;
        }

        if (!regPassword.getText().trim().equals(regPasswordConfirm.getText().trim())) {
            showAlert(Alert.AlertType.ERROR, "Hiba", "A jelszavak nem egyeznek!");
            return false;
        }

        if (isUserExists()) {
            showAlert(Alert.AlertType.WARNING, "Hiba", "Ez a felhasználónév már foglalt!");
            return false;
        }

        if (isEmailExists()) {
            showAlert(Alert.AlertType.WARNING, "Hiba", "Ezzel az email címmel már van regisztráció!");
            return false;
        }

        String sql = "INSERT INTO users (username, email, password) VALUES (?, ?, SHA2(?, 256))";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, regUsername.getText().trim());
            stmt.setString(2, regEmail.getText().trim());
            stmt.setString(3, regPassword.getText().trim());
            stmt.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Siker", "A regisztráció sikeres!");
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Adatbázis hiba", "Nem sikerült a regisztráció.");
            return false;
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
