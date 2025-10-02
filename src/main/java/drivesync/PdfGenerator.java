package drivesync;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

public class PdfGenerator {

    public static void generateCarReport(int carId, String username) {
        try (Connection conn = Database.getConnection();
             PreparedStatement carStmt = conn.prepareStatement("SELECT * FROM cars WHERE id=?")) {

            carStmt.setInt(1, carId);
            ResultSet carRs = carStmt.executeQuery();

            if (!carRs.next()) {
                System.out.println("Nincs ilyen autó ID: " + carId);
                return;
            }

            String brand = carRs.getString("brand") != null ? carRs.getString("brand") : "Unknown";
            String license = carRs.getString("license") != null ? carRs.getString("license") : "Unknown";

            // PDF fájl neve: Márka-Rendszám.pdf
            String pdfPath = brand + "-" + license + ".pdf";

            try (PdfWriter writer = new PdfWriter(pdfPath);
                 PdfDocument pdfDoc = new PdfDocument(writer);
                 Document doc = new Document(pdfDoc)) {

                // Logo hozzáadása
                try {
                    InputStream logoStream = PdfGenerator.class.getResourceAsStream("/DriveSync logo-2.png");
                    if (logoStream != null) {
                        ImageData logoData = ImageDataFactory.create(logoStream.readAllBytes());
                        Image logo = new Image(logoData);
                        logo.setAutoScale(true);
                        doc.add(logo);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // PDF fejléc
                doc.add(new Paragraph("Autó és Szerviz Jelentés").setBold().setFontSize(24));
                doc.add(new Paragraph("Felhasználó: " + username + "    |    Létrehozva: " + LocalDate.now()).setFontSize(12));
                doc.add(new Paragraph("\n"));

                // Autó adatok
                Table carTable = new Table(new float[]{30, 70});
                carTable.setMarginBottom(15);

                carTable.addCell(createHeaderCell("Rendszám"));
                carTable.addCell(createValueCell(license));

                carTable.addCell(createHeaderCell("Márka"));
                carTable.addCell(createValueCell(brand));

                carTable.addCell(createHeaderCell("Típus"));
                carTable.addCell(createValueCell(carRs.getString("type")));

                carTable.addCell(createHeaderCell("Évjárat"));
                carTable.addCell(createValueCell(String.valueOf(carRs.getInt("vintage"))));

                carTable.addCell(createHeaderCell("KM"));
                carTable.addCell(createValueCell(String.valueOf(carRs.getInt("km"))));

                carTable.addCell(createHeaderCell("Motor típusa"));
                carTable.addCell(createValueCell(carRs.getString("engine_type")));

                carTable.addCell(createHeaderCell("Üzemanyag"));
                carTable.addCell(createValueCell(carRs.getString("fuel_type")));

                doc.add(new Paragraph("Autó adatok").setBold().setFontSize(16));
                doc.add(carTable);

                // Szervizek
                doc.add(new Paragraph("Szervizelőzmények").setBold().setFontSize(16));

                try (PreparedStatement serviceStmt = conn.prepareStatement(
                        "SELECT s.service_date, t.name, s.km, s.price, s.replaced_parts " +
                                "FROM services s LEFT JOIN service_types t ON s.service_type_id=t.id " +
                                "WHERE s.car_id=? ORDER BY s.service_date DESC")) {

                    serviceStmt.setInt(1, carId);
                    ResultSet rs = serviceStmt.executeQuery();

                    Table serviceTable = new Table(new float[]{15, 25, 15, 15, 30});

                    serviceTable.addCell(createHeaderCell("Dátum"));
                    serviceTable.addCell(createHeaderCell("Típus"));
                    serviceTable.addCell(createHeaderCell("KM"));
                    serviceTable.addCell(createHeaderCell("Ár (Ft)"));
                    serviceTable.addCell(createHeaderCell("Cserélt alkatrészek"));

                    while (rs.next()) {
                        serviceTable.addCell(createValueCell(rs.getDate("service_date") != null ? rs.getDate("service_date").toString() : "-"));
                        serviceTable.addCell(createValueCell(rs.getString("name") != null ? rs.getString("name") : "-"));
                        serviceTable.addCell(createValueCell(String.valueOf(rs.getInt("km"))));
                        serviceTable.addCell(createValueCell(String.valueOf(rs.getInt("price"))));
                        serviceTable.addCell(createValueCell(rs.getString("replaced_parts") != null ? rs.getString("replaced_parts") : "-"));
                    }

                    doc.add(serviceTable);
                }

                System.out.println("PDF elkészült: " + new File(pdfPath).getAbsolutePath());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Cell createHeaderCell(String text) {
        return new Cell().add(new Paragraph(text).setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY);
    }

    private static Cell createValueCell(String text) {
        return new Cell().add(new Paragraph(text));
    }
}
