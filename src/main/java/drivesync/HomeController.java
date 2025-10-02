package drivesync;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;

public class HomeController {

    @FXML private Label usernameLabel;
    @FXML private Button logoutButton;

    @FXML private Button homeBtn, carsBtn, budgetBtn, linksBtn, calculatorBtn;
    @FXML private FlowPane contentFlow;

    private String username;
    private ObservableList<Car> cars = FXCollections.observableArrayList();

    public void setUsername(String username) {
        this.username = username;
        usernameLabel.setText(username);
        showHome();
    }

    @FXML
    private void handleLogout() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(HomeController.class);
        prefs.remove("username");
        stage.setScene(App.getLoginScene());
        stage.setTitle("DriveSync");
    }

    @FXML private void showHome() {
        contentFlow.getChildren().clear();
        Label lbl = new Label("Főoldal tartalom");
        contentFlow.getChildren().add(lbl);
    }

    @FXML private void showCars() {
        contentFlow.getChildren().clear();
        cars.clear();
        loadCarsFromDB();
        for (Car car : cars) {
            VBox carBox = new VBox(5);
            carBox.setStyle("-fx-border-color: black; -fx-padding: 10; -fx-background-color: #FFD700;");
            Label lbl = new Label(car.getBrand() + " " + car.getType() + " (" + car.getLicense() + ")");
            Button deleteBtn = new Button("Törlés");
            deleteBtn.setOnAction(e -> deleteCar(car));
            carBox.getChildren().addAll(lbl, deleteBtn);
            contentFlow.getChildren().add(carBox);
        }

        Button addCarBtn = new Button("Új autó hozzáadása");
        addCarBtn.setOnAction(e -> showAddCarDialog());
        contentFlow.getChildren().add(addCarBtn);
    }

    private void loadCarsFromDB() {
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
    }

    private void deleteCar(Car car) {
        String sql = "DELETE FROM cars WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, car.getId());
            stmt.executeUpdate();
            showCars();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showAddCarDialog() {
        Dialog<Car> dialog = new Dialog<>();
        dialog.setTitle("Új autó hozzáadása");
        VBox box = new VBox(10);

        TextField licenseField = new TextField();
        licenseField.setPromptText("Rendszám");
        TextField brandField = new TextField();
        brandField.setPromptText("Márka");
        TextField typeField = new TextField();
        typeField.setPromptText("Típus");
        TextField vintageField = new TextField();
        vintageField.setPromptText("Évjárat");

        box.getChildren().addAll(new Label("Rendszám:"), licenseField,
                new Label("Márka:"), brandField,
                new Label("Típus:"), typeField,
                new Label("Évjárat:"), vintageField);

        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                return new Car(0, 0, licenseField.getText(), brandField.getText(),
                        typeField.getText(), Integer.parseInt(vintageField.getText()),
                        "", "", 0, 0, 0, LocalDate.now(), LocalDate.now());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(this::addCarToDB);
    }

    private void addCarToDB(Car car) {
        String sql = "INSERT INTO cars (owner_id, license, brand, type, vintage, engine_type, fuel_type, km, oil, tire_size, service, insurance) " +
                "VALUES ((SELECT id FROM users WHERE username = ?), ?, ?, ?, ?, '', '', 0,0,0, NOW(), NOW())";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, car.getLicense());
            stmt.setString(3, car.getBrand());
            stmt.setString(4, car.getType());
            stmt.setInt(5, car.getVintage());
            stmt.executeUpdate();
            showCars();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ----------------- Új módszer az FXML-ek betöltésére -----------------
    private void loadFXMLToContent(String fxmlFileName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
            Parent pane = loader.load();

            contentFlow.getChildren().clear();
            contentFlow.getChildren().add(pane);

            // Ha szükséges, itt elérhetjük a controller-t
            Object controller = loader.getController();
            if (controller instanceof CalculatorController) {
                CalculatorController calcController = (CalculatorController) controller;
                // további inicializáció, ha kell
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ----------------- Menüpontok -----------------
    @FXML private void showBudget() {
        loadFXMLToContent("BudgetController.fxml");
    }

    @FXML private void showLinks() {
        loadFXMLToContent("LinksController.fxml");
    }

    @FXML private void showCalculator() {
        loadFXMLToContent("CalculatorController.fxml");
    }
}
