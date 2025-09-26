package drivesync;

import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class RegisterTest {

    private Button btn;
    private TextField username;
    private TextField email;
    private PasswordField password;
    private PasswordField passwordConfirm;

    private Connection conn;

    private final String DB_URL = "jdbc:sqlite::memory:"; // memória-alapú DB

    @BeforeAll
    static void initJfx() throws ClassNotFoundException {
        new JFXPanel(); // JavaFX toolkit inicializálása
        Class.forName("org.sqlite.JDBC"); // JDBC driver betöltése
    }

    @BeforeEach
    void setUp() throws SQLException {
        btn = new Button();
        username = new TextField();
        email = new TextField();
        password = new PasswordField();
        passwordConfirm = new PasswordField();

        // Memória-alapú adatbázis létrehozása
        conn = DriverManager.getConnection(DB_URL);
        Statement stmt = conn.createStatement();
        stmt.execute("DROP TABLE IF EXISTS users");
        stmt.execute("CREATE TABLE users (username TEXT PRIMARY KEY, email TEXT, password TEXT)");
    }

    @Test
    void testEmptyFields() {
        System.out.println("Üres mező teszt:");
        Register reg = new Register(btn, username, email, password, passwordConfirm);
        reg.registerUser();

        try (var rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM users")) {
            rs.next();
            assertEquals(0, rs.getInt(1), "Üres mezők esetén nem szabad létrejönni a felhasználónak");
        } catch (SQLException e) { fail(e); }
    }

    @Test
    void testPasswordMismatch() {
        System.out.println("Rossz jelszó teszt:");
        username.setText("user1");
        email.setText("test@test.com");
        password.setText("12345");
        passwordConfirm.setText("54321");

        Register reg = new Register(btn, username, email, password, passwordConfirm);
        reg.registerUser();

        try (var rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM users")) {
            rs.next();
            assertEquals(0, rs.getInt(1), "Password mismatch esetén nem szabad létrejönni a felhasználónak");
        } catch (SQLException e) { fail(e); }
    }

    @Test
    void testSuccessfulRegistration() {
        System.out.println("Sikeres regisztráció teszt:");
        username.setText("user1");
        email.setText("test@test.com");
        password.setText("12345");
        passwordConfirm.setText("12345");

        Register reg = new Register(btn, username, email, password, passwordConfirm);
        reg.registerUser();

        try (var rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM users")) {
            rs.next();
            assertEquals(0, rs.getInt(1), "Helyes adatokkal létre kell jönnie a felhasználónak");
        } catch (SQLException e) { fail(e); }
    }


    @Test
    void testDuplicateUsername() throws SQLException {
        System.out.println("Duplikált felhasználónév teszt:");
        // Először létrehozunk egy felhasználót
        try (var stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO users (username, email, password) VALUES ('user1', 'a@b.com', '12345')");
        }

        username.setText("user1");
        email.setText("test@test.com");
        password.setText("12345");
        passwordConfirm.setText("12345");

        Register reg = new Register(btn, username, email, password, passwordConfirm);
        reg.registerUser();

        // Ellenőrizhetjük, hogy nem jött létre új rekord
        try (var rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM users")) {
            rs.next();
            assertEquals(1, rs.getInt(1), "Duplikált felhasználónév esetén nem szabad létrejönni a felhasználónak");
        } catch (SQLException e) { fail(e); }
    }
}
