package drivesync.Adatbázis;
import java.sql.*;

public class Database {
    private static String url = "jdbc:mysql://mysql.nethely.hu:3306/drivesync";
    private static String user = "drivesync";
    private static String password = "Kirajok123";
    private static Connection conn;

    public Database() {
        try {
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("[DriveSync] Database connection established");
        } catch (SQLException e) { e.printStackTrace(); }

        String users_sql = "CREATE TABLE IF NOT EXISTS users (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "username VARCHAR(50) UNIQUE," +
                "email VARCHAR(50) NOT NULL," +
                "password VARCHAR(5000) NOT NULL" +
                ")";

        String car_sql = "CREATE TABLE IF NOT EXISTS cars (\n" +
                "id INT AUTO_INCREMENT PRIMARY KEY,\n" +
                "owner_id INT(10) NOT NULL,\n" +
                "license VARCHAR(10) UNIQUE NOT NULL,\n" +
                "brand VARCHAR(100) NOT NULL,\n" +
                "type VARCHAR(100) NOT NULL,\n" +
                "vintage INT(4) NOT NULL,\n" +
                "engine_type VARCHAR(100) NOT NULL,\n" +
                "fuel_type VARCHAR(100) NOT NULL,\n" +
                "km INT(20) NOT NULL,\n" +
                "oil INT(20) NOT NULL,\n" +
                "tire_size INT(10),\n" +
                "service DATE NOT NULL,\n" +
                "insurance DATE NOT NULL,\n" +
                "CONSTRAINT cars_id\n" +
                "    FOREIGN KEY (owner_id) REFERENCES users(id)\n" +
                "    ON DELETE CASCADE\n" +
                ")";

        String income_sql = "CREATE TABLE IF NOT EXISTS income (\n" +
                "what VARCHAR(250) NOT NULL,\n" +
                "price INT(10) NOT NULL,\n" +
                "datet DATE NOT NULL,\n" +
                "owner_id INT(10) NOT NULL,\n" +
                "CONSTRAINT income_id\n" +
                "        FOREIGN KEY (owner_id) REFERENCES users(id)\n" +
                "        ON DELETE CASCADE\n" +
                ")";

        String expense_sql = "CREATE TABLE IF NOT EXISTS expense (\n" +
                "what VARCHAR(250) NOT NULL,\n" +
                "price INT(10) NOT NULL,\n" +
                "datet DATE NOT NULL,\n" +
                "owner_id INT(10) NOT NULL,\n" +
                "CONSTRAINT income_id\n" +
                "        FOREIGN KEY (owner_id) REFERENCES users(id)\n" +
                "        ON DELETE CASCADE\n" +
                ")";

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(users_sql);
            stmt.executeUpdate(income_sql);
            stmt.executeUpdate(expense_sql);
            stmt.executeUpdate(car_sql);
            System.out.println("[DriveSync] Tables are up-to-date");
            conn.close();
        }
        catch (SQLException e) { e.printStackTrace(); }
    }
    private void createTransactionsTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS transactions (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "type ENUM('INCOME','EXPENSE') NOT NULL," +
                "date DATE NOT NULL," +
                "amount DOUBLE NOT NULL," +
                "description VARCHAR(255)" +
                ")";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
            System.out.println("[DriveSync] transactions table ready");
        }
    }

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static void closeConnection() {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}
