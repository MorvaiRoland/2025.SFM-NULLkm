package drivesync;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import drivesync.TransactionDAO;
import drivesync.model.Transaction;
import java.time.LocalDate;
import drivesync.TransactionChartUI;

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
    @FXML private void showLinks() { setupWidgetsLinks(); }
    @FXML private void showBudget() {setupWidgetsBudget();}
    private BarChart<String, Number> createChartFromTransactions(Transaction.Type type) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Dátum");
        yAxis.setLabel("Összeg (Ft)");

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setPrefSize(220, 100);
        chart.setLegendVisible(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        transactions.stream()
                .filter(t -> t.getType() == type)
                .forEach(t -> {
                    String dateStr = t.getDate().toString();
                    series.getData().stream()
                            .filter(d -> d.getXValue().equals(dateStr))
                            .findFirst()
                            .ifPresentOrElse(d -> d.setYValue(d.getYValue().doubleValue() + t.getAmount()),
                                    () -> series.getData().add(new XYChart.Data<>(dateStr, t.getAmount())));
                });

        chart.getData().add(series);
        return chart;
    }
    private TransactionDAO dao = new TransactionDAO();
    private List<Transaction> transactions = new ArrayList<>();

    @FXML
    private void initialize() {
        Database db = new Database(); // táblák létrehozása ha nem létezik
        transactions = dao.getAll();  // betölti az összes tranzakciót
        showHome();
    }

    // Például új bevétel mentése:
    @FXML private TextField amountField;
    @FXML private TextField descriptionField;
    @FXML private ChoiceBox<String> typeChoice;




    @FXML
    private void showCalculator() {
        contentFlow.getChildren().clear();
        widgets.clear();
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/drivesync/CalculatorController.fxml")
            );
            Pane calculatorPane = loader.load();
            contentFlow.getChildren().add(calculatorPane);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


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
        // Bevételek – kék
        contentFlow.getChildren().add(createWidgetWithChart("Bevételek", TransactionChartUI.createChart(transactions, Transaction.Type.INCOME), "#87CEEB", Transaction.Type.INCOME));
        // Kiadások – piros
        contentFlow.getChildren().add(createWidgetWithChart("Kiadások", TransactionChartUI.createChart(transactions, Transaction.Type.EXPENSE), "#FF8C00", Transaction.Type.EXPENSE));
    }

    private VBox createWidgetWithChart(String title, BarChart<String, Number> chart, String color, Transaction.Type type) {
        VBox box = new VBox(10);
        box.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 15; -fx-padding: 15;");

        Label lblTitle = new Label(title);
        lblTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: black;");

        Button addBtn = new Button("➕ Hozzáadás");
        addBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 12;");
        addBtn.setOnAction(e -> showAddTransactionDialog(type));

        box.getChildren().addAll(lblTitle, chart, addBtn);
        return box;
    }
    private void showAddTransactionDialog(Transaction.Type type) {
        Stage dialog = new Stage();
        dialog.setTitle(type == Transaction.Type.INCOME ? "Új bevétel" : "Új kiadás");

        VBox vbox = new VBox(10);
        vbox.setStyle("-fx-padding: 20;");

        TextField nameField = new TextField();
        nameField.setPromptText("Tétel neve");

        TextField amountField = new TextField();
        amountField.setPromptText("Összeg");

        DatePicker datePicker = new DatePicker(LocalDate.now());

        Button addBtn = new Button("Hozzáadás");
        addBtn.setOnAction(e -> {
            try {
                String name = nameField.getText();
                double amount = Double.parseDouble(amountField.getText());
                LocalDate date = datePicker.getValue();

                Transaction t = new Transaction(type, date, amount, name);
                dao.insert(t);
                transactions = dao.getAll();
                setupWidgetsBudget();
                dialog.close();
            } catch (NumberFormatException ex) {
                System.out.println("Hibás összeg!");
            }
        });

        vbox.getChildren().addAll(nameField, amountField, datePicker, addBtn);

        Scene scene = new Scene(vbox);
        dialog.setScene(scene);
        dialog.show();
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
