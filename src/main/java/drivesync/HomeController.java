package drivesync;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HomeController {

    @FXML private Label usernameLabel;
    @FXML private Button logoutButton;

    @FXML private Button homeBtn, carsBtn, budgetBtn, linksBtn, calculatorBtn;
    @FXML private FlowPane contentFlow;

    private List<Pane> widgets = new ArrayList<>();

    @FXML
    private void initialize() {
        showHome();
    }

    public void setUsername(String username) {
        usernameLabel.setText(username);
    }

    @FXML
    private void handleLogout() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        File rememberFile = new File("remember_me.txt");
        if (rememberFile.exists()) rememberFile.delete();
        stage.setScene(App.getLoginScene());
        stage.setTitle("DriveSync");
    }

    // ===== Menü gombok =====
    @FXML private void showHome() { setupWidgetsHome(); }
    @FXML private void showCars() { setupWidgetsCars(); }
    @FXML private void showBudget() { setupWidgetsBudget(); }
    @FXML private void showLinks() { setupWidgetsLinks(); }
    @FXML private void showCalculator() { setupWidgetsCalculator(); }

    // ===== Főoldal widgetek =====
    private void setupWidgetsHome() {
        contentFlow.getChildren().clear();
        widgets.clear();
        addWidgetWithChart("Legutóbbi autók", "Utoljára hozzáadott autóid.", "#FFD700");
        addWidgetWithChart("Értesítések", "Új üzenetek és emlékeztetők.", "#FF8C00");
        addWidgetWithChart("Havi költség", "Havi költségek grafikon formában.", "#87CEEB");
        addWidgetWithChart("Közelgő események", "Fontos határidők és események.", "#90EE90");
    }

    private void setupWidgetsCars() {
        contentFlow.getChildren().clear();
        widgets.clear();
        addWidget("Autó 1", "Részletek és statisztikák", "#FFD700");
        addWidget("Autó 2", "Részletek és statisztikák", "#FFD700");
    }

    private void setupWidgetsBudget() {
        contentFlow.getChildren().clear();
        widgets.clear();
        addWidgetWithChart("Bevételek", "Havi bevételek grafikonja.", "#87CEEB");
        addWidgetWithChart("Kiadások", "Havi kiadások grafikonja.", "#FF8C00");
        addWidgetWithChart("Összegzés", "Havi egyenleg grafikonja.", "#90EE90");
    }

    private void setupWidgetsLinks() {
        contentFlow.getChildren().clear();
        widgets.clear();
        addWidget("Autós blogok", "Hasznos linkek autós témákhoz", "#FFD700");
        addWidget("Pályázatok", "Támogatások és pályázatok linkjei", "#87CEEB");
    }

    private void setupWidgetsCalculator() {
        contentFlow.getChildren().clear();
        widgets.clear();
        addWidget("Üzemanyag kalkulátor", "Költségek számítása", "#FFD700");
        addWidget("Éves költség kalkulátor", "Éves tervezés", "#90EE90");
    }

    // ===== Widget logika =====
    private void addWidget(String title, String description, String color) {
        VBox box = createBaseWidget(title, description, color);
        widgets.add(box);
        contentFlow.getChildren().add(box);
    }

    private void addWidgetWithChart(String title, String description, String color) {
        VBox box = createBaseWidget(title, description, color);

        // Mini BarChart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setPrefSize(220, 80);
        chart.setLegendVisible(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Hét 1", Math.random() * 100));
        series.getData().add(new XYChart.Data<>("Hét 2", Math.random() * 100));
        series.getData().add(new XYChart.Data<>("Hét 3", Math.random() * 100));
        series.getData().add(new XYChart.Data<>("Hét 4", Math.random() * 100));
        chart.getData().add(series);

        box.getChildren().add(chart);
        widgets.add(box);
        contentFlow.getChildren().add(box);
    }

    private VBox createBaseWidget(String title, String description, String color) {
        VBox box = new VBox(10);
        box.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 15; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 8,0,0,4);");
        box.setPrefSize(250, 150);

        Label lblTitle = new Label(title);
        lblTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: black;");

        Label lblDesc = new Label(description);
        lblDesc.setWrapText(true);
        lblDesc.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");

        Button deleteBtn = new Button("✖");
        deleteBtn.setStyle("-fx-background-color: #FF4C4C; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 12;");
        deleteBtn.setOnAction(e -> {
            contentFlow.getChildren().remove(box);
            widgets.remove(box);
        });

        box.getChildren().addAll(lblTitle, lblDesc, deleteBtn);
        return box;
    }

    @FXML private void addNewWidget() {
        int count = widgets.size() + 1;
        addWidget("Új widget " + count, "Ez egy testreszabható widget, ide bármit tehetsz.", "#D3D3D3");
    }
}
