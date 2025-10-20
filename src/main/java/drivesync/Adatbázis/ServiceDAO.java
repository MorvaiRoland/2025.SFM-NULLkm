package drivesync.Adatbázis;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceDAO {

    // Szerviz adatok modell osztálya
    public static class Service {
        public int id;          // hozzáadott mező a szerviz azonosításához
        public int carId;
        public String brand;
        public String type;
        public String serviceDate;
        public String location;
        public String notes;
        public boolean reminder;

        public Service(int id, int carId, String brand, String type, String serviceDate, String location, String notes, boolean reminder) {
            this.id = id;
            this.carId = carId;
            this.brand = brand;
            this.type = type;
            this.serviceDate = serviceDate;
            this.location = location;
            this.notes = notes;
            this.reminder = reminder;
        }
    }

    // Közelgő szervizek lekérdezése
    public List<Service> getUpcomingServices() {
        List<Service> services = new ArrayList<>();
        String sql = "SELECT u.id, u.car_id, c.brand, c.type, u.service_date, u.location, u.notes, u.reminder " +
                "FROM upcoming_services u " +
                "JOIN cars c ON u.car_id = c.id " +
                "ORDER BY u.service_date ASC";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                int carId = rs.getInt("car_id");
                String brand = rs.getString("brand");
                String type = rs.getString("type");
                String serviceDate = rs.getString("service_date");
                String location = rs.getString("location");
                String notes = rs.getString("notes");
                boolean reminder = rs.getBoolean("reminder");

                services.add(new Service(id, carId, brand, type, serviceDate, location, notes, reminder));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return services;
    }
}
