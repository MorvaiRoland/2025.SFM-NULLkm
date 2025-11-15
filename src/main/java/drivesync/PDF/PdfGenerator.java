package drivesync.PDF;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import drivesync.Adatbázis.Database;

import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PdfGenerator {

    // --- Szín definíciók ---
    private static final com.itextpdf.kernel.colors.Color PRIMARY_COLOR = com.itextpdf.kernel.colors.Color.convertRgbToCmyk(new com.itextpdf.kernel.colors.DeviceRgb(44, 62, 80));
    private static final com.itextpdf.kernel.colors.Color PRIMARY_DARK_COLOR = com.itextpdf.kernel.colors.Color.convertRgbToCmyk(new com.itextpdf.kernel.colors.DeviceRgb(30, 48, 66));
    private static final com.itextpdf.kernel.colors.Color SECONDARY_COLOR = com.itextpdf.kernel.colors.Color.convertRgbToCmyk(new com.itextpdf.kernel.colors.DeviceRgb(255, 215, 0));
    private static final com.itextpdf.kernel.colors.Color SECONDARY_DARK_COLOR = com.itextpdf.kernel.colors.Color.convertRgbToCmyk(new com.itextpdf.kernel.colors.DeviceRgb(220, 185, 0));
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy. MM. dd.");

    // --- Font BÁJTOK betöltése statikusan (UNICODE FIX) ---
    private static final byte[] FONT_BYTES;
    static {
        try (InputStream fontStream = PdfGenerator.class.getResourceAsStream("/fonts/arial.ttf")) {
            if (fontStream != null) {
                FONT_BYTES = fontStream.readAllBytes();
                System.out.println("✅ Unicode betűtípus bájtok betöltve a memóriába.");
            } else {
                FONT_BYTES = null;
                System.err.println("❌ HIBA: Az arial.ttf fájl nem található. Ékezetes karakterek hibásan jelenhetnek meg.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Hiba a betűtípus inicializálásakor.", e);
        }
    }

    // VÁLTOZÁS 1: Minden PDF-hez új PdfFont objektumot hozunk létre a bájtokból.
    private static PdfFont getUnicodeFont() throws Exception {
        if (FONT_BYTES != null) {
            return PdfFontFactory.createFont(FONT_BYTES, "Identity-H", PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
        }
        return PdfFontFactory.createFont(StandardFonts.HELVETICA);
    }


    public static void generateCarReport(int carId, String username) {
        // VÁLTOZÁS 2: SQL Lekérdezés oil -> oil_type, oil_quantity
        String carSql = "SELECT license, brand, type, vintage, engine_type, fuel_type, km, oil_type, oil_quantity, tire_size, insurance, inspection_date, color, notes FROM cars WHERE id=?";

        try (Connection conn = Database.getConnection();
             PreparedStatement carStmt = conn.prepareStatement(carSql)) {

            carStmt.setInt(1, carId);
            ResultSet carRs = carStmt.executeQuery();

            if (!carRs.next()) {
                System.out.println("Nincs ilyen autó ID: " + carId);
                return;
            }

            String brand = carRs.getString("brand") != null ? carRs.getString("brand") : "Nincs adat";
            String license = carRs.getString("license") != null ? carRs.getString("license") : "Nincs adat";

            // Olaj adatok kinyerése
            String oilType = carRs.getString("oil_type") != null ? carRs.getString("oil_type") : "-";
            String oilQuantity = carRs.getString("oil_quantity") != null ? carRs.getString("oil_quantity") : "-";


            String pdfPath = "DriveSync_Jelentes_" + license + ".pdf";

            try (PdfWriter writer = new PdfWriter(pdfPath);
                 PdfDocument pdfDoc = new PdfDocument(writer);
                 Document doc = new Document(pdfDoc)) {

                // VÁLTOZÁS 3: Itt hozzuk létre a friss PdfFont objektumot
                doc.setFont(getUnicodeFont());
                doc.setMargins(40, 40, 60, 40);

                // ==================== 1. FEJLÉC ÉS LOGÓ ====================
                Table headerTable = new Table(UnitValue.createPercentArray(new float[]{60, 40}));
                headerTable.setWidth(UnitValue.createPercentValue(100));

                // Logo hozzáadása (MÉRET FIX)
                try {
                    InputStream logoStream = PdfGenerator.class.getResourceAsStream("/drivesync/Logók/DriveSync logo-2.png");
                    if (logoStream != null) {
                        ImageData logoData = ImageDataFactory.create(logoStream.readAllBytes());
                        Image logo = new Image(logoData);

                        // ABSZOLÚT MÉRET KÉNYSZERÍTÉSE: 120 széles, 120 magas (korábbi fix)
                        logo.setWidth(UnitValue.createPointValue(120));
                        logo.setHeight(UnitValue.createPointValue(120));

                        headerTable.addCell(new Cell().add(logo).setBorder(Border.NO_BORDER));
                    } else {
                        headerTable.addCell(new Cell().add(new Paragraph("DriveSync").setBold().setFontSize(28).setFontColor(PRIMARY_COLOR)).setBorder(Border.NO_BORDER));
                    }
                } catch (Exception e) {
                    headerTable.addCell(new Cell().add(new Paragraph("DriveSync").setBold().setFontSize(28).setFontColor(PRIMARY_COLOR)).setBorder(Border.NO_BORDER));
                }

                // Jelentés címe (jobbra)
                Paragraph title = new Paragraph("Autó és Szerviz Jelentés")
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setBold()
                        .setFontSize(18)
                        .setFontColor(PRIMARY_COLOR);
                headerTable.addCell(new Cell().add(title).setBorder(Border.NO_BORDER));

                doc.add(headerTable);
                doc.add(new Paragraph("\n"));

                // Alfejléc
                doc.add(new Paragraph("Rendszám: " + license + " |  Felhasználó: " + username)
                        .setFontSize(10)
                        .setFontColor(ColorConstants.GRAY)
                        .setMarginBottom(20));

                // ==================== 2. AUTÓ RÉSZLETEK ====================
                doc.add(new Paragraph("Autó Részletes Adatai").setBold().setFontSize(14).setFontColor(PRIMARY_COLOR));

                // A táblázat most 4 egyenlő oszlopot használ (25, 25, 25, 25)
                Table carTable = new Table(UnitValue.createPercentArray(new float[]{25, 25, 25, 25}));
                carTable.setWidth(UnitValue.createPercentValue(100));
                carTable.setMarginBottom(20);
                carTable.setFontSize(10);

                // Sor: Márka, Típus, Évjárat
                carTable.addCell(createCarHeaderCell("Márka / Típus"));
                carTable.addCell(createCarValueCell(brand + " " + carRs.getString("type"), 1));
                carTable.addCell(createCarHeaderCell("Évjárat"));
                carTable.addCell(createCarValueCell(String.valueOf(carRs.getInt("vintage"))));

                // Sor: Motor, Üzemanyag, KM
                carTable.addCell(createCarHeaderCell("Motor Típusa"));
                carTable.addCell(createCarValueCell(carRs.getString("engine_type")));
                carTable.addCell(createCarHeaderCell("Üzemanyag"));
                carTable.addCell(createCarValueCell(carRs.getString("fuel_type")));

                // Sor: KM, Szín, Megjegyzés (a helytakarékosság miatt csoportosítva)
                carTable.addCell(createCarHeaderCell("KM Állás"));
                carTable.addCell(createCarValueCell(String.valueOf(carRs.getInt("km")) + " km"));
                carTable.addCell(createCarHeaderCell("Szín"));
                carTable.addCell(createCarValueCell(carRs.getString("color") != null ? carRs.getString("color") : "-"));

                // VÁLTOZÁS 8: ÚJ SOR: Olaj Típus és Mennyiség
                carTable.addCell(createCarHeaderCell("Olaj Típusa"));
                carTable.addCell(createCarValueCell(oilType));
                carTable.addCell(createCarHeaderCell("Olaj Mennyisége"));
                carTable.addCell(createCarValueCell(oilQuantity));


                // Sor: Gumi, Biztosítás, Műszaki
                carTable.addCell(createCarHeaderCell("Gumi Méret"));
                carTable.addCell(createCarValueCell(carRs.getString("tire_size") != null ? carRs.getString("tire_size") : "-"));

                carTable.addCell(createCarHeaderCell("Biztosítás Érvényes"));
                carTable.addCell(createCarValueCell(carRs.getDate("insurance") != null ? carRs.getDate("insurance").toLocalDate().format(DATE_FORMATTER) : "Nincs adat"));

                carTable.addCell(createCarHeaderCell("Műszaki Érvényes"));
                carTable.addCell(createCarValueCell(carRs.getDate("inspection_date") != null ? carRs.getDate("inspection_date").toLocalDate().format(DATE_FORMATTER) : "Nincs adat"));

                // Megjegyzés, teljes szélességben (1, 4)
                carTable.addCell(createCarHeaderCell("Megjegyzés"));
                carTable.addCell(createCarValueCell(carRs.getString("notes") != null ? carRs.getString("notes") : "-", 3));

                doc.add(carTable);

                doc.add(new Paragraph("\n"));

                // ==================== 3. SZERVIZELŐZMÉNYEK ====================
                doc.add(new Paragraph("Szervizelőzmények").setBold().setFontSize(14).setFontColor(PRIMARY_COLOR));

                try (PreparedStatement serviceStmt = conn.prepareStatement(
                        "SELECT s.service_date, t.name, s.km, s.price, s.replaced_parts " +
                                "FROM services s LEFT JOIN service_types t ON s.service_type_id=t.id " +
                                "WHERE s.car_id=? ORDER BY s.service_date DESC")) {

                    serviceStmt.setInt(1, carId);
                    ResultSet rs = serviceStmt.executeQuery();

                    Table serviceTable = new Table(UnitValue.createPercentArray(new float[]{15, 25, 15, 15, 30}));
                    serviceTable.setWidth(UnitValue.createPercentValue(100));
                    serviceTable.setFontSize(10);

                    // Táblázat Fejléc
                    serviceTable.addCell(createServiceHeaderCell("Dátum"));
                    serviceTable.addCell(createServiceHeaderCell("Szerviz Típusa"));
                    serviceTable.addCell(createServiceHeaderCell("KM Állás"));
                    serviceTable.addCell(createServiceHeaderCell("Ár (Ft)"));
                    serviceTable.addCell(createServiceHeaderCell("Cserélt Alkatrészek"));

                    boolean hasServices = false;
                    while (rs.next()) {
                        hasServices = true;
                        serviceTable.addCell(createServiceValueCell(rs.getDate("service_date") != null ? rs.getDate("service_date").toLocalDate().format(DATE_FORMATTER) : "-"));
                        serviceTable.addCell(createServiceValueCell(rs.getString("name") != null ? rs.getString("name") : "-"));
                        serviceTable.addCell(createServiceValueCell(String.format("%,d", rs.getInt("km"))));
                        serviceTable.addCell(createServiceValueCell(String.format("%,d Ft", rs.getInt("price"))));
                        serviceTable.addCell(createServiceValueCell(rs.getString("replaced_parts") != null ? rs.getString("replaced_parts") : "-"));
                    }

                    if (!hasServices) {
                        serviceTable.addCell(new Cell(1, 5).add(new Paragraph("Nincs rögzített szerviz előzmény.")).setTextAlignment(TextAlignment.CENTER).setFontColor(ColorConstants.GRAY).setItalic());
                    }

                    doc.add(serviceTable);
                }

                // ==================== 4. LÁBLÉC ====================
                doc.add(new Paragraph("\n"));
                doc.add(new Paragraph("Jelentés automatikusan generálva a DriveSync rendszer által " + LocalDate.now().format(DATE_FORMATTER) + ".")
                        .setFontSize(8)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginTop(10)
                        .setFontColor(ColorConstants.DARK_GRAY));

                System.out.println("✅ PDF elkészült: " + new File(pdfPath).getAbsolutePath());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- Segéd metódusok (Változatlan) ---

    private static Cell createCarHeaderCell(String text) {
        return new Cell().add(new Paragraph(text))
                .setBold()
                .setFontSize(10)
                .setBackgroundColor(PRIMARY_DARK_COLOR)
                .setFontColor(ColorConstants.WHITE)
                .setBorder(Border.NO_BORDER)
                .setPadding(5);
    }

    private static Cell createCarValueCell(String text) {
        return new Cell().add(new Paragraph(text))
                .setFontSize(10)
                .setBackgroundColor(ColorConstants.WHITE)
                .setBorder(Border.NO_BORDER)
                .setPadding(5);
    }

    private static Cell createCarValueCell(String text, int colSpan) {
        Cell cell = new Cell(1, colSpan).add(new Paragraph(text))
                .setFontSize(10)
                .setBackgroundColor(ColorConstants.WHITE)
                .setBorder(Border.NO_BORDER)
                .setPadding(5);
        return cell;
    }

    private static Cell createServiceHeaderCell(String text) {
        return new Cell().add(new Paragraph(text).setBold())
                .setFontSize(10)
                .setBackgroundColor(SECONDARY_DARK_COLOR)
                .setFontColor(PRIMARY_COLOR)
                .setTextAlignment(TextAlignment.CENTER)
                .setBorder(Border.NO_BORDER)
                .setPadding(5);
    }

    private static Cell createServiceValueCell(String text) {
        return new Cell().add(new Paragraph(text))
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setBorder(Border.NO_BORDER)
                .setPadding(4)
                .setBackgroundColor(ColorConstants.WHITE);
    }
}