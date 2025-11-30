package drivesync.Bejelentkezes;

import drivesync.Adatbazis.Database;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Login {
    private Button btn;
    private TextField usernameField;
    private PasswordField passwordField;

    public Login(Button btn, TextField usernameField, PasswordField passwordField) {
        this.btn = btn;
        this.usernameField = usernameField;
        this.passwordField = passwordField;
    }

    public boolean loginUser() {
        String sql = "SELECT * FROM users WHERE username = ? AND password = SHA2(?, 256)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usernameField.getText().trim());
            stmt.setString(2, passwordField.getText().trim());

            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
