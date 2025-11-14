package drivesync;

import drivesync.Adatbázis.Database;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class CarReminderPopup {

    private static boolean shown = false;

    private static class CarReminder {
        int id;
        String license;
        String brand;
        String type;
        LocalDate inspectionDate;
        LocalDate insuranceDate;
        LocalDate nextServiceDate;
    }

    public void showReminders(String username) {
        if (shown) return;
        shown = true;

        List<CarReminder> reminders = getCarReminders(username);
        if (reminders.isEmpty()) return;

        Stage popup = new Stage();
        popup.initStyle(StageStyle.UNDECORATED);
        popup.setAlwaysOnTop(true);

        VBox container = new VBox(10);
        container.setPadding(new Insets(10));
        container.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: gray; -fx-border-radius: 5; -fx-background-radius: 5;");
        container.setAlignment(Pos.CENTER_LEFT);

        Label header = new Label("Autós értesítés");
        header.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        container.getChildren().add(header);

        for (CarReminder car : reminders) {
            VBox carBox = new VBox(5);

            Label title = new Label(car.brand + " " + car.type + " (" + car.license + ")");
            title.setStyle("-fx-font-weight: bold;");
            carBox.getChildren().add(title);

            carBox.getChildren().add(createProgressHBox("✅ Műszaki vizsga", car.inspectionDate));
            carBox.getChildren().add(createProgressHBox("🛡️ Biztosítás", car.insuranceDate));
            carBox.getChildren().add(createProgressHBox("🔧 Következő szerviz", car.nextServiceDate));

            container.getChildren().add(carBox);
        }

        Scene scene = new Scene(container);
        popup.setScene(scene);

        // Popup jobb alsó sarok
        javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
        popup.setX(screenBounds.getWidth() - 350);
        popup.setY(screenBounds.getHeight() - 200);

        popup.show();

        // 8 másodperc után automatikus eltűnés
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(8), e -> popup.close()));
        timeline.play();
    }

    private List<CarReminder> getCarReminders(String username) {
        List<CarReminder> reminders = new ArrayList<>();
        String sql = """
            SELECT c.id, c.license, c.brand, c.type,
                   c.inspection_date, c.insurance,
                   (SELECT MIN(u.service_date)
                    FROM upcoming_services u
                    WHERE u.car_id = c.id AND u.archived = 0
                   ) AS next_service
            FROM cars c
            WHERE c.owner_id = (SELECT id FROM users WHERE username=?)
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                CarReminder c = new CarReminder();
                c.id = rs.getInt("id");
                c.license = rs.getString("license");
                c.brand = rs.getString("brand");
                c.type = rs.getString("type");

                if (rs.getDate("inspection_date") != null)
                    c.inspectionDate = rs.getDate("inspection_date").toLocalDate();

                if (rs.getDate("insurance") != null)
                    c.insuranceDate = rs.getDate("insurance").toLocalDate();

                if (rs.getDate("next_service") != null)
                    c.nextServiceDate = rs.getDate("next_service").toLocalDate();

                reminders.add(c);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return reminders;
    }

    /**
     * Arányos ProgressBar készítése, szín változik a közelgő napok alapján
     */
    private HBox createProgressHBox(String label, LocalDate date) {
        Label lbl = new Label(label + ": " + (date != null ? date.toString() : "Nincs adat"));
        lbl.setPrefWidth(180);

        ProgressBar progress = new ProgressBar();
        progress.setPrefWidth(220);

        if (date != null) {
            long days = ChronoUnit.DAYS.between(LocalDate.now(), date);
            double value;
            String color;

            if (days < 0) {
                value = 1.0;
                color = "red"; // lejárt
            } else if (days <= 7) {
                value = (double) days / 7; // közel
                color = "orange";
            } else if (days <= 30) {
                value = (double) days / 30; // távolabbi, lassan növekszik
                color = "green";
            } else {
                value = 1.0; // messze, teljes csík
                color = "green";
            }

            progress.setProgress(value);
            progress.setStyle("-fx-accent: " + color + ";");
        } else {
            progress.setProgress(0);
            progress.setStyle("-fx-accent: gray;");
        }

        HBox hBox = new HBox(10, lbl, progress);
        hBox.setAlignment(Pos.CENTER_LEFT);
        return hBox;
    }
}
