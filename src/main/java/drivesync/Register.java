package drivesync;

import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.*;

public class Register {
    private Connection conn;
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
        try {
            conn = Database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, regUsername.getText().trim());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Database.closeConnection();
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        Database.closeConnection();
        return false;
    }

    public void registerUser() {
        if (regUsername.getText().trim().isEmpty() || regEmail.getText().trim().isEmpty() || regPassword.getText().trim().isEmpty() || regPasswordConfirm.getText().trim().isEmpty()) {
            System.out.println("[DriveSync] Field(s) are emtpy"); //Átírni majd error pop-up-ba.
            return;
        }
        if (!regPassword.getText().trim().equals(regPasswordConfirm.getText().trim())) {
            System.out.println("[DriveSync] The password are not matching"); //Átírni majd error pop-up-ba.
            return;
        }
        if (isUserExists()) {
            System.out.println("[DriveSync] Username is already registered"); //Átírni majd error pop-up-ba.
        }
        else {
            String sql = "INSERT INTO users (username, email, password) VALUES (?, ?, SHA2(?, 256))";
            try {
                conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, regUsername.getText().trim());
                stmt.setString(2, regEmail.getText().trim());
                stmt.setString(3, regPassword.getText().trim());
                stmt.executeUpdate();
                System.out.println("[DriveSync] User registered"); //Átírni majd error pop-up-ba.
                Database.closeConnection();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}
