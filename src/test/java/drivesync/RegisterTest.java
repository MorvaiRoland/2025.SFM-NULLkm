package drivesync;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class RegisterTest {

    private Register register;
    private TextField usernameField;
    private TextField emailField;
    private PasswordField passwordField;
    private PasswordField passwordConfirmField;
    private Button button;

    @BeforeEach
    void setUp() {
        usernameField = new TextField();
        emailField = new TextField();
        passwordField = new PasswordField();
        passwordConfirmField = new PasswordField();
        button = new Button();

        register = new Register(button, usernameField, emailField, passwordField, passwordConfirmField);
    }

    @Test
    void testEmptyFields_ShouldShowErrorAndFail() {
        assertFalse(register.registerUser());
    }

    @Test
    void testPasswordMismatch_ShouldFail() {
        usernameField.setText("user");
        emailField.setText("user@example.com");
        passwordField.setText("pass123");
        passwordConfirmField.setText("pass123");

        assertFalse(register.registerUser());
    }

    @Test
    void testUserAlreadyExists_ShouldFail() throws Exception {
        usernameField.setText("existingUser");
        emailField.setText("new@example.com");
        passwordField.setText("pass");
        passwordConfirmField.setText("pass");

        try (MockedStatic<Database> dbMock = Mockito.mockStatic(Database.class)) {
            Connection conn = mock(Connection.class);
            PreparedStatement stmt = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            dbMock.when(Database::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(stmt);
            when(stmt.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true); // simulate user exists

            assertFalse(register.registerUser());
        }
    }

    @Test
    void testEmailAlreadyExists_ShouldFail() throws Exception {
        usernameField.setText("newUser");
        emailField.setText("existing@example.com");
        passwordField.setText("pass");
        passwordConfirmField.setText("pass");

        try (MockedStatic<Database> dbMock = Mockito.mockStatic(Database.class)) {
            Connection conn = mock(Connection.class);
            PreparedStatement stmt = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            dbMock.when(Database::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(stmt);
            when(stmt.executeQuery()).thenReturn(rs);

            // First call (check username) -> rs.next() = false
            // Second call (check email) -> rs.next() = true
            when(rs.next()).thenReturn(false, true);

            assertFalse(register.registerUser());
        }
    }

    @Test
    void testSuccessfulRegistration_ShouldPass() throws Exception {
        usernameField.setText("newUser");
        emailField.setText("new@example.com");
        passwordField.setText("pass");
        passwordConfirmField.setText("pass");

        try (MockedStatic<Database> dbMock = Mockito.mockStatic(Database.class)) {
            Connection conn = mock(Connection.class);
            PreparedStatement stmt = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            dbMock.when(Database::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(stmt);
            when(stmt.executeQuery()).thenReturn(rs);

            // username + email both don't exist
            when(rs.next()).thenReturn(false, false);

            // simulate insert success
            doNothing().when(stmt).executeUpdate();

            assertTrue(register.registerUser());
        }
    }
}
