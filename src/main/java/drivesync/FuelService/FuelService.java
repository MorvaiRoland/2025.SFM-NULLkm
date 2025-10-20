package drivesync.FuelService;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.LinkedHashMap;
import java.util.Map;

public class FuelService {

    private static final String URL = "https://holtankoljak.hu/index.php";

    /**
     * Lekéri a legfrissebb üzemanyagárakat a Holtankoljak.hu-ról
     * @return Map ahol kulcs az üzemanyag típusa, érték [min, átlag, max]
     */
    public static Map<String, String[]> getFuelPrices() {
        Map<String, String[]> prices = new LinkedHashMap<>();
        try {
            Document doc = Jsoup.connect(URL)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();

            Elements arElements = doc.select("span.ar");

            if (arElements.size() >= 9) {
                // 95-ös benzin
                prices.put("95-ös benzin", new String[]{
                        arElements.get(0).text() + " Ft",
                        arElements.get(1).text() + " Ft",
                        arElements.get(2).text() + " Ft"
                });

                // Gázolaj
                prices.put("Gázolaj", new String[]{
                        arElements.get(3).text() + " Ft",
                        arElements.get(4).text() + " Ft",
                        arElements.get(5).text() + " Ft"
                });

                // 100-as benzin
                prices.put("100-as benzin", new String[]{
                        arElements.get(6).text() + " Ft",
                        arElements.get(7).text() + " Ft",
                        arElements.get(8).text() + " Ft"
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return prices;
    }
}
