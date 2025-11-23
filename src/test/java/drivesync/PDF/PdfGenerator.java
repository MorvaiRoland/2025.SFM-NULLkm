package drivesync.PDF;

import drivesync.Adatbázis.Database;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.File;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

class PdfGeneratorTest {

    // Teszt után takarítsuk el a létrehozott PDF-et, hogy ne szemeteljük tele a projektet
    private final String TEST_LICENSE = "TEST-999";
    private final String PDF_FILENAME = "DriveSync_Jelentes_" + TEST_LICENSE + ".pdf";

    @AfterEach
    void tearDown() {
        File file = new File(PDF_FILENAME);
        if (file.exists()) {
            boolean deleted = file.delete();
            System.out.println("Teszt PDF törölve: " + deleted);
        }
    }

    @Test
    void testGenerateCarReport_Success() throws Exception {
        // --- 1. Arrange (Mockok beállítása) ---
        Connection mockConn = mock(Connection.class);

        // Két külön PreparedStatement kell: egy az autónak, egy a szervizeknek
        PreparedStatement mockCarStmt = mock(PreparedStatement.class);
        ResultSet mockCarRs = mock(ResultSet.class);

        PreparedStatement mockServiceStmt = mock(PreparedStatement.class);
        ResultSet mockServiceRs = mock(ResultSet.class);

        // Statikus Database mockolása
        try (MockedStatic<Database> mockedDb = Mockito.mockStatic(Database.class)) {
            mockedDb.when(Database::getConnection).thenReturn(mockConn);

            // 1.1 Autó lekérdezés mockolása
            // Fontos: a contains segít megkülönböztetni a két SQL hívást
            when(mockConn.prepareStatement(contains("FROM cars"))).thenReturn(mockCarStmt);
            when(mockCarStmt.executeQuery()).thenReturn(mockCarRs);

            // Autó adatok beállítása
            when(mockCarRs.next()).thenReturn(true); // Van találat
            when(mockCarRs.getString("brand")).thenReturn("Toyota");
            when(mockCarRs.getString("type")).thenReturn("Corolla");
            when(mockCarRs.getString("license")).thenReturn(TEST_LICENSE); // Ebből lesz a fájlnév!
            when(mockCarRs.getInt("vintage")).thenReturn(2021);
            when(mockCarRs.getString("engine_type")).thenReturn("Hybrid");
            when(mockCarRs.getString("fuel_type")).thenReturn("Benzin/Elektromos");
            when(mockCarRs.getInt("km")).thenReturn(45000);
            when(mockCarRs.getString("color")).thenReturn("Fehér");
            when(mockCarRs.getString("oil_type")).thenReturn("5W-30");
            when(mockCarRs.getString("oil_quantity")).thenReturn("4.2 L");
            when(mockCarRs.getString("tire_size")).thenReturn("205/55 R16");
            when(mockCarRs.getDate("insurance")).thenReturn(Date.valueOf(LocalDate.now().plusMonths(6)));
            when(mockCarRs.getDate("inspection_date")).thenReturn(Date.valueOf(LocalDate.now().plusYears(1)));
            when(mockCarRs.getString("notes")).thenReturn("Mockolt teszt autó.");

            // 1.2 Szerviz lekérdezés mockolása
            when(mockConn.prepareStatement(contains("FROM services"))).thenReturn(mockServiceStmt);
            when(mockServiceStmt.executeQuery()).thenReturn(mockServiceRs);

            // Szerviz adatok beállítása (legyen egy sor)
            when(mockServiceRs.next()).thenReturn(true, false); // Egy sor, utána vége
            when(mockServiceRs.getDate("service_date")).thenReturn(Date.valueOf(LocalDate.now().minusMonths(1)));
            when(mockServiceRs.getString("name")).thenReturn("Olajcsere");
            when(mockServiceRs.getInt("km")).thenReturn(44000);
            when(mockServiceRs.getInt("price")).thenReturn(35000);
            when(mockServiceRs.getString("replaced_parts")).thenReturn("Olajszűrő, Olaj");

            // --- 2. Act (Végrehajtás) ---
            PdfGenerator.generateCarReport(1, "TesztUser");

            // --- 3. Assert (Ellenőrzés) ---
            File pdfFile = new File(PDF_FILENAME);

            // a) Létezik-e a fájl?
            assertTrue(pdfFile.exists(), "A PDF fájlnak létrejötte után léteznie kell: " + pdfFile.getAbsolutePath());

            // b) Nem üres-e? (méret > 0)
            assertTrue(pdfFile.length() > 0, "A PDF fájl mérete nem lehet 0 bájt");

            // c) Ellenőrizzük, hogy tényleg PDF-e (magic number ellenőrzés - opcionális, de profi)
            // A PDF fájlok "%PDF" karakterekkel kezdődnek
            try (java.io.InputStream is = new java.io.FileInputStream(pdfFile)) {
                byte[] header = new byte[4];
                int read = is.read(header);
                String headerStr = new String(header);
                assertTrue(headerStr.startsWith("%PDF"), "A generált fájl nem érvényes PDF (fejléc hiba)");
            }
        }
    }

    @Test
    void testGenerateCarReport_CarNotFound() {
        // Azt teszteljük, mi történik, ha az ID nem létezik az adatbázisban

        Connection mockConn = mock(Connection.class);
        PreparedStatement mockCarStmt = mock(PreparedStatement.class);
        ResultSet mockCarRs = mock(ResultSet.class);

        try (MockedStatic<Database> mockedDb = Mockito.mockStatic(Database.class)) {
            mockedDb.when(Database::getConnection).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenReturn(mockCarStmt);
            when(mockCarStmt.executeQuery()).thenReturn(mockCarRs);

            // Nincs találat az adatbázisban
            when(mockCarRs.next()).thenReturn(false);

            // Futtatás
            PdfGenerator.generateCarReport(999, "Senki");

            // Ellenőrzés: NEM szabad fájlnak létrejönnie (vagy a fájlnév nem definiált, mert nincs license)
            // Mivel a kód return-el tér vissza, ha nincs autó, nem dob hibát, de fájlt se gyárt.
            // Ezt nehéz fájlnév alapján ellenőrizni, mert nem tudjuk mi lenne a neve.
            // De azt ellenőrizhetjük, hogy nem omlott össze.
        } catch (Exception e) {
            fail("Nem szabadna kivételt dobnia, ha nem talál autót, csak logolnia.");
        }
    }
}