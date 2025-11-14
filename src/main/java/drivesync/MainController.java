package drivesync;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
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
    @FXML private MediaView sidebarVideo;

    private String DB_URL;
    private String DB_USER;
    private String DB_PASS;
    private MediaPlayer mediaPlayer;

    @FXML
    private void initialize() {
        loadDBConfig();
        setupPasswordToggle();
        setupSidebarVideo();   // Csak videó betöltése, méretezés FXML-ben!
        autoLoginIfPossible();
    }

    private void loadDBConfig() {
        try (InputStream input = getClass().getResourceAsStream("/drivesync/Adatbázis/db_config.properties")) {
            Properties props = new Properties();
            props.load(input);

            DB_URL = props.getProperty("db.url");
            DB_USER = props.getProperty("db.user");
            DB_PASS = props.getProperty("db.password");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupPasswordToggle() {
        loginPassword.textProperty().bindBidirectional(loginPasswordVisible.textProperty());
        regPassword.textProperty().bindBidirectional(regPasswordVisible.textProperty());
        regPasswordConfirm.textProperty().bindBidirectional(regPasswordConfirmVisible.textProperty());

        loginPasswordVisible.visibleProperty().bind(showLoginPasswordCheck.selectedProperty());
        loginPassword.visibleProperty().bind(showLoginPasswordCheck.selectedProperty().not());

        regPasswordVisible.visibleProperty().bind(showPasswordCheck.selectedProperty());
        regPassword.visibleProperty().bind(showPasswordCheck.selectedProperty().not());
        regPasswordConfirmVisible.visibleProperty().bind(showPasswordCheck.selectedProperty());
        regPasswordConfirm.visibleProperty().bind(showPasswordCheck.selectedProperty().not());
    }

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
                cleanupMediaPlayer();
                loadHomeScene(username, stage);

            } else {
                showAlert(Alert.AlertType.ERROR, "Hiba", "Helytelen felhasználónév vagy jelszó!");
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Hiba", e.getMessage());
        }
    }

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
            showAlert(Alert.AlertType.INFORMATION, "Siker", "Sikeres regisztráció!");

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Hiba", e.getMessage());
        }
    }

    @FXML
    private void showRegister() {
        loginPane.setVisible(false);
        registerPane.setVisible(true);
    }

    @FXML
    private void showLogin() {
        registerPane.setVisible(false);
        loginPane.setVisible(true);
    }

    @FXML
    private void handleGoogleLogin() {
        // letiltjuk a gombot, hogy többször ne lehessen kattintani
        googleLoginButton.setDisable(true);
        String originalText = googleLoginButton.getText();
        googleLoginButton.setText("Bejelentkezés...");

        Task<GoogleLoginResult> task = new Task<>() {
            @Override
            protected GoogleLoginResult call() {
                // InputStream try-with-resources OK
                try (InputStream stream = getClass().getResourceAsStream("/drivesync/NO-GITHUB/client_secret.json")) {
                    if (stream == null) {
                        return GoogleLoginResult.error("A client_secret.json nem található!");
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

                    // LocalServerReceiver NEM AutoCloseable -> kezeljük manuálisan
                    LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
                    try {
                        Credential credential = flow.loadCredential("user");
                        if (credential == null || credential.getAccessToken() == null) {
                            credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
                        } else if (credential.getExpiresInSeconds() != null && credential.getExpiresInSeconds() <= 60) {
                            credential.refreshToken();
                        }

                        if (credential == null || credential.getAccessToken() == null) {
                            return GoogleLoginResult.error("Nem kaptunk hozzáférést a Google-től.");
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
                            return GoogleLoginResult.error("Nem sikerült az email lekérése!");
                        }

                        // DB művelet: új user beszúrása, ha nincs
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
                                insertStmt.setString(3, "");
                                insertStmt.executeUpdate();
                                insertStmt.close();
                            }
                            checkStmt.close();
                        }

                        Preferences prefs = Preferences.userNodeForPackage(MainController.class);
                        prefs.put("google_email", email);

                        return GoogleLoginResult.success(name, email);

                    } finally {
                        // mindig állítsuk le a receiver-t, különben a lokális szerver futva maradhat
                        try {
                            receiver.stop();
                        } catch (Exception stopEx) {
                            // csak logoljuk, ne dobjuk el (a Task a fő hibakezelő)
                            stopEx.printStackTrace();
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    return GoogleLoginResult.error("Nem sikerült a Google-bejelentkezés: " + e.getMessage());
                }
            }
        };

        // Sikeres befejezés (JavaFX Application Thread-en fut)
        task.setOnSucceeded(evt -> {
            GoogleLoginResult result = task.getValue();
            googleLoginButton.setDisable(false);
            googleLoginButton.setText(originalText);

            if (!result.success) {
                showAlert(Alert.AlertType.ERROR, "Hiba", result.message);
                return;
            }

            showAlert(Alert.AlertType.INFORMATION, "Sikeres bejelentkezés",
                    "Üdv, " + result.name + " (" + result.email + ")");

            Stage stage = (Stage) googleLoginButton.getScene().getWindow();
            cleanupMediaPlayer();
            loadHomeScene(result.name, stage);
        });

        task.setOnFailed(evt -> {
            Throwable ex = task.getException();
            ex.printStackTrace();
            googleLoginButton.setDisable(false);
            googleLoginButton.setText(originalText);
            showAlert(Alert.AlertType.ERROR, "Hiba", "Nem sikerült a Google-bejelentkezés:\n" + ex.getMessage());
        });

        task.setOnCancelled(evt -> {
            googleLoginButton.setDisable(false);
            googleLoginButton.setText(originalText);
            showAlert(Alert.AlertType.INFORMATION, "Megszakítva", "A Google-bejelentkezés megszakadt.");
        });

        Thread t = new Thread(task, "google-login-task");
        t.setDaemon(true);
        t.start();
    }

    // Egyszerű wrapper eredmény visszaadására
    private static class GoogleLoginResult {
        final boolean success;
        final String name;
        final String email;
        final String message;

        private GoogleLoginResult(boolean success, String name, String email, String message) {
            this.success = success;
            this.name = name;
            this.email = email;
            this.message = message;
        }

        static GoogleLoginResult success(String name, String email) {
            return new GoogleLoginResult(true, name, email, null);
        }

        static GoogleLoginResult error(String message) {
            return new GoogleLoginResult(false, null, null, message);
        }
    }



    private void autoLoginIfPossible() {
        Preferences prefs = Preferences.userNodeForPackage(MainController.class);
        String savedUsername = prefs.get("username", null);

        if (savedUsername != null) {
            Stage stage = (Stage) loginPane.getScene().getWindow();
            cleanupMediaPlayer();
            loadHomeScene(savedUsername, stage);
        }
    }

    private static boolean remindersShown = false;

    private void loadHomeScene(String username, Stage stage) {
        try {
            // Főoldal betöltése
            Scene home = App.getHomeScene();
            HomeController hc = App.getHomeController();
            if (hc != null) hc.setUsername(username);

            stage.setScene(home);

            // Ablak középre igazítása
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            stage.setX((screenBounds.getWidth() - 1200) / 2);
            stage.setY((screenBounds.getHeight() - 700) / 2);

            // CarReminderPopup egyszeri megjelenítése
            if (!remindersShown) {
                remindersShown = true;               // egyszeri flag beállítása
                CarReminderPopup popup = new CarReminderPopup();
                popup.showReminders(username);       // popup megjelenítése
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void setupSidebarVideo() {
        try {
            String videoPath = getClass().getResource("/drivesync/Videók/intro.mp4").toExternalForm();
            mediaPlayer = new MediaPlayer(new Media(videoPath));

            mediaPlayer.setAutoPlay(true);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            mediaPlayer.setMute(true);

            sidebarVideo.setMediaPlayer(mediaPlayer);

        } catch (Exception e) {
            System.err.println("Videó hiba: " + e.getMessage());
        }
    }

    private void cleanupMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
    }

    private void showAlert(Alert.AlertType t, String title, String msg) {
        Platform.runLater(() -> {
            Alert a = new Alert(t);
            a.setTitle(title);
            a.setHeaderText(null);
            a.setContentText(msg);
            a.showAndWait();
        });
    }
}
