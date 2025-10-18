package drivesync.Főoldal;

import drivesync.Időjárás.WeatherService;
import drivesync.Időjárás.WeatherService.Weather;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;

public class HomeDashboardController {

    @FXML private FlowPane widgetContainer;
    @FXML private VBox menuVBox, buttonsVBox;
    @FXML private Button toggleMenuBtn, weatherBtn, carsBtn, budgetBtn, linksBtn;

    private boolean isCollapsed = false;
    private final Map<String, VBox> activeWidgets = new HashMap<>();
    private String username;

    // ---------------- Felhasználónév ----------------
    public void setUsername(String username) { this.username = username; }
    public String getUsername() { return username; }

    // ---------------- Inicializálás ----------------
    @FXML
    public void initialize() {
        // Menü összecsukás gomb
        toggleMenuBtn.setOnAction(e -> toggleMenu());

        // Hover animáció minden gombra
        addHoverAnimation(weatherBtn);
        addHoverAnimation(carsBtn);
        addHoverAnimation(budgetBtn);
        addHoverAnimation(linksBtn);

        // Gomb események
        weatherBtn.setOnAction(e -> toggleWidget("weather", this::createWeatherWidget));
        carsBtn.setOnAction(e -> toggleWidget("cars", this::createCarsWidget));
        budgetBtn.setOnAction(e -> toggleWidget("budget", this::createBudgetWidget));
        linksBtn.setOnAction(e -> toggleWidget("links", this::createLinksWidget));
    }

    // ---------------- Hover animáció ----------------
    private void addHoverAnimation(Button btn) {
        ScaleTransition grow = new ScaleTransition(Duration.millis(150), btn);
        ScaleTransition shrink = new ScaleTransition(Duration.millis(150), btn);

        btn.setOnMouseEntered(e -> {
            grow.setToX(1.05);
            grow.setToY(1.05);
            grow.playFromStart();
        });
        btn.setOnMouseExited(e -> {
            shrink.setToX(1);
            shrink.setToY(1);
            shrink.playFromStart();
        });
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

        // Város beírása
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
                feelsLikeLabel.setText("");
                humidityLabel.setText("");
                windLabel.setText("");
                descLabel.setText("");
            }
        };

        // Frissítés amikor várost írunk be
        cityInput.setOnAction(e -> updateWeather.run());
        updateWeather.run();

        box.getChildren().addAll(cityInput, cityLabel, tempLabel, feelsLikeLabel, humidityLabel, windLabel, descLabel);
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

    // ---------------- Widget stílus ----------------
    private VBox baseWidget(String title, String color) {
        VBox box = new VBox(8);
        box.setPrefWidth(300);
        box.setStyle("-fx-background-color: white; -fx-border-radius: 12; -fx-background-radius: 12;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 5); -fx-padding: 15;");
        Label header = new Label(title);
        header.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        header.setTextFill(Color.web(color));
        box.getChildren().add(header);

        box.setOnMouseEntered(e -> box.setStyle("-fx-background-color: #f8fbff; -fx-border-radius: 12;"
                + "-fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 15, 0, 0, 5); -fx-padding: 15;"));
        box.setOnMouseExited(e -> box.setStyle("-fx-background-color: white; -fx-border-radius: 12;"
                + "-fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 5); -fx-padding: 15;"));

        return box;
    }

    @FunctionalInterface
    private interface WidgetCreator { VBox create(); }
}
