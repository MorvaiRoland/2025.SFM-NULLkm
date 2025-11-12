package drivesync;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.Arrays;
import java.util.Properties;
import java.util.prefs.Preferences;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfo;

public class MainController {

    @FXML private VBox loginPane, registerPane;
    @FXML private TextField loginUsername, regUsername, regEmail;
    @FXML private PasswordField loginPassword, regPassword, regPasswordConfirm;
    @FXML private TextField loginPasswordVisible, regPasswordVisible, regPasswordConfirmVisible;
    @FXML private Button loginButton, registerButton, googleLoginButton;
    @FXML private CheckBox showLoginPasswordCheck, showPasswordCheck, rememberMeCheck;
    @FXML private ImageView logoImage;

    private String DB_URL;
    private String DB_USER;
    private String DB_PASS;

    @FXML
    private void initialize() {
        loadDBConfig();
        setupPasswordToggle();
        autoLoginIfPossible();

        try {
            Image logo = new Image(getClass().getResourceAsStream("/drivesync/icons/logo.png"));
            logoImage.setImage(logo);
        } catch (Exception e) {
            System.err.println("Logo betöltése sikertelen: " + e.getMessage());
        }
    }

    // ---------------- DB CONFIG ----------------
    private void loadDBConfig() {
        try (InputStream input = getClass().getResourceAsStream("/drivesync/Adatbázis/db_config.properties")) {
            if (input == null) {
                System.err.println("Nem található a db_config.properties!");
                return;
            }
            Properties props = new Properties();
            props.load(input);
            DB_URL = props.getProperty("db.url");
            DB_USER = props.getProperty("db.user");
            DB_PASS = props.getProperty("db.password");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------- PASSWORD TOGGLE ----------------
    private void setupPasswordToggle() {
        loginPasswordVisible.managedProperty().bind(showLoginPasswordCheck.selectedProperty());
        loginPasswordVisible.visibleProperty().bind(showLoginPasswordCheck.selectedProperty());
        loginPassword.managedProperty().bind(showLoginPasswordCheck.selectedProperty().not());
        loginPassword.visibleProperty().bind(showLoginPasswordCheck.selectedProperty().not());
        loginPassword.textProperty().bindBidirectional(loginPasswordVisible.textProperty());

        regPasswordVisible.managedProperty().bind(showPasswordCheck.selectedProperty());
        regPasswordVisible.visibleProperty().bind(showPasswordCheck.selectedProperty());
        regPassword.managedProperty().bind(showPasswordCheck.selectedProperty().not());
        regPassword.visibleProperty().bind(showPasswordCheck.selectedProperty().not());
        regPassword.textProperty().bindBidirectional(regPasswordVisible.textProperty());

        regPasswordConfirmVisible.managedProperty().bind(showPasswordCheck.selectedProperty());
        regPasswordConfirmVisible.visibleProperty().bind(showPasswordCheck.selectedProperty());
        regPasswordConfirm.managedProperty().bind(showPasswordCheck.selectedProperty().not());
        regPasswordConfirm.visibleProperty().bind(showPasswordCheck.selectedProperty().not());
        regPasswordConfirm.textProperty().bindBidirectional(regPasswordConfirmVisible.textProperty());
    }

    // ---------------- LOGIN ----------------
    @FXML
    private void handleLogin() {
        String username = loginUsername.getText().trim();
        String password = loginPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Hiba", "Add meg a felhasználónevet és a jelszót!");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE username=? AND password=?")) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                if (rememberMeCheck.isSelected()) {
                    Preferences prefs = Preferences.userNodeForPackage(MainController.class);
                    prefs.put("username", username);
                }
                Stage stage = (Stage) loginButton.getScene().getWindow();
                loadHomeSceneAfterStageShown(username, stage);
            } else {
                showAlert(Alert.AlertType.ERROR, "Hiba", "Helytelen felhasználónév vagy jelszó!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Hiba", "Adatbázis hiba: " + e.getMessage());
        }
    }

    // ---------------- REGISTER ----------------
    @FXML
    private void handleRegister() {
        String username = regUsername.getText().trim();
        String email = regEmail.getText().trim();
        String password = regPassword.getText().trim();
        String confirm = regPasswordConfirm.getText().trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Hiba", "Tölts ki minden mezőt!");
            return;
        }
        if (!password.equals(confirm)) {
            showAlert(Alert.AlertType.ERROR, "Hiba", "A jelszavak nem egyeznek!");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO users (username, email, password, isDark) VALUES (?, ?, ?, 0)")) {

            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, password);
            stmt.executeUpdate();

            showLogin();
            showAlert(Alert.AlertType.INFORMATION, "Siker", "Sikeres regisztráció! Most bejelentkezhetsz.");

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Hiba", "Adatbázis hiba: " + e.getMessage());
        }
    }

    @FXML private void showRegister() {
        loginPane.setVisible(false);
        registerPane.setVisible(true);
    }

    @FXML private void showLogin() {
        registerPane.setVisible(false);
        loginPane.setVisible(true);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ---------------- GOOGLE LOGIN ----------------
    @FXML
    private void handleGoogleLogin() {
        try {
            InputStream stream = getClass().getResourceAsStream("/drivesync/NO-GITHUB/client_secret.json");
            if (stream == null) {
                showAlert(Alert.AlertType.ERROR, "Hiba", "A client_secret.json nem található!");
                return;
            }

            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                    GsonFactory.getDefaultInstance(),
                    new InputStreamReader(stream)
            );

            FileDataStoreFactory dataStoreFactory = new FileDataStoreFactory(
                    new File(System.getProperty("user.home"), ".drivesync_credentials")
            );

            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    clientSecrets,
                    Arrays.asList(
                            "https://www.googleapis.com/auth/userinfo.profile",
                            "https://www.googleapis.com/auth/userinfo.email"
                    )
            ).setAccessType("offline").setDataStoreFactory(dataStoreFactory).build();

            LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();

            Credential credential = flow.loadCredential("user");
            if (credential == null || credential.getAccessToken() == null) {
                credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
            } else if (credential.getExpiresInSeconds() != null && credential.getExpiresInSeconds() <= 60) {
                credential.refreshToken();
            }

            Oauth2 oauth2 = new Oauth2.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    credential
            ).setApplicationName("DriveSync").build();

            Userinfo userInfo = oauth2.userinfo().get().execute();
            String name = userInfo.getName() != null ? userInfo.getName() : userInfo.getEmail();
            String email = userInfo.getEmail();

            if (email == null || email.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Hiba", "Nem sikerült az email lekérése! Ellenőrizd, hogy a Google fiókod engedélyezte az email hozzáférést.");
                return;
            }

            // Mentés az adatbázisba, ha még nincs ott
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                PreparedStatement checkStmt = conn.prepareStatement("SELECT * FROM users WHERE email=?");
                checkStmt.setString(1, email);
                ResultSet rs = checkStmt.executeQuery();

                if (!rs.next()) {
                    PreparedStatement insertStmt = conn.prepareStatement(
                            "INSERT INTO users (username, email, password, isDark) VALUES (?, ?, ?, 0)"
                    );
                    insertStmt.setString(1, name);
                    insertStmt.setString(2, email);
                    insertStmt.setString(3, ""); // Google login jelszó nélkül
                    insertStmt.executeUpdate();
                }
            }

            Preferences prefs = Preferences.userNodeForPackage(MainController.class);
            prefs.put("google_email", email);

            showAlert(Alert.AlertType.INFORMATION, "Sikeres bejelentkezés",
                    "Üdv, " + name + " (" + email + ")");

            Stage stage = (Stage) googleLoginButton.getScene().getWindow();
            loadHomeSceneAfterStageShown(name, stage);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Hiba", "Nem sikerült a Google-bejelentkezés:\n" + e.getMessage());
        }
    }

    // ---------------- AUTOLOGIN ----------------
    private void autoLoginIfPossible() {
        Preferences prefs = Preferences.userNodeForPackage(MainController.class);
        String savedUsername = prefs.get("username", null);
        String savedGoogleEmail = prefs.get("google_email", null);

        if (savedUsername != null) {
            Platform.runLater(() -> {
                try {
                    Stage stage = (Stage) loginPane.getScene().getWindow();
                    loadHomeSceneAfterStageShown(savedUsername, stage);
                } catch (Exception e) { e.printStackTrace(); }
            });
        } else if (savedGoogleEmail != null) {
            Platform.runLater(this::handleGoogleLogin);
        }
    }

    // ---------------- LOAD HOME ----------------
    public void loadHomeSceneAfterStageShown(String username, Stage stage) {
        try {
            // Használjuk az App által előre betöltött és stílusozott fő jelenetet,
            // így garantált a style.css és a globális beállítások (téma, betűméret) alkalmazása.
            Scene homeScene = App.getHomeScene();
            HomeController homeController = App.getHomeController();
            if (homeController != null) {
                homeController.setUsername(username);
            }

            if (homeScene != null) {
                stage.setScene(homeScene);
            }
            stage.setTitle("DriveSync - Főoldal");

            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            stage.setX((screenBounds.getWidth() - 1200) / 2);
            stage.setY((screenBounds.getHeight() - 700) / 2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
