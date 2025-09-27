package drivesync;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SajatAutokController {

    @FXML private Label welcomeLabel;
    @FXML private VBox carsList;

    @FXML private TextField licenseField, brandField, typeField, vintageField, engineTypeField, fuelTypeField, kmField, oilField, tireSizeField;
    @FXML private DatePicker serviceDatePicker, insuranceDatePicker;

    private String username;

    public void setUsername(String username) {
        this.username = username;
        welcomeLabel.setText("Üdv, " + username + "! Itt a saját autóid listája:");
        loadUserCars();
    }

    private void loadUserCars() {
        carsList.getChildren().clear();
        String sql = "SELECT * FROM cars WHERE owner_id = (SELECT id FROM users WHERE username = ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int carId = rs.getInt("id");
                String carInfo = String.format("%s %s (%s) - %d, KM: %d",
                        rs.getString("brand"),
                        rs.getString("type"),
                        rs.getString("license"),
                        rs.getInt("vintage"),
                        rs.getInt("km")
                );
                VBox carBox = createCarBox(carId, carInfo);
                carsList.getChildren().add(carBox);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createCarBox(int carId, String carInfo) {
        VBox box = new VBox(5);
        Label infoLabel = new Label(carInfo);
        infoLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Button deleteBtn = new Button("❌ Törlés");
        deleteBtn.setStyle("-fx-background-color: #FF4C4C; -fx-text-fill: white; -fx-background-radius: 8;");
        deleteBtn.setOnAction(e -> deleteCar(carId));

        box.getChildren().addAll(infoLabel, deleteBtn);
        box.setStyle("-fx-background-color: #FFD700; -fx-padding: 10; -fx-background-radius: 8;");
        return box;
    }

    @FXML
    private void handleAddCar() {
        try {
            String sql = "INSERT INTO cars (owner_id, license, brand, type, vintage, engine_type, fuel_type, km, oil, tire_size, service, insurance) " +
                    "VALUES ((SELECT id FROM users WHERE username = ?), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (Connection conn = Database.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, username);
                stmt.setString(2, licenseField.getText().trim());
                stmt.setString(3, brandField.getText().trim());
                stmt.setString(4, typeField.getText().trim());
                stmt.setInt(5, Integer.parseInt(vintageField.getText().trim()));
                stmt.setString(6, engineTypeField.getText().trim());
                stmt.setString(7, fuelTypeField.getText().trim());
                stmt.setInt(8, Integer.parseInt(kmField.getText().trim()));
                stmt.setInt(9, Integer.parseInt(oilField.getText().trim()));
                stmt.setInt(10, tireSizeField.getText().trim().isEmpty() ? 0 : Integer.parseInt(tireSizeField.getText().trim()));
                stmt.setDate(11, Date.valueOf(serviceDatePicker.getValue()));
                stmt.setDate(12, Date.valueOf(insuranceDatePicker.getValue()));

                stmt.executeUpdate();
            }

            clearFields();
            loadUserCars();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Hiba", "Nem sikerült az autó hozzáadása!");
        } catch (NumberFormatException e) {
            showAlert("Hiba", "Hibás szám formátum a mezőkben!");
        }
    }

    private void deleteCar(int carId) {
        String sql = "DELETE FROM cars WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, carId);
            stmt.executeUpdate();
            loadUserCars();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Hiba", "Nem sikerült az autó törlése!");
        }
    }

    private void clearFields() {
        licenseField.clear();
        brandField.clear();
        typeField.clear();
        vintageField.clear();
        engineTypeField.clear();
        fuelTypeField.clear();
        kmField.clear();
        oilField.clear();
        tireSizeField.clear();
        serviceDatePicker.setValue(null);
        insuranceDatePicker.setValue(null);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
