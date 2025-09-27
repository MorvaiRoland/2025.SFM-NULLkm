package drivesync;

import drivesync.model.Transaction;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import java.util.List;

public class TransactionChartUI {

    public static BarChart<String, Number> createChart(List<Transaction> transactions, Transaction.Type type) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Dátum");
        yAxis.setLabel("Összeg (Ft)");

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setPrefSize(250, 150);
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

}
