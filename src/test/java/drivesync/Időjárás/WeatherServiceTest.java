package drivesync.Időjárás;

import drivesync.Időjárás.WeatherService;
import drivesync.Időjárás.WeatherService.Weather;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.*;

import static org.junit.jupiter.api.Assertions.*;

public class WeatherServiceTest {

    @Test
    void testGetWeather() throws Exception {

        MockWebServer server = new MockWebServer();

        String json = """
        {
          "main": { "temp": 21.3, "feels_like": 20.0, "humidity": 50 },
          "wind": { "speed": 5.2 },
          "weather": [ { "description": "sunny", "icon": "01d" } ]
        }
        """;

        server.enqueue(new MockResponse().setResponseCode(200).setBody(json));
        server.start();

        // Custom URLStreamHandler to redirect HTTPS → MockWebServer
        URLStreamHandler handler = new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL u) throws IOException {
                URL newUrl = server.url("/weather").url();
                return newUrl.openConnection();
            }
        };

        // Build URL with custom handler → WeatherService will use our handler
        URL.setURLStreamHandlerFactory(protocol -> protocol.equals("https") ? handler : null);

        // Act
        Weather weather = WeatherService.getWeather("Budapest");

        // Assert
        assertNotNull(weather);
        assertEquals(21.3, weather.getTemperature());
        assertEquals(20.0, weather.getFeelsLike());
        assertEquals(50, weather.getHumidity());
        assertEquals(5.2, weather.getWindSpeed());
        assertEquals("sunny", weather.getDescription());
        assertEquals("01d", weather.getIcon());

        server.shutdown();
    }
}
