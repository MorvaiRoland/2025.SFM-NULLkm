package drivesync.Költségvetés;

import java.time.LocalDate;

public class Expense {
    private int id;
    private String what;
    private int amount;
    private LocalDate date;
    private String category;

    public Expense(int id, String what, int amount, LocalDate date, String category) {
        this.id = id;
        this.what = what;
        this.amount = amount;
        this.date = date;
        this.category = category;
    }

    public int getId() { return id; }
    public String getWhat() { return what; }
    public int getAmount() { return amount; }
    public LocalDate getDate() { return date; }
    public String getCategory() { return category; }
}