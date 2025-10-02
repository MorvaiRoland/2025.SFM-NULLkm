package drivesync;

import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.sql.*;

public class SajatAutokController {

    @FXML private Label welcomeLabel, formTitle;
    @FXML private FlowPane carsList;
    @FXML private TitledPane addCarTitledPane;
    @FXML private Button addCarButton;

    @FXML private TextField licenseField, brandField, typeField, vintageField, engineTypeField, fuelTypeField, kmField, oilField, tireSizeField;
    @FXML private DatePicker serviceDatePicker, insuranceDatePicker;

    @FXML private Button scrollLeftBtn, scrollRightBtn; // nyilakhoz (ha csinálsz a FXML-ben)

    private String username;
    private Integer editingCarId = null;

    // ----------------- Inicializálás -----------------
    public void setUsername(String username) {
        this.username = username;
        welcomeLabel.setText("Itt a saját autóid listája:");
        loadUserCars();

        // Panel ikon változtatása a nyitás/záráskor
        addCarTitledPane.expandedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) addCarTitledPane.setText("❌ Bezárás");
            else addCarTitledPane.setText("➕ Új autó hozzáadása");
        });
    }

    // ----------------- Autók betöltése -----------------
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

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Hiba", "Nem sikerült betölteni az autókat!");
        }
    }

    // ----------------- Autó kártya létrehozása -----------------
    private VBox createCarCard(ResultSet rs, int carId) throws SQLException {
        final int id = carId; // final a lambda-hoz

        VBox box = new VBox(8);
        box.setStyle("-fx-background-color: #ffffff; -fx-padding: 15; -fx-background-radius: 12;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0.5, 0, 3);");

        Label infoLabel = new Label(String.format("%s %s (%s) - %d, KM: %d",
                rs.getString("brand"),
                rs.getString("type"),
                rs.getString("license"),
                rs.getInt("vintage"),
                rs.getInt("km")
        ));
        infoLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #34495e;");

        Button editBtn = new Button("✏ Szerkeszt");
        editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 8;");
        editBtn.setOnAction(e -> showEditForm(id));

        Button deleteBtn = new Button("❌ Törlés");
        deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 8;");
        deleteBtn.setOnAction(e -> deleteCar(id));

        VBox buttonsBox = new VBox(5, editBtn, deleteBtn);

        box.getChildren().addAll(infoLabel, buttonsBox);
        return box;
    }

    // ----------------- Hozzáadás panel -----------------
    @FXML
    private void showAddForm() {
        editingCarId = null;
        formTitle.setText("Új autó hozzáadása");
        clearFields();

        if (!addCarTitledPane.isExpanded()) {
            animateTitledPaneOpen();
        }
    }

    private void showEditForm(int carId) {
        editingCarId = carId;
        formTitle.setText("Autó szerkesztése");
        addCarTitledPane.setExpanded(true);

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM cars WHERE id = ?")) {
            stmt.setInt(1, carId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                licenseField.setText(rs.getString("license"));
                brandField.setText(rs.getString("brand"));
                typeField.setText(rs.getString("type"));
                vintageField.setText(String.valueOf(rs.getInt("vintage")));
                engineTypeField.setText(rs.getString("engine_type"));
                fuelTypeField.setText(rs.getString("fuel_type"));
                kmField.setText(String.valueOf(rs.getInt("km")));
                oilField.setText(String.valueOf(rs.getInt("oil")));
                tireSizeField.setText(String.valueOf(rs.getInt("tire_size")));
                serviceDatePicker.setValue(rs.getDate("service").toLocalDate());
                insuranceDatePicker.setValue(rs.getDate("insurance").toLocalDate());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Hiba", "Nem sikerült az autót betölteni!");
        }
    }

    // ----------------- Hozzáadás / szerkesztés gomb -----------------
    @FXML
    private void handleAddOrEditCar() {
        if (editingCarId == null) addCar();
        else editCar(editingCarId);
    }

    private void addCar() {
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
            afterSave();
        } catch (Exception e) { showAlert("Hiba", "Nem sikerült az autó hozzáadása!"); }
    }

    private void editCar(int carId) {
        try {
            String sql = "UPDATE cars SET license=?, brand=?, type=?, vintage=?, engine_type=?, fuel_type=?, km=?, oil=?, tire_size=?, service=?, insurance=? WHERE id=?";
            try (Connection conn = Database.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, licenseField.getText().trim());
                stmt.setString(2, brandField.getText().trim());
                stmt.setString(3, typeField.getText().trim());
                stmt.setInt(4, Integer.parseInt(vintageField.getText().trim()));
                stmt.setString(5, engineTypeField.getText().trim());
                stmt.setString(6, fuelTypeField.getText().trim());
                stmt.setInt(7, Integer.parseInt(kmField.getText().trim()));
                stmt.setInt(8, Integer.parseInt(oilField.getText().trim()));
                stmt.setInt(9, tireSizeField.getText().trim().isEmpty() ? 0 : Integer.parseInt(tireSizeField.getText().trim()));
                stmt.setDate(10, Date.valueOf(serviceDatePicker.getValue()));
                stmt.setDate(11, Date.valueOf(insuranceDatePicker.getValue()));
                stmt.setInt(12, carId);
                stmt.executeUpdate();
            }
            afterSave();
        } catch (Exception e) { showAlert("Hiba", "Nem sikerült az autó szerkesztése!"); }
    }

    private void afterSave() {
        clearFields();
        addCarTitledPane.setExpanded(false);
        editingCarId = null;
        loadUserCars();
    }

    // ----------------- Autó törlése -----------------
    private void deleteCar(int carId) {
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM cars WHERE id = ?")) {
            stmt.setInt(1, carId);
            stmt.executeUpdate();
            loadUserCars();
        } catch (SQLException e) { showAlert("Hiba", "Nem sikerült az autó törlése!"); }
    }

    // ----------------- Segédmetódusok -----------------
    private void clearFields() {
        licenseField.clear(); brandField.clear(); typeField.clear(); vintageField.clear();
        engineTypeField.clear(); fuelTypeField.clear(); kmField.clear(); oilField.clear();
        tireSizeField.clear(); serviceDatePicker.setValue(null); insuranceDatePicker.setValue(null);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(message); alert.showAndWait();
    }

    // ----------------- Animáció -----------------
    private void animateTitledPaneOpen() {
        addCarTitledPane.setExpanded(true);
        RotateTransition rt = new RotateTransition(Duration.millis(150), addCarTitledPane.lookup(".arrow"));
        rt.setFromAngle(0);
        rt.setToAngle(90);
        rt.play();
    }

    // ----------------- Scroll animáció kártyákhoz -----------------
    @FXML
    private void scrollLeft() {
        double h = carsList.getParent() instanceof ScrollPane scroll ? scroll.getHvalue() : 0;
        animateScroll((ScrollPane) carsList.getParent(), Math.max(h - 0.2, 0), 300);
    }

    @FXML
    private void scrollRight() {
        double h = carsList.getParent() instanceof ScrollPane scroll ? scroll.getHvalue() : 0;
        animateScroll((ScrollPane) carsList.getParent(), Math.min(h + 0.2, 1), 300);
    }

    private void animateScroll(ScrollPane scrollPane, double target, int durationMs) {
        Timeline timeline = new Timeline();
        KeyValue kv = new KeyValue(scrollPane.hvalueProperty(), target);
        KeyFrame kf = new KeyFrame(Duration.millis(durationMs), kv);
        timeline.getKeyFrames().add(kf);
        timeline.play();
    }
}
