package drivesync;

import drivesync.model.Transaction;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    public void insert(Transaction t) {
        String sql = "INSERT INTO transactions (type, date, amount, description) VALUES (?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, t.getType().name());
            ps.setDate(2, Date.valueOf(t.getDate()));
            ps.setDouble(3, t.getAmount());
            ps.setString(4, t.getDescription());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public List<Transaction> getAll() {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY date";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Transaction t = new Transaction(
                        Transaction.Type.valueOf(rs.getString("type")),
                        rs.getDate("date").toLocalDate(),
                        rs.getDouble("amount"),
                        rs.getString("description")
                );
                list.add(t);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}
