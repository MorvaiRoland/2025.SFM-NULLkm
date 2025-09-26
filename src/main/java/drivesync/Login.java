package drivesync;

import com.mysql.cj.protocol.Resultset;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Login {
    private Connection conn;
    private Button btn;
    private TextField loginUsername;
    private PasswordField loginPassword;

    public Login(Button btn, TextField loginUsername, PasswordField loginPassword) {
        this.btn = btn;
        this.loginUsername = loginUsername;
        this.loginPassword = loginPassword;
    }

    public boolean loginUser() {
        if (loginUsername.getText().trim().isEmpty() || loginPassword.getText().trim().isEmpty()) {
            System.out.println("[DriveSync] Field(s) are emtpy"); //Átírni majd error pop-up-ba.
        }
        else {
            String sql = "SELECT 1 FROM users WHERE username = ? AND password = SHA2(?, 256)";
            try {
                conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, loginUsername.getText());
                stmt.setString(2, loginPassword.getText().trim());
                ResultSet rs = stmt.executeQuery();
                if (!rs.next()) System.out.println("[DriveSync] No user found"); //Átírni majd error pop-up-ba.
                else {
                    System.out.println("[DriveSync] Login successfull!"); //Átírni majd error pop-up-ba.
                    return true;
                }
            } catch (SQLException e) { e.printStackTrace(); }
        }
        return false;
    }
}
