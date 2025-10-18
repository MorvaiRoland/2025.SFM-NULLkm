package drivesync.Költségvetés;

import drivesync.Adatbázis.Database;
import drivesync.Főoldal.HomeDashboardController;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.*;
import java.time.LocalDate;

public class Budget {

    @FXML TextField Source, Money;
    @FXML DatePicker When;
    @FXML Label errorMsg;
    @FXML Button addBudget;
    @FXML RadioButton isExpense, isIncome;
    @FXML ToggleGroup toggleBudget;
    private Connection conn;

    @FXML
    public void initialize() {
        toggleBudget = new ToggleGroup();
        isExpense.setToggleGroup(toggleBudget);
        isIncome.setToggleGroup(toggleBudget);
        errorMsg.setText("");
    }

    public void insertBudget() {
        if (Source.getText().trim().isEmpty() || Money.getText().trim().isEmpty() || When.getValue() == null) {
            errorMsg.setText("Kérlek minden mezőt tölts ki!");
            return;
        }
        int money = 0;
        try {
            money = Integer.parseInt(Money.getText());
        } catch (NumberFormatException e) {
            errorMsg.setText("Kérlek számot adj meg!");
            return;
        }
        if (!isExpense.isSelected() && !isIncome.isSelected()) {
            errorMsg.setText("Kérlek választ ki a költségvetés fajtáját!");
            return;
        }
        LocalDate date = LocalDate.now();
        int owner_id = 0;
        if (isExpense.isSelected()) {
            conn = Database.getConnection();
            String sql = "INSERT INTO expense VALUES (?, ?, ?, (SELECT id FROM users WHERE username = ?))";
            try {
                conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, this.Source.getText());
                stmt.setInt(2, money);
                stmt.setDate(3, Date.valueOf(When.getValue()));
                stmt.setString(4, HomeDashboardController.getUsername());
                stmt.executeUpdate();
                conn.close();
                errorMsg.setText("Kiadás sikeresen rogzítve!");
            }
            catch (SQLException e) { e.printStackTrace(); }
        }
        else {
            conn = Database.getConnection();
            String sql = "INSERT INTO income VALUES (?, ?, ?, (SELECT id FROM users WHERE username = ?))";
            try {
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, this.Source.getText());
                stmt.setInt(2, money);
                stmt.setDate(3, Date.valueOf(When.getValue()));
                stmt.setString(4, HomeDashboardController.getUsername());
                stmt.executeUpdate();
                conn.close();
                errorMsg.setText("Bevétel sikeresen rögzítve!");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
