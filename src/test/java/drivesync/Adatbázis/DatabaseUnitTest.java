package drivesync.Adatbázis;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;


public class DatabaseUnitTest {
    private static final String H2_URL = "jdbc:h2:mem:drivesync_test"; // In-memory H2 adatbázis

    // Segédfüggvény a statikus privát mezők beállításához reflexióval
    private static void setStaticField(String fieldName, Object value) throws Exception {
        Field field = Database.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(null, value); // Statikus mező beállítása
    }

    // Segédfüggvény a statikus privát mező lekérdezéséhez
    private static Object getStaticField(String fieldName) throws Exception {
        Field field = Database.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(null);
    }

    @BeforeEach
    @AfterEach
    public void resetDatabaseState() throws Exception {
        // Minden teszt előtt és után töröljük a statikus állapotot
        setStaticField("url", null);
        setStaticField("user", null);
        setStaticField("password", null);
        setStaticField("initialized", false);

        // Annak érdekében, hogy a statikus blokk újra lefusson, ha szükséges (bár a legtöbb környezetben nehéz)
        // Mivel a JUnit ClassLoader egyszer tölti be az osztályt, a fenti reset a kritikus.
    }

    // --- TESZTEK ---

    /**
     * Teszteli, hogy a getConnection() null-t ad vissza, ha nincs URL beállítva.
     * Ez szimulálja azt, ha a db_config.properties nem található.
     */
    @Test
    public void testGetConnection() throws Exception {
        Connection conn = Database.getConnection();
        assertNull(conn, "A kapcsolatnak null-nak kell lennie, ha a konfiguráció sikertelen.");
    }
}
