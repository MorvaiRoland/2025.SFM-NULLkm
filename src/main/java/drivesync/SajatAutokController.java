package drivesync;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.sql.*;

public class SajatAutokController {

    @FXML private Label welcomeLabel, formTitle, carDetailsLabel;
    @FXML private FlowPane carsList;
    @FXML private ScrollPane carsScrollPane;
    @FXML private TitledPane addCarTitledPane, carDetailsPane;
    @FXML private TextField licenseField, brandField, typeField, vintageField, engineTypeField, fuelTypeField, kmField, oilField, tireSizeField;
    @FXML private DatePicker serviceDatePickerCar, insuranceDatePicker;
    @FXML private ChoiceBox<String> serviceTypeChoice;
    @FXML private TextField serviceKmField, servicePriceField, replacedPartsField;
    @FXML private DatePicker serviceDatePicker;
    @FXML private ListView<String> serviceListView;
    @FXML private Button scrollLeftBtn, scrollRightBtn;

    private String username;
    private Integer editingCarId = null;
    private int selectedCarId = -1;

    public void setUsername(String username) {
        this.username = username;
        welcomeLabel.setText("Itt a saját autóid listája:");
        loadUserCars();
        loadServiceTypes();

        addCarTitledPane.expandedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) addCarTitledPane.setText("❌ Bezárás");
            else {
                addCarTitledPane.setText("➕ Új autó hozzáadása");
                clearFields();
                editingCarId = null;
                formTitle.setText("Új autó hozzáadása");
            }
        });
    }

    // Autók betöltése
    private void loadUserCars() {
        carsList.getChildren().clear();
        String sql = "SELECT * FROM cars WHERE owner_id = (SELECT id FROM users WHERE username = ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int carId = rs.getInt("id");
                VBox carBox = createCarCard(rs, carId);
                carsList.getChildren().add(carBox);
            }

            // Automatikusan az első autót mutatja
            if (!carsList.getChildren().isEmpty()) {
                VBox firstCar = (VBox) carsList.getChildren().get(0);
                firstCar.fireEvent(new MouseEvent(MouseEvent.MOUSE_CLICKED,
                        0,0,0,0, null,1,true,true,true,true,
                        true,true,true,true,true,true,null));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Hiba", "Nem sikerült betölteni az autókat!");
        }
    }

    // Kártya létrehozása
    private VBox createCarCard(ResultSet rs, int carId) throws SQLException {
        VBox box = new VBox(8);
        box.setStyle(
                "-fx-background-color: #ffffff;" +
                        "-fx-padding: 15;" +
                        "-fx-background-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0.5, 0, 3);" +
                        "-fx-alignment: center;"
        );
        box.setPrefWidth(200);
        box.setMinHeight(120);

        // Autó adatok
        String brand = rs.getString("brand") != null ? rs.getString("brand") : "N/A";
        String type = rs.getString("type") != null ? rs.getString("type") : "N/A";
        String license = rs.getString("license") != null ? rs.getString("license") : "N/A";
        int vintage = rs.getInt("vintage");
        int km = rs.getInt("km");

        // Márka + típus kiemelve
        Label brandTypeLabel = new Label(brand + " " + type);
        brandTypeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Rendszám, évjárat, km alatta
        Label detailsLabel = new Label("Rendszám: " + license + "\nÉvjárat: " + vintage + "\nKM: " + km);
        detailsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");
        detailsLabel.setWrapText(true);
        detailsLabel.setMaxWidth(180);

        box.getChildren().addAll(brandTypeLabel, detailsLabel);

        box.setOnMouseClicked(e -> showCarDetails(carId));
        return box;
    }



    // Autó részletek és szervizek
    private void showCarDetails(int carId) {
        selectedCarId = carId;
        carDetailsPane.setExpanded(true);

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM cars WHERE id=?")) {
            stmt.setInt(1, carId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                carDetailsLabel.setText(
                        rs.getString("brand") + " " + rs.getString("type") +
                                " (" + rs.getString("license") + "), " +
                                rs.getInt("vintage") + " - " +
                                rs.getInt("km") + " km"
                );
            }
        } catch (SQLException e) {
            showAlert("Hiba", "Nem sikerült betölteni az autó részleteit.");
        }

        loadServices(carId);
    }

    // Szervizek betöltése
    private void loadServices(int carId) {
        serviceListView.getItems().clear();

        if (carId <= 0) {
            System.out.println("Nincs kiválasztott autó: carId = " + carId);
            return;
        }

        String sql = """
                    SELECT s.service_date, t.name AS service_type, s.km, s.price, s.replaced_parts
                    FROM services s
                    LEFT JOIN service_types t ON s.service_type_id = t.id
                    WHERE s.car_id = ?
                    ORDER BY s.service_date DESC
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, carId);
            ResultSet rs = stmt.executeQuery();

            boolean hasService = false;

            while (rs.next()) {
                hasService = true;

                String date = rs.getDate("service_date") != null ? rs.getDate("service_date").toString() : "N/A";
                String type = rs.getString("service_type") != null ? rs.getString("service_type") : "Ismeretlen";
                int km = rs.getInt("km");
                int price = rs.getInt("price");
                String replacedParts = rs.getString("replaced_parts") != null ? rs.getString("replaced_parts") : "";

                String row = date + " - " + type + " (" + km + " km, " + price + " Ft)";
                if (!replacedParts.isEmpty()) {
                    row += " | Cserélt alkatrészek: " + replacedParts;
                }

                serviceListView.getItems().add(row);

                System.out.println("Service row: " + row); // debug
            }

            if (!hasService) {
                System.out.println("Nincs szerviz a kiválasztott autóhoz: carId = " + carId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Hiba", "Nem sikerült betölteni a szervizeket.");
        }
    }
    @FXML private Button generatePdfBtn; // PDF gomb

    @FXML
    private void handleGeneratePdf() {
        if (selectedCarId != -1) {
            PdfGenerator.generateCarReport(selectedCarId, username); // username-t is átadhatjuk
            showAlert("PDF elkészült", "A PDF jelentés sikeresen létrehozva!");
        } else {
            showAlert("Hiba", "Nincs kiválasztott autó!");
        }
    }




    // Szerviz típusok betöltése
    private void loadServiceTypes() {
        serviceTypeChoice.getItems().clear();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT name FROM service_types")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                serviceTypeChoice.getItems().add(rs.getString("name"));
            }
        } catch (SQLException e) {
            showAlert("Hiba", "Nem sikerült betölteni a szerviz típusokat.");
        }
    }

    // Szerviz mentése
    @FXML
    private void saveService() {
        if (selectedCarId == -1) {
            showAlert("Hiba", "Nincs kiválasztott autó!");
            return;
        }

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO services (car_id, service_type_id, km, price, service_date, replaced_parts) " +
                             "VALUES (?, (SELECT id FROM service_types WHERE name=?), ?, ?, ?, ?)")) {

            stmt.setInt(1, selectedCarId);
            stmt.setString(2, serviceTypeChoice.getValue());
            stmt.setInt(3, Integer.parseInt(serviceKmField.getText().trim()));
            stmt.setInt(4, Integer.parseInt(servicePriceField.getText().trim()));
            stmt.setDate(5, Date.valueOf(serviceDatePicker.getValue()));
            stmt.setString(6, replacedPartsField.getText().trim());

            stmt.executeUpdate();

            showAlert("Sikeres", "A szerviz rögzítve!");
            loadServices(selectedCarId);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Hiba", "Nem sikerült menteni a szervizt!");
        }
    }

    private void clearFields() {
        licenseField.clear(); brandField.clear(); typeField.clear(); vintageField.clear();
        engineTypeField.clear(); fuelTypeField.clear(); kmField.clear(); oilField.clear();
        tireSizeField.clear(); serviceDatePickerCar.setValue(null); insuranceDatePicker.setValue(null);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(message); alert.showAndWait();
    }

    @FXML
    private void scrollLeft() {
        animateScroll(-0.2);
    }

    @FXML
    private void scrollRight() {
        animateScroll(0.2);
    }

    private void animateScroll(double delta) {
        double h = carsScrollPane.getHvalue();
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(300),
                        new KeyValue(carsScrollPane.hvalueProperty(), Math.max(0, Math.min(1, h + delta))))
        );
        timeline.play();
    }

    @FXML
    private void handleAddOrEditCar() {
        if (editingCarId == null) addCar();
        else editCar(editingCarId);
    }

    private void addCar() {
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO cars (owner_id, license, brand, type, vintage, engine_type, fuel_type, km, oil, tire_size, service, insurance) " +
                             "VALUES ((SELECT id FROM users WHERE username=?), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {

            stmt.setString(1, username);
            stmt.setString(2, licenseField.getText().trim());
            stmt.setString(3, brandField.getText().trim());
            stmt.setString(4, typeField.getText().trim());
            stmt.setInt(5, Integer.parseInt(vintageField.getText().trim()));
            stmt.setString(6, engineTypeField.getText().trim());
            stmt.setString(7, fuelTypeField.getText().trim());
            stmt.setInt(8, Integer.parseInt(kmField.getText().trim()));
            stmt.setInt(9, Integer.parseInt(oilField.getText().trim()));
            stmt.setInt(10, Integer.parseInt(tireSizeField.getText().trim()));
            stmt.setDate(11, serviceDatePickerCar.getValue() != null ? Date.valueOf(serviceDatePickerCar.getValue()) : null);
            stmt.setDate(12, insuranceDatePicker.getValue() != null ? Date.valueOf(insuranceDatePicker.getValue()) : null);

            stmt.executeUpdate();
            showAlert("Sikeres", "Autó hozzáadva!");
            loadUserCars();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Hiba", "Nem sikerült az autó hozzáadása!");
        }
    }

    private void editCar(int carId) {
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE cars SET license=?, brand=?, type=?, vintage=?, engine_type=?, fuel_type=?, km=?, oil=?, tire_size=?, service=?, insurance=? " +
                             "WHERE id=?")) {

            stmt.setString(1, licenseField.getText().trim());
            stmt.setString(2, brandField.getText().trim());
            stmt.setString(3, typeField.getText().trim());
            stmt.setInt(4, Integer.parseInt(vintageField.getText().trim()));
            stmt.setString(5, engineTypeField.getText().trim());
            stmt.setString(6, fuelTypeField.getText().trim());
            stmt.setInt(7, Integer.parseInt(kmField.getText().trim()));
            stmt.setInt(8, Integer.parseInt(oilField.getText().trim()));
            stmt.setInt(9, Integer.parseInt(tireSizeField.getText().trim()));
            stmt.setDate(10, serviceDatePickerCar.getValue() != null ? Date.valueOf(serviceDatePickerCar.getValue()) : null);
            stmt.setDate(11, insuranceDatePicker.getValue() != null ? Date.valueOf(insuranceDatePicker.getValue()) : null);
            stmt.setInt(12, carId);

            stmt.executeUpdate();
            showAlert("Sikeres", "Az autó adatai frissítve!");
            loadUserCars();

            editingCarId = null;
            clearFields();
            addCarTitledPane.setExpanded(false);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Hiba", "Nem sikerült frissíteni az autót!");
        }
    }
}
