package drivesync.Bejelentkezés;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.embed.swing.JFXPanel;
import org.junit.jupiter.api.*;

import java.util.concurrent.CountDownLatch;
import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions.*;

public class LoginControllerUnitTest {

    private LoginController controller;

    @BeforeAll
    public static void initJavaFX() {
        // Initialize JavaFX toolkit
        new JFXPanel();
    }

    @BeforeEach
    public void setup() throws InterruptedException {
        // Override openHome and loginUser to avoid Stage/DB
        controller = new LoginController() {

            private void openHome(String username) {
                // Do nothing in tests to avoid Stage/Scene
            }


            private boolean loginUser(String username, String password) {
                // Simulate login success only for the test user
                return "testuser".equals(username) && "testpass".equals(password);
            }
        };

        // Clear Preferences for "remember me"
        Preferences.userNodeForPackage(LoginController.class).remove("username");

        // Assign the private @FXML fields manually
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            controller.usernameField = new TextField();
            controller.passwordField = new PasswordField();
            controller.passwordVisibleField = new TextField();
            controller.errorLabel = new Label();
            controller.rememberMeCheck = new CheckBox();
            controller.showPasswordCheck = new CheckBox();
            controller.loginButton = new Button();

            // Setup password toggle only (skip MediaView/video)
            controller.setupPasswordToggle();
            latch.countDown();
        });
        latch.await();
    }

    @Test
    public void testEmptyFieldsShowsError() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            controller.usernameField.setText("");
            controller.passwordField.setText("");
            controller.handleLogin();
            assertEquals("Kérlek, töltsd ki az összes mezőt!", controller.errorLabel.getText());
            latch.countDown();
        });
        latch.await();
    }

    @Test
    public void testInvalidLoginShowsError() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            controller.usernameField.setText("wronguser");
            controller.passwordField.setText("wrongpass");
            controller.handleLogin();
            assertEquals("Hibás felhasználónév vagy jelszó!", controller.errorLabel.getText());
            latch.countDown();
        });
        latch.await();
    }

    @Test
    public void testValidLoginStoresPreference() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            controller.usernameField.setText("testuser");
            controller.passwordField.setText("testpass");
            controller.rememberMeCheck.setSelected(true);
            controller.handleLogin();

            // Preference should be stored
            String remembered = Preferences.userNodeForPackage(LoginController.class).get("username", null);
            assertEquals("testuser", remembered);
            latch.countDown();
        });
        latch.await();
    }

    @Test
    public void testPasswordToggle() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            controller.passwordField.setText("secret");
            controller.showPasswordCheck.setSelected(true); // show password
            assertEquals(controller.passwordField.getText(), controller.passwordVisibleField.getText());
            assertTrue(controller.passwordVisibleField.isVisible());
            assertFalse(controller.passwordField.isVisible());

            controller.showPasswordCheck.setSelected(false); // hide password
            assertEquals(controller.passwordVisibleField.getText(), controller.passwordField.getText());
            assertTrue(controller.passwordField.isVisible());
            assertFalse(controller.passwordVisibleField.isVisible());
            latch.countDown();
        });
        latch.await();
    }
}
