package drivesync.Költségvetés;

import drivesync.Adatbázis.Database;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.*;
import java.time.LocalDate;

public class Budget {

    @FXML private TextField Source, Money;
    @FXML private DatePicker When;
    @FXML private Label errorMsg;
    @FXML private Button addBudget;
    @FXML private RadioButton isExpense, isIncome;
    @FXML private ToggleGroup toggleBudget;

    private Connection conn;
    private String username; // Felhasználónév átadása HomeControllerből

    @FXML
    public void initialize() {
        toggleBudget = new ToggleGroup();
        isExpense.setToggleGroup(toggleBudget);
        isIncome.setToggleGroup(toggleBudget);
        errorMsg.setText("");
    }

    // Felhasználónév beállítása
    public void setUsername(String username) {
        this.username = username;
    }

    @FXML
    public void insertBudget() {
        if (Source.getText().trim().isEmpty() || Money.getText().trim().isEmpty() || When.getValue() == null) {
            errorMsg.setText("Kérlek minden mezőt tölts ki!");
            return;
        }

        int money;
        try {
            money = Integer.parseInt(Money.getText());
        } catch (NumberFormatException e) {
            errorMsg.setText("Kérlek számot adj meg!");
            return;
        }

        if (!isExpense.isSelected() && !isIncome.isSelected()) {
            errorMsg.setText("Kérlek válaszd ki a költségvetés típusát!");
            return;
        }

        if (username == null || username.isEmpty()) {
            errorMsg.setText("Hiba: nincs bejelentkezett felhasználó!");
            return;
        }

        try {
            conn = Database.getConnection();
            if (isExpense.isSelected()) {
                String sql = "INSERT INTO expense (source, amount, date, owner_id) VALUES (?, ?, ?, (SELECT id FROM users WHERE username = ?))";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, Source.getText());
                stmt.setInt(2, money);
                stmt.setDate(3, Date.valueOf(When.getValue()));
                stmt.setString(4, username);
                stmt.executeUpdate();
                errorMsg.setText("💸 Kiadás sikeresen rögzítve!");
            } else {
                String sql = "INSERT INTO income (source, amount, date, owner_id) VALUES (?, ?, ?, (SELECT id FROM users WHERE username = ?))";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, Source.getText());
                stmt.setInt(2, money);
                stmt.setDate(3, Date.valueOf(When.getValue()));
                stmt.setString(4, username);
                stmt.executeUpdate();
                errorMsg.setText("💰 Bevétel sikeresen rögzítve!");
            }
        } catch (SQLException e) {
            errorMsg.setText("Hiba történt az adatbázis művelet során!");
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException ignored) {}
        }
    }
}
