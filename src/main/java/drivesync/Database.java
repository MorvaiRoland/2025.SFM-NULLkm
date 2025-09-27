package drivesync;

import java.sql.*;

public class Database {
    private static String url = "jdbc:mysql://mysql.nethely.hu:3306/drivesync";
    private static String user = "drivesync";
    private static String password = "Kirajok123";

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
