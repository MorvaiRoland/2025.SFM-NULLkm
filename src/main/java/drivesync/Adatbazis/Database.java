package drivesync.Adatbazis;

import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class Database {
    private static String url;
    private static String user;
    private static String password;
    private static boolean initialized = false;

    static {
        try (InputStream input = Database.class.getResourceAsStream("/drivesync/NO-GITHUB/db_config.properties")) {
            if (input == null) {
                throw new RuntimeException("❌ FATAL: Az adatbázis konfigurációs fájl (db_config.properties) NEM található a /drivesync/NO-GITHUB mappában!");
            }
            Properties props = new Properties();
            props.load(input);

            url = props.getProperty("db.url");
            user = props.getProperty("db.user");
            password = props.getProperty("db.password");

        } catch (Exception e) {
            System.err.println("[DriveSync] Hiba a konfiguráció betöltésekor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void initializeDatabase() {
        if (initialized || url == null) return;

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {

            System.out.println("[DriveSync] Database connection established");

            // --- TÁBLA DEFINÍCIÓK (Több tábla kihagyva a rövidség kedvéért, de a cars tábla a fókuszban) ---

            // Lekérdezés a felhasználók táblára (szükséges a FOREIGN KEY-hez)
            String users_sql = "CREATE TABLE IF NOT EXISTS users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "username VARCHAR(50) UNIQUE," +
                    "email VARCHAR(50) NOT NULL," +
                    "password VARCHAR(5000) NOT NULL," +
                    "isDark BOOLEAN DEFAULT FALSE" +
                    ")";
            stmt.executeUpdate(users_sql);

            // Lekérdezés a service_types táblára (szükséges a FOREIGN KEY-hez)
            String service_types_sql = "CREATE TABLE IF NOT EXISTS service_types (\n" +
                    "id INT AUTO_INCREMENT PRIMARY KEY,\n" +
                    "name VARCHAR(100) UNIQUE NOT NULL\n" +
                    ")";
            stmt.executeUpdate(service_types_sql);

            // Lekérdezés a services táblára (szükséges a FOREIGN KEY-hez)
            String services_sql = "CREATE TABLE IF NOT EXISTS services (\n" +
                    "id INT AUTO_INCREMENT PRIMARY KEY,\n" +
                    "car_id INT(10) NOT NULL,\n" +
                    "service_type_id INT(10) NOT NULL,\n" +
                    "km INT(20) NOT NULL,\n" +
                    "price INT(10) NOT NULL,\n" +
                    "service_date DATE,\n" +
                    "replaced_parts VARCHAR(255),\n" +
                    "CONSTRAINT fk_car_service FOREIGN KEY (car_id) REFERENCES cars(id) ON DELETE CASCADE,\n" +
                    "CONSTRAINT fk_service_type FOREIGN KEY (service_type_id) REFERENCES service_types(id)\n" +
                    ")";
            stmt.executeUpdate(services_sql);

            // VÁLTOZÁS: oil mező felosztása oil_type és oil_quantity mezőkre (mindkettő VARCHAR)
            String car_sql = "CREATE TABLE IF NOT EXISTS cars (\n" +
                    "id INT AUTO_INCREMENT PRIMARY KEY,\n" +
                    "owner_id INT(11) NOT NULL,\n" +
                    "license VARCHAR(50) UNIQUE NOT NULL,\n" +
                    "brand VARCHAR(50) NOT NULL,\n" +
                    "type VARCHAR(50) NOT NULL,\n" +
                    "vintage INT(11) ,\n" +
                    "engine_type VARCHAR(50),\n" +
                    "fuel_type VARCHAR(20),\n" +
                    "km INT(11),\n" +
                    "oil_type VARCHAR(50),\n" +      // ÚJ: Olaj típusa (VARCHAR)
                    "oil_quantity VARCHAR(20),\n" +  // ÚJ: Olaj mennyisége (VARCHAR)
                    "tire_size VARCHAR(255),\n" +
                    "insurance DATE,\n" +
                    "inspection_date DATE,\n" +
                    "color VARCHAR(20),\n" +
                    "notes TEXT,\n" +
                    "archived TINYINT(1) DEFAULT 0,\n" +
                    "CONSTRAINT fk_owner_car FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE\n" +
                    ")";
            stmt.executeUpdate(car_sql);

            // Megjegyzés: Az archívált mező (archived TINYINT(1)) vissza lett állítva, ahogy a kimenetben szerepelt.

            System.out.println("[DriveSync] Tables are up-to-date");
            initialized = true;

        } catch (SQLException e) {
            System.err.println("[DriveSync] Database initialization FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        initializeDatabase();
        if (url == null) return null;
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void overrideConnection(String dbUrl) {
    }
}