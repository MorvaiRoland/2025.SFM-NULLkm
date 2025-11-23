package drivesync.Bejelentkezés;


import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.framework.junit5.ApplicationTest;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.atomic.AtomicBoolean;


import static org.junit.jupiter.api.Assertions.*;


// This annotation requires the 'mockito-junit-jupiter' dependency
@ExtendWith(MockitoExtension.class)
public class LoginControllerUnitTest extends ApplicationTest {

    // We need a concrete class because LoginController is abstract
    public static class ConcreteLoginController extends LoginController {
        private Connection testConnection;
        private boolean homeOpened = false;

        public void setTestConnection(Connection c) { this.testConnection = c; }
        public boolean isHomeOpened() { return homeOpened; }

        @Override
        protected Connection getConnection() throws Exception {
            if (testConnection != null) return testConnection;
            return super.getConnection();
        }

        @Override
        protected void openHome(String username) {
            // Avoid real scene changes during unit tests
            homeOpened = true;
        }
    }

    // Defined at class level so the lambda expressions can see it
    private ConcreteLoginController controller;

    // Remove static mocks to avoid JDK23 agent issues; we override in controller instead

    // Note: Do NOT use @Mock for JDK interfaces on JDK 23

    /**
     * This runs automatically by TestFX to initialize the JavaFX thread.
     * We manually initialize the UI components here because we are not loading the FXML file.
     */
    @Override
    public void start(Stage stage) {
        controller = new ConcreteLoginController();

        // Manually create the JavaFX controls to avoid NullPointerException
        controller.usernameField = new TextField();
        controller.passwordField = new PasswordField();
        controller.passwordVisibleField = new TextField();
        controller.loginButton = new Button("Login");
        controller.errorLabel = new Label();
        controller.rememberMeCheck = new CheckBox();
        controller.showPasswordCheck = new CheckBox();
        // Note: We skip sidebarVideo to avoid complex media errors in tests

        // We need a dummy scene for the stage logic to work
        StackPane root = new StackPane(controller.loginButton);
        stage.setScene(new Scene(root, 800, 600));
        stage.show();
    }

    @BeforeEach
    public void setupMocks() {
        // Initialize listeners manually
        controller.setupPasswordToggle();
    }

    @AfterEach
    public void tearDown() {
        // No static mocks to close
    }

    // --- TESTS ---

    @Test
    public void testFieldsAreRequired() throws Exception {
        // Runs on the JavaFX Thread to safely update UI
        interact(() -> {
            controller.usernameField.setText("");
            controller.passwordField.setText("");
            controller.handleLogin();
        });

        assertEquals("Kérlek, töltsd ki az összes mezőt!", controller.errorLabel.getText());
    }

    @Test
    public void testPasswordToggleLogic() throws Exception {
        String secret = "MySecretPass";

        interact(() -> {
            controller.passwordField.setText(secret);
            controller.showPasswordCheck.setSelected(true);
        });

        assertTrue(controller.passwordVisibleField.isVisible());
        assertFalse(controller.passwordField.isVisible());
        assertEquals(secret, controller.passwordVisibleField.getText());
    }

    @Test
    public void testLoginFailure_WrongCredentials() throws Exception {
        // Provide a fake connection that returns no rows
        Connection fakeConn = createFakeConnection(false);
        controller.setTestConnection(fakeConn);

        interact(() -> {
            controller.usernameField.setText("wrongUser");
            controller.passwordField.setText("wrongPass");
            controller.handleLogin();
        });

        assertEquals("Hibás felhasználónév vagy jelszó!", controller.errorLabel.getText());
    }

    @Test
    public void testLoginSuccess_OpensHome() throws Exception {
        // Provide a fake connection that returns one row
        Connection fakeConn = createFakeConnection(true);
        controller.setTestConnection(fakeConn);

        interact(() -> {
            controller.usernameField.setText("validUser");
            controller.passwordField.setText("validPass");
            controller.handleLogin();
        });

        // Ensure success implied by openHome override
        assertTrue(((ConcreteLoginController)controller).isHomeOpened());
    }

    // --- Helpers ---
    private Connection createFakeConnection(boolean hasRow) {
        AtomicBoolean hasRowState = new AtomicBoolean(hasRow);
        ResultSet rsProxy = (ResultSet) Proxy.newProxyInstance(
                ResultSet.class.getClassLoader(),
                new Class[]{ResultSet.class},
                new SimpleHandler(method -> {
                    String name = method.getName();
                    switch (name) {
                        case "next":
                            if (hasRowState.get()) {
                                hasRowState.set(false);
                                return true;
                            }
                            return false;
                        case "close":
                            return null;
                        default:
                            throw new UnsupportedOperationException("ResultSet method not supported: " + name);
                    }
                })
        );

        PreparedStatement stmtProxy = (PreparedStatement) Proxy.newProxyInstance(
                PreparedStatement.class.getClassLoader(),
                new Class[]{PreparedStatement.class},
                new SimpleHandler(method -> {
                    String name = method.getName();
                    switch (name) {
                        case "setString":
                            return null; // ignore parameters
                        case "executeQuery":
                            return rsProxy;
                        case "close":
                            return null;
                        default:
                            throw new UnsupportedOperationException("PreparedStatement method not supported: " + name);
                    }
                })
        );

        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class[]{Connection.class},
                new SimpleHandler(method -> {
                    String name = method.getName();
                    switch (name) {
                        case "prepareStatement":
                            return stmtProxy;
                        case "close":
                            return null;
                        default:
                            throw new UnsupportedOperationException("Connection method not supported: " + name);
                    }
                })
        );
    }

    // Small functional-style invocation handler to avoid verbose proxies
    private static class SimpleHandler implements InvocationHandler {
        interface Func { Object apply(Method method) throws Throwable; }
        private final Func func;
        SimpleHandler(Func func) { this.func = func; }
        @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return func.apply(method);
        }
    }
}