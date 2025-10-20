package drivesync;

import com.mysql.cj.protocol.Resultset;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.util.Duration;

import java.sql.*;
import java.text.DecimalFormat;
import java.time.LocalDate;

public class BudgetController {

    @FXML TextField txt_what, txt_amount;
    @FXML DatePicker txt_date;
    @FXML ComboBox categoryBox;
    @FXML Label msg, monthlyAmount, yearlyAmount;
    @FXML Button saveBtn;
    @FXML BarChart<String, Integer> monthlyChart;
    @FXML CategoryAxis months;
    @FXML NumberAxis expense;
    private Connection conn;
    private String username;

    public void setUsername(String username) {
        this.username = username;
        refreshExpenses();
    }

    public void refreshExpenses() {
        try {
            conn = Database.getConnection();
            String sql = "SELECT * FROM expense WHERE owner_id = (SELECT id FROM users WHERE username = ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            int yearAmount = 0;
            int[] monthAmounts = new int[12];
            while (rs.next()) {
                int price = rs.getInt("price");
                yearAmount += price;
                LocalDate date = rs.getDate("datet").toLocalDate();
                if (date.getYear() == LocalDate.now().getYear()) {
                    int monthIndex = date.getMonthValue()-1;
                    monthAmounts[monthIndex] += price;
                }
            }

            DecimalFormat df = new DecimalFormat("#,###");
            monthlyAmount.setText(df.format(monthAmounts[LocalDate.now().getMonthValue() - 1]) + " Ft");
            yearlyAmount.setText(df.format(yearAmount) + " Ft");

            XYChart.Series<String, Integer> series = new XYChart.Series<>();
            String[] months = {"Jan", "Feb", "Már", "Ápr", "Máj", "Jún", "Júl", "Aug", "Szep", "Okt", "Nov", "Dec"};

            for (int i = 0; i < 12; i++) {
                series.getData().add(new XYChart.Data<>(months[i], monthAmounts[i]));
            }

            monthlyChart.getData().clear();
            monthlyChart.getData().add(series);
        }
        catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    public void initialize() {
        ObservableList<String> categories =  FXCollections.observableArrayList();
        categories.add("Üzemanyag");
        categories.add("Szervíz");
        categories.add("Egyéb");
        categoryBox.setItems(categories);
    }

    public void saveData() {
        if (txt_what.getText().trim().isEmpty() || txt_amount.getText().trim().isEmpty() || txt_date.getValue() == null || categoryBox.getValue() == null || categoryBox.getValue().equals("Kategória")) {
            msg.setStyle("-fx-text-fill: red;");
            msg.setText("Valemelyik mező üres!");
            return;
        }
        int amount = 0;
        try {
            amount = Integer.parseInt(txt_amount.getText());
            if (amount <= 0) {
                msg.setStyle("-fx-text-fill: red;");
                msg.setText("0 és negatív számot nem adhatsz meg!");
                return;
            }
        }
        catch (NumberFormatException e) {
            msg.setStyle("-fx-text-fill: red;");
            msg.setText("Nem számot adtál meg!");
            return;
        }
        int user_id = 0;
        try {
            conn = Database.getConnection();
            String sql = "INSERT INTO expense VALUES (?,?,?,?,(SELECT id FROM users WHERE username=?))";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, txt_what.getText());
            stmt.setInt(2, amount);
            stmt.setString(3, txt_date.getValue().toString());
            stmt.setString(4, categoryBox.getValue().toString());
            stmt.setString(5, username);
            stmt.executeUpdate();
            conn.close();

            msg.setStyle("-fx-text-fill: green;");
            msg.setText("Adat sikeresen rögzítve!");

            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(event -> msg.setText(""));
            pause.play();
            txt_what.setText("");
            txt_amount.setText("");
            txt_date.setValue(null);
            categoryBox.setPromptText("Kategória");
            refreshExpenses();

        }
        catch (SQLException e) { e.printStackTrace(); }
    }

}
