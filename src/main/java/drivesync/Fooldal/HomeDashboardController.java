package drivesync.Fooldal;

import drivesync.Adatbazis.ServiceDAO;
import drivesync.AI.AIDiagnosticsService;
import drivesync.FuelService.FuelService;
import drivesync.Idojaras.WeatherService;
import drivesync.Idojaras.WeatherService.Weather;
import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import javafx.concurrent.Task;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class HomeDashboardController {

    @FXML private FlowPane widgetContainer;
    @FXML private HBox menuHBox;
    @FXML private Button weatherBtn, fuelBtn, carsBtn, budgetBtn, linksBtn, notificationsBtn;

    // VÁLTOZÁS 1: AI DIAGNOSZTIKAI GOMB ÉS WIDGET ELEMEK
    @FXML private Button diagnosticsBtn; // FXML-ben deklarált gomb
    private TextField symptomField; // Dynamikusan létrehozott bemenet
    private TextArea diagnosisResultArea; // Dynamikusan létrehozott kimenet
    private Button diagnoseButton; // Dynamikusan létrehozott indító gomb

    private final AIDiagnosticsService aiService = new AIDiagnosticsService(); // AI szolgáltatás

    @FXML
    protected BorderPane mainLayout;

    private boolean isCollapsed = false;
    private final Map<String, VBox> activeWidgets = new HashMap<>();
    private String username;
    private boolean darkTheme = false;

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    @FXML
    public void initialize() {
        // Tooltip-ek
        weatherBtn.setTooltip(new Tooltip("Időjárás"));
        fuelBtn.setTooltip(new Tooltip("Üzemanyag"));
        carsBtn.setTooltip(new Tooltip("Autók"));
        budgetBtn.setTooltip(new Tooltip("Költségvetés"));
        linksBtn.setTooltip(new Tooltip("Linkek"));
        notificationsBtn.setTooltip(new Tooltip("Értesítések"));
        diagnosticsBtn.setTooltip(new Tooltip("AI Diagnosztika"));

        // Ikonok beállítása
        setButtonGraphic(weatherBtn, "/drivesync/icons/weather.png");
        setButtonGraphic(fuelBtn, "/drivesync/icons/fuel.png");
        setButtonGraphic(carsBtn, "/drivesync/icons/car.png");
        setButtonGraphic(budgetBtn, "/drivesync/icons/budget.png");
        setButtonGraphic(linksBtn, "/drivesync/icons/links.png");
        setButtonGraphic(notificationsBtn, "/drivesync/icons/notification.png");
        setButtonGraphic(diagnosticsBtn, "/drivesync/icons/ai.png"); // Feltételezve, hogy van egy 'ai.png' ikon

        // Hover effekt
        addHover(weatherBtn);
        addHover(fuelBtn);
        addHover(carsBtn);
        addHover(budgetBtn);
        addHover(linksBtn);
        addHover(notificationsBtn);
        addHover(diagnosticsBtn);


        // Widget-ek gombjai
        weatherBtn.setOnAction(e -> toggleWidget("weather", this::createWeatherWidget));
        fuelBtn.setOnAction(e -> toggleWidget("fuel", this::createFuelWidget));
        carsBtn.setOnAction(e -> toggleWidget("cars", this::createCarsWidget));
        budgetBtn.setOnAction(e -> toggleWidget("budget", this::createBudgetWidget));
        linksBtn.setOnAction(e -> toggleWidget("links", this::createLinksWidget));
        notificationsBtn.setOnAction(e -> toggleWidget("notifications", this::createNotificationWidgets));
        // VÁLTOZÁS 4: AI Diagnosztika hozzáadása
        diagnosticsBtn.setOnAction(e -> toggleWidget("diagnostics", this::createAIDiagnosticsWidget));

        mainLayout.getStyleClass().add("theme-light");
    }

    @FXML
    protected void toggleTheme() {
        darkTheme = !darkTheme;
        var root = mainLayout.getScene().getRoot();
        root.getStyleClass().removeAll("theme-light", "theme-dark");
        root.getStyleClass().add(darkTheme ? "theme-dark" : "theme-light");
    }

    private void setButtonGraphic(Button btn, String resourcePath) {
        ImageView iv = new ImageView(new Image(getClass().getResourceAsStream(resourcePath)));
        iv.setFitWidth(32);
        iv.setFitHeight(32);
        iv.setPreserveRatio(true);
        btn.setGraphic(iv);
    }

    private void addHover(Button btn) {
        ScaleTransition grow = new ScaleTransition(Duration.millis(150), btn);
        ScaleTransition shrink = new ScaleTransition(Duration.millis(150), btn);
        btn.setOnMouseEntered(e -> {
            grow.setToX(1.1);
            grow.setToY(1.1);
            grow.playFromStart();
        });
        btn.setOnMouseExited(e -> {
            shrink.setToX(1);
            shrink.setToY(1);
            shrink.playFromStart();
        });
    }

    private void toggleWidget(String key, WidgetCreator creator) {
        if (activeWidgets.containsKey(key)) {
            widgetContainer.getChildren().remove(activeWidgets.get(key));
            activeWidgets.remove(key);
        } else {
            VBox widget = creator.create();
            widgetContainer.getChildren().add(widget);
            activeWidgets.put(key, widget);
        }
    }

    // ---------------- Widget létrehozók ----------------



    // VÁLTOZÁS 6: AI DIAGNOSZTIKAI LOGIKA
    // drivesync.Főoldal.HomeDashboardController.java

    @FXML
    private void handleAIDiagnosis() {
        String symptom = symptomField.getText().trim();
        if (symptom.isEmpty()) {
            diagnosisResultArea.setText("Kérlek írj be egy tünetet a diagnózis megkezdéséhez.");
            return;
        }

        // VÁLTOZTATÁS: Dinamikus autóadatok lekérése a felhasználó első autójáról
        String[] carDetails = getPrimaryCarDetails();
        String carBrand = carDetails[0];
        String carType = carDetails[1];

        if (carBrand.equals("Ismeretlen")) {
            diagnosisResultArea.setText("Hiba: Nem találtunk autót a felhasználódhoz a diagnózishoz. Kérlek rögzíts egy autót!");
            return;
        }
        // -----------------------------------------------------------------

        diagnoseButton.setDisable(true);
        diagnosisResultArea.setText("Diagnózis készítése... Kérlek várj.");

        Task<String> diagnosisTask = new Task<>() {
            @Override
            protected String call() throws Exception {
                // Hálózati hívás a háttérszálon
                return aiService.getDiagnosis(carBrand, carType, symptom);
            }

            @Override
            protected void succeeded() {
                // Visszatérés a JavaFX szálra
                diagnosisResultArea.setText(getValue());
                diagnoseButton.setDisable(false);
            }

            @Override
            protected void failed() {
                // Hiba kezelése (pl. API kulcs hiba, timeout)
                diagnosisResultArea.setText("Hiba a diagnózis közben: " + getException().getMessage() + "\nEllenőrizd az API kulcsot és a hálózati kapcsolatot.");
                diagnoseButton.setDisable(false);
                getException().printStackTrace();
            }
        };

        // A task elindítása egy új szálon
        new Thread(diagnosisTask).start();
    }

    /**
     * ÚJ SEGÉD METÓDUS: Lekéri a felhasználó első rögzített autójának adatait.
     * @return String[Márka, Típus]. Ha nincs autó, {"Ismeretlen", "Ismeretlen"}.
     */
    private String[] getPrimaryCarDetails() {
        if (username == null || username.isEmpty()) {
            return new String[]{"Ismeretlen", "Ismeretlen"};
        }
        String sql = """
        SELECT c.brand, c.type 
        FROM cars c 
        JOIN users u ON c.owner_id = u.id
        WHERE u.username = ?
        LIMIT 1
    """;
        try (Connection conn = drivesync.Adatbazis.Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new String[]{rs.getString("brand"), rs.getString("type")};
            }
        } catch (SQLException e) {
            System.err.println("Hiba az első autó adatainak lekérdezésekor: " + e.getMessage());
        }
        return new String[]{"Ismeretlen", "Ismeretlen"};
    }


    protected VBox createWeatherWidget() {
        VBox box = baseWidget("🌤 Időjárás", "#f1c40f");
        box.getStyleClass().add("widget-card"); // Alap widget stílus

        // Város mező
        TextField cityInput = new TextField();
        cityInput.setPromptText("Írd be a várost");
        cityInput.getStyleClass().add("text-input");
        cityInput.setPrefWidth(200);

        // Widget feliratok
        Label cityLabel = new Label();
        cityLabel.getStyleClass().addAll("card-title"); // sárga főcím

        Label tempLabel = new Label();
        tempLabel.getStyleClass().addAll("card-title"); // sárga nagyobb szöveg

        Label feelsLikeLabel = new Label();
        feelsLikeLabel.getStyleClass().add("card-subtitle"); // világosabb felirat

        Label humidityLabel = new Label();
        humidityLabel.getStyleClass().add("card-subtitle");

        Label windLabel = new Label();
        windLabel.getStyleClass().add("card-subtitle");

        Label descLabel = new Label();
        descLabel.getStyleClass().add("card-subtitle");

        // Frissítő logika
        Runnable updateWeather = () -> {
            String city = cityInput.getText().isEmpty() ? "Budapest" : cityInput.getText();
            Weather weather = WeatherService.getWeather(city);
            if (weather != null) {
                cityLabel.setText(city);
                tempLabel.setText(String.format("🌡 Hőmérséklet: %.1f°C", weather.getTemperature()));
                feelsLikeLabel.setText(String.format("🤗 Hőérzet: %.1f°C", weather.getFeelsLike()));
                humidityLabel.setText(String.format("💧 Páratartalom: %d%%", weather.getHumidity()));
                windLabel.setText(String.format("🌬 Szél: %.1f m/s", weather.getWindSpeed()));
                descLabel.setText("Leírás: " + weather.getDescription());
            } else {
                cityLabel.setText(city);
                tempLabel.setText("Nem sikerült lekérni az adatokat");
                feelsLikeLabel.setText("");
                humidityLabel.setText("");
                windLabel.setText("");
                descLabel.setText("");
            }
        };

        cityInput.setOnAction(e -> updateWeather.run());
        updateWeather.run();

        box.getChildren().addAll(
                cityInput,
                cityLabel,
                tempLabel,
                feelsLikeLabel,
                humidityLabel,
                windLabel,
                descLabel
        );

        // Középre igazítás
        box.setAlignment(Pos.CENTER);

        return box;
    }



    public VBox createFuelWidget() {
        VBox box = baseWidget("⛽ Üzemanyagárak", "#f1c40f");

        // Alap CSS osztály a widgethez
        box.getStyleClass().add("widget-card");

        String[] fuelOrder = {"95-ös benzin", "Gázolaj", "100-as benzin"};
        Map<String, String> fuelIcons = Map.of(
                "95-ös benzin", "/drivesync/icons/benzin.png",
                "Gázolaj", "/drivesync/icons/gazolaj.png",
                "100-as benzin", "/drivesync/icons/benzin-100.png"
        );

        Label lastUpdatedLabel = new Label();
        lastUpdatedLabel.getStyleClass().addAll("widget-text", "widget-small");
        lastUpdatedLabel.setAlignment(Pos.CENTER);

        Runnable updateFuelPrices = () -> {
            Map<String, String[]> prices = FuelService.getFuelPrices();
            if (prices.isEmpty()) {
                Label errorLabel = new Label("Nem sikerült lekérni az adatokat");
                errorLabel.getStyleClass().add("widget-text");
                box.getChildren().setAll(errorLabel);
                return;
            }

            HBox fuelRow = new HBox(50); // nagyobb spacing
            fuelRow.setAlignment(Pos.CENTER);

            for (String fuel : fuelOrder) {
                VBox fuelBox = new VBox(20); // nagyobb spacing
                fuelBox.setAlignment(Pos.CENTER);
                fuelBox.getStyleClass().add("widget-card"); // CSS osztály
                fuelBox.setPrefWidth(300);
                fuelBox.setMinWidth(240);
                fuelBox.setMaxWidth(320);

                URL iconUrl = getClass().getResource(fuelIcons.get(fuel));
                ImageView icon = new ImageView(iconUrl != null ? iconUrl.toExternalForm() : null);
                icon.setFitWidth(70);
                icon.setFitHeight(70);


                Label fuelLabel = new Label(fuel);
                fuelLabel.getStyleClass().add("widget-header");
                fuelLabel.setWrapText(true);
                fuelLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
                fuelLabel.setAlignment(Pos.CENTER);

                VBox headerBox = new VBox(10, icon, fuelLabel);
                headerBox.setAlignment(Pos.CENTER);

                String[] fuelPrices = prices.getOrDefault(fuel, new String[]{"-", "-", "-"});
                Label minLabel = new Label("Min: " + fuelPrices[0]);
                Label avgLabel = new Label("Átlag: " + fuelPrices[1]);
                Label maxLabel = new Label("Max: " + fuelPrices[2]);

                for (Label lbl : new Label[]{minLabel, avgLabel, maxLabel}) {
                    lbl.getStyleClass().add("widget-text"); // CSS osztály
                    lbl.setAlignment(Pos.CENTER);
                    lbl.setWrapText(false);
                }

                VBox pricesBox = new VBox(8, minLabel, avgLabel, maxLabel);
                pricesBox.setAlignment(Pos.CENTER);

                fuelBox.getChildren().addAll(headerBox, pricesBox);
                fuelRow.getChildren().add(fuelBox);
            }

            lastUpdatedLabel.setText(
                    "Utoljára frissítve: " +
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd. HH:mm"))
            );

            VBox.setMargin(fuelRow, new Insets(20, 0, 15, 0));
            box.getChildren().setAll(fuelRow, lastUpdatedLabel);
            box.setAlignment(Pos.CENTER);
        };

        updateFuelPrices.run();

        Timeline timeline = new Timeline(new KeyFrame(Duration.hours(1), e -> updateFuelPrices.run()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        return box;
    }




    protected VBox createCarsWidget() {
        VBox box = baseWidget("🚗 Autók", "#f1c40f");

        // CSS osztály a theme-kompatibilitáshoz
        box.getStyleClass().add("widget-card");

        Label infoLabel = new Label("Saját autók listája:");
        infoLabel.getStyleClass().add("widget-text");

        VBox carsContainer = new VBox(10);
        carsContainer.setPrefWidth(380);

        new Thread(() -> {
            List<Map<String, Object>> cars = new ArrayList<>();
            try (Connection conn = drivesync.Adatbazis.Database.getConnection()) {
                String sql = "SELECT license, brand, type, vintage, fuel_type, km, color FROM cars WHERE owner_id = (SELECT id FROM users WHERE username = ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
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

            javafx.application.Platform.runLater(() -> {
                if (cars.isEmpty()) {
                    Label noCars = new Label("Nincs regisztrált autó.");
                    noCars.getStyleClass().addAll("widget-text", "widget-subtitle");
                    carsContainer.getChildren().add(noCars);
                } else {
                    for (Map<String, Object> car : cars) {
                        VBox carBox = new VBox(4);
                        carBox.getStyleClass().add("widget-card");

                        Label title = new Label(car.get("brand") + " " + car.get("type"));
                        title.getStyleClass().addAll("widget-text", "widget-title");

                        Label details = new Label(
                                "Rendszám: " + car.get("license") +
                                        "\nÉvjárat: " + car.get("vintage") +
                                        "\nÜzemanyag: " + car.get("fuel") +
                                        "\nKm: " + String.format("%,d km", car.get("km")) +
                                        (car.get("color") != null && !((String) car.get("color")).isEmpty() ? "\nSzín: " + car.get("color") : "")
                        );
                        details.getStyleClass().addAll("widget-text", "widget-subtitle");
                        details.setWrapText(true);

                        carBox.getChildren().addAll(title, details);
                        carsContainer.getChildren().add(carBox);
                    }
                }
            });
        }).start();

        box.getChildren().addAll(infoLabel, carsContainer);
        return box;
    }


    protected VBox createBudgetWidget() {
        // Widget alap
        VBox box = baseWidget("💰 Költségvetés", "#f1c40f");

        // CSS osztály a theme-kompatibilitáshoz
        box.getStyleClass().add("widget-card");

        // Információs szöveg
        Label infoLabel = new Label("Kiadások és bevételek összegzése:");
        infoLabel.getStyleClass().add("widget-text");

        // Havi és éves összesítés
        Label monthlyLabel = new Label("Havi összesítés: ...");
        monthlyLabel.getStyleClass().addAll("widget-text", "widget-title");

        Label yearlyLabel = new Label("Éves összesítés: ...");
        yearlyLabel.getStyleClass().addAll("widget-text", "widget-title");

        // Diagram
        // Diagram
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);

        chart.setTitle("Havi költések (Ft)");
        chart.setLegendVisible(false);
        chart.setPrefHeight(250); // Kicsit magasabbra vettem, hogy kiférjenek a döntött feliratok

        // --- JAVÍTÁS: Feliratok elforgatása ---
        xAxis.setLabel("Hónap");
        xAxis.setTickLabelRotation(45); // 45 fokkal elforgatja a hónapok nevét, így nem érnek össze
        // Vagy használhatsz 90-et is, ha függőlegesen akarod őket.

        yAxis.setLabel("Összeg (Ft)");

        // --- JAVÍTÁS: Animáció kikapcsolása (stabilabb megjelenés) ---
        chart.setAnimated(false);

        // --- JAVÍTÁS: Minimális szélesség kényszerítése ---
        // Ha a widget nagyon összenyomódik, ez biztosítja, hogy a grafikon olvasható maradjon
        chart.setMinWidth(300);

        // Adatok lekérése külön szálon
        new Thread(() -> {
            try (Connection conn = drivesync.Adatbazis.Database.getConnection()) {
                String sql = "SELECT price, datet FROM expense WHERE owner_id=(SELECT id FROM users WHERE username=?)";
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

                final int yearlySum = yearlySumTemp;
                final int[] monthlySum = monthlySumTemp;

                DecimalFormat df = new DecimalFormat("#,###");
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                String[] months = {"Jan", "Feb", "Már", "Ápr", "Máj", "Jún", "Júl", "Aug", "Szep", "Okt", "Nov", "Dec"};
                for (int i = 0; i < 12; i++) {
                    series.getData().add(new XYChart.Data<>(months[i], monthlySum[i]));
                }

                javafx.application.Platform.runLater(() -> {
                    monthlyLabel.setText("Havi összesítés: " + df.format(monthlySum[LocalDate.now().getMonthValue() - 1]) + " Ft");
                    yearlyLabel.setText("Éves összesítés: " + df.format(yearlySum) + " Ft");
                    chart.getData().add(series);
                });

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).start();

        box.getChildren().addAll(infoLabel, monthlyLabel, yearlyLabel, chart);
        return box;
    }


    protected VBox createLinksWidget() {
        VBox box = baseWidget("🔗 Linkek", "#f1c40f");

        // CSS osztály a theme-kompatibilitáshoz
        box.getStyleClass().add("widget-card");

        Label label = new Label("Gyakran használt linkek.");
        label.getStyleClass().add("widget-text");

        box.getChildren().add(label);
        return box;
    }


    // drivesync.Főoldal.HomeDashboardController.java

    protected VBox createNotificationWidgets() {
        // Base widget létrehozása a "widget-card" stílussal
        VBox box = baseWidget("🔔 Szerviz értesítések", "#f1c40f");
        box.getStyleClass().add("widget-card");

        ServiceDAO dao = new ServiceDAO();
        // Változtatás: Hívjuk az új metódust a felhasználónévvel
        List<ServiceDAO.Service> services = dao.getUpcomingServicesForUser(this.username); // <-- JAVÍTVA

        if (services.isEmpty()) {
            Label empty = new Label("Nincs elérhető szerviz információ.");
            empty.getStyleClass().addAll("widget-text", "widget-empty");
            box.getChildren().add(empty);
            return box;
        }

        // Minden szolgáltatás külön Label-ként, egyetlen VBox-ban
        for (ServiceDAO.Service s : services) {
            StringBuilder text = new StringBuilder("Autó ID: ").append(s.carId) // IDEIGLENES: Ha nincs brand/type a Service modellben
                    .append("\nDátum: ").append(s.serviceDate)
                    .append("\nHelyszín: ").append(s.location)
                    .append(s.notes != null && !s.notes.isEmpty() ? "\nMegjegyzés: " + s.notes : "")
                    .append("\nEmlékeztető: ").append(s.reminder ? "Igen" : "Nem");

            Label serviceLabel = new Label(text.toString());
            serviceLabel.getStyleClass().add("widget-text");
            serviceLabel.setWrapText(true);

            box.getChildren().add(serviceLabel);
        }

        return box;
    }

    // VÁLTOZÁS 5: AI DIAGNOSZTIKAI WIDGET LÉTREHOZÁSA
    protected VBox createAIDiagnosticsWidget() {
        // 1. Felhasználói felület elemek inicializálása
        symptomField = new TextField();
        symptomField.setPromptText("Pl.: Rángat a motor alacsony fordulaton...");

        diagnosisResultArea = new TextArea();
        diagnosisResultArea.getStyleClass().add("ai-output");
        diagnosisResultArea.setEditable(false);
        diagnosisResultArea.setPrefRowCount(10);
        diagnosisResultArea.setText("Írd le a tünetet, majd kattints a Diagnózis indítása gombra.");
        diagnosisResultArea.setWrapText(true);

        diagnoseButton = new Button("Diagnózis indítása");
        diagnoseButton.getStyleClass().add("btn-primary");
        // Az eseménykezelő hozzárendelése:
        diagnoseButton.setOnAction(event -> handleAIDiagnosis());

        Label title = new Label("AI Autódiagnosztika");
        title.getStyleClass().add("card-title");

        Label description = new Label("Kérlek írj be egy részletes tünetet. Az AI javaslatai tájékoztató jellegűek!");
        description.getStyleClass().add("text-muted");

        // 2. Konténer (Widget) létrehozása
        VBox aiWidget = new VBox(10);
        aiWidget.getStyleClass().add("widget-card");
        aiWidget.setPrefWidth(600);
        aiWidget.getChildren().addAll(
                title,
                description,
                symptomField,
                diagnoseButton,
                diagnosisResultArea
        );

        return aiWidget;
    }





    private VBox baseWidget(String title, String color) {
        VBox box = new VBox(8);
        box.setPrefWidth(400);
        box.getStyleClass().add("widget-card"); // inline style helyett CSS osztály
        box.getChildren().add(baseWidgetHeader(title));
        return box;
    }


    private Label baseWidgetHeader(String title) {
        Label header = new Label(title);
        header.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        header.setTextFill(Color.web("#f1c40f"));
        return header;
    }

    // ------------------- itt kell a WidgetCreator -------------------
    @FunctionalInterface
    private interface WidgetCreator {
        VBox create();
    }
    private ServiceDAO serviceDAO = new ServiceDAO();

    public void setServiceDAO(ServiceDAO serviceDAO) {
        this.serviceDAO = serviceDAO;
    }

}