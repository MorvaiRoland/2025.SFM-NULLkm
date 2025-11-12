package drivesync;

import drivesync.Bejelentkezés.Login;
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

class LoginTest {

    private Button btn;
    private TextField username;
    private PasswordField password;

    // JDBC URL memória-alapú adatbázishoz
    private final String DB_URL = "jdbc:sqlite::memory:";

    @BeforeAll
    static void initJfx() {
        new JFXPanel(); // JavaFX toolkit inicializálása
    }

    @BeforeEach
    void setUp() throws SQLException {
        btn = new Button();
        username = new TextField();
        password = new PasswordField();

        createMemoryDb();
    }

    private void createMemoryDb() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            Statement stmt = conn.createStatement();
            stmt.execute("CREATE TABLE users (username TEXT, password TEXT)");
            stmt.execute("INSERT INTO users (username, password) VALUES ('test','test')");
        }
    }

    @Test
    void testEmptyFields() {
        System.out.printf("Üres mező tesz:");
        Login login = new Login(btn, username, password);
        assertFalse(login.loginUser(), "Üres mezők esetén false-t kell adnia");
    }

    @Test
    void testEmptyPassword() {
        System.out.println("Nem megadott jelszó teszt:");
        username.setText("test");
        password.setText("");

        Login login = new Login(btn, username, password);
        assertFalse(login.loginUser(), "Üres jelszó esetén false-t kell adnia");
    }

    @Test
    void testEmptyUsername() {
        System.out.println("Nem megadott felhasználónév teszt:");

        username.setText("");
        password.setText("test");

        Login login = new Login(btn, username, password);
        assertFalse(login.loginUser(), "Üres felhasználónév esetén false-t kell adnia");
    }

    @Test
    void testValidLogin() throws SQLException {
        System.out.println("Sikeres bejelentkezés teszt");
        username.setText("test");
        password.setText("test");

        // A Login osztály belsőleg a valós DB-t használja, itt a memória-alapú DB-t
        Login login = new Login(btn, username, password);
        assertTrue(login.loginUser(), "Helyes felhasználónév/jelszó esetén true-t kell adnia");
    }


    @Test
    void testInvalidLogin() throws SQLException {
        System.out.println("Rossz adatok teszt");
        username.setText("testr");
        password.setText("rosszjelszo");

        Login login = new Login(btn, username, password);
        assertFalse(login.loginUser(), "Helytelen jelszó esetén false-t kell adnia");
    }
}
