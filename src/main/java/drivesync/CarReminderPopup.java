package drivesync;

import drivesync.Adatbázis.Database;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
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
    private static final int MAX_WIDTH = 400;
    private static final int SAFE_MAX_HEIGHT = 350;

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
        if (reminders.isEmpty()) {
            shown = false; // Ha nincs mit mutatni, reseteljük a flag-et
            return;
        }

        Stage popup = new Stage();
        popup.initStyle(StageStyle.UNDECORATED);
        popup.setAlwaysOnTop(true);

        // --- 1. Fejléc és Bezáró Gomb ---
        Button closeBtn = new Button("❌");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #555;");
        closeBtn.setOnAction(e -> popup.close());

        Label headerText = new Label("🔔 Közelgő Autós Események");
        headerText.setStyle("-fx-font-weight: bold; -fx-font-size: 16; -fx-text-fill: #1a237e;");

        HBox header = new HBox(10, headerText);
        header.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(headerText, Priority.ALWAYS);
        header.getChildren().add(closeBtn);

        VBox contentVBox = new VBox(15);
        contentVBox.setPadding(new Insets(10, 15, 10, 15));
        contentVBox.setAlignment(Pos.TOP_LEFT);

        contentVBox.getChildren().add(header);
        contentVBox.getChildren().add(new Separator());

        // Autó bejegyzések
        for (CarReminder car : reminders) {
            VBox carBox = createCarReminderBox(car);
            contentVBox.getChildren().add(carBox);
        }

        // ScrollPane
        ScrollPane scrollPane = new ScrollPane(contentVBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setMaxHeight(SAFE_MAX_HEIGHT);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        VBox mainContainer = new VBox(0, scrollPane);
        mainContainer.setPrefWidth(MAX_WIDTH);

        // Modern popup stílus
        mainContainer.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0.2, 0, 4);"
        );

        Scene scene = new Scene(mainContainer);
        scene.setFill(Color.TRANSPARENT);
        popup.setScene(scene);

        // Pozicionálás
        javafx.geometry.Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        popup.sizeToScene();
        double actualHeight = popup.getHeight();

        popup.setX(screenBounds.getWidth() - MAX_WIDTH - 20);
        popup.setY(screenBounds.getHeight() - actualHeight - 20);

        popup.show();

        // VÁLTOZÁS 8: Automatikus eltűnés ELTÁVOLÍTVA.
    }

    /**
     * Autó doboz létrehozása és Progress Bar hívása
     */
    private VBox createCarReminderBox(CarReminder car) {
        VBox carBox = new VBox(5);
        carBox.getStyleClass().add("reminder-car-box");
        carBox.setPadding(new Insets(10, 0, 10, 0));

        Label title = new Label(car.brand + " " + car.type + " (" + car.license + ")");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1a237e;");
        carBox.getChildren().add(title);

        carBox.getChildren().add(createProgressHBox("Műszaki vizsga", car.inspectionDate, 730));
        carBox.getChildren().add(createProgressHBox("Biztosítás", car.insuranceDate, 365));
        carBox.getChildren().add(createProgressHBox("Következő szerviz", car.nextServiceDate, 90));

        carBox.getChildren().add(new Separator());
        return carBox;
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

                // Csak akkor adjuk hozzá, ha van bármilyen dátum beállítva
                if (c.inspectionDate != null || c.insuranceDate != null || c.nextServiceDate != null) {
                    reminders.add(c);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return reminders;
    }

    /**
     * Arányos ProgressBar készítése, szín változik a közelgő napok alapján
     * @param maxDays A teljes sávot reprezentáló maximális napok száma.
     */
    private HBox createProgressHBox(String label, LocalDate date, int maxDays) {

        ProgressBar progress = new ProgressBar();
        progress.setPrefWidth(150);

        // VÁLTOZÁS 9: Kezeli a NULL (Nincs adat) esetet külön
        if (date == null) {
            Label lbl = new Label("❔ " + label);
            lbl.setPrefWidth(120);
            lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: gray;");

            Label daysLbl = new Label("Nincs adat");
            daysLbl.setPrefWidth(100);
            daysLbl.setAlignment(Pos.CENTER_RIGHT);
            daysLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: gray;");

            progress.setProgress(0);
            progress.setStyle("-fx-accent: lightgray;");

            return new HBox(5, lbl, daysLbl, progress);
        }

        // --- Dátum és progressz számítás ---
        String statusIcon = " ";
        String dateText;
        long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), date);

        String color;
        double value;

        if (daysRemaining < 0) {
            value = 1.0;
            color = "red";
            statusIcon = "❌";
            dateText = "LEJÁRT (" + Math.abs(daysRemaining) + " napja)";
        } else if (daysRemaining <= 7) {
            value = 1.0;
            color = "red";
            statusIcon = "⚠️";
            dateText = daysRemaining + " napig";
        } else if (daysRemaining <= 30) {
            value = 0.75;
            color = "orange";
            statusIcon = "🔔";
            dateText = daysRemaining + " napig";
        } else {
            // Messze (> 30 nap) vagy távoli (> maxDays, teljes sáv)
            value = Math.min(1.0, (double) daysRemaining / maxDays);
            color = "green";
            statusIcon = "✅";
            // Pontos dátum, ha már nem a napokat számoljuk kiemelten
            dateText = date.toString();
        }

        progress.setProgress(value);
        progress.getStyleClass().add(color + "-bar");

        Label lbl = new Label(statusIcon + " " + label);
        lbl.setPrefWidth(120);
        lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #444;");

        Label daysLbl = new Label(dateText);
        daysLbl.setPrefWidth(100);
        daysLbl.setAlignment(Pos.CENTER_RIGHT);
        daysLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #333;");

        HBox hBox = new HBox(5, lbl, daysLbl, progress);
        hBox.setAlignment(Pos.CENTER_LEFT);

        // Kiemelés, ha közeleg vagy lejárt
        if (daysRemaining <= 30 && daysRemaining >= 0) {
            hBox.setStyle("-fx-background-color: #fff3e0; -fx-padding: 3; -fx-background-radius: 4;");
        } else if (daysRemaining < 0) {
            hBox.setStyle("-fx-background-color: #ffe0e0; -fx-padding: 3; -fx-background-radius: 4;");
        }

        return hBox;
    }
}