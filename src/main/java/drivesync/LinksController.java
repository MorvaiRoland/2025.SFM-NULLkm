package drivesync;

import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class LinksController {

    @FXML
    private Hyperlink linkHAHU;
    @FXML
    private Hyperlink linkAUTODOC;
    @FXML
    private Hyperlink linkHOLTANKOLJAK;
    @FXML
    private Hyperlink linkWAZE;
    @FXML
    private Hyperlink linkGARVISOR;

    // Linkekhez tartozó URL-ek
    private static final String URL_HAHU = "https://www.hasznaltauto.hu/";
    private static final String URL_AUTODOC = "https://www.autodoc.hu/jarmu-alkatreszek/fogasszij-keszlet-10553";
    private static final String URL_HOLTANKOLJAK = "https://holtankoljak.hu/#!";
    private static final String URL_WAZE = "https://www.waze.com/hu/live-map/";
    private static final String URL_GARVISOR = "https://garvisor.com/";

    @FXML
    private void initialize() {
        linkHAHU.setOnAction(e -> openLink(URL_HAHU));
        linkAUTODOC.setOnAction(e -> openLink(URL_AUTODOC));
        linkHOLTANKOLJAK.setOnAction(e -> openLink(URL_HOLTANKOLJAK));
        linkWAZE.setOnAction(e -> openLink(URL_WAZE));
        linkGARVISOR.setOnAction(e -> openLink(URL_GARVISOR));
    }

    private void openLink(String url) {
        if (url == null || url.isBlank()) {
            return;
        }

        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }
}
