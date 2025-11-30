package drivesync.FuelService;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FuelServiceTest {

    @Test
    void testGetFuelPrices_Success() {
        // --- 1. Arrange (Előkészítés) ---
        // Ez egy minta HTML, ami úgy néz ki, mint amit a weboldalról várnánk (9 db ár span)
        String htmlContent = "<html><body>" +
                "<div class='price-table'>" +
                // 95-ös benzin (Min, Átlag, Max)
                "<span class='ar'>580</span> <span class='ar'>600</span> <span class='ar'>620</span>" +
                // Gázolaj (Min, Átlag, Max)
                "<span class='ar'>610</span> <span class='ar'>630</span> <span class='ar'>650</span>" +
                // 100-as benzin (Min, Átlag, Max)
                "<span class='ar'>680</span> <span class='ar'>700</span> <span class='ar'>720</span>" +
                "</div></body></html>";

        // Parse-oljuk be ezt a stringet egy valódi Document objektummá
        Document mockDoc = Jsoup.parse(htmlContent);

        // Itt jön a varázslat: Mockoljuk a Jsoup statikus hívásait
        // A "try-with-resources" blokk biztosítja, hogy a teszt végén a mockolás megszűnjön
        try (MockedStatic<Jsoup> mockedJsoup = Mockito.mockStatic(Jsoup.class)) {

            // Létrehozunk egy mock Connection-t
            Connection mockConnection = mock(Connection.class);

            // Megmondjuk, hogy ha meghívják a Jsoup.connect-et BÁRMILYEN stringgel,
            // akkor adja vissza a mi mockConnection-ünket.
            mockedJsoup.when(() -> Jsoup.connect(anyString())).thenReturn(mockConnection);

            // Aztán a connection beállításait is mockoljuk (láncolt hívások miatt)
            when(mockConnection.userAgent(anyString())).thenReturn(mockConnection);
            when(mockConnection.timeout(Mockito.anyInt())).thenReturn(mockConnection);

            // És végül: ha meghívják a .get()-et, adja vissza az előre gyártott HTML dokumentumunkat
            when(mockConnection.get()).thenReturn(mockDoc);

            // --- 2. Act (Végrehajtás) ---
            Map<String, String[]> result = FuelService.getFuelPrices();

            // --- 3. Assert (Ellenőrzés) ---
            assertNotNull(result, "Az eredmény map nem lehet null");
            assertEquals(3, result.size(), "3 üzemanyagtípust várunk");

            // Ellenőrizzük a 95-ös benzint
            assertTrue(result.containsKey("95-ös benzin"));
            String[] benzin95 = result.get("95-ös benzin");
            assertEquals("580 Ft", benzin95[0]); // Min
            assertEquals("600 Ft", benzin95[1]); // Átlag
            assertEquals("620 Ft", benzin95[2]); // Max

            // Ellenőrizzük a Gázolajat
            assertTrue(result.containsKey("Gázolaj"));
            String[] gazolaj = result.get("Gázolaj");
            assertEquals("610 Ft", gazolaj[0]);
            assertEquals("630 Ft", gazolaj[1]);

            // Ellenőrizzük a 100-as benzint
            assertTrue(result.containsKey("100-as benzin"));
            String[] benzin100 = result.get("100-as benzin");
            assertEquals("680 Ft", benzin100[0]);
        } catch (IOException e) {
            fail("Nem szabadna IO kivételt dobnia mockolt környezetben");
        }
    }

    @Test
    void testGetFuelPrices_NetworkError() {
        // Azt teszteljük, hogy ha hiba van a nettel, a program nem omlik össze, hanem üres map-et ad.
        try (MockedStatic<Jsoup> mockedJsoup = Mockito.mockStatic(Jsoup.class)) {

            // Beállítjuk, hogy a connect híváskor dobjon kivételt
            mockedJsoup.when(() -> Jsoup.connect(anyString())).thenThrow(new RuntimeException("Nincs internet"));

            Map<String, String[]> result = FuelService.getFuelPrices();

            assertNotNull(result);
            assertTrue(result.isEmpty(), "Hiba esetén üres map-et kell visszaadnia a catch ág miatt");
        }
    }
    @Test
    void testRealConnection_IntegrationTest() {
        // FIGYELEM: Ez elbukik, ha nincs net, vagy ha megváltozott a holtankoljak.hu szerkezete!
        Map<String, String[]> result = FuelService.getFuelPrices();

        assertNotNull(result);
        if (!result.isEmpty()) {
            // Csak akkor ellenőrzünk, ha sikerült letölteni (hogy ne legyen flaky a teszt)
            assertTrue(result.containsKey("95-ös benzin"));
            assertNotNull(result.get("95-ös benzin")[1]); // Átlagár ne legyen null
            System.out.println("Valós 95-ös átlagár: " + result.get("95-ös benzin")[1]);
        }
    }
}