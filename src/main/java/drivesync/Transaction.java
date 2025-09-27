package drivesync.model;

import java.time.LocalDate;

public class Transaction {
    public enum Type { INCOME, EXPENSE }

    private Type type;
    private LocalDate date;
    private double amount;
    private String description;

    public Transaction(Type type, LocalDate date, double amount, String description) {
        this.type = type;
        this.date = date;
        this.amount = amount;
        this.description = description;
    }

    public Type getType() { return type; }
    public LocalDate getDate() { return date; }
    public double getAmount() { return amount; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return "[" + type + "] " + date + " - " + amount + " Ft (" + description + ")";
    }
}
