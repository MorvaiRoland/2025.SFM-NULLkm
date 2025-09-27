package drivesync;
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

        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "username VARCHAR(50) UNIQUE," +
                "email VARCHAR(50) NOT NULL," +
                "password VARCHAR(5000) NOT NULL" +
                ")";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
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
