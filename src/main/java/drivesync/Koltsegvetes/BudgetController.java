package drivesync.Koltsegvetes;

import drivesync.Adatbazis.Database;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class BudgetController {

    @FXML private TextField txt_what, txt_amount;
    @FXML private DatePicker txt_date;
    @FXML private ComboBox<String> categoryBox;
    @FXML private ComboBox<Integer> yearBox;
    @FXML private ComboBox<String> filterBox;
    @FXML private Label msg, monthlyAmount, yearlyAmount;
    @FXML private Button saveBtn, exportPdfBtn;
    @FXML private BarChart<String, Number> monthlyChart;
    @FXML private LineChart<String, Number> trendChart;
    @FXML private CategoryAxis months;
    @FXML private NumberAxis expense;
    @FXML private TableView<Expense> table;
    @FXML private TableColumn<Expense, String> colWhat;
    @FXML private TableColumn<Expense, Number> colAmount;
    @FXML private TableColumn<Expense, String> colCategory;
    @FXML private TableColumn<Expense, String> colDate;
    @FXML private TableColumn<Expense, Void> colEdit;
    @FXML private TableColumn<Expense, Void> colDelete;
    @FXML private ComboBox<String> filterCategoryBox;


    private Connection conn;
    private String username;
    private final String[] categories = {"Üzemanyag", "Szervíz", "Egyéb"};
    private final int MONTHLY_LIMIT = 200000;

    public void setUsername(String username) {
        this.username = username;

        initializeYearBox();

        loadTableData();     // <-- Előbb a tábla töltődjön be
        refreshExpenses();   // <-- Csak utána számold a grafikont
    }


    @FXML
    public void initialize() {
        categoryBox.setItems(FXCollections.observableArrayList(categories));
        filterBox.setItems(FXCollections.observableArrayList("Havi", "Negyedéves", "Éves"));
        filterBox.setValue("Havi");

        // Új: szűrés kategória
        filterCategoryBox.setItems(FXCollections.observableArrayList(categories));
        filterCategoryBox.setValue(""); // alapértelmezett: nincs szűrés

        // Táblázat oszlopok
        colWhat.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getWhat()));
        colAmount.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getAmount()));
        colCategory.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategory()));
        colDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDate().toString()));

        addEditButton();
        addDeleteButton();

        // Listener-ek: év, szűrés típus, kategória
        yearBox.valueProperty().addListener((obs, oldV, newV) -> refreshExpenses());
        filterBox.valueProperty().addListener((obs, oldV, newV) -> refreshExpenses());
        filterCategoryBox.valueProperty().addListener((obs, oldV, newV) -> refreshExpenses()); // új
    }





    private void initializeYearBox() {
        int currentYear = LocalDate.now().getYear();
        ObservableList<Integer> years = FXCollections.observableArrayList();
        for (int i = currentYear; i >= currentYear - 10; i--) years.add(i);
        yearBox.setItems(years);
        yearBox.setValue(currentYear);
    }


    @FXML
    public void saveData() {
        if (txt_what.getText().trim().isEmpty() || txt_amount.getText().trim().isEmpty()
                || txt_date.getValue() == null || categoryBox.getValue() == null) {
            showMessage("Valamelyik mező üres!", "red");
            return;
        }

        int amount;
        try {
            amount = Integer.parseInt(txt_amount.getText());
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showMessage("Hibás összeg!", "red");
            return;
        }

        try {
            conn = Database.getConnection();
            String sql = "INSERT INTO expense (what, price, datet, category, owner_id) VALUES (?,?,?,?," +
                    "(SELECT id FROM users WHERE username=?))";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, txt_what.getText());
            stmt.setInt(2, amount);
            stmt.setString(3, txt_date.getValue().toString());
            stmt.setString(4, categoryBox.getValue());
            stmt.setString(5, username);
            stmt.executeUpdate();
            conn.close();

            showMessage("Adat sikeresen rögzítve!", "green");
            txt_what.clear();
            txt_amount.clear();
            txt_date.setValue(null);
            categoryBox.setValue(null);

            refreshExpenses();
            loadTableData();
        } catch (SQLException e) {
            e.printStackTrace();
            showMessage("Hiba az adatbázisba íráskor!", "red");
        }
    }

    private void showMessage(String text, String color) {
        msg.setText(text);
        msg.setStyle("-fx-text-fill: " + color + ";");
        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(e -> msg.setText(""));
        pause.play();
    }

    @FXML
    public void refreshExpenses() {
        if (yearBox.getValue() == null) return;
        int selectedYear = yearBox.getValue();
        String filter = filterBox.getValue();       // Havi, Negyedéves, Éves
        String categoryFilter = filterCategoryBox.getValue(); // MOST már innen jön a szűrés

        try {
            conn = Database.getConnection();
            String sql = "SELECT * FROM expense WHERE owner_id = (SELECT id FROM users WHERE username=?) ORDER BY datet DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            ObservableList<Expense> list = FXCollections.observableArrayList();
            Map<String, int[]> categoryMonthAmounts = new HashMap<>();
            for (String cat : categories) categoryMonthAmounts.put(cat, new int[12]);
            int yearTotal = 0;

            LocalDate now = LocalDate.now();
            int currentMonth = now.getMonthValue();
            int currentQuarter = (currentMonth - 1) / 3 + 1;

            while (rs.next()) {
                int price = rs.getInt("price");
                LocalDate date = rs.getDate("datet").toLocalDate();
                String cat = rs.getString("category");

                if (date.getYear() != selectedYear) continue;

                // Szűrés típusa
                boolean filterPass = switch (filter) {
                    case "Havi" -> date.getMonthValue() == currentMonth;
                    case "Negyedéves" -> ((date.getMonthValue() - 1) / 3 + 1) == currentQuarter;
                    case "Éves" -> true;
                    default -> true;
                };

                // Kategória szűrés
                boolean categoryPass = (categoryFilter == null || categoryFilter.isEmpty()) || cat.equals(categoryFilter);

                if (!filterPass || !categoryPass) continue;

                // Táblázat
                list.add(new Expense(rs.getInt("id"), rs.getString("what"), price, date, cat));

                // Grafikon
                categoryMonthAmounts.get(cat)[date.getMonthValue() - 1] += price;
                yearTotal += price;
            }

            table.setItems(list);
            conn.close();

            DecimalFormat df = new DecimalFormat("#,###");
            monthlyAmount.setText(df.format(categoryMonthAmounts.values().stream()
                    .mapToInt(a -> a[LocalDate.now().getMonthValue() - 1]).sum()) + " Ft");
            yearlyAmount.setText(df.format(yearTotal) + " Ft");

            updateBarChart(categoryMonthAmounts);
            updateTrendChart(categoryMonthAmounts);
            checkWarnings(categoryMonthAmounts);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    private void updateBarChart(Map<String, int[]> categoryMonthAmounts) {
        String[] monthsNames = {"Jan", "Feb", "Már", "Ápr", "Máj", "Jún", "Júl", "Aug", "Szep", "Okt", "Nov", "Dec"};
        months.setCategories(FXCollections.observableArrayList(monthsNames));
        monthlyChart.getData().clear();

        for (String cat : categories) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(cat);
            int[] amounts = categoryMonthAmounts.get(cat);
            for (int i = 0; i < 12; i++) {
                XYChart.Data<String, Number> data = new XYChart.Data<>(monthsNames[i], amounts[i]);
                series.getData().add(data);
                Tooltip tooltip = new Tooltip(cat + ": " + amounts[i] + " Ft");
                Tooltip.install(data.getNode(), tooltip);
            }
            monthlyChart.getData().add(series);
        }
    }

    private void loadTableData() {
        ObservableList<Expense> list = FXCollections.observableArrayList();

        try {
            conn = Database.getConnection();
            String sql = "SELECT * FROM expense WHERE owner_id = (SELECT id FROM users WHERE username=?) ORDER BY datet DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(new Expense(
                        rs.getInt("id"),
                        rs.getString("what"),
                        rs.getInt("price"),
                        rs.getDate("datet").toLocalDate(),
                        rs.getString("category")));
            }
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        table.setItems(list);
    }


    private void addEditButton() {
        colEdit.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("✏ Szerkeszt");

            {
                btn.setOnAction(e -> {
                    Expense ex = getTableView().getItems().get(getIndex());

                    // mezők feltöltése
                    txt_what.setText(ex.getWhat());
                    txt_amount.setText(String.valueOf(ex.getAmount()));
                    txt_date.setValue(ex.getDate());
                    categoryBox.setValue(ex.getCategory());

                    // mentés helyett UPDATE lesz
                    saveBtn.setText("Módosítás");
                    saveBtn.setOnAction(ev -> updateExpense(ex.getId()));
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }
    private void addDeleteButton() {
        colDelete.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("🗑 Törlés");

            {
                btn.setOnAction(e -> {
                    Expense ex = getTableView().getItems().get(getIndex());
                    deleteExpense(ex.getId());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }
    private void updateExpense(int id) {
        try {
            conn = Database.getConnection();
            String sql = "UPDATE expense SET what=?, price=?, datet=?, category=? WHERE id=?";
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, txt_what.getText());
            stmt.setInt(2, Integer.parseInt(txt_amount.getText()));
            stmt.setString(3, txt_date.getValue().toString());
            stmt.setString(4, categoryBox.getValue());
            stmt.setInt(5, id);

            stmt.executeUpdate();
            conn.close();

            showMessage("Sikeres módosítás!", "green");
            refreshExpenses();
            loadTableData();

            // visszaállítás
            saveBtn.setText("Rögzítés");
            saveBtn.setOnAction(e -> saveData());

        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Hiba módosításkor!", "red");
        }
    }
    private void deleteExpense(int id) {
        try {
            conn = Database.getConnection();
            String sql = "DELETE FROM expense WHERE id=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            stmt.executeUpdate();
            conn.close();

            showMessage("Törölve!", "green");
            refreshExpenses();
            loadTableData();


        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Hiba törléskor!", "red");
        }
    }



    private void updateTrendChart(Map<String, int[]> categoryMonthAmounts) {
        String[] monthsNames = {"Jan", "Feb", "Már", "Ápr", "Máj", "Jún", "Júl", "Aug", "Szep", "Okt", "Nov", "Dec"};
        trendChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Trend");
        for (int i = 0; i < 12; i++) {
            int total = 0;
            for (String cat : categories) total += categoryMonthAmounts.get(cat)[i];
            XYChart.Data<String, Number> data = new XYChart.Data<>(monthsNames[i], total);
            Tooltip tooltip = new Tooltip("Összesen: " + total + " Ft");
            Tooltip.install(data.getNode(), tooltip);
            series.getData().add(data);
        }
        trendChart.getData().add(series);
    }

    private void checkWarnings(Map<String, int[]> categoryMonthAmounts) {
        int currentMonth = LocalDate.now().getMonthValue() - 1;
        for (String cat : categories) {
            int val = categoryMonthAmounts.get(cat)[currentMonth];
            if (val > MONTHLY_LIMIT) {
                showMessage(cat + " havi kiadása meghaladja a limitet!", "orange");
            }
        }
    }

    // ──────────────────────────────────────────────
    // PDF EXPORT (PDFBox, Type1 fontokkal)
    // ──────────────────────────────────────────────


    @FXML
    public void exportPDF() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exportálás PDF-be");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF fájlok", "*.pdf"));
        File file = fileChooser.showSaveDialog(null);
        if (file == null) return;

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);

            // Load the Unicode font (e.g., Arial)
            InputStream fontStream = getClass().getResourceAsStream("/drivesync/fonts/arial.ttf");
            if (fontStream == null) throw new IOException("Font file not found: arial.ttf");
            PDType0Font font = PDType0Font.load(doc, fontStream);

            PDPageContentStream cs = new PDPageContentStream(doc, page);

            float y = 750; // Starting position on the Y-axis for the content
            float margin = 50; // Left margin
            float[] columnOffsets = { margin, margin + 100, margin + 200, margin + 300 }; // X-offsets for columns

            // Title
            cs.beginText();
            cs.setFont(font, 18);
            cs.newLineAtOffset(200, y);
            cs.showText("Költségvetési Jelentés"); // Hungarian letters
            cs.endText();

            y -= 30; // Move down after the title

            // Username
            cs.beginText();
            cs.setFont(font, 12);
            cs.newLineAtOffset(margin, y);
            cs.showText("Felhasználó: " + username);
            cs.endText();

            y -= 20;

            // Date
            cs.beginText();
            cs.setFont(font, 12);
            cs.newLineAtOffset(margin, y);
            cs.showText("Generálva: " + LocalDate.now());
            cs.endText();

            y -= 30;

            // Table header
            cs.beginText();
            cs.setFont(font, 12);
            cs.newLineAtOffset(columnOffsets[0], y);
            cs.showText("Hónap");
            cs.endText();

            cs.beginText();
            cs.newLineAtOffset(columnOffsets[1], y);
            cs.showText(categories[0]);
            cs.endText();

            cs.beginText();
            cs.newLineAtOffset(columnOffsets[2], y);
            cs.showText(categories[1]);
            cs.endText();

            cs.beginText();
            cs.newLineAtOffset(columnOffsets[3], y);
            cs.showText(categories[2]);
            cs.endText();

            y -= 20;

            // Table data
            String[] monthsNames = {"Jan", "Feb", "Már", "Ápr", "Máj", "Jún", "Júl", "Aug", "Szep", "Okt", "Nov", "Dec"};
            for (int i = 0; i < 12; i++) {
                cs.beginText();
                cs.setFont(font, 12);
                cs.newLineAtOffset(columnOffsets[0], y);
                cs.showText(monthsNames[i]);
                cs.endText();

                for (int j = 0; j < categories.length; j++) {
                    int currentIndex = j; // Declare a new effectively final variable
                    Number value = 0;

                    // Retrieve data for each category in the series
                    var matchingSeries = monthlyChart.getData().stream()
                            .filter(s -> s.getName().equals(categories[currentIndex])) // Use the effectively final variable
                            .findFirst();

                    if (matchingSeries.isPresent() && matchingSeries.get().getData().size() > i) {
                        value = matchingSeries.get().getData().get(i).getYValue();
                    }

                    cs.beginText();
                    cs.newLineAtOffset(columnOffsets[j + 1], y);
                    cs.showText(value + " Ft");
                    cs.endText();
                }
                y -= 20;

                // Add a new page if Y-position is too low
                if (y < 50) {
                    cs.close();
                    page = new PDPage();
                    doc.addPage(page);
                    cs = new PDPageContentStream(doc, page);
                    y = 750;
                }
            }

            y -= 20;

            // Yearly total
            cs.beginText();
            cs.setFont(font, 12);
            cs.newLineAtOffset(margin, y);
            cs.showText("Összes éves kiadás: " + yearlyAmount.getText());
            cs.endText();

            y -= 15;

            // Monthly total
            cs.beginText();
            cs.newLineAtOffset(margin, y);
            cs.showText("Aktuális havi kiadás: " + monthlyAmount.getText());
            cs.endText();

            cs.close();
            doc.save(file);
            showMessage("PDF export sikeres!", "green");
        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Hiba a PDF export során!", "red");
        }
    }
}