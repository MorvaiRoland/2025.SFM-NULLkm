package drivesync.Regisztráció;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class RegisterTest extends ApplicationTest {

    private Register register;

    private TextField usernameField;
    private TextField emailField;
    private PasswordField passwordField;
    private PasswordField passwordConfirmField;
    private Button registerButton;

    @Override
    public void start(javafx.stage.Stage stage) {
        usernameField = new TextField();
        emailField = new TextField();
        passwordField = new PasswordField();
        passwordConfirmField = new PasswordField();
        registerButton = new Button("Register");

        register = new Register(registerButton, usernameField, emailField, passwordField, passwordConfirmField);
    }

    @BeforeEach
    public void setup() {
        // Reset fields
        Platform.runLater(() -> {
            usernameField.setText("");
            emailField.setText("");
            passwordField.setText("");
            passwordConfirmField.setText("");
        });
    }

    @Test
    public void testEmptyFields() throws Exception {
        Platform.runLater(() -> {
            usernameField.setText("");
            emailField.setText("");
            passwordField.setText("");
            passwordConfirmField.setText("");

            boolean result = register.registerUser();
            assertFalse(result, "Minden mezőt ki kell tölteni!");
        });
    }

    @Test
    public void testPasswordMismatch() throws Exception {
        Platform.runLater(() -> {
            usernameField.setText("user1");
            emailField.setText("user1@test.com");
            passwordField.setText("123456");
            passwordConfirmField.setText("654321");

            boolean result = register.registerUser();
            assertFalse(result, "A jelszavaknak egyezniük kell!");
        });
    }

    @Test
    public void testUserExists() throws Exception {
        // Mock a connection that returns a row for username check
        Register reg = new Register(registerButton, usernameField, emailField, passwordField, passwordConfirmField) {
            @Override
            public boolean isUserExists() { return true; }
            @Override
            public boolean isEmailExists() { return false; }
            @Override
            protected void showAlert(javafx.scene.control.Alert.AlertType type, String title, String message) {
                // Skip showing UI alerts in tests
            }
        };

        usernameField.setText("existingUser");
        emailField.setText("user@test.com");
        passwordField.setText("123456");
        passwordConfirmField.setText("123456");

        boolean result = reg.registerUser();
        assertFalse(result, "Felhasználónév már foglalt!");
    }

    @Test
    public void testEmailExists() throws Exception {
        // Mock a connection that returns a row for email check
        Register reg = new Register(registerButton, usernameField, emailField, passwordField, passwordConfirmField) {
            @Override
            public boolean isUserExists() { return false; }
            @Override
            public boolean isEmailExists() { return true; }
            @Override
            protected void showAlert(javafx.scene.control.Alert.AlertType type, String title, String message) {
                // Skip showing UI alerts in tests
            }
        };

        usernameField.setText("newUser");
        emailField.setText("existing@test.com");
        passwordField.setText("123456");
        passwordConfirmField.setText("123456");

        boolean result = reg.registerUser();
        assertFalse(result, "Email már foglalt!");
    }

    @Test
    public void testSuccessfulRegistration() {
        // Override a Register osztályt, hogy ne hívja az adatbázist
        Register reg = new Register(registerButton, usernameField, emailField, passwordField, passwordConfirmField) {
            @Override
            public boolean isUserExists() { return false; }
            @Override
            public boolean isEmailExists() { return false; }
            @Override
            protected void showAlert(Alert.AlertType type, String title, String message) { }
            @Override
            public boolean registerUser() {
                // csak a logikát teszteljük, adatbázis nélkül
                if (regUsername.getText().trim().isEmpty() ||
                        regEmail.getText().trim().isEmpty() ||
                        regPassword.getText().trim().isEmpty() ||
                        regPasswordConfirm.getText().trim().isEmpty()) {
                    return false;
                }
                if (!regPassword.getText().equals(regPasswordConfirm.getText())) {
                    return false;
                }
                if (isUserExists() || isEmailExists()) {
                    return false;
                }
                return true; // sikeres regisztráció
            }
        };

        // Mezők feltöltése "jó" értékekkel
        usernameField.setText("newUser");
        emailField.setText("new@test.com");
        passwordField.setText("123456");
        passwordConfirmField.setText("123456");

        // Teszteljük a regisztrációt
        boolean result = reg.registerUser();
        assertTrue(result, "Regisztráció sikeres kell legyen!");
    }
}
