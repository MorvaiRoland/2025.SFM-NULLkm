package drivesync.Linkek;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import static org.junit.jupiter.api.Assertions.*;

public class LinksControllerTest extends ApplicationTest {

    private LinksController controller;

    @Override
    public void start(javafx.stage.Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/drivesync/Linkek/links.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        stage.setScene(new javafx.scene.Scene(root));
        stage.show();
    }

    @BeforeEach
    void setup() {
        assertNotNull(controller, "Controller should be initialized");
    }

    @Test
    void testHyperlinksExist() {
        assertNotNull(controller.linkHAHU, "HAHU link should exist");
        assertNotNull(controller.linkFEKMESTER, "FEKMESTER link should exist");
        assertNotNull(controller.linkHOLTANKOLJAK, "Holtankoljak link should exist");
        assertNotNull(controller.linkWAZE, "Waze link should exist");
        assertNotNull(controller.linkGARVISOR, "Garvisor link should exist");
    }

    @Test
    void testImagesLoaded() {
        assertImage(controller.imgHAHU);
        assertImage(controller.imgFEKMESTER);
        assertImage(controller.imgHOLTANKOLJAK);
        assertImage(controller.imgWAZE);
        assertImage(controller.imgGARVISOR);
    }

    private void assertImage(ImageView iv) {
        assertNotNull(iv, "ImageView should not be null");
        assertNotNull(iv.getImage(), "ImageView should have an image");
    }

    @Test
    void testTooltipsPresent() {
        assertTooltip(controller.linkHAHU);
        assertTooltip(controller.linkFEKMESTER);
        assertTooltip(controller.linkHOLTANKOLJAK);
        assertTooltip(controller.linkWAZE);
        assertTooltip(controller.linkGARVISOR);
    }

    private void assertTooltip(Hyperlink link) {
        Tooltip t = link.getTooltip();
        assertNotNull(t, "Link must have tooltip");
        assertFalse(t.getText().isEmpty(), "Tooltip must contain text");
    }

    @Test
    void testOpenLinkDoesNotThrow() {
        // We do NOT want to open a browser, only ensure method doesn't crash
        assertDoesNotThrow(() -> {
            controllerTestOpen("https://example.com");
        }, "openLink should not throw exceptions");
    }

    /** Call openLink via reflection to avoid opening Desktop */
    private void controllerTestOpen(String url) throws Exception {
        var m = LinksController.class.getDeclaredMethod("openLink", String.class);
        m.setAccessible(true);
        m.invoke(controller, url);
    }
}
