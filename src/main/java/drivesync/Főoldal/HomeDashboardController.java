package drivesync.Főoldal;

import com.itextpdf.layout.properties.TextAlignment;
import drivesync.Időjárás.WeatherService;
import drivesync.Időjárás.WeatherService.Weather;
import drivesync.FuelService.FuelService;
import drivesync.Adatbázis.ServiceDAO;
import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;


import java.awt.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeDashboardController {

    @FXML private FlowPane widgetContainer;
    @FXML private HBox menuHBox;
    @FXML private Button toggleMenuBtn, weatherBtn, fuelBtn, carsBtn, budgetBtn, linksBtn, notificationsBtn;
    @FXML
    private BorderPane mainLayout; // a fő BorderPane, az FXML gyökerében


    private boolean isCollapsed = false;
    private final Map<String, VBox> activeWidgets = new HashMap<>();
    private String username;

    public void setUsername(String username) { this.username = username; }
    public String getUsername() { return username; }

    @FXML
    public void initialize() {
        // Tooltip-ek
        weatherBtn.setTooltip(new Tooltip("Időjárás"));
        fuelBtn.setTooltip(new Tooltip("Üzemanyag"));
        carsBtn.setTooltip(new Tooltip("Autók"));
        budgetBtn.setTooltip(new Tooltip("Költségvetés"));
        linksBtn.setTooltip(new Tooltip("Linkek"));
        notificationsBtn.setTooltip(new Tooltip("Értesítések"));

        // Ikonok beállítása
        setButtonGraphic(weatherBtn, "/drivesync/icons/weather.png");
        setButtonGraphic(fuelBtn, "/drivesync/icons/fuel.png");
        setButtonGraphic(carsBtn, "/drivesync/icons/car.png");
        setButtonGraphic(budgetBtn, "/drivesync/icons/budget.png");
        setButtonGraphic(linksBtn, "/drivesync/icons/links.png");
        setButtonGraphic(notificationsBtn, "/drivesync/icons/notification.png");

        // Hover effekt
        addHover(weatherBtn); addHover(fuelBtn); addHover(carsBtn);
        addHover(budgetBtn); addHover(linksBtn); addHover(notificationsBtn);

        // Menü összecsukás
        toggleMenuBtn.setOnAction(e -> toggleMenu());

        // Widget-ek gombjai
        weatherBtn.setOnAction(e -> toggleWidget("weather", this::createWeatherWidget));
        fuelBtn.setOnAction(e -> toggleWidget("fuel", this::createFuelWidget));
        carsBtn.setOnAction(e -> toggleWidget("cars", this::createCarsWidget));
        budgetBtn.setOnAction(e -> toggleWidget("budget", this::createBudgetWidget));
        linksBtn.setOnAction(e -> toggleWidget("links", this::createLinksWidget));
        notificationsBtn.setOnAction(e -> toggleWidget("notifications", this::createNotificationWidgets));
    }

    private void setButtonGraphic(Button btn, String resourcePath) {
        ImageView iv = new ImageView(new Image(getClass().getResourceAsStream(resourcePath)));
        iv.setFitWidth(32); iv.setFitHeight(32); iv.setPreserveRatio(true);
        btn.setGraphic(iv);
    }

    private void addHover(Button btn) {
        ScaleTransition grow = new ScaleTransition(Duration.millis(150), btn);
        ScaleTransition shrink = new ScaleTransition(Duration.millis(150), btn);
        btn.setOnMouseEntered(e -> { grow.setToX(1.1); grow.setToY(1.1); grow.playFromStart(); });
        btn.setOnMouseExited(e -> { shrink.setToX(1); shrink.setToY(1); shrink.playFromStart(); });
    }

    private void toggleMenu() {
        double targetHeight = isCollapsed ? 60 : 0;
        menuHBox.setPrefHeight(targetHeight);
        isCollapsed = !isCollapsed;
    }

    private void toggleWidget(String key, WidgetCreator creator) {
        if(activeWidgets.containsKey(key)) {
            widgetContainer.getChildren().remove(activeWidgets.get(key));
            activeWidgets.remove(key);
        } else {
            VBox widget = creator.create();
            widgetContainer.getChildren().add(widget);
            activeWidgets.put(key, widget);
        }
    }

    // ---------------- Widget létrehozók ----------------

    private VBox createWeatherWidget() {
        VBox box = baseWidget("🌤 Időjárás", "#f1c40f");

        TextField cityInput = new TextField();
        cityInput.setPromptText("Írd be a várost");
        cityInput.setPrefWidth(200);

        Label cityLabel = new Label(); cityLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        Label tempLabel = new Label(); tempLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        Label feelsLikeLabel = new Label(); Label humidityLabel = new Label();
        Label windLabel = new Label(); Label descLabel = new Label();
        descLabel.setFont(Font.font("Segoe UI", 16)); descLabel.setTextFill(Color.DARKSLATEGRAY);

        Runnable updateWeather = () -> {
            String city = cityInput.getText().isEmpty() ? "Budapest" : cityInput.getText();
            Weather weather = WeatherService.getWeather(city);
            if(weather != null) {
                cityLabel.setText(city);
                tempLabel.setText(String.format("🌡 Hőmérséklet: %.1f°C", weather.getTemperature()));
                feelsLikeLabel.setText(String.format("🤗 Hőérzet: %.1f°C", weather.getFeelsLike()));
                humidityLabel.setText(String.format("💧 Páratartalom: %d%%", weather.getHumidity()));
                windLabel.setText(String.format("🌬 Szél: %.1f m/s", weather.getWindSpeed()));
                descLabel.setText("Leírás: " + weather.getDescription());
            } else {
                cityLabel.setText(city); tempLabel.setText("Nem sikerült lekérni az adatokat");
                feelsLikeLabel.setText(""); humidityLabel.setText(""); windLabel.setText(""); descLabel.setText("");
            }
        };

        cityInput.setOnAction(e -> updateWeather.run());
        updateWeather.run();

        box.getChildren().addAll(cityInput, cityLabel, tempLabel, feelsLikeLabel, humidityLabel, windLabel, descLabel);
        return box;
    }

    private VBox createFuelWidget() {
        VBox box = baseWidget("⛽ Üzemanyagárak", "#f1c40f");

        String[] fuelOrder = {"95-ös benzin", "Gázolaj", "100-as benzin"};
        Map<String, String> fuelIcons = Map.of(
                "95-ös benzin", "/drivesync/icons/benzin.png",
                "Gázolaj", "/drivesync/icons/gazolaj.png",
                "100-as benzin", "/drivesync/icons/benzin-100.png"
        );

        Label lastUpdatedLabel = new Label();
        lastUpdatedLabel.setFont(Font.font("Segoe UI", FontPosture.ITALIC, 12));
        lastUpdatedLabel.setStyle("-fx-text-fill: gray;");
        lastUpdatedLabel.setAlignment(Pos.CENTER);

        Runnable updateFuelPrices = () -> {
            Map<String, String[]> prices = FuelService.getFuelPrices();
            if (prices.isEmpty()) {
                box.getChildren().setAll(new Label("Nem sikerült lekérni az adatokat"));
                return;
            }

            HBox fuelRow = new HBox(40); // több tér a dobozok között
            fuelRow.setAlignment(Pos.CENTER);

            for (String fuel : fuelOrder) {
                VBox fuelBox = new VBox(15);
                fuelBox.setAlignment(Pos.CENTER);
                fuelBox.setStyle(
                        "-fx-background-color: #f9f9f9; " +
                                "-fx-padding: 20; " +
                                "-fx-border-color: #cccccc; " +
                                "-fx-border-radius: 14; " +
                                "-fx-background-radius: 14; " +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 4, 0, 0, 1);"
                );

                fuelBox.setPrefWidth(260);  // 🔹 Szélesebb kártya, hogy minden szöveg kiférjen
                fuelBox.setMinWidth(240);   // biztos minimumszélesség
                fuelBox.setMaxWidth(300);   // ne nőjön túl

                ImageView icon = new ImageView(getClass().getResource(fuelIcons.get(fuel)).toExternalForm());
                icon.setFitWidth(50);
                icon.setFitHeight(50);

                Label fuelLabel = new Label(fuel);
                fuelLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
                fuelLabel.setWrapText(true);
                fuelLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
                fuelLabel.setAlignment(Pos.CENTER);

                VBox headerBox = new VBox(8, icon, fuelLabel);
                headerBox.setAlignment(Pos.CENTER);

                String[] fuelPrices = prices.getOrDefault(fuel, new String[]{"-", "-", "-"});
                Label minLabel = new Label("Min: " + fuelPrices[0] );
                Label avgLabel = new Label("Átlag: " + fuelPrices[1] );
                Label maxLabel = new Label("Max: " + fuelPrices[2] );

                for (Label lbl : new Label[]{minLabel, avgLabel, maxLabel}) {
                    lbl.setFont(Font.font("Segoe UI", 15)); // picit nagyobb, olvashatóbb
                    lbl.setAlignment(Pos.CENTER);
                    lbl.setWrapText(false);
                }

                VBox pricesBox = new VBox(6, minLabel, avgLabel, maxLabel);
                pricesBox.setAlignment(Pos.CENTER);

                fuelBox.getChildren().addAll(headerBox, pricesBox);
                fuelRow.getChildren().add(fuelBox);
            }

            lastUpdatedLabel.setText(
                    "Utoljára frissítve: " +
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd. HH:mm"))
            );

            javafx.scene.layout.VBox.setMargin(fuelRow, new javafx.geometry.Insets(15, 0, 8, 0));
            box.getChildren().setAll(fuelRow, lastUpdatedLabel);
            box.setAlignment(Pos.CENTER);
        };

        updateFuelPrices.run();
        Timeline timeline = new Timeline(new KeyFrame(Duration.hours(1), e -> updateFuelPrices.run()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        return box;
    }




    private VBox createCarsWidget() {
        VBox box = baseWidget("🚗 Autók", "#f1c40f");

        Label infoLabel = new Label("Saját autók listája:");
        infoLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));

        VBox carsContainer = new VBox(10);
        carsContainer.setAlignment(Pos.CENTER_LEFT);
        carsContainer.setPrefWidth(380);





        // Háttérszál az autók adatainak lekérésére
        new Thread(() -> {
            List<Map<String, Object>> cars = new ArrayList<>();

            try (Connection conn = drivesync.Adatbázis.Database.getConnection()) {
                String sql = """
                SELECT license, brand, type, vintage, fuel_type, km, color
                FROM cars
                WHERE owner_id = (SELECT id FROM users WHERE username = ?)
                """;
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();

                // 🔹 Itt még háttérszálon olvassuk a ResultSet-et
                while (rs.next()) {
                    Map<String, Object> car = new HashMap<>();
                    car.put("brand", rs.getString("brand"));
                    car.put("type", rs.getString("type"));
                    car.put("license", rs.getString("license"));
                    car.put("vintage", rs.getString("vintage"));
                    car.put("fuel", rs.getString("fuel_type"));
                    car.put("km", rs.getInt("km"));
                    car.put("color", rs.getString("color"));
                    cars.add(car);
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

            // 🔹 UI-frissítés már csak az összegyűjtött adatokból
            javafx.application.Platform.runLater(() -> {
                if (cars.isEmpty()) {
                    Label noCars = new Label("Nincs regisztrált autó.");
                    noCars.setFont(Font.font("Segoe UI", FontPosture.ITALIC, 14));
                    noCars.setTextFill(Color.GRAY);
                    carsContainer.getChildren().add(noCars);
                } else {
                    for (Map<String, Object> car : cars) {
                        VBox carBox = new VBox(4);
                        carBox.setStyle(
                                "-fx-background-color: #f9f9f9; -fx-padding: 12; " +
                                        "-fx-border-radius: 10; -fx-background-radius: 10; " +
                                        "-fx-border-color: #f1c40f; -fx-border-width: 1;"
                        );

                        Label title = new Label(car.get("brand") + " " + car.get("type"));
                        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
                        title.setTextFill(Color.web("#2c3e50"));

                        Label details = new Label(
                                "Rendszám: " + car.get("license") +
                                        "\nÉvjárat: " + car.get("vintage") +
                                        "\nÜzemanyag: " + car.get("fuel") +
                                        "\nKm: " + String.format("%,d km", car.get("km")) +
                                        (car.get("color") != null && !((String) car.get("color")).isEmpty()
                                                ? "\nSzín: " + car.get("color") : "")
                        );
                        details.setFont(Font.font("Segoe UI", 14));
                        details.setTextFill(Color.DARKSLATEGRAY);

                        carBox.getChildren().addAll(title, details);
                        carsContainer.getChildren().add(carBox);
                    }
                }
            });
        }).start();


        box.getChildren().addAll(infoLabel, carsContainer);
        return box;
    }



    private VBox createBudgetWidget() {
        VBox box = baseWidget("💰 Költségvetés", "#f1c40f");

        Label infoLabel = new Label("Kiadások és bevételek összegzése:");
        infoLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));

        Label monthlyLabel = new Label("Havi összesítés: ...");
        monthlyLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        monthlyLabel.setTextFill(Color.web("#2c3e50"));

        Label yearlyLabel = new Label("Éves összesítés: ...");
        yearlyLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        yearlyLabel.setTextFill(Color.web("#2c3e50"));

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Havi költések (Ft)");
        xAxis.setLabel("Hónap");
        yAxis.setLabel("Összeg (Ft)");
        chart.setPrefHeight(200);
        chart.setLegendVisible(false);

        new Thread(() -> {
            try (Connection conn = drivesync.Adatbázis.Database.getConnection()) {
                String sql = "SELECT price, datet FROM expense WHERE owner_id = (SELECT id FROM users WHERE username = ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();

                int yearlySumTemp = 0;
                int[] monthlySumTemp = new int[12];

                while (rs.next()) {
                    int amount = rs.getInt("price");
                    LocalDate date = rs.getDate("datet").toLocalDate();
                    yearlySumTemp += amount;
                    if (date.getYear() == LocalDate.now().getYear()) {
                        monthlySumTemp[date.getMonthValue() - 1] += amount;
                    }
                }

                // final változók a lambdához
                final int yearlySum = yearlySumTemp;
                final int[] monthlySum = monthlySumTemp;

                DecimalFormat df = new DecimalFormat("#,###");
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                String[] months = {"Jan", "Feb", "Már", "Ápr", "Máj", "Jún", "Júl", "Aug", "Szep", "Okt", "Nov", "Dec"};

                for (int i = 0; i < 12; i++) {
                    series.getData().add(new XYChart.Data<>(months[i], monthlySum[i]));
                }

                javafx.application.Platform.runLater(() -> {
                    monthlyLabel.setText("Havi összesítés: " +
                            df.format(monthlySum[LocalDate.now().getMonthValue() - 1]) + " Ft");
                    yearlyLabel.setText("Éves összesítés: " + df.format(yearlySum) + " Ft");
                    chart.getData().add(series);
                });

            } catch (SQLException e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() ->
                        infoLabel.setText("Hiba az adatok betöltésekor."));
            }
        }).start();

        box.getChildren().addAll(infoLabel, monthlyLabel, yearlyLabel, chart);
        return box;
    }


    private VBox createLinksWidget() { VBox box = baseWidget("🔗 Linkek", "#f1c40f"); box.getChildren().add(new Label("Gyakran használt linkek.")); return box; }

    private VBox createNotificationWidgets() {
        VBox container = new VBox(15); container.setPrefWidth(400);
        ServiceDAO dao = new ServiceDAO(); List<ServiceDAO.Service> services = dao.getUpcomingServices();
        if(services.isEmpty()){ Label empty = new Label("Nincs elérhető szerviz információ."); empty.setFont(Font.font("Segoe UI", FontPosture.ITALIC, 14)); empty.setTextFill(Color.GRAY); container.getChildren().add(empty); return container; }

        for(ServiceDAO.Service s: services){
            StringBuilder text = new StringBuilder("Autó: ").append(s.brand).append(" ").append(s.type)
                    .append("\nDátum: ").append(s.serviceDate)
                    .append("\nHelyszín: ").append(s.location)
                    .append(s.notes != null && !s.notes.isEmpty() ? "\nMegjegyzés: "+s.notes : "")
                    .append("\nEmlékeztető: ").append(s.reminder ? "Igen" : "Nem");

            VBox widget = new VBox(8); widget.setPrefWidth(350); widget.setStyle("-fx-background-color: #fff; -fx-padding: 15; -fx-border-radius: 12; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10,0,0,5);");
            Label header = new Label("🔔 Szerviz értesítés"); header.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16)); header.setTextFill(Color.web("#f1c40f"));
            Label serviceLabel = new Label(text.toString()); serviceLabel.setFont(Font.font("Segoe UI", 14)); serviceLabel.setTextFill(Color.DARKSLATEGRAY); serviceLabel.setWrapText(true);
            widget.getChildren().addAll(header, serviceLabel);
            container.getChildren().add(widget);
        }
        return container;
    }

    private VBox baseWidget(String title, String color) {
        VBox box = new VBox(8); box.setPrefWidth(400);
        box.setStyle("-fx-background-color: white; -fx-border-radius: 12; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15),10,0,0,5); -fx-padding: 20;");
        box.getChildren().add(baseWidgetHeader(title));
        box.setOnMouseEntered(e -> box.setStyle("-fx-background-color: #f8fbff; -fx-border-radius: 12; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25),15,0,0,5); -fx-padding: 20;"));
        box.setOnMouseExited(e -> box.setStyle("-fx-background-color: white; -fx-border-radius: 12; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15),10,0,0,5); -fx-padding: 20;"));
        return box;
    }

    private Label baseWidgetHeader(String title) { Label header = new Label(title); header.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20)); header.setTextFill(Color.web("#f1c40f")); return header; }

    private void openServicePage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/drivesync/SajátAutók/SajatAutok.fxml"));
            Parent servicePage = loader.load();

            // Tartalom frissítése a középső részben
            mainLayout.setCenter(servicePage);

            // (opcionális) adatok átadása a SajátAutók controllernek
            Object controller = loader.getController();
            if (controller instanceof drivesync.SajátAutók.SajatAutokController sc) {
                sc.setUsername(username);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FunctionalInterface
    private interface WidgetCreator { VBox create(); }
}
