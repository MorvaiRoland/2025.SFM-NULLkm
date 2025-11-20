package drivesync.AI;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Properties;

public class AIDiagnosticsService {

    private static String API_KEY;

    // VÁLTOZÁS 1: Statikus inicializáló blokk a kulcs biztonságos betöltésére
    static {
        try (InputStream input = AIDiagnosticsService.class.getResourceAsStream("/drivesync/NO-GITHUB/gemini_config.properties")) {
            if (input == null) {
                System.err.println("❌ Hiba: A Gemini API kulcs konfigurációja (gemini_config.properties) nem található!");
            } else {
                Properties props = new Properties();
                props.load(input);
                API_KEY = props.getProperty("gemini.api_key");
            }
        } catch (Exception e) {
            System.err.println("Gemini kulcs betöltési hiba.");
            // Az alkalmazás nem indulhat el kulcs nélkül
            throw new RuntimeException("Az AI szolgáltatás inicializálása sikertelen. Ellenőrizd a konfigurációs fájlt.", e);
        }
    }

    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public AIDiagnosticsService() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                // Növeljük a timeoutot a hálózati stabilitás érdekében
                .connectTimeout(Duration.ofSeconds(20))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * AI diagnózis lekérése a Gemini API-n keresztül.
     * @param carBrand Az autó márkája (pl. BMW).
     * @param carType Az autó típusa (pl. E46).
     * @param symptom A felhasználó által leírt tünet.
     * @return A Gemini által generált diagnózis és javaslat.
     * @throws Exception Hálózati vagy JSON feldolgozási hiba esetén.
     */
    public String getDiagnosis(String carBrand, String carType, String symptom) throws Exception {

        if (API_KEY == null || API_KEY.isEmpty()) {
            return "Hiba: Az AI szolgáltatás nincs beállítva (hiányzik az API kulcs).";
        }

        // A PROMPT: Szakszerű, célzott válaszra ösztönzi az AI-t.
        String prompt = String.format(
                "Ön egy szakképzett, tapasztalt autószerelő. Az autó típusa: %s %s. A felhasználó tünete és kérdése: '%s'. Adjon 3 legvalószínűbb okot, és egy rövid, lényegre törő tanácsot, mindig számozott listában. Ne adjon semmilyen bevezetést vagy lezárást, csak a listát.",
                carBrand, carType, symptom
        );

        // JSON kérés összeállítása
        ObjectNode rootNode = objectMapper.createObjectNode();
        ObjectNode contentNode = rootNode.putArray("contents").addObject();
        contentNode.putArray("parts").addObject().put("text", prompt);
        rootNode.put("generationConfig", objectMapper.createObjectNode().put("temperature", 0.3));

        String requestBody = objectMapper.writeValueAsString(rootNode);

        // HTTP kérés
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + API_KEY))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        // HTTP válasz
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        String responseBody = response.body();

        if (response.statusCode() != 200) {
            // Részletesebb hiba, ha az API kód 200-tól eltér
            System.err.println("Gemini API Hiba: " + responseBody);
            return "Hiba történt az AI szolgáltatásban. Kód: " + response.statusCode();
        }

        // Válasz (JSON) feldolgozása
        JsonNode jsonResponse = objectMapper.readTree(responseBody);

        // Stabil JSON útvonal beolvasása
        String diagnosis = jsonResponse.path("candidates").path(0)
                .path("content").path("parts").path(0)
                .path("text").asText("Nem sikerült diagnózist felállítani.");

        // Biztonsági ellenőrzés
        if (diagnosis.startsWith("Nem sikerült") || jsonResponse.path("candidates").isEmpty()) {
            return "Hiba: Az AI nem adott választ. Kérlek próbálj meg pontosabb tünetet leírni.";
        }

        return diagnosis.trim();
    }
}