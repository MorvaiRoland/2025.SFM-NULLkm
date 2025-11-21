package drivesync.Időjárás;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherService {

    private static final String API_KEY = "20416dd29fc013ae95f192f613128490";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather?q=%s&units=metric&appid=%s";

    public static Weather getWeather(String city) {
        try {
            String urlString = String.format(BASE_URL, city, API_KEY);
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            if (conn.getResponseCode() != 200) {
                System.out.println("Hiba az időjárás lekérésénél: " + conn.getResponseCode());
                return null;
            }

            JsonObject json = JsonParser.parseReader(new InputStreamReader(conn.getInputStream())).getAsJsonObject();

            JsonObject main = json.getAsJsonObject("main");
            double temp = main.get("temp").getAsDouble();
            double feelsLike = main.get("feels_like").getAsDouble();
            int humidity = main.get("humidity").getAsInt();

            double windSpeed = json.getAsJsonObject("wind").get("speed").getAsDouble();

            String description = json.getAsJsonArray("weather")
                    .get(0).getAsJsonObject().get("description").getAsString();
            String icon = json.getAsJsonArray("weather")
                    .get(0).getAsJsonObject().get("icon").getAsString();

            return new Weather(temp, feelsLike, humidity, windSpeed, description, icon);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class Weather {
        private final double temperature;
        private final double feelsLike;
        private final int humidity;
        private final double windSpeed;
        private final String description;
        private final String icon;

        public Weather(double temperature, double feelsLike, int humidity, double windSpeed, String description, String icon) {
            this.temperature = temperature;
            this.feelsLike = feelsLike;
            this.humidity = humidity;
            this.windSpeed = windSpeed;
            this.description = description;
            this.icon = icon;
        }

        public double getTemperature() { return temperature; }
        public double getFeelsLike() { return feelsLike; }
        public int getHumidity() { return humidity; }
        public double getWindSpeed() { return windSpeed; }
        public String getDescription() { return description; }
        public String getIcon() { return icon; }
    }
}
