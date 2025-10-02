package drivesync;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.geometry.Pos;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class HomeDashboardController {

    @FXML private FlowPane carsFlowPane;
    @FXML private Label welcomeLabel;
    @FXML private Label dateLabel;

    private static String username;

    public void setUsername(String user) {
        username = user;
        welcomeLabel.setText("Üdvözöllek, " + user + "!");
        dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")));
        loadCars();
    }

    public static String getUsername() {return username;}

    private void loadCars() {
        carsFlowPane.getChildren().clear();
        List<Car> cars = new ArrayList<>();
        String sql = "SELECT * FROM cars WHERE owner_id = (SELECT id FROM users WHERE username = ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Car car = new Car(
                        rs.getInt("id"),
                        rs.getInt("owner_id"),
                        rs.getString("license"),
                        rs.getString("brand"),
                        rs.getString("type"),
                        rs.getInt("vintage"),
                        rs.getString("engine_type"),
                        rs.getString("fuel_type"),
                        rs.getInt("km"),
                        rs.getInt("oil"),
                        rs.getInt("tire_size"),
                        rs.getDate("service").toLocalDate(),
                        rs.getDate("insurance").toLocalDate()
                );
                cars.add(car);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        for (Car car : cars) {
            carsFlowPane.getChildren().add(createCarWidget(car));
        }
    }

    private VBox createCarWidget(Car car) {
        VBox box = new VBox(12);
        box.setAlignment(Pos.TOP_CENTER);
        box.setStyle(
                "-fx-padding: 20; " +
                        "-fx-background-radius: 20; " +
                        "-fx-background-color: linear-gradient(to bottom right, #6dd5ed, #2193b0);" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.25), 15, 0, 0, 5);"
        );

        // Hover effekt
        box.setOnMouseEntered(e -> box.setScaleX(1.05));
        box.setOnMouseEntered(e -> box.setScaleY(1.05));
        box.setOnMouseExited(e -> box.setScaleX(1));
        box.setOnMouseExited(e -> box.setScaleY(1));

        // Ikonok
        Image carIcon = new Image(getClass().getResourceAsStream("/drivesync/icons/car.png"), 60, 60, true, true);
        Image oilIcon = new Image(getClass().getResourceAsStream("/drivesync/icons/oil.png"), 24, 24, true, true);
        Image insuranceIcon = new Image(getClass().getResourceAsStream("/drivesync/icons/insurance.png"), 24, 24, true, true);

        ImageView carView = new ImageView(carIcon);
        ImageView oilView = new ImageView(oilIcon);
        ImageView insuranceView = new ImageView(insuranceIcon);

        // Feliratok
        Label licenseLbl = new Label(car.getLicense());
        licenseLbl.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #ffffff;");

        Label brandTypeLbl = new Label(car.getBrand() + " " + car.getType());
        brandTypeLbl.setStyle("-fx-font-size: 14; -fx-text-fill: #ecf0f1;");

        Label vintageLbl = new Label("Évjárat: " + car.getVintage());
        Label kmLbl = new Label("Km: " + car.getKm());
        vintageLbl.setStyle("-fx-font-size: 12; -fx-text-fill: #dff9fb;");
        kmLbl.setStyle("-fx-font-size: 12; -fx-text-fill: #dff9fb;");

        // Progress bar-ok
        double serviceProgress = calculateProgress(car.getService());
        ProgressBar serviceBar = new ProgressBar(serviceProgress);
        serviceBar.setStyle("-fx-accent: #2ecc71;");

        Label serviceLbl = new Label("Szerviz: " + car.getService().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")));
        serviceLbl.setStyle("-fx-font-size: 12; -fx-text-fill: #ffffff;");

        double insuranceProgress = calculateProgress(car.getInsurance());
        ProgressBar insuranceBar = new ProgressBar(insuranceProgress);
        insuranceBar.setStyle("-fx-accent: #e74c3c;");

        Label insuranceLbl = new Label("Bizt.: " + car.getInsurance().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")));
        insuranceLbl.setStyle("-fx-font-size: 12; -fx-text-fill: #ffffff;");

        box.getChildren().addAll(carView, licenseLbl, brandTypeLbl, vintageLbl, kmLbl,
                oilView, serviceLbl, serviceBar,
                insuranceView, insuranceLbl, insuranceBar);

        return box;
    }

    private double calculateProgress(LocalDate date) {
        LocalDate today = LocalDate.now();
        long totalDays = ChronoUnit.DAYS.between(today, date);
        double progress = 1.0 - Math.min(Math.max(totalDays / 365.0, 0), 1);
        return progress;
    }
}
