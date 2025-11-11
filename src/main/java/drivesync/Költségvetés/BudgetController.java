package drivesync.Költségvetés;

import drivesync.Adatbázis.Database;
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
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.FileOutputStream;
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

    private Connection conn;
    private String username;
    private final String[] categories = {"Üzemanyag", "Szervíz", "Egyéb"};
    private final int MONTHLY_LIMIT = 200000;

    public void setUsername(String username) {
        this.username = username;
        initializeYearBox();
        refreshExpenses();
    }

    @FXML
    public void initialize() {
        categoryBox.setItems(FXCollections.observableArrayList(categories));
        filterBox.setItems(FXCollections.observableArrayList("Havi", "Negyedéves", "Éves"));
        filterBox.setValue("Havi");
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
        int selectedYear = yearBox.getValue();

        try {
            conn = Database.getConnection();
            String sql = "SELECT * FROM expense WHERE owner_id = (SELECT id FROM users WHERE username=?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            int yearTotal = 0;
            Map<String, int[]> categoryMonthAmounts = new HashMap<>();
            for (String cat : categories) categoryMonthAmounts.put(cat, new int[12]);

            while (rs.next()) {
                int price = rs.getInt("price");
                LocalDate date = rs.getDate("datet").toLocalDate();
                if (date.getYear() != selectedYear) continue;

                yearTotal += price;
                int monthIndex = date.getMonthValue() - 1;
                String cat = rs.getString("category");
                categoryMonthAmounts.get(cat)[monthIndex] += price;
            }

            DecimalFormat df = new DecimalFormat("#,###");
            monthlyAmount.setText(df.format(categoryMonthAmounts.values().stream()
                    .mapToInt(a -> a[LocalDate.now().getMonthValue() - 1]).sum()) + " Ft");
            yearlyAmount.setText(df.format(yearTotal) + " Ft");

            updateBarChart(categoryMonthAmounts);
            updateTrendChart(categoryMonthAmounts);
            checkWarnings(categoryMonthAmounts);

        } catch (SQLException e) { e.printStackTrace(); }
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

            PDPageContentStream cs = new PDPageContentStream(doc, page);

            float y = 750;
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 18);
            cs.newLineAtOffset(200, y);
            cs.showText("Költségvetési Jelentés");
            cs.endText();

            y -= 30;
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA, 12);
            cs.newLineAtOffset(50, y);
            cs.showText("Felhasználó: " + username);
            cs.endText();

            y -= 20;
            cs.beginText();
            cs.newLineAtOffset(50, y);
            cs.showText("Generálva: " + LocalDate.now());
            cs.endText();

            y -= 30;

            // Táblázat-fejléc
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
            cs.newLineAtOffset(50, y);
            cs.showText(String.format("%-10s %-12s %-12s %-12s", "Hónap", categories[0], categories[1], categories[2]));
            cs.endText();
            y -= 20;

            // Táblázat adatok
            String[] monthsNames = {"Jan", "Feb", "Már", "Ápr", "Máj", "Jún", "Júl", "Aug", "Szep", "Okt", "Nov", "Dec"};
            for (int i = 0; i < 12; i++) {
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 12);
                cs.newLineAtOffset(50, y);

                String line = String.format("%-10s", monthsNames[i]);
                for (String cat : categories) {
                    Number value = 0;
                    var series = monthlyChart.getData().stream()
                            .filter(s -> s.getName().equals(cat))
                            .findFirst();
                    if (series.isPresent() && series.get().getData().size() > i)
                        value = series.get().getData().get(i).getYValue();
                    line += String.format("%-12s", value + " Ft");
                }

                cs.showText(line);
                cs.endText();
                y -= 20;
                if (y < 50) {
                    cs.close();
                    page = new PDPage();
                    doc.addPage(page);
                    cs = new PDPageContentStream(doc, page);
                    y = 750;
                }
            }

            y -= 20;
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
            cs.newLineAtOffset(50, y);
            cs.showText("Összes éves kiadás: " + yearlyAmount.getText());
            cs.endText();

            y -= 15;
            cs.beginText();
            cs.newLineAtOffset(50, y);
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
