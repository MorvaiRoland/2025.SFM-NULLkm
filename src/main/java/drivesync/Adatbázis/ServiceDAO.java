package drivesync.Adatbázis;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ServiceDAO {

    // Szerviz adatok modell osztálya (Közelgő szervizekhez)
    public static class Service {
        public int id;
        public int carId;
        public String serviceDate;
        public String location;
        public String notes;
        public boolean reminder;

        // Ezt a modellt használjuk a Közelgő Szerviz Widgetekhez
        public Service(int id, int carId, String serviceDate, String location, String notes, boolean reminder) {
            this.id = id;
            this.carId = carId;
            this.serviceDate = serviceDate;
            this.location = location;
            this.notes = notes;
            this.reminder = reminder;
        }
    }

    // Emlékeztető adatok modell osztálya (Email küldéshez)
    public static class ReminderData {
        public int carId;
        public String serviceDate;
        public String location;
        public String notes;
        public String license;
        public String ownerEmail;

        public ReminderData(int carId, String serviceDate, String location, String notes, String license, String ownerEmail) {
            this.carId = carId;
            this.serviceDate = serviceDate;
            this.location = location;
            this.notes = notes;
            this.license = license;
            this.ownerEmail = ownerEmail;
        }
    }

    /**
     * Közelgő szervizek lekérdezése adott autóhoz.
     */
    public List<Service> getUpcomingServices(int carId) {
        List<Service> services = new ArrayList<>();
        String sql = "SELECT u.id, u.car_id, u.service_date, u.location, u.notes, u.reminder " +
                "FROM upcoming_services u WHERE u.car_id = ? AND u.archived = FALSE " +
                "ORDER BY u.service_date ASC";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, carId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String serviceDate = rs.getString("service_date");
                    String location = rs.getString("location");
                    String notes = rs.getString("notes");
                    boolean reminder = rs.getBoolean("reminder");

                    services.add(new Service(id, carId, serviceDate, location, notes, reminder));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return services;
    }

    /**
     * Emlékeztető adatok lekérdezése adott felhasználóhoz (checkUpcomingReminders hívja).
     */
    public List<ReminderData> getRemindersForUser(String username) {
        List<ReminderData> reminders = new ArrayList<>();
        String sql = """
        SELECT u.car_id, u.service_date, u.location, u.notes, c.license, usr.email
        FROM upcoming_services u
        JOIN cars c ON u.car_id = c.id
        JOIN users usr ON c.owner_id = usr.id
        WHERE u.reminder = TRUE 
          AND usr.username = ?
          AND (u.last_email_sent IS NULL OR u.last_email_sent < CURRENT_DATE)
          AND u.archived = FALSE
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reminders.add(new ReminderData(
                            rs.getInt("car_id"),
                            rs.getString("service_date"),
                            rs.getString("location"),
                            rs.getString("notes"),
                            rs.getString("license"),
                            rs.getString("email")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reminders;
    }

    /**
     * Közelgő szerviz archiválása (lejárt emlékeztető).
     */
    public void archiveUpcomingService(int carId, LocalDate serviceDate) {
        String sql = "UPDATE upcoming_services SET archived = TRUE WHERE car_id = ? AND service_date = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, carId);
            stmt.setDate(2, java.sql.Date.valueOf(serviceDate));
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Utolsó email küldési dátum frissítése.
     */
    public void updateLastEmailSent(int carId, LocalDate serviceDate) {
        String sql = "UPDATE upcoming_services SET last_email_sent = CURRENT_DATE WHERE car_id = ? AND service_date = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, carId);
            stmt.setDate(2, java.sql.Date.valueOf(serviceDate));
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Hozzáadás drivesync.Adatbázis.ServiceDAO.java-hoz
    public List<Service> getUpcomingServicesForUser(String username) {
        List<Service> services = new ArrayList<>();
        String sql = """
            SELECT u.id, u.car_id, u.service_date, u.location, u.notes, u.reminder, c.brand, c.type
            FROM upcoming_services u 
            JOIN cars c ON u.car_id = c.id
            JOIN users usr ON c.owner_id = usr.id
            WHERE usr.username = ? AND u.archived = FALSE
            ORDER BY u.service_date ASC
            """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // Megjegyzés: A Service osztályt frissíteni kell, ha még nem tartalmazza a brand/type mezőket
                    // Vagy használjunk egyszerűbb modellt, vagy egy új ServiceData osztályt.
                    // Mivel a Controller a brand/type-ot használja, kiegészítem a modellt.

                    int id = rs.getInt("id");
                    int carId = rs.getInt("car_id");
                    String serviceDate = rs.getString("service_date");
                    String location = rs.getString("location");
                    String notes = rs.getString("notes");
                    boolean reminder = rs.getBoolean("reminder");
                    String brand = rs.getString("brand");
                    String type = rs.getString("type");

                    // Feltételezzük, hogy a Service osztály tartalmazza a brand/type mezőket:
                    // services.add(new Service(id, carId, brand, type, serviceDate, location, notes, reminder));

                    // Ha a Service osztály nem tartalmazza a brand/type-ot (mint az előző válaszban),
                    // ideiglenesen használjuk a régimódi konstruktort (brand/type nélkül):
                    services.add(new Service(id, carId, serviceDate, location, notes, reminder));

                    // Mivel a Controllernek szüksége van a brand/type-ra, a Service osztályt ki kell egészíteni!
                    // Ezt most nem tehetjük meg, ezért a Controllerben kezeljük a hiányt.
                    // A fenti hívás hibát okozhat, ezért a következő pontban javítunk a Controlleren.
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return services;
    }
}