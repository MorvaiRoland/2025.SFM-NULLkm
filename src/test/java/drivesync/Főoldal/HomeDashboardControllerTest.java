package drivesync.Főoldal;

import drivesync.Adatbázis.ServiceDAO;
import drivesync.FuelService.FuelService;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class HomeDashboardControllerTest {

    @BeforeAll
    static void initJfxRuntime() {
        // ensures JavaFX platform starts once for all tests
        new JFXPanel();
        Platform.setImplicitExit(false);
    }

    // ---------- Segéd: rekurzív keresés Label vagy ImageView típusra ----------
    private boolean containsNodeOfType(Node node, Class<?> type) {
        if (type.isInstance(node)) return true;

        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                if (containsNodeOfType(child, type)) return true;
            }
        }
        return false;
    }


    // -------------------- WEATHER WIDGET TEST --------------------
    @Test
    void testWeatherWidget() {
        HomeDashboardController controller = new HomeDashboardController();

        VBox widget = controller.createWeatherWidget();

        assertNotNull(widget);
        assertTrue(widget.getChildren().size() > 0);
    }


    // -------------------- FUEL WIDGET TEST --------------------
    @Test
    void testFuelWidget() {
        // mock FuelService
        Map<String, String[]> mockPrices = Map.of(
                "95-ös benzin", new String[]{"500", "520", "540"},
                "Gázolaj", new String[]{"480", "500", "520"},
                "100-as benzin", new String[]{"560", "580", "600"}
        );

        // STATIC MOCK
        var staticMock = Mockito.mockStatic(FuelService.class);
        staticMock.when(FuelService::getFuelPrices).thenReturn(mockPrices);

        HomeDashboardController controller = new HomeDashboardController();
        VBox widget = controller.createFuelWidget();

        assertNotNull(widget);

        // keresünk Label-t vagy ImageView-t a widget bármely szintjén
        boolean found = containsNodeOfType(widget, Label.class)
                || containsNodeOfType(widget, ImageView.class);

        assertTrue(found, "Fuel widget should contain Label or ImageView");

        staticMock.close();
    }


    // -------------------- CARS WIDGET TEST --------------------
    @Test
    void testCarsWidget() {
        HomeDashboardController controller = new HomeDashboardController();
        controller.setUsername("testuser");

        VBox widget = controller.createCarsWidget();

        assertNotNull(widget);
        assertTrue(widget.getChildren().size() > 0);
    }


    // -------------------- BUDGET WIDGET TEST --------------------
    @Test
    void testBudgetWidget() {
        HomeDashboardController controller = new HomeDashboardController();
        controller.setUsername("testuser");
        VBox widget = controller.createBudgetWidget();
        assertNotNull(widget);

        // Mivel a chart async, de a teszt alatt synch-ban fut → benne kell lennie
        boolean hasChart = containsNodeOfType(widget, BarChart.class);

        assertTrue(hasChart, "Chart missing in budget widget");
    }


    // -------------------- NOTIFICATION WIDGET TEST --------------------
    @Test
    void testNotificationWidget() {
        // Mock DAO
        ServiceDAO mockDao = mock(ServiceDAO.class);

        ServiceDAO.Service mockService = new ServiceDAO.Service(
                1,
                1,
                "2025-10-10",
                "Debrecem",
                "Test Megjegyzés",
                true
        );


        when(mockDao.getUpcomingServicesForUser("Levi"))
                .thenReturn(List.of(mockService));

        HomeDashboardController controller = new HomeDashboardController();
        controller.setUsername("Levi");
        controller.setServiceDAO(mockDao);

        VBox widget = controller.createNotificationWidgets();

        assertNotNull(widget);

        boolean foundLabel = containsNodeOfType(widget, Label.class);

        assertTrue(foundLabel, "Notification widget should contain labels");
    }
    @Test
    void testLinksWidget() {
        HomeDashboardController controller = new HomeDashboardController();

        VBox widget = controller.createLinksWidget();

        assertNotNull(widget, "Links widget should not be null");
        assertTrue(widget.getChildren().size() >= 2, "Links widget should have at least 2 children");

        boolean hasLabel = widget.getChildren()
                .stream()
                .anyMatch(node -> node instanceof Label);

        assertTrue(hasLabel, "Links widget should contain a label");
    }

    @Test
    void testAIDiagnosticsWidget() {
        HomeDashboardController controller = new HomeDashboardController();

        VBox widget = controller.createAIDiagnosticsWidget();

        assertNotNull(widget, "AI Diagnostics widget should not be null");
        assertTrue(widget.getChildren().size() >= 4, "AI Diagnostics widget should have UI elements");

        boolean hasTextField = widget.getChildren()
                .stream()
                .anyMatch(node -> node instanceof TextField);

        boolean hasButton = widget.getChildren()
                .stream()
                .anyMatch(node -> node instanceof Button);

        boolean hasTextArea = widget.getChildren()
                .stream()
                .anyMatch(node -> node instanceof TextArea);

        assertTrue(hasTextField, "AI widget should contain a TextField");
        assertTrue(hasButton, "AI widget should contain a Button");
        assertTrue(hasTextArea, "AI widget should contain a TextArea");
    }
}
