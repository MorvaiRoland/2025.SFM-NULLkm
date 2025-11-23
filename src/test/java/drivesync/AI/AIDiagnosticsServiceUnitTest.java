package drivesync.AI;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


public class AIDiagnosticsServiceUnitTest {
    private AIDiagnosticsService service;

    // Érvényes válasz szimulálása
    private static final String SUCCESS_RESPONSE_JSON = """
        {
          "candidates": [
            {
              "content": {
                "parts": [
                  {
                    "text": "1. Hibás gyújtógyertyák\\n2. Tömítetlenség a szívórendszerben\\n3. Üzemanyagszűrő eltömődése\\nTanács: Ellenőrizd a szívócső tömítéseit."
                  }
                ]
              }
            }
          ]
        }
        """;

    // --- Reflexiós Segédmetódusok a statikus mezőkhöz ---

    private static void setStaticField(String fieldName, Object value) throws Exception {
        Field field = AIDiagnosticsService.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(null, value);
    }

    // --- Setup/Teardown ---

    @BeforeEach
    public void setup() throws Exception {
        // Győződjünk meg róla, hogy az API kulcs be van állítva a tesztek futásához
        setStaticField("API_KEY", "TEST_API_KEY");
        service = new AIDiagnosticsService();

        // A tesztben a service.httpClient-t fogjuk felülírni, vagy a HttpClient-t mockoljuk.
    }

    @AfterEach
    public void reset() throws Exception {
        // Visszaállítjuk az API_KEY-t null-ra a többi teszt esetleges inicializálási hibáinak elkerülése végett
        setStaticField("API_KEY", null);
    }

    // --- TESZTEK ---

    // ------------------------------------------------------------------------------------------------
    // 1. API Kulcs Tesztek
    // ------------------------------------------------------------------------------------------------

    /**
     * Teszteli, ha az API kulcs nincs beállítva (pl. nem található a config fájl),
     * a metódus hibaüzenetet ad vissza hálózati hívás nélkül.
     */
    @Test
    public void testGetDiagnosis_NoApiKey() throws Exception {
        setStaticField("API_KEY", null); // Töröljük a kulcsot szimulálva a hiányzó konfigot
        service = new AIDiagnosticsService(); // Új példány, hogy az API_KEY null legyen

        String result = service.getDiagnosis("BMW", "E46", "Nem indul");

        assertTrue(result.contains("Hiba: Az AI szolgáltatás nincs beállítva"),
                "Hibaüzenetet várunk, ha az API kulcs null.");
    }

}
