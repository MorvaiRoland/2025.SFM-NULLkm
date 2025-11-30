package drivesync.SajátAutók;

import drivesync.Adatbázis.Database;
import drivesync.Adatbázis.ServiceDAO;
import drivesync.Email.EmailService;
import drivesync.PDF.PdfGenerator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class SajatAutokController {

    // --- FXML Referenciák (Autó Adatok) ---
    @FXML private Label welcomeLabel, carDetailsLabel;
    @FXML private FlowPane carsList;
    @FXML private ScrollPane carsScrollPane;
    @FXML private TitledPane addCarTitledPane, carDetailsPane, upcomingServicePane, addServicePane;
    @FXML
    protected TextField licenseField;
    @FXML
    protected TextField kmField;

    // FXML ComboBox/ChoiceBox-ok (Minimalizált bevitelhez)
    @FXML
    protected ComboBox<String> brandCombo;
    @FXML
    protected ComboBox<String> typeCombo;
    @FXML
    protected ComboBox<String> serviceTypeCombo;
    @FXML
    protected ComboBox<String> engineTypeCombo;
    @FXML
    protected ComboBox<String> oilTypeCombo;
    @FXML
    protected ChoiceBox<String> oilQuantityChoice;
    @FXML
    protected ComboBox<String> tireSizeCombo;

    @FXML
    protected ChoiceBox<String> fuelTypeField;
    @FXML
    protected ChoiceBox<Integer> vintageField;
    @FXML
    protected DatePicker insuranceDatePicker;
    @FXML
    protected DatePicker inspection_date;
    @FXML
    protected ColorPicker colorPicker;
    @FXML
    protected TextArea notesField;

    // --- FXML Referenciák (Szerviz Adatok) ---
    @FXML
    protected TextField serviceKmField;
    @FXML
    protected TextField servicePriceField;
    @FXML
    protected TextField replacedPartsField;
    @FXML
    protected DatePicker serviceDatePicker;
    @FXML private Label selectedCarLabel;
    @FXML private ListView<String> serviceListView;
    @FXML private VBox servicesContainer;

    // --- FXML Referenciák (Következő Szerviz) ---
    @FXML private DatePicker upcomingServiceDatePicker;
    @FXML private TextField upcomingServiceLocation;
    @FXML private TextArea upcomingServiceNotes;
    @FXML private CheckBox upcomingServiceReminder;

    // --- Belső állapot ---
    protected String username;
    protected int selectedCarId = -1;
    private VBox currentlySelectedCard = null;
    private Integer editingCarId = null;

    // A márkák és típusok kereshetőségéhez
    private ObservableList<String> typeItems = FXCollections.observableArrayList();
    private FilteredList<String> filteredTypes;

    @FXML
    public void initialize() {
        // --- Évjáratok és üzemanyag beállítása ---
        IntStream.rangeClosed(1960, 2025).forEach(vintageField.getItems()::add);
        vintageField.setValue(2025);
        fuelTypeField.getItems().addAll("Benzin", "Dízel", "Elektromos", "Hibrid");
        fuelTypeField.setValue("Benzin");

        // --- Márkák és típusok inicializálása ---
        loadBrands();
        loadServiceTypes();

        // Feltölti a 4 új listát adatbázisból (motor, olaj, gumi)
        setupFillableCombos();

        // --- ComboBox Kereshetőség beállítása (Brand/Type) ---
        filteredTypes = new FilteredList<>(typeItems, p -> true);
        typeCombo.setEditable(true);
        typeCombo.setItems(filteredTypes);

        typeCombo.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            final String search = newVal.toLowerCase();
            filteredTypes.setPredicate(item -> item.toLowerCase().contains(search));
        });

        // Frissíti a motor listát, ha a márka/típus megváltozik
        brandCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            loadTypesForBrand(newVal);
            engineTypeCombo.getItems().clear();
            engineTypeCombo.getEditor().clear();
        });

        // VÁLTOZÁS: Típus kiválasztásakor frissíti a motor típustáblát
        typeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            String brand = brandCombo.getValue();
            if (brand != null && newVal != null) {
                Platform.runLater(() -> loadEnginesForModel(brand, newVal));
            }
        });


        // TitledPane viselkedés: Új autó hozzáadása
        if (addCarTitledPane != null) {
            addCarTitledPane.expandedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) addCarTitledPane.setText("❌ Bezárás");
                else {
                    addCarTitledPane.setText("➕ Új autó hozzáadása");
                    clearFields();
                    editingCarId = null;
                }
            });
        }
    }

    // VÁLTOZÁS: Motor Típusok betöltése Márka és Típus alapján (szűrt lista)
    private void loadEnginesForModel(String brand, String type) {
        engineTypeCombo.getItems().clear();
        engineTypeCombo.getEditor().clear(); // Csak a szövegmező tartalmát törli

        List<String> engines = new ArrayList<>();
        String sql = "SELECT engine_name FROM model_engine_types WHERE brand = ? AND type = ? ORDER BY engine_name";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, brand);
            stmt.setString(2, type);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                engines.add(rs.getString("engine_name"));
            }

        } catch (SQLException e) {
            System.err.println("Hiba a model_engine_types lekérdezésekor: " + e.getMessage());
            showAlert("Hiba", "Nem sikerült betölteni a motor típusokat. Ellenőrizze a model_engine_types táblát!");
            return;
        }

        engineTypeCombo.setItems(FXCollections.observableArrayList(engines));

        // JAVÍTÁS: Dinamikus promptText beállításának eltávolítása, ami a kötési hibát okozta.
        // Helyette csak kitisztítjuk a szövegmezőt, és bízunk az FXML promptText-ben.

        if (engines.isEmpty()) {
            // Ha üres, beállítunk egy értesítő szöveget a beviteli mezőbe
            engineTypeCombo.getEditor().setText("Nincs motor ehhez a modellhez. Gépeld be!");
        }
    }

    /**
     * Adatbázis táblák alapján tölti fel az általános ComboBoxokat.
     */
    private void setupFillableCombos() {
        // Motor Típusa (alaphelyzetben üres, a loadEnginesForModel tölti fel)
        engineTypeCombo.setEditable(true);

        // Olaj Típusa
        loadDataIntoCombo(oilTypeCombo, "oil_types", "name");
        oilTypeCombo.setEditable(true);

        // Olaj Mennyisége (ChoiceBox-ként kezelve)
        loadDataIntoChoice(oilQuantityChoice, "oil_quantities", "name");
        oilQuantityChoice.setValue("5.0 L"); // Alapértelmezett beállítás (feltéve, hogy létezik)

        // Gumi Méret
        loadDataIntoCombo(tireSizeCombo, "tire_sizes", "name");
        tireSizeCombo.setEditable(true);
    }

    // Generikus metódus ComboBox feltöltéshez (kereshető)
    private void loadDataIntoCombo(ComboBox<String> combo, String tableName, String columnName) {
        List<String> items = new ArrayList<>();
        String sql = "SELECT " + columnName + " FROM " + tableName + " ORDER BY " + columnName;
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                items.add(rs.getString(columnName));
            }
            ObservableList<String> allItems = FXCollections.observableArrayList(items);
            FilteredList<String> filteredItems = new FilteredList<>(allItems, p -> true);
            combo.setItems(filteredItems);

            combo.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
                final String search = newValue.toLowerCase();
                filteredItems.setPredicate(item -> item.toLowerCase().contains(search));
            });

        } catch (SQLException e) {
            System.err.println("Hiba a " + tableName + " adatok betöltésekor.");
        }
    }

    // Generikus metódus ChoiceBox feltöltéshez
    private void loadDataIntoChoice(ChoiceBox<String> choiceBox, String tableName, String columnName) {
        List<String> items = new ArrayList<>();
        String sql = "SELECT " + columnName + " FROM " + tableName + " ORDER BY " + columnName;
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                items.add(rs.getString(columnName));
            }
            choiceBox.setItems(FXCollections.observableArrayList(items));

        } catch (SQLException e) {
            System.err.println("Hiba a " + tableName + " adatok betöltésekor.");
        }
    }


    /**
     * Beállítja a bejelentkezett felhasználót, betölti az autókat és elindítja az emlékeztető ellenőrzést.
     */
    public void setUsername(String username) {
        this.username = username;
        if (welcomeLabel != null) welcomeLabel.setText("Itt a saját autóid listája:");
        loadUserCars();

        // --- Közelgő szerviz ellenőrzés indítása aszinkron szálon ---
        if (this.username != null && !this.username.isEmpty()) {
            new Thread(() -> {
                try {
                    checkUpcomingReminders(this.username);
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> showAlert("Hiba", "Hiba az emlékeztetők ellenőrzésekor."));
                }
            }).start();
        }
    }


    /**
     * Közelgő szerviz emlékeztetők ellenőrzése és e-mail küldés.
     */
    private void checkUpcomingReminders(String username) {
        ServiceDAO dao = new ServiceDAO();
        List<ServiceDAO.ReminderData> reminders = dao.getRemindersForUser(username);
        LocalDate today = LocalDate.now();

        for (ServiceDAO.ReminderData rd : reminders) {
            LocalDate serviceDate = LocalDate.parse(rd.serviceDate);

            // Ha lejárt, archiváljuk
            if (serviceDate.isBefore(today)) {
                dao.archiveUpcomingService(rd.carId, serviceDate);
                continue;
            }

            // Ha 3 napon belül van, küldjük az emailt
            long days = java.time.temporal.ChronoUnit.DAYS.between(today, serviceDate);
            if (days >= 0 && days <= 3) {
                String messageBody = String.format("""
                Közelgő szerviz:

                Autó: %s
                Dátum: %s
                Helyszín: %s
                Megjegyzés: %s

                Üdvözlettel:
                DriveSync rendszer
                """, rd.license, serviceDate, rd.location, (rd.notes != null ? rd.notes : "-"));

                boolean success = EmailService.sendEmail(rd.ownerEmail, "Közelgő szerviz emlékeztető", messageBody);

                if (success) {
                    dao.updateLastEmailSent(rd.carId, serviceDate);
                }
            }
        }
    }


// ----------------------------- MÁRKA ÉS TÍPUS BETÖLTÉS -----------------------------

    private void loadBrands() {
        if (brandCombo == null) return;

        List<String> brands = new ArrayList<>();
        String sql = "SELECT DISTINCT brand FROM car_types ORDER BY brand";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

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

        brandCombo.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            final String search = newVal.toLowerCase();
            filteredBrands.setPredicate(item -> item.toLowerCase().contains(search));
        });
    }

    private void loadTypesForBrand(String brand) {
        typeItems.clear();
        typeCombo.getSelectionModel().clearSelection();
        typeCombo.getEditor().clear();

        if (brand == null || brand.isEmpty()) return;

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
        typeItems.setAll(types);
    }


    // ----------------------------- AUTÓK -----------------------------

    private void loadUserCars() {
        if (carsList == null) return;
        carsList.getChildren().clear();

        String sql = "SELECT id, brand, type, license, vintage, km FROM cars WHERE owner_id = (SELECT id FROM users WHERE username = ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            List<VBox> carCards = new ArrayList<>();

            while (rs.next()) {
                carCards.add(createCarCard(rs, rs.getInt("id")));
            }
            carsList.getChildren().addAll(carCards);

            // Kijelöljük az elsőt programozottan
            if (!carsList.getChildren().isEmpty()) {
                VBox firstCar = (VBox) carsList.getChildren().get(0);

                // Programozott kattintás
                firstCar.getOnMouseClicked().handle(new MouseEvent(MouseEvent.MOUSE_CLICKED,
                        0, 0, 0, 0, null, 1, false, false, false, false, false, false, false, false, false, false, null));
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
        box.getStyleClass().addAll("car-card", "default-card"); // Stílusok CSS-ből
        box.setUserData(carId);

        Label brandTypeLabel = new Label(brand + " " + type);
        brandTypeLabel.getStyleClass().add("card-brand-type");

        Label licenseLabel = new Label(license);
        licenseLabel.getStyleClass().add("card-license");

        Label detailsLabel = new Label("Évjárat: " + vintage + "\nKM: " + km);
        detailsLabel.getStyleClass().add("card-details");
        detailsLabel.setWrapText(true);
        detailsLabel.setMaxWidth(180);

        box.getChildren().addAll(brandTypeLabel, licenseLabel, detailsLabel);

        // Hover effekt CSS osztályokkal
        box.setOnMouseEntered(e -> {
            if (box != currentlySelectedCard) box.getStyleClass().add("hover-card");
        });
        box.setOnMouseExited(e -> {
            if (box != currentlySelectedCard) box.getStyleClass().remove("hover-card");
        });

        // Kattintás: kiválasztás
        box.setOnMouseClicked(e -> {
            if (currentlySelectedCard != null) {
                currentlySelectedCard.getStyleClass().remove("selected-card");
                currentlySelectedCard.getStyleClass().add("default-card");
            }
            currentlySelectedCard = box;
            box.getStyleClass().remove("default-card");
            box.getStyleClass().remove("hover-card");
            box.getStyleClass().add("selected-card");

            int clickedCarId = (int) box.getUserData();
            String carLabel = brand + " " + type + " (" + license + ")";
            if (selectedCarLabel != null) selectedCarLabel.setText(carLabel);

            showCarDetails(clickedCarId);
            loadServices(clickedCarId);
        });

        return box;
    }


    private void showCarDetails(int carId) {
        selectedCarId = carId;

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

        // ----------------- Közelgő szervizek betöltése (CarId alapján) -----------------
        ServiceDAO dao = new ServiceDAO();
        List<ServiceDAO.Service> upcomingServices = dao.getUpcomingServices(carId);

        servicesContainer.getChildren().clear();

        if (upcomingServices.isEmpty()) {
            Label emptyLabel = new Label("Nincs közelgő szerviz erre az autóra.");
            emptyLabel.setFont(Font.font("Segoe UI", javafx.scene.text.FontPosture.ITALIC, 14));
            emptyLabel.setTextFill(Color.GRAY);
            servicesContainer.getChildren().add(emptyLabel);
        } else {
            for (ServiceDAO.Service s : upcomingServices) {
                servicesContainer.getChildren().add(createServiceWidget(s));
            }
        }
    }

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
        widget.getStyleClass().addAll("service-widget", "card");

        Label header = new Label("🔧 Közelgő szerviz");
        header.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        header.setTextFill(Color.web("#f1c40f"));

        Label serviceLabel = new Label(textBuilder.toString());
        serviceLabel.getStyleClass().add("widget-content");
        serviceLabel.setWrapText(true);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button editBtn = new Button("Szerkesztés");
        editBtn.getStyleClass().add("btn-info");
        editBtn.setOnAction(e -> openEditServiceDialog(service));

        Button deleteBtn = new Button("Törlés");
        deleteBtn.getStyleClass().add("btn-danger");
        deleteBtn.setOnAction(e -> deleteUpcomingService(service.carId, LocalDate.parse(service.serviceDate)));

        buttonBox.getChildren().addAll(editBtn, deleteBtn);

        widget.getChildren().addAll(header, serviceLabel, buttonBox);
        return widget;
    }

    private void deleteUpcomingService(int carId, LocalDate serviceDate) {
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
                    stmt.setDate(2, java.sql.Date.valueOf(serviceDate));
                    stmt.executeUpdate();
                    showAlert("Siker", "A szerviz törölve!");
                    showCarDetails(carId);

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

        LocalDate date = LocalDate.parse(service.serviceDate);
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

                    String dateStr = datePicker.getValue().toString();

                    stmt.setString(1, dateStr);
                    stmt.setString(2, locationField.getText().trim());
                    stmt.setString(3, notesArea.getText().trim());
                    stmt.setBoolean(4, reminderCheck.isSelected());
                    stmt.setInt(5, service.carId);
                    stmt.setString(6, service.serviceDate);
                    stmt.executeUpdate();

                    showAlert("Siker", "A szerviz frissítve!");
                    showCarDetails(service.carId);
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

        // VÁLTOZÁS: Törli a kijelölést, ami a korábbi IndexOutOfBounds hibát okozta
        serviceListView.getSelectionModel().clearSelection();

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

                String row = String.format("[%d] %s - %s (%d km, %d Ft)", serviceId, date, type, km, price);
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
        String sql = "SELECT name FROM service_types ORDER BY name";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) types.add(rs.getString("name"));
        } catch (SQLException e) {
            showAlert("Hiba", "Nem sikerült betölteni a szerviz típusokat.");
            return;
        }

        ObservableList<String> allItems = FXCollections.observableArrayList(types);
        FilteredList<String> filteredItems = new FilteredList<>(allItems, p -> true);

        serviceTypeCombo.setEditable(true);
        serviceTypeCombo.setItems(filteredItems);

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

        int serviceId;
        try {
            serviceId = Integer.parseInt(
                    selectedItem.substring(selectedItem.indexOf('[') + 1, selectedItem.indexOf(']'))
            );
        } catch (Exception e) {
            showAlert("Hiba", "Nem sikerült azonosítani a szervizt!");
            return;
        }

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
                    loadServices(selectedCarId);

                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Hiba", "Nem sikerült törölni a szervizt!");
                }
            }
        });
    }


    @FXML
    protected void saveService() {
        if (selectedCarId == -1) {
            showAlert("Hiba", "Nincs kiválasztott autó!");
            return;
        }

        String serviceTypeName = serviceTypeCombo.getEditor().getText().trim(); // A beírt vagy kiválasztott érték

        if (serviceTypeName.isEmpty() || serviceKmField.getText().isEmpty() || servicePriceField.getText().isEmpty()) {
            showAlert("Hiba", "Kérlek töltsd ki a kötelező mezőket (Szerviz típus, KM, Ár)!");
            return;
        }

        try (Connection conn = Database.getConnection()) {

            // --- VÁLTOZÁS 7: ÚJ SZERVIZ TÍPUS AUTOMATIKUS BESZÚRÁSA ---
            int serviceTypeId = -1;
            try (PreparedStatement checkStmt = conn.prepareStatement("SELECT id FROM service_types WHERE name = ?")) {
                checkStmt.setString(1, serviceTypeName);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    serviceTypeId = rs.getInt(1);
                }
            }

            // Ha nem létezik, beszúrjuk
            if (serviceTypeId == -1) {
                try (PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO service_types (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
                    insertStmt.setString(1, serviceTypeName);
                    insertStmt.executeUpdate();

                    // Lekérjük az újonnan beszúrt ID-t
                    ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        serviceTypeId = generatedKeys.getInt(1);
                    }
                    // Frissítjük a ComboBoxot is a felhasználói élményért
                    Platform.runLater(this::loadServiceTypes);
                }
            }

            if (serviceTypeId == -1) {
                showAlert("Hiba", "Nem sikerült rögzíteni a szerviz típust az adatbázisban.");
                return;
            }

            // --- SZERVIZ RÖGZÍTÉSE (Már meglévő ID-val) ---

            String sql = "INSERT INTO services (car_id, service_type_id, km, price, service_date, replaced_parts) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {

                // Érvényesítés és konverzió
                int km = Integer.parseInt(serviceKmField.getText().trim());
                int price = Integer.parseInt(servicePriceField.getText().trim());

                stmt.setInt(1, selectedCarId);
                stmt.setInt(2, serviceTypeId);
                stmt.setInt(3, km);
                stmt.setInt(4, price);

                // Dátum
                LocalDate date = serviceDatePicker.getValue();
                if (date != null) {
                    stmt.setDate(5, java.sql.Date.valueOf(date));
                } else {
                    stmt.setNull(5, Types.DATE);
                }

                stmt.setString(6, replacedPartsField.getText().trim());

                stmt.executeUpdate();
            }

            showAlert("Sikeres", "A szerviz rögzítve!");
            loadServices(selectedCarId);

            // Mezők törlése
            serviceTypeCombo.getSelectionModel().clearSelection();
            serviceKmField.clear();
            servicePriceField.clear();
            replacedPartsField.clear();
            serviceDatePicker.setValue(null);

        } catch (NumberFormatException e) {
            showAlert("Hiba", "A KM és Ár mezők csak számokat tartalmazhatnak!");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Hiba", "Nem sikerült menteni a szervizt (SQL hiba: " + e.getMessage() + ")!");
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
        if (editingCarId == null) {
            addCar();
        } else {
            editCar(editingCarId);
        }
    }

    protected void addCar() {
        // VÁLTOZÁS: SQL Lekérdezés oil -> oil_type, oil_quantity
        String sql = """
    INSERT INTO cars 
    (owner_id, license, brand, type, vintage, engine_type, fuel_type, km, oil_type, oil_quantity, tire_size, insurance, inspection_date, color, notes)
    VALUES ((SELECT id FROM users WHERE username=?), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String selectedBrand = brandCombo.getEditor().getText().trim();
            String selectedType = typeCombo.getEditor().getText().trim();

            if (selectedBrand.isEmpty() || selectedType.isEmpty() || licenseField.getText().trim().isEmpty()) {
                showAlert("Hiba", "Rendszám, Márka és Típus megadása kötelező!");
                return;
            }

            // Szín HEX formátumban
            String color = "#FFFFFF";
            if (colorPicker != null && colorPicker.getValue() != null) {
                javafx.scene.paint.Color c = colorPicker.getValue();
                color = String.format("#%02X%02X%02X",
                        (int) (c.getRed() * 255),
                        (int) (c.getGreen() * 255),
                        (int) (c.getBlue() * 255));
            }

            // Adat konverzió
            int km = !kmField.getText().trim().isEmpty() ? Integer.parseInt(kmField.getText().trim()) : 0;

            stmt.setString(1, username);
            stmt.setString(2, licenseField.getText().trim());
            stmt.setString(3, selectedBrand);
            stmt.setString(4, selectedType);
            stmt.setInt(5, vintageField.getValue() != null ? vintageField.getValue() : 0);

            // EngineType a ComboBoxból
            stmt.setString(6, engineTypeCombo.getEditor().getText().trim());

            stmt.setString(7, fuelTypeField.getValue() != null ? fuelTypeField.getValue() : "");
            stmt.setInt(8, km);

            // Olaj adatok ComboBox/ChoiceBox-ból
            stmt.setString(9, oilTypeCombo.getEditor().getText().trim());      // oil_type
            stmt.setString(10, oilQuantityChoice.getValue()); // oil_quantity

            // Gumi méret a ComboBoxból
            stmt.setString(11, tireSizeCombo.getEditor().getText().trim());

            // insurance
            stmt.setDate(12, insuranceDatePicker.getValue() != null ? java.sql.Date.valueOf(insuranceDatePicker.getValue()) : null);

            // inspection_date
            stmt.setDate(13, inspection_date.getValue() != null ? java.sql.Date.valueOf(inspection_date.getValue()) : null);

            // color
            stmt.setString(14, color);

            // notes
            stmt.setString(15, notesField != null ? notesField.getText().trim() : "");

            stmt.executeUpdate();
            showAlert("Sikeres", "Az autó hozzáadva!");
            loadUserCars();
            clearFields();
            if (addCarTitledPane != null) addCarTitledPane.setExpanded(false);


        } catch (NumberFormatException e) {
            showAlert("Hiba", "A futásteljesítmény (KM) mező csak számokat tartalmazhat!");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Hiba", "Nem sikerült az autó hozzáadása!\n" + e.getMessage());
        }
    }


    private void editCar(int carId) {
        // VÁLTOZÁS: SQL Lekérdezés frissítése oil -> oil_type, oil_quantity
        String sql = "UPDATE cars SET license=?, brand=?, type=?, vintage=?, engine_type=?, fuel_type=?, km=?, oil_type=?, oil_quantity=?, tire_size=?, insurance=?, inspection_date=?, color=?, notes=? WHERE id=?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String selectedBrand = brandCombo.getEditor().getText().trim();
            String selectedType = typeCombo.getEditor().getText().trim();

            if (selectedBrand.isEmpty() || selectedType.isEmpty() || licenseField.getText().trim().isEmpty()) {
                showAlert("Hiba", "Rendszám, Márka és Típus megadása kötelező!");
                return;
            }

            String color = "#FFFFFF";
            if (colorPicker != null && colorPicker.getValue() != null) {
                javafx.scene.paint.Color c = colorPicker.getValue();
                color = String.format("#%02X%02X%02X",
                        (int)(c.getRed() * 255),
                        (int)(c.getGreen() * 255),
                        (int)(c.getBlue() * 255));
            }

            // Adat konverzió
            int km = !kmField.getText().trim().isEmpty() ? Integer.parseInt(kmField.getText().trim()) : 0;


            stmt.setString(1, licenseField.getText().trim());
            stmt.setString(2, selectedBrand);
            stmt.setString(3, selectedType);
            stmt.setInt(4, vintageField.getValue() != null ? vintageField.getValue() : 0);

            // EngineType a ComboBoxból
            stmt.setString(5, engineTypeCombo.getEditor().getText().trim());

            stmt.setString(6, fuelTypeField.getValue() != null ? fuelTypeField.getValue() : "");
            stmt.setInt(7, km);

            // Olaj adatok ComboBox/ChoiceBox-ból
            stmt.setString(8, oilTypeCombo.getEditor().getText().trim());      // oil_type
            stmt.setString(9, oilQuantityChoice.getValue()); // oil_quantity

            // Gumi méret a ComboBoxból
            stmt.setString(10, tireSizeCombo.getEditor().getText().trim());

            // insurance
            stmt.setDate(11, insuranceDatePicker.getValue() != null ? java.sql.Date.valueOf(insuranceDatePicker.getValue()) : null);

            // inspection_date
            stmt.setDate(12, inspection_date.getValue() != null ? java.sql.Date.valueOf(inspection_date.getValue()) : null);

            // color
            stmt.setString(13, color);

            // notes
            stmt.setString(14, notesField != null ? notesField.getText().trim() : "");

            stmt.setInt(15, carId);

            stmt.executeUpdate();
            showAlert("Sikeres", "Az autó adatai frissítve!");
            loadUserCars();
            editingCarId = null;
            clearFields();
            if (addCarTitledPane != null) addCarTitledPane.setExpanded(false);

        } catch (NumberFormatException e) {
            showAlert("Hiba", "A futásteljesítmény (KM) mező csak számokat tartalmazhat!");
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

    private void showPdfSelectionDialog() {
        // ... (metódus kódja változatlan) ...
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

        Button generateButton = (Button) dialog.getDialogPane().lookupButton(generateButtonType);
        generateButton.getStyleClass().add("btn-danger");

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
        // ... (handleDeleteCar metódus kódja változatlan) ...
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
        // ... (saveUpcomingService metódus kódja változatlan) ...
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

            showCarDetails(selectedCarId);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Hiba", "Nem sikerült menteni a következő szervizt!");
        }
    }


    // ----------------------------- SEGÉD METÓDUSOK -----------------------------

    private void clearFields() {
        if (licenseField != null) licenseField.clear();
        if (brandCombo != null) brandCombo.getSelectionModel().clearSelection();
        if (typeCombo != null) typeCombo.getSelectionModel().clearSelection();
        if (engineTypeCombo != null) engineTypeCombo.getEditor().clear();
        if (kmField != null) kmField.clear();

        // VÁLTOZÁS 6: Új olajmezők törlése
        if (oilTypeCombo != null) oilTypeCombo.getEditor().clear();
        if (oilQuantityChoice != null) oilQuantityChoice.setValue("5.0 L"); // Vissza az alapértelmezettre

        if (tireSizeCombo != null) tireSizeCombo.getEditor().clear();
        if (insuranceDatePicker != null) insuranceDatePicker.setValue(null);
        if (inspection_date != null) inspection_date.setValue(null);
        if (vintageField != null) vintageField.setValue(2025);
        if (fuelTypeField != null) fuelTypeField.setValue("Benzin");
        if (colorPicker != null) colorPicker.setValue(Color.WHITE);
        if (notesField != null) notesField.clear();
        editingCarId = null;
    }

    protected void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}