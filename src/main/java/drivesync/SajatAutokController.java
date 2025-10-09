package drivesync;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.geometry.Insets;
import javafx.scene.paint.Color;



import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class SajatAutokController {

    @FXML private Label welcomeLabel, formTitle, carDetailsLabel;
    @FXML private FlowPane carsList;
    @FXML private ScrollPane carsScrollPane;
    @FXML private TitledPane addCarTitledPane, carDetailsPane;
    @FXML private TextField licenseField, brandField, typeField, engineTypeField, kmField, oilField, tireSizeField;
    @FXML private ChoiceBox<String> fuelTypeField;
    @FXML private ChoiceBox<Integer> vintageField;
    @FXML private DatePicker serviceDatePickerCar, insuranceDatePicker;
    @FXML private ComboBox<String> serviceTypeCombo;
    @FXML private DatePicker serviceDatePicker;
    @FXML private TextField serviceKmField, servicePriceField, replacedPartsField;
    @FXML private ListView<String> serviceListView;
    @FXML private Button scrollLeftBtn, scrollRightBtn, generatePdfBtn;
    @FXML private ColorPicker colorPicker;
    @FXML private TextArea notesField;
    @FXML
    private Label selectedCarLabel; // ez mutatja a kiválasztott autót a szerviz hozzáadásnál




    private String username;
    private Integer editingCarId = null;
    private int selectedCarId = -1;

    @FXML
    private ComboBox<String> brandCombo;
    @FXML
    private ComboBox<String> typeCombo;

    @FXML
    public void initialize() {
        // Évjáratok
        IntStream.rangeClosed(1900, 2025).forEach(vintageField.getItems()::add);
        vintageField.setValue(2025);

        // Üzemanyag
        fuelTypeField.getItems().addAll("Benzin", "Dízel", "Elektromos", "Hibrid");
        fuelTypeField.setValue("Benzin");

        // Márkák betöltése az adatbázisból
        loadBrands();

        // Márka kiválasztásakor frissül a típus lista
        brandCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) loadTypesForBrand(newVal);
        });

        // TitledPane viselkedés
        if (addCarTitledPane != null) {
            addCarTitledPane.expandedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) addCarTitledPane.setText("❌ Bezárás");
                else {
                    addCarTitledPane.setText("➕ Új autó hozzáadása");
                    clearFields();
                }
            });
        }
    }

// ----------------------------- MÁRKA ÉS TÍPUS BETÖLTÉS -----------------------------


    // --- Globális változók a controllerben ---
    private ObservableList<String> typeItems = FXCollections.observableArrayList(); // típusok alapja
    private FilteredList<String> filteredTypes; // típusokhoz

    // --- Márkák betöltése ---
    private void loadBrands() {
        if (brandCombo == null || typeCombo == null) return;

        brandCombo.getItems().clear();
        typeCombo.getItems().clear();

        List<String> brands = new ArrayList<>();
        String sql = "SELECT DISTINCT brand FROM car_types ORDER BY brand";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                brands.add(rs.getString("brand"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Hiba", "Nem sikerült betölteni a márkákat!");
            return;
        }

        ObservableList<String> brandItems = FXCollections.observableArrayList(brands);
        FilteredList<String> filteredBrands = new FilteredList<>(brandItems, p -> true);

        brandCombo.setEditable(true);
        brandCombo.setItems(filteredBrands);

        // Márkák keresése gépelésre
        brandCombo.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            final String search = newVal.toLowerCase();
            filteredBrands.setPredicate(item -> item.toLowerCase().contains(search));
        });

        // Típus ComboBox inicializálása egyszer
        filteredTypes = new FilteredList<>(typeItems, p -> true);
        typeCombo.setEditable(true);
        typeCombo.setItems(filteredTypes);

        typeCombo.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            final String search = newVal.toLowerCase();
            filteredTypes.setPredicate(item -> item.toLowerCase().contains(search));
        });

        // Márka kiválasztásakor frissítjük a típusokat
        brandCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            loadTypesForBrand(newVal);
        });
    }

    // --- Típusok betöltése adott márkához ---
    private void loadTypesForBrand(String brand) {
        typeItems.clear(); // előző típusok törlése

        if (brand == null || brand.isEmpty()) {
            typeCombo.getSelectionModel().clearSelection();
            typeCombo.getEditor().clear();
            return;
        }

        List<String> types = new ArrayList<>();
        String sql = "SELECT DISTINCT type FROM car_types WHERE brand=? ORDER BY type";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, brand);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                types.add(rs.getString("type"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Hiba", "Nem sikerült betölteni a típusokat!");
            return;
        }

        typeItems.setAll(types); // frissítjük az ObservableList-et
        typeCombo.getSelectionModel().clearSelection();
        typeCombo.getEditor().clear();
    }




    public void setUsername(String username) {
        this.username = username;
        if (welcomeLabel != null) welcomeLabel.setText("Itt a saját autóid listája:");
        loadUserCars();
        loadServiceTypes(); // kereshető combo feltöltése
    }

    // ----------------------------- AUTÓK -----------------------------

    private VBox currentlySelectedCard = null; // a kijelölt kártya referenciája

    private void loadUserCars() {
        if (carsList == null) return;
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

            // Ha van legalább egy autó, jelöljük ki az elsőt
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

    private VBox createCarCard(ResultSet rs, int carId) throws SQLException {
        String brand = rs.getString("brand");
        String type = rs.getString("type");
        String license = rs.getString("license");
        int vintage = rs.getInt("vintage");
        int km = rs.getInt("km");

        VBox box = new VBox(12);
        box.setPrefWidth(230);
        box.setMinHeight(150);
        box.setAlignment(javafx.geometry.Pos.TOP_CENTER);
        box.setStyle(getDefaultCardStyle());

        Label brandTypeLabel = new Label(brand + " " + type);
        brandTypeLabel.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #1a237e;");

        Label licenseLabel = new Label(license);
        licenseLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white; " +
                "-fx-background-color: #2196F3; -fx-background-radius: 6; -fx-padding: 4 8 4 8;");

        Label detailsLabel = new Label("Évjárat: " + vintage + "\nKM: " + km);
        detailsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #444; -fx-opacity: 0.9;");
        detailsLabel.setWrapText(true);
        detailsLabel.setMaxWidth(180);

        box.getChildren().addAll(brandTypeLabel, licenseLabel, detailsLabel);

        // Hover effekt
        box.setOnMouseEntered(e -> {
            if (box != currentlySelectedCard) box.setStyle(getHoverCardStyle());
        });
        box.setOnMouseExited(e -> {
            if (box != currentlySelectedCard) box.setStyle(getDefaultCardStyle());
        });

        // Kattintás: kiválasztás
        box.setOnMouseClicked(e -> {
            // előző kijelölés visszaállítása
            if (currentlySelectedCard != null) currentlySelectedCard.setStyle(getDefaultCardStyle());
            currentlySelectedCard = box;
            box.setStyle(getSelectedCardStyle());

            selectedCarId = carId;
            if (selectedCarLabel != null)
                selectedCarLabel.setText(brand + " " + type + " (" + license + ")");

            if (carDetailsPane != null) carDetailsPane.setExpanded(true);
            showCarDetails(carId);
        });

        return box;
    }

    private String getDefaultCardStyle() {
        return "-fx-background-color: linear-gradient(to bottom right, #f9f9f9, #ffffff);" +
                "-fx-background-radius: 18; -fx-padding: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 12, 0.3, 0, 4); -fx-cursor: hand;";
    }

    private String getHoverCardStyle() {
        return "-fx-background-color: linear-gradient(to bottom right, #e3f2fd, #ffffff);" +
                "-fx-background-radius: 18; -fx-padding: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(33,150,243,0.4), 15, 0.3, 0, 5); -fx-cursor: hand;";
    }

    private String getSelectedCardStyle() {
        return "-fx-background-color: linear-gradient(to bottom right, #bbdefb, #90caf9);" +
                "-fx-background-radius: 18; -fx-padding: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(33,150,243,0.6), 18, 0.3, 0, 6); -fx-cursor: hand;";
    }


    private void showCarDetails(int carId) {
        selectedCarId = carId;
        if (carDetailsPane != null) carDetailsPane.setExpanded(true);

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM cars WHERE id=?")) {

            stmt.setInt(1, carId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && carDetailsLabel != null) {
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

    // ----------------------------- SZERVIZEK -----------------------------

    private void loadServices(int carId) {
        if (serviceListView == null) return;
        serviceListView.getItems().clear();
        if (carId <= 0) return;

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
            while (rs.next()) {
                String date = rs.getDate("service_date") != null ? rs.getDate("service_date").toString() : "N/A";
                String type = rs.getString("service_type") != null ? rs.getString("service_type") : "Ismeretlen";
                int km = rs.getInt("km");
                int price = rs.getInt("price");
                String replacedParts = rs.getString("replaced_parts") != null ? rs.getString("replaced_parts") : "";
                String row = date + " - " + type + " (" + km + " km, " + price + " Ft)";
                if (!replacedParts.isEmpty()) row += " | Cserélt alkatrészek: " + replacedParts;
                serviceListView.getItems().add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Hiba", "Nem sikerült betölteni a szervizeket.");
        }
    }

    private void loadServiceTypes() {
        if (serviceTypeCombo == null) return;

        List<String> types = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT name FROM service_types ORDER BY name")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) types.add(rs.getString("name"));
        } catch (SQLException e) {
            showAlert("Hiba", "Nem sikerült betölteni a szerviz típusokat.");
            return;
        }

        ObservableList<String> allItems = FXCollections.observableArrayList(types);
        FilteredList<String> filteredItems = new FilteredList<>(allItems, p -> true);

        serviceTypeCombo.setEditable(true);
        serviceTypeCombo.setItems(filteredItems);

        // Gépelésre szűrés
        serviceTypeCombo.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            final String search = newValue.toLowerCase();
            filteredItems.setPredicate(item -> item.toLowerCase().contains(search));
        });
    }

    @FXML
    private void saveService() {
        if (selectedCarId == -1) {
            showAlert("Hiba", "Nincs kiválasztott autó!");
            return;
        }

        if (serviceTypeCombo.getValue() == null || serviceKmField.getText().isEmpty() || servicePriceField.getText().isEmpty()
                || serviceDatePicker.getValue() == null) {
            showAlert("Hiba", "Kérlek töltsd ki a kötelező mezőket!");
            return;
        }

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO services (car_id, service_type_id, km, price, service_date, replaced_parts) " +
                             "VALUES (?, (SELECT id FROM service_types WHERE name=? LIMIT 1), ?, ?, ?, ?)")) {

            stmt.setInt(1, selectedCarId);
            stmt.setString(2, serviceTypeCombo.getValue());
            stmt.setInt(3, Integer.parseInt(serviceKmField.getText().trim()));
            stmt.setInt(4, Integer.parseInt(servicePriceField.getText().trim()));
            stmt.setDate(5, Date.valueOf(serviceDatePicker.getValue()));
            stmt.setString(6, replacedPartsField.getText().trim());
            stmt.executeUpdate();

            showAlert("Sikeres", "A szerviz rögzítve!");
            loadServices(selectedCarId);

            // mezők törlése
            serviceTypeCombo.getSelectionModel().clearSelection();
            serviceKmField.clear();
            servicePriceField.clear();
            replacedPartsField.clear();
            serviceDatePicker.setValue(null);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Hiba", "Nem sikerült menteni a szervizt!");
        }
    }


    // ----------------------------- GÖRGETÉS / SEGÉD -----------------------------

    @FXML private void scrollLeft() { animateScroll(-0.2); }
    @FXML private void scrollRight() { animateScroll(0.2); }

    private void animateScroll(double delta) {
        if (carsScrollPane == null) return;
        double h = carsScrollPane.getHvalue();
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(300),
                        new KeyValue(carsScrollPane.hvalueProperty(), Math.max(0, Math.min(1, h + delta))))
        );
        timeline.play();
    }

    // ----------------------------- AUTÓ HOZZÁADÁS / SZERKESZTÉS -----------------------------

    @FXML
    private void handleAddOrEditCar() {
        if (editingCarId == null) addCar();
        else editCar(editingCarId);
    }

    private void addCar() {
        String sql = "INSERT INTO cars (owner_id, license, brand, type, vintage, engine_type, fuel_type, km, oil, tire_size, service, insurance, color, notes) " +
                "VALUES ((SELECT id FROM users WHERE username=?), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String selectedBrand = brandCombo.getEditor().getText().trim();
            String selectedType = typeCombo.getEditor().getText().trim();

            if (selectedBrand.isEmpty() || selectedType.isEmpty()) {
                showAlert("Hiba", "Kérlek válassz márkát és típust!");
                return;
            }

            // Szín HEX kód
            String color = "";
            if (colorPicker != null && colorPicker.getValue() != null) {
                javafx.scene.paint.Color c = colorPicker.getValue();
                color = String.format("#%02X%02X%02X",
                        (int)(c.getRed() * 255),
                        (int)(c.getGreen() * 255),
                        (int)(c.getBlue() * 255));
            }

            stmt.setString(1, username);
            stmt.setString(2, licenseField.getText().trim());
            stmt.setString(3, selectedBrand);
            stmt.setString(4, selectedType);
            stmt.setInt(5, vintageField.getValue() != null ? vintageField.getValue() : 0);
            stmt.setString(6, engineTypeField.getText().trim());
            stmt.setString(7, fuelTypeField.getValue() != null ? fuelTypeField.getValue() : "");
            stmt.setInt(8, !kmField.getText().trim().isEmpty() ? Integer.parseInt(kmField.getText().trim()) : 0);
            stmt.setString(9, oilField.getText().trim());
            stmt.setString(10, tireSizeField.getText().trim());
            stmt.setDate(11, serviceDatePickerCar.getValue() != null ? java.sql.Date.valueOf(serviceDatePickerCar.getValue()) : null);
            stmt.setDate(12, insuranceDatePicker.getValue() != null ? java.sql.Date.valueOf(insuranceDatePicker.getValue()) : null);
            stmt.setString(13, color);
            stmt.setString(14, notesField.getText().trim());

            stmt.executeUpdate();
            showAlert("Sikeres", "Autó hozzáadva!");
            loadUserCars();
            clearFields();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Hiba", "Nem sikerült az autó hozzáadása!");
        }
    }


    private void editCar(int carId) {
        String sql = "UPDATE cars SET license=?, brand=?, type=?, vintage=?, engine_type=?, fuel_type=?, km=?, oil=?, tire_size=?, service=?, insurance=?, color=?, notes=? WHERE id=?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String selectedBrand = brandCombo.getEditor().getText().trim();
            String selectedType = typeCombo.getEditor().getText().trim();

            if (selectedBrand.isEmpty() || selectedType.isEmpty()) {
                showAlert("Hiba", "Kérlek válassz márkát és típust!");
                return;
            }

            String color = "";
            if (colorPicker != null && colorPicker.getValue() != null) {
                javafx.scene.paint.Color c = colorPicker.getValue();
                color = String.format("#%02X%02X%02X",
                        (int)(c.getRed() * 255),
                        (int)(c.getGreen() * 255),
                        (int)(c.getBlue() * 255));
            }

            stmt.setString(1, licenseField.getText().trim());
            stmt.setString(2, selectedBrand);
            stmt.setString(3, selectedType);
            stmt.setInt(4, vintageField.getValue() != null ? vintageField.getValue() : 0);
            stmt.setString(5, engineTypeField.getText().trim());
            stmt.setString(6, fuelTypeField.getValue() != null ? fuelTypeField.getValue() : "");
            stmt.setInt(7, !kmField.getText().trim().isEmpty() ? Integer.parseInt(kmField.getText().trim()) : 0);
            stmt.setString(8, oilField.getText().trim());
            stmt.setString(9, tireSizeField.getText().trim());
            stmt.setDate(10, serviceDatePickerCar.getValue() != null ? java.sql.Date.valueOf(serviceDatePickerCar.getValue()) : null);
            stmt.setDate(11, insuranceDatePicker.getValue() != null ? java.sql.Date.valueOf(insuranceDatePicker.getValue()) : null);
            stmt.setString(12, color);
            stmt.setString(13, notesField.getText().trim());
            stmt.setInt(14, carId);

            stmt.executeUpdate();
            showAlert("Sikeres", "Az autó adatai frissítve!");
            loadUserCars();
            editingCarId = null;
            clearFields();
            if (addCarTitledPane != null) addCarTitledPane.setExpanded(false);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Hiba", "Nem sikerült frissíteni az autót!");
        }
    }



    // ----------------------------- PDF -----------------------------

    @FXML
    private void handleGeneratePdf() {
        showPdfSelectionDialog();
    }

    /**
     * Felugró ablak: autóválasztás PDF generáláshoz
     */
    private void showPdfSelectionDialog() {
        Dialog<List<Integer>> dialog = new Dialog<>();
        dialog.setTitle("PDF generálás");
        dialog.setHeaderText("Válaszd ki, melyik autó(k)ról szeretnél PDF-et generálni:");

        ButtonType generateButtonType = new ButtonType("Generálás", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(generateButtonType, ButtonType.CANCEL);

        VBox container = new VBox(10);
        container.setPadding(new Insets(10));
        List<CheckBox> checkBoxes = new ArrayList<>();

        // 🔹 Autók betöltése az adatbázisból
        String sql = "SELECT id, brand, type, license FROM cars WHERE owner_id = (SELECT id FROM users WHERE username = ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            boolean vanAuto = false;
            while (rs.next()) {
                vanAuto = true;
                int carId = rs.getInt("id");
                String brand = rs.getString("brand");
                String type = rs.getString("type");
                String license = rs.getString("license");

                CheckBox cb = new CheckBox(brand + " " + type + " (" + license + ")");
                cb.setUserData(carId);
                cb.setStyle("-fx-font-size: 14px;");
                container.getChildren().add(cb);
                checkBoxes.add(cb);
            }

            if (!vanAuto) {
                Label empty = new Label("Nincs még autód az adatbázisban.");
                empty.setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
                container.getChildren().add(empty);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Hiba", "Nem sikerült betölteni az autókat a PDF generáláshoz!");
            return;
        }

        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);
        scrollPane.setStyle("-fx-background-color: transparent;");

        dialog.getDialogPane().setContent(scrollPane);

        // Stílus
        dialog.getDialogPane().setStyle("""
        -fx-background-color: white;
        -fx-border-color: #ccc;
        -fx-border-radius: 10;
        -fx-background-radius: 10;
    """);

        Button generateButton = (Button) dialog.getDialogPane().lookupButton(generateButtonType);
        generateButton.setStyle("""
        -fx-background-color: #FF5722;
        -fx-text-fill: white;
        -fx-font-weight: bold;
        -fx-background-radius: 8;
    """);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == generateButtonType) {
                List<Integer> selected = new ArrayList<>();
                for (CheckBox cb : checkBoxes)
                    if (cb.isSelected()) selected.add((Integer) cb.getUserData());
                return selected;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(selectedIds -> {
            if (selectedIds == null || selectedIds.isEmpty()) {
                showAlert("Figyelem", "Nem választottál ki egy autót sem!");
                return;
            }

            for (int id : selectedIds) {
                try {
                    PdfGenerator.generateCarReport(id, username);
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert("Hiba", "Nem sikerült PDF-et készíteni az egyik autóra!");
                    return;
                }
            }
            showAlert("Siker", "A PDF(ek) sikeresen létrehozva!");
        });
    }



    // ----------------------------- SEGÉD METÓDUSOK -----------------------------

    private void clearFields() {
        if (licenseField != null) licenseField.clear();
        if (brandField != null) brandField.clear();
        if (typeField != null) typeField.clear();
        if (engineTypeField != null) engineTypeField.clear();
        if (kmField != null) kmField.clear();
        if (oilField != null) oilField.clear();
        if (tireSizeField != null) tireSizeField.clear();
        if (serviceDatePickerCar != null) serviceDatePickerCar.setValue(null);
        if (insuranceDatePicker != null) insuranceDatePicker.setValue(null);
        if (vintageField != null) vintageField.setValue(2025);
        if (fuelTypeField != null) fuelTypeField.setValue("Benzin");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
