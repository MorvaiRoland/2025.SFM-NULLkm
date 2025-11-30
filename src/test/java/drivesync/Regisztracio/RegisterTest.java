package drivesync.Regisztráció;

import drivesync.Adatbázis.Database;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RegisterFxTest {

    private Button btn;
    private TextField usernameField;
    private TextField emailField;
    private PasswordField passwordField;
    private PasswordField passwordConfirmField;

    @BeforeAll
    static void initJFX() {
        // Initialize JavaFX toolkit
        new JFXPanel();
    }

    @BeforeEach
    void setUp() {
        btn = new Button();
        usernameField = new TextField();
        emailField = new TextField();
        passwordField = new PasswordField();
        passwordConfirmField = new PasswordField();
    }

    @Test
    void testRegisterUser_success() throws Exception {
        // Set test values
        usernameField.setText("testuser");
        emailField.setText("test@example.com");
        passwordField.setText("password123");
        passwordConfirmField.setText("password123");

        // Mock Database
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockSelectStmt = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        PreparedStatement mockInsertStmt = mock(PreparedStatement.class);

        try (MockedStatic<Database> mockedDb = mockStatic(Database.class)) {
            mockedDb.when(Database::getConnection).thenReturn(mockConn);

            // User check
            when(mockConn.prepareStatement("SELECT 1 FROM users WHERE username = ?")).thenReturn(mockSelectStmt);
            when(mockSelectStmt.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            // Email check
            when(mockConn.prepareStatement("SELECT 1 FROM users WHERE email = ?")).thenReturn(mockSelectStmt);
            when(mockSelectStmt.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            // Insert
            when(mockConn.prepareStatement("INSERT INTO users (username, email, password) VALUES (?, ?, SHA2(?, 256))"))
                    .thenReturn(mockInsertStmt);
            when(mockInsertStmt.executeUpdate()).thenReturn(1);

            Register register = new Register(btn, usernameField, emailField, passwordField, passwordConfirmField);

            // Use CountDownLatch to wait for FX thread to finish
            CountDownLatch latch = new CountDownLatch(1);
            final boolean[] resultHolder = new boolean[1];

            Platform.runLater(() -> {
                try {
                    resultHolder[0] = register.registerUser();
                } finally {
                    latch.countDown();
                }
            });

            // Wait for FX thread to finish
            latch.await();

            assertTrue(resultHolder[0], "Registration should succeed");
        }
    }

    @Test
    void testRegisterUser_passwordMismatch() throws Exception {
        usernameField.setText("user");
        emailField.setText("email@test.com");
        passwordField.setText("pass1");
        passwordConfirmField.setText("pass2");

        Register register = new Register(btn, usernameField, emailField, passwordField, passwordConfirmField);

        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] resultHolder = new boolean[1];

        Platform.runLater(() -> {
            try {
                resultHolder[0] = register.registerUser();
            } finally {
                latch.countDown();
            }
        });

        latch.await();
        assertFalse(resultHolder[0], "Registration should fail due to password mismatch");
    }

    @Test
    void testRegisterUser_emptyFields() throws Exception {
        usernameField.setText("");
        emailField.setText("");
        passwordField.setText("");
        passwordConfirmField.setText("");

        Register register = new Register(btn, usernameField, emailField, passwordField, passwordConfirmField);

        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] resultHolder = new boolean[1];

        Platform.runLater(() -> {
            try {
                resultHolder[0] = register.registerUser();
            } finally {
                latch.countDown();
            }
        });

        latch.await();
        assertFalse(resultHolder[0], "Registration should fail due to empty fields");
    }
}
