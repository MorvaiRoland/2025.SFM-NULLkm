package  drivesync.Linkek;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class LinksController {

    @FXML private Hyperlink linkHAHU;
    @FXML private Hyperlink linkFEKMESTER;
    @FXML private Hyperlink linkHOLTANKOLJAK;
    @FXML private Hyperlink linkWAZE;
    @FXML private Hyperlink linkGARVISOR;

    @FXML private ImageView imgHAHU;
    @FXML private ImageView imgFEKMESTER;
    @FXML private ImageView imgHOLTANKOLJAK;
    @FXML private ImageView imgWAZE;
    @FXML private ImageView imgGARVISOR;

    @FXML
    private void initialize() {
        // Képek betöltése classpath-ról
        imgHAHU.setImage(new Image(getClass().getResourceAsStream("/drivesync/icons/hasznaltauto.png")));
        imgFEKMESTER.setImage(new Image(getClass().getResourceAsStream("/drivesync/icons/fekmester.png")));
        imgHOLTANKOLJAK.setImage(new Image(getClass().getResourceAsStream("/drivesync/icons/holtankoljak.png")));
        imgWAZE.setImage(new Image(getClass().getResourceAsStream("/drivesync/icons/waze.png")));
        imgGARVISOR.setImage(new Image(getClass().getResourceAsStream("/drivesync/icons/garvisor.png")));

        // Tooltip-ek beállítása programból
        linkHAHU.setTooltip(new Tooltip("Használt autók keresése"));
        linkFEKMESTER.setTooltip(new Tooltip("Autóalkatrész áruház"));
        linkHOLTANKOLJAK.setTooltip(new Tooltip("Üzemanyag árak országosan"));
        linkWAZE.setTooltip(new Tooltip("Valós idejű navigáció"));
        linkGARVISOR.setTooltip(new Tooltip("Szervizek, értékelések"));

        // Linkek akciói
        linkHAHU.setOnAction(e -> openLink("https://www.hasznaltauto.hu/"));
        linkFEKMESTER.setOnAction(e -> openLink("https://www.fekmester.hu/"));
        linkHOLTANKOLJAK.setOnAction(e -> openLink("https://holtankoljak.hu/#!"));
        linkWAZE.setOnAction(e -> openLink("https://www.waze.com/hu/live-map/"));
        linkGARVISOR.setOnAction(e -> openLink("https://garvisor.com/"));
    }

    private void openLink(String url) {
        if (Desktop.isDesktopSupported()) {
            try { Desktop.getDesktop().browse(new URI(url)); }
            catch (IOException | URISyntaxException ex) { ex.printStackTrace(); }
        }
    }
}