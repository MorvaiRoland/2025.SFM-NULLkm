package drivesync.SajátAutók;

import drivesync.Adatbázis.Database;
import drivesync.Adatbázis.ServiceDAO;
import drivesync.Email.EmailService;
import drivesync.PDF.PdfGenerator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.FontPosture;
import javafx.util.Duration;
import javafx.geometry.Insets;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    @FXML private TextField serviceKmField, servicePriceField, replacedPartsField;
    @FXML private ListView<String> serviceListView;
    @FXML private Button scrollLeftBtn, scrollRightBtn, generatePdfBtn;
    @FXML private ColorPicker colorPicker;
    @FXML private TextArea notesField;
    @FXML private Label selectedCarLabel; // ez mutatja a kiválasztott autót a szerviz hozzáadásnál
    @FXML private DatePicker serviceDatePicker; // FXML-ben is szerepel!
    @FXML private TitledPane upcomingServicePane;
    @FXML private DatePicker upcomingServiceDatePicker;
    @FXML private TextField upcomingServiceLocation;
    @FXML private TextArea upcomingServiceNotes;
    @FXML private CheckBox upcomingServiceReminder;
    @FXML private Label selectedCarLabelUpcoming;
    @FXML private VBox servicesContainer; // ide töltjük a közelgő szervizek widgeteket






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
        IntStream.rangeClosed(1960, 2025).forEach(vintageField.getItems()::add);
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

    /**
     * Beállítja a bejelentkezett felhasználót, betölti az autókat és szervizeket,
     * valamint ellenőrzi a közelgő szerviz emlékeztetőket.
     */
    public void setUsername(String username) {
        this.username = username;

        if (welcomeLabel != null) welcomeLabel.setText("Itt a saját autóid listája:");
        loadUserCars();
        loadServiceTypes(); // kereshető combo feltöltése

        // --- Közelgő szerviz ellenőrzés indítása ---
        if (this.username != null && !this.username.isEmpty()) {
            new Thread(() -> {
                try {
                    checkUpcomingReminders(this.username);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            System.err.println("⚠️ Nincs bejelentkezett felhasználó — emlékeztető ellenőrzés kihagyva.");
        }
    }



    /**
     * Közelgő szerviz emlékeztetők ellenőrzése és e-mail küldés
     */
    private void checkUpcomingReminders(String username) {
        String sql = """
        SELECT u.service_date, u.location, u.notes, c.license, usr.email
        FROM upcoming_services u
        JOIN cars c ON u.car_id = c.id
        JOIN users usr ON c.owner_id = usr.id
        WHERE u.reminder = TRUE AND usr.username = ?
    """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                LocalDate today = LocalDate.now();
                boolean foundReminder = false;

                while (rs.next()) {
                    LocalDate serviceDate = rs.getDate("service_date").toLocalDate();
                    long days = java.time.temporal.ChronoUnit.DAYS.between(today, serviceDate);

                    if (days >= 0 && days <= 3) {
                        foundReminder = true;

                        String license = rs.getString("license");
                        String location = rs.getString("location");
                        String notes = rs.getString("notes");
                        String ownerEmail = rs.getString("email");

                        String messageBody = """
                        Közelgő szerviz:

                        Autó: %s
                        Dátum: %s
                        Helyszín: %s
                        Megjegyzés: %s

                        Üdvözlettel:
                        DriveSync rendszer
                        """.formatted(license, serviceDate, location, (notes != null ? notes : "-"));

                        boolean success = EmailService.sendEmail(
                                ownerEmail,
                                "Közelgő szerviz emlékeztető",
                                messageBody
                        );

                        if (success) {
                            System.out.println("✅ Emlékeztető elküldve: " + ownerEmail + " (" + license + ")");
                        } else {
                            System.err.println("❌ Hiba az email küldésénél: " + ownerEmail);
                        }
                    }
                }

                if (!foundReminder) {
                    System.out.println("ℹ️ Nincs közelgő szerviz a felhasználónál: " + username);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Adatbázis hiba az emlékeztetők lekérdezésekor:");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ Váratlan hiba az emlékeztetők feldolgozásakor:");
            e.printStackTrace();
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
            String carLabel = brand + " " + type + " (" + license + ")";
            if (selectedCarLabel != null) selectedCarLabel.setText(carLabel);
            if (selectedCarLabelUpcoming != null) selectedCarLabelUpcoming.setText(carLabel);

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

        // Autó részletek megjelenítése
        if (carDetailsPane != null) carDetailsPane.setExpanded(true);

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM cars WHERE id=?")) {

            stmt.setInt(1, carId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next() && carDetailsLabel != null) {
                String brand = rs.getString("brand");
                String type = rs.getString("type");
                String license = rs.getString("license");
                int vintage = rs.getInt("vintage");
                int km = rs.getInt("km");

                carDetailsLabel.setText(String.format("%s %s (%s), %d - %d km",
                        brand, type, license, vintage, km));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Hiba", "Nem sikerült betölteni az autó részleteit.");
        }

        // ----------------- Közelgő szervizek betöltése -----------------
        ServiceDAO dao = new ServiceDAO();
        List<ServiceDAO.Service> upcomingServices = dao.getUpcomingServices()
                .stream()
                .filter(s -> s.carId == carId) // csak a kiválasztott autóhoz
                .toList();

        servicesContainer.getChildren().clear(); // előző tartalom törlése

        if (upcomingServices.isEmpty()) {
            Label emptyLabel = new Label("Nincs közelgő szerviz erre az autóra.");
            emptyLabel.setFont(Font.font("Segoe UI", FontPosture.ITALIC, 14));
            emptyLabel.setTextFill(Color.GRAY);
            servicesContainer.getChildren().add(emptyLabel);
        } else {
            for (ServiceDAO.Service s : upcomingServices) {
                VBox widget = createServiceWidget(s);
                servicesContainer.getChildren().add(widget);
            }
        }
    }

    /**
     * Widget létrehozása egy közelgő szervizhez
     */
    private VBox createServiceWidget(ServiceDAO.Service service) {
        StringBuilder textBuilder = new StringBuilder();
        textBuilder.append("Dátum: ").append(service.serviceDate).append("\n");
        textBuilder.append("Helyszín: ").append(service.location);
        if (service.notes != null && !service.notes.isEmpty()) {
            textBuilder.append("\nMegjegyzés: ").append(service.notes);
        }
        textBuilder.append("\nEmlékeztető: ").append(service.reminder ? "Igen" : "Nem");

        VBox widget = new VBox(8);
        widget.setPrefWidth(350);
        widget.setStyle(
                "-fx-background-color: #ffffff;" +
                        "-fx-padding: 15;" +
                        "-fx-border-radius: 12;" +
                        "-fx-background-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 5);"
        );

        Label header = new Label("🔧 Közelgő szerviz");
        header.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        header.setTextFill(Color.web("#f1c40f"));

        Label serviceLabel = new Label(textBuilder.toString());
        serviceLabel.setFont(Font.font("Segoe UI", 14));
        serviceLabel.setTextFill(Color.DARKSLATEGRAY);
        serviceLabel.setWrapText(true);

        // --- Gombok hozzáadása ---
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button editBtn = new Button("Szerkesztés");
        editBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 6;");
        editBtn.setOnAction(e -> openEditServiceDialog(service));

        Button deleteBtn = new Button("Törlés");
        deleteBtn.setStyle("-fx-background-color: #e53935; -fx-text-fill: white; -fx-background-radius: 6;");
        deleteBtn.setOnAction(e -> {
            // A metódus most két paramétert vár: carId és serviceDate
            deleteUpcomingService(service.carId, service.serviceDate);
        });


        buttonBox.getChildren().addAll(editBtn, deleteBtn);

        widget.getChildren().addAll(header, serviceLabel, buttonBox);
        return widget;
    }


    private void deleteUpcomingService(int carId, String serviceDate) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Szerviz törlése");
        confirm.setHeaderText("Biztosan törölni szeretnéd ezt a szervizt?");
        confirm.setContentText("A törlés végleges és nem visszavonható!");
        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try (Connection conn = Database.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(
                             "DELETE FROM upcoming_services WHERE car_id = ? AND service_date = ?")) {

                    stmt.setInt(1, carId);
                    stmt.setString(2, serviceDate);
                    stmt.executeUpdate();
                    showAlert("Siker", "A szerviz törölve!");
                    showCarDetails(carId); // frissítjük a listát

                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Hiba", "Nem sikerült törölni a szervizt!");
                }
            }
        });
    }

    private void openEditServiceDialog(ServiceDAO.Service service) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Szerviz szerkesztése");

        VBox container = new VBox(10);
        container.setPadding(new Insets(15));

        // --- String -> LocalDate konverzió ---
        LocalDate date = null;
        if (service.serviceDate != null && !service.serviceDate.isEmpty()) {
            date = LocalDate.parse(service.serviceDate); // "YYYY-MM-DD" formátumot vár
        }
        DatePicker datePicker = new DatePicker(date);

        TextField locationField = new TextField(service.location);
        TextArea notesArea = new TextArea(service.notes);
        CheckBox reminderCheck = new CheckBox("Küldjön emlékeztetőt");
        reminderCheck.setSelected(service.reminder);

        container.getChildren().addAll(
                new Label("Dátum:"), datePicker,
                new Label("Helyszín:"), locationField,
                new Label("Megjegyzés:"), notesArea,
                reminderCheck
        );

        dialog.getDialogPane().setContent(container);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try (Connection conn = Database.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(
                             "UPDATE upcoming_services SET service_date=?, location=?, notes=?, reminder=? WHERE car_id=? AND service_date=?")) {

                    // --- LocalDate -> String konverzió ---
                    String dateStr = datePicker.getValue().toString();

                    stmt.setString(1, dateStr);
                    stmt.setString(2, locationField.getText().trim());
                    stmt.setString(3, notesArea.getText().trim());
                    stmt.setBoolean(4, reminderCheck.isSelected());
                    stmt.setInt(5, service.carId);
                    stmt.setString(6, service.serviceDate); // az eredeti dátum a WHERE feltételhez
                    stmt.executeUpdate();

                    showAlert("Siker", "A szerviz frissítve!");
                    showCarDetails(service.carId); // frissítés
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Hiba", "Nem sikerült frissíteni a szervizt!");
                }
            }
            return null;
        });

        dialog.showAndWait();
    }





    // ----------------------------- SZERVIZEK -----------------------------

    private void loadServices(int carId) {
        serviceListView.getItems().clear();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT s.id, s.service_date, t.name AS service_type, s.km, s.price, s.replaced_parts " +
                             "FROM services s " +
                             "LEFT JOIN service_types t ON s.service_type_id = t.id " +
                             "WHERE s.car_id = ? " +
                             "ORDER BY s.service_date DESC")) {

            stmt.setInt(1, carId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int serviceId = rs.getInt("id");
                String date = rs.getString("service_date");
                String type = rs.getString("service_type");
                int km = rs.getInt("km");
                int price = rs.getInt("price");
                String replacedParts = rs.getString("replaced_parts");

                String row = "[" + serviceId + "] " + date + " - " + type + " (" + km + " km, " + price + " Ft)";
                if (replacedParts != null && !replacedParts.isEmpty()) {
                    row += " | Cserélt alkatrészek: " + replacedParts;
                }

                serviceListView.getItems().add(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Hiba", "Nem sikerült betölteni a szerviz adatokat!");
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
    private void handleDeleteService() {
        String selectedItem = serviceListView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert("Hiba", "Nincs kiválasztott szerviz!");
            return;
        }

        // Azonosító kinyerése: a sor elején lévő [id] rész
        int serviceId;
        try {
            serviceId = Integer.parseInt(
                    selectedItem.substring(selectedItem.indexOf('[') + 1, selectedItem.indexOf(']'))
            );
        } catch (Exception e) {
            showAlert("Hiba", "Nem sikerült azonosítani a szervizt!");
            return;
        }

        // Megerősítés
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Szerviz törlése");
        confirm.setHeaderText("Biztosan törölni szeretnéd ezt a szervizt?");
        confirm.setContentText("A törlés végleges és nem visszavonható!");
        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try (Connection conn = Database.getConnection();
                     PreparedStatement stmt = conn.prepareStatement("DELETE FROM services WHERE id = ?")) {

                    stmt.setInt(1, serviceId);
                    stmt.executeUpdate();

                    showAlert("Siker", "A szerviz törölve!");
                    loadServices(selectedCarId); // frissíti a listát

                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Hiba", "Nem sikerült törölni a szervizt!");
                }
            }
        });
    }


    @FXML
    private void saveService() {
        if (selectedCarId == -1) {
            showAlert("Hiba", "Nincs kiválasztott autó!");
            return;
        }

        if (serviceTypeCombo.getValue() == null || serviceKmField.getText().isEmpty() || servicePriceField.getText().isEmpty()) {
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

            // --- 5. paraméter: Szerviz dátuma (a DatePicker-ből vagy mostani dátum) ---
            LocalDate date = serviceDatePicker.getValue();
            if (date != null) {
                stmt.setString(5, date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            } else {
                // ha nincs kiválasztva, akkor az aktuális dátumot tesszük be
                stmt.setString(5, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }

            // --- 6. paraméter: cserélt alkatrészek ---
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
        String sql = """
        INSERT INTO cars 
        (owner_id, license, brand, type, vintage, engine_type, fuel_type, km, oil, tire_size, service, insurance, color, notes)
        VALUES ((SELECT id FROM users WHERE username=?), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String selectedBrand = brandCombo.getEditor().getText().trim();
            String selectedType = typeCombo.getEditor().getText().trim();

            if (selectedBrand.isEmpty() || selectedType.isEmpty()) {
                showAlert("Hiba", "Kérlek válassz márkát és típust!");
                return;
            }

            // Szín HEX formátumban
            String color = "";
            if (colorPicker != null && colorPicker.getValue() != null) {
                javafx.scene.paint.Color c = colorPicker.getValue();
                color = String.format("#%02X%02X%02X",
                        (int) (c.getRed() * 255),
                        (int) (c.getGreen() * 255),
                        (int) (c.getBlue() * 255));
            }

            // Kötelező mezők ellenőrzése
            if (licenseField.getText().trim().isEmpty()) {
                showAlert("Hiba", "Rendszám megadása kötelező!");
                return;
            }

            stmt.setString(1, username); // owner
            stmt.setString(2, licenseField.getText().trim());
            stmt.setString(3, selectedBrand);
            stmt.setString(4, selectedType);
            stmt.setInt(5, vintageField.getValue() != null ? vintageField.getValue() : 0);
            stmt.setString(6, engineTypeField.getText().trim());
            stmt.setString(7, fuelTypeField.getValue() != null ? fuelTypeField.getValue() : "");
            stmt.setInt(8, !kmField.getText().trim().isEmpty() ? Integer.parseInt(kmField.getText().trim()) : 0);
            stmt.setString(9, oilField.getText().trim());
            stmt.setString(10, tireSizeField.getText().trim());

            // 🔹 11. paraméter: service (nincs dátum picker, így NULL)
            stmt.setNull(11, Types.DATE);

            // 🔹 12. paraméter: insurance (dátum, ha van)
            if (insuranceDatePicker != null && insuranceDatePicker.getValue() != null)
                stmt.setDate(12, java.sql.Date.valueOf(insuranceDatePicker.getValue()));
            else
                stmt.setNull(12, Types.DATE);

            stmt.setString(13, color);
            stmt.setString(14, notesField != null ? notesField.getText().trim() : "");

            stmt.executeUpdate();
            showAlert("Sikeres", "Az autó hozzáadva!");
            loadUserCars();
            clearFields();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Hiba", "Nem sikerült az autó hozzáadása!\n" + e.getMessage());
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

    @FXML
    private void handleDeleteCar() {
        if (selectedCarId == -1) {
            showAlert("Hiba", "Nincs kiválasztott autó!");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Autó törlése");
        confirm.setHeaderText("Biztosan törölni szeretnéd ezt az autót?");
        confirm.setContentText("A törlés minden hozzá tartozó szervizt is eltávolít!");
        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try (Connection conn = Database.getConnection();
                     PreparedStatement stmt = conn.prepareStatement("DELETE FROM cars WHERE id = ?")) {
                    stmt.setInt(1, selectedCarId);
                    stmt.executeUpdate();
                    showAlert("Siker", "Az autó törölve!");
                    selectedCarId = -1;
                    currentlySelectedCard = null;
                    loadUserCars();
                    serviceListView.getItems().clear();
                    carDetailsLabel.setText("Válassz egy autót a listából!");
                    selectedCarLabel.setText("Válassz egy autót a listából!");
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Hiba", "Nem sikerült törölni az autót!");
                }
            }
        });
    }
    @FXML
    private void saveUpcomingService() {
        if (selectedCarId == -1) {
            showAlert("Hiba", "Nincs kiválasztott autó!");
            return;
        }

        LocalDate date = upcomingServiceDatePicker.getValue();
        if (date == null) {
            showAlert("Hiba", "Kérlek válassz dátumot!");
            return;
        }

        String location = upcomingServiceLocation.getText().trim();
        String notes = upcomingServiceNotes.getText().trim();
        boolean reminder = upcomingServiceReminder.isSelected();

        String sql = "INSERT INTO upcoming_services (car_id, service_date, location, notes, reminder) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, selectedCarId);
            stmt.setDate(2, java.sql.Date.valueOf(date));
            stmt.setString(3, location);
            stmt.setString(4, notes);
            stmt.setBoolean(5, reminder);

            stmt.executeUpdate();
            showAlert("Sikeres", "Következő szerviz rögzítve!");
            upcomingServiceDatePicker.setValue(null);
            upcomingServiceLocation.clear();
            upcomingServiceNotes.clear();
            upcomingServiceReminder.setSelected(false);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Hiba", "Nem sikerült menteni a következő szervizt!");
        }
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
