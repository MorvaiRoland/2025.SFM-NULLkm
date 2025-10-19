package drivesync.Főoldal;

import com.itextpdf.layout.properties.TextAlignment;
import drivesync.Időjárás.WeatherService;
import drivesync.Időjárás.WeatherService.Weather;
import drivesync.FuelService.FuelService;
import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import drivesync.Adatbázis.ServiceDAO;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeDashboardController {

    @FXML private FlowPane widgetContainer;
    @FXML private VBox menuVBox, buttonsVBox;
    @FXML private Button toggleMenuBtn, weatherBtn, fuelBtn, carsBtn, budgetBtn, linksBtn;
    @FXML private Button notificationsBtn;


    private boolean isCollapsed = false;
    private final Map<String, VBox> activeWidgets = new HashMap<>();
    private String username; // Felhasználónév

    // ---------------- Publikus setUsername ----------------
    public void setUsername(String username) { this.username = username; }
    public String getUsername() { return username; }

    // ---------------- Inicializálás ----------------
    @FXML
    public void initialize() {
        toggleMenuBtn.setOnAction(e -> toggleMenu());

        addHoverAnimation(weatherBtn);
        addHoverAnimation(fuelBtn);
        addHoverAnimation(carsBtn);
        addHoverAnimation(budgetBtn);
        addHoverAnimation(linksBtn);

        weatherBtn.setOnAction(e -> toggleWidget("weather", this::createWeatherWidget));
        fuelBtn.setOnAction(e -> toggleWidget("fuel", this::createFuelWidget));
        carsBtn.setOnAction(e -> toggleWidget("cars", this::createCarsWidget));
        budgetBtn.setOnAction(e -> toggleWidget("budget", this::createBudgetWidget));
        linksBtn.setOnAction(e -> toggleWidget("links", this::createLinksWidget));
        notificationsBtn.setOnAction(e -> toggleWidget("notifications", this::createNotificationWidgets));
        addHoverAnimation(notificationsBtn);

    }

    // ---------------- Hover animáció ----------------
    private void addHoverAnimation(Button btn) {
        ScaleTransition grow = new ScaleTransition(Duration.millis(150), btn);
        ScaleTransition shrink = new ScaleTransition(Duration.millis(150), btn);
        btn.setOnMouseEntered(e -> { grow.setToX(1.05); grow.setToY(1.05); grow.playFromStart(); });
        btn.setOnMouseExited(e -> { shrink.setToX(1); shrink.setToY(1); shrink.playFromStart(); });
    }

    // ---------------- Menü összecsukás ----------------
    private void toggleMenu() {
        if(isCollapsed) {
            menuVBox.setPrefWidth(220);
            buttonsVBox.setVisible(true);
            toggleMenuBtn.setText("Widgetek ⏴");
            isCollapsed = false;
        } else {
            menuVBox.setPrefWidth(60);
            buttonsVBox.setVisible(false);
            toggleMenuBtn.setText("⏵");
            isCollapsed = true;
        }
    }

    // ---------------- Widget toggle ----------------
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

        Label cityLabel = new Label();
        cityLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        Label tempLabel = new Label();
        tempLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        Label feelsLikeLabel = new Label();
        Label humidityLabel = new Label();
        Label windLabel = new Label();
        Label descLabel = new Label();
        descLabel.setFont(Font.font("Segoe UI", 16));
        descLabel.setTextFill(Color.DARKSLATEGRAY);

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
                cityLabel.setText(city);
                tempLabel.setText("Nem sikerült lekérni az adatokat");
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
                box.getChildren().clear();
                box.getChildren().add(new Label("Nem sikerült lekérni az adatokat"));
                return;
            }

            HBox fuelRow = new HBox(20);
            fuelRow.setAlignment(Pos.CENTER);

            for (String fuel : fuelOrder) {
                VBox fuelBox = new VBox(10);
                fuelBox.setStyle("-fx-background-color: #f8f8f8; -fx-padding: 15; -fx-border-radius: 12; -fx-background-radius: 12;");
                fuelBox.setPrefWidth(150);
                fuelBox.setMinWidth(150);
                fuelBox.setAlignment(Pos.CENTER);

                // Ikon
                ImageView icon = new ImageView(getClass().getResource(fuelIcons.get(fuel)).toExternalForm());
                icon.setFitWidth(48);
                icon.setFitHeight(48);

                // Üzemanyag név
                Label fuelLabel = new Label(fuel);
                fuelLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
                fuelLabel.setWrapText(true);
                // fully-qualified név, így nincs importütközés
                fuelLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
                fuelLabel.setAlignment(Pos.CENTER);

                VBox headerBox = new VBox(5, icon, fuelLabel);
                headerBox.setAlignment(Pos.CENTER);

                // Ár sorok
                String[] fuelPrices = prices.getOrDefault(fuel, new String[]{"-", "-", "-"});
                Label minLabel = new Label("Min: " + fuelPrices[0]);
                Label avgLabel = new Label("Átlag: " + fuelPrices[1]);
                Label maxLabel = new Label("Max: " + fuelPrices[2]);
                minLabel.setFont(Font.font("Segoe UI", 14));
                avgLabel.setFont(Font.font("Segoe UI", 14));
                maxLabel.setFont(Font.font("Segoe UI", 14));

                VBox pricesBox = new VBox(4, minLabel, avgLabel, maxLabel);
                pricesBox.setAlignment(Pos.CENTER);

                fuelBox.getChildren().addAll(headerBox, pricesBox);
                fuelRow.getChildren().add(fuelBox);
            }

            // Frissítés idejének beállítása
            String formattedTime = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy.MM.dd. HH:mm"));
            lastUpdatedLabel.setText("Utoljára frissítve: " + formattedTime);

            // Frissített elrendezés megjelenítése
            box.getChildren().clear();
            box.getChildren().addAll(fuelRow, lastUpdatedLabel);
        };

        updateFuelPrices.run();

        Timeline timeline = new Timeline(new KeyFrame(Duration.hours(1), e -> updateFuelPrices.run()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        return box;
    }


    private VBox createCarsWidget() {
        VBox box = baseWidget("🚗 Autók", "#f1c40f");
        box.getChildren().add(new Label("Saját autók listája és statisztikák jelennek meg itt."));
        return box;
    }

    private VBox createBudgetWidget() {
        VBox box = baseWidget("💰 Költségvetés", "#f1c40f");
        box.getChildren().add(new Label("Kiadások és bevételek összegzése."));
        return box;
    }

    private VBox createLinksWidget() {
        VBox box = baseWidget("🔗 Linkek", "#f1c40f");
        box.getChildren().add(new Label("Gyakran használt hivatkozások."));
        return box;
    }

    private VBox createNotificationWidgets() {
        VBox container = new VBox(15); // távolság a kártyák között
        container.setPrefWidth(400);

        ServiceDAO dao = new ServiceDAO();
        List<ServiceDAO.Service> services = dao.getUpcomingServices();

        if (services.isEmpty()) {
            Label emptyLabel = new Label("Nincs elérhető szerviz információ.");
            emptyLabel.setFont(Font.font("Segoe UI", FontPosture.ITALIC, 14));
            emptyLabel.setTextFill(Color.GRAY);
            container.getChildren().add(emptyLabel);
            return container;
        }

        for (ServiceDAO.Service s : services) {
            // Szöveg összeállítása: márka + típus
            StringBuilder textBuilder = new StringBuilder();
            textBuilder.append("Autó: ").append(s.brand).append(" ").append(s.type).append("\n");
            textBuilder.append("Dátum: ").append(s.serviceDate).append("\n");
            textBuilder.append("Helyszín: ").append(s.location);
            if (s.notes != null && !s.notes.isEmpty()) {
                textBuilder.append("\nMegjegyzés: ").append(s.notes);
            }
            textBuilder.append("\nEmlékeztető: ").append(s.reminder ? "Igen" : "Nem");

            VBox widget = new VBox(8);
            widget.setPrefWidth(350);
            widget.setStyle("-fx-background-color: #ffffff; -fx-padding: 15; -fx-border-radius: 12; -fx-background-radius: 12;"
                    + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 5);");

            // Fejléc
            Label header = new Label("🔔 Szerviz értesítés");
            header.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
            header.setTextFill(Color.web("#f1c40f"));

            // Részletek
            Label serviceLabel = new Label(textBuilder.toString());
            serviceLabel.setFont(Font.font("Segoe UI", 14));
            serviceLabel.setTextFill(Color.DARKSLATEGRAY);
            serviceLabel.setWrapText(true);

            widget.getChildren().addAll(header, serviceLabel);
            container.getChildren().add(widget);
        }

        return container;
    }



    // ---------------- Widget stílus ----------------
    private VBox baseWidget(String title, String color) {
        VBox box = new VBox(8);
        box.setPrefWidth(400); // nagyobb kártya
        box.setStyle("-fx-background-color: white; -fx-border-radius: 12; -fx-background-radius: 12;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 5); -fx-padding: 20;");
        box.getChildren().add(baseWidgetHeader(title));

        box.setOnMouseEntered(e -> box.setStyle("-fx-background-color: #f8fbff; -fx-border-radius: 12;"
                + "-fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 15, 0, 0, 5); -fx-padding: 20;"));
        box.setOnMouseExited(e -> box.setStyle("-fx-background-color: white; -fx-border-radius: 12;"
                + "-fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 5); -fx-padding: 20;"));

        return box;
    }

    private Label baseWidgetHeader(String title) {
        Label header = new Label(title);
        header.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        header.setTextFill(Color.web("#f1c40f"));
        return header;
    }

    @FunctionalInterface
    private interface WidgetCreator { VBox create(); }
}
