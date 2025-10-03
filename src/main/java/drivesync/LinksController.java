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


    // Linkekhez tartozó URL-ek
    private final String URL_HAHU = "https://www.hasznaltauto.hu/";
    private final String URL_AUTODOC = "https://www.autodoc.hu/jarmu-alkatreszek/fogasszij-keszlet-10553";
    private final String URL_HOLTANKOLJAK = "https://holtankoljak.hu/#!";


    @FXML
    private void initialize() {
        linkHAHU.setOnAction(e -> openLink(URL_HAHU));
        linkAUTODOC.setOnAction(e -> openLink(URL_AUTODOC));
        linkHOLTANKOLJAK.setOnAction(e -> openLink(URL_HOLTANKOLJAK));

    }

    private void openLink(String url) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }



}
