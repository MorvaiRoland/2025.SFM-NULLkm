package drivesync;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

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
    private static boolean remindersShown = false;

    @FXML
    private void initialize() {
        loadDBConfig();
        setupPasswordToggle();
        setupSidebarVideo();

        // Elhalasztjuk az ablak műveletet addig, amíg az App.java hozzáadja a Scene-hez
        Platform.runLater(this::autoLoginIfPossible);
    }

    private void loadDBConfig() {
        // A konfigurációs útvonal feltételezve, hogy a drivesync/Adatbázis/db_config.properties a helyes.
        // Ha /drivesync/NO-GITHUB/db_config.properties a helyes, módosítsd az útvonalat!
        try (InputStream input = getClass().getResourceAsStream("/drivesync/Adatbazis/db_config.properties")) {
            Properties props = new Properties();
            if (input == null) {
                System.err.println("Hiba: db_config.properties nem található!");
                return;
            }
            props.load(input);

            DB_URL = props.getProperty("db.url");
            DB_USER = props.getProperty("db.user");
            DB_PASS = props.getProperty("db.password");

        } catch (Exception e) {
            System.err.println("Hiba az adatbázis konfiguráció betöltésekor: " + e.getMessage());
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

        // Jelszó ellenőrzése SHA2(?, 256) használatával
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE username=? AND password=SHA2(?, 256)")) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                if (rememberMeCheck.isSelected()) {
                    Preferences prefs = Preferences.userNodeForPackage(MainController.class);
                    prefs.put("username", username);
                }

                // Sikeres bejelentkezés esetén nem zavarjuk a felhasználót, rögtön váltunk a főoldalra.
                // A siker visszajelzését a főoldalon is megtehetjük, vagy itt hívhatunk egy rövid Toast-ot, ha szükséges.

                Stage stage = (Stage) loginButton.getScene().getWindow();
                cleanupMediaPlayer();
                loadHomeScene(username, stage);

            } else {
                showAlert(Alert.AlertType.ERROR, "Hiba", "Helytelen felhasználónév vagy jelszó!");
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Adatbázis Hiba", e.getMessage());
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
                     "INSERT INTO users (username, email, password, isDark) VALUES (?, ?, SHA2(?, 256), 0)")) {

            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, password);
            stmt.executeUpdate();

            showLogin();

            // VÁLTOZÁS 1: Nem blokkoló Toast a sikeres regisztrációhoz
            showToast("Siker", "Sikeres regisztráció! Jelentkezz be.", Duration.seconds(3));

        } catch (SQLIntegrityConstraintViolationException e) {
            showAlert(Alert.AlertType.ERROR, "Adatbázis hiba", "A felhasználónév vagy email cím már foglalt.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Adatbázis Hiba", e.getMessage());
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
        googleLoginButton.setDisable(true);
        String originalText = googleLoginButton.getText();
        googleLoginButton.setText("Bejelentkezés...");

        Task<GoogleLoginResult> task = new Task<>() {
            @Override
            protected GoogleLoginResult call() {
                try (InputStream stream = getClass().getResourceAsStream("/drivesync/NO-GITHUB/client_secret.json")) {
                    if (stream == null) {
                        return GoogleLoginResult.error("A client_secret.json nem található!");
                    }

                    GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                            GsonFactory.getDefaultInstance(), new InputStreamReader(stream)
                    );

                    FileDataStoreFactory dataStoreFactory = new FileDataStoreFactory(
                            new File(System.getProperty("user.home"), ".drivesync_credentials")
                    );

                    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                            GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance(), clientSecrets,
                            Arrays.asList("https://www.googleapis.com/auth/userinfo.profile", "https://www.googleapis.com/auth/userinfo.email")
                    ).setAccessType("offline").setDataStoreFactory(dataStoreFactory).build();

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
                                GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance(), credential
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
                        try {
                            receiver.stop();
                        } catch (Exception stopEx) {
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

            // VÁLTOZÁS 2: Nem blokkoló Toast a sikeres bejelentkezéshez
            showToast("Sikeres bejelentkezés",
                    "Üdv, " + result.name + " (" + result.email + ")",
                    Duration.seconds(3));

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
            showToast("Megszakítva", "A Google-bejelentkezés megszakadt.", Duration.seconds(2));
        });

        Thread t = new Thread(task, "google-login-task");
        t.setDaemon(true);
        t.start();
    }

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


    /**
     * Megpróbál automatikusan bejelentkezni, ha van mentett felhasználónév a Preferences-ben.
     */
    private void autoLoginIfPossible() {
        Preferences prefs = Preferences.userNodeForPackage(MainController.class);
        String savedUsername = prefs.get("username", null);

        if (savedUsername != null) {
            Scene currentScene = loginPane.getScene();

            if (currentScene != null && currentScene.getWindow() instanceof Stage stage) {
                cleanupMediaPlayer();
                loadHomeScene(savedUsername, stage);
            } else {
                System.err.println("Figyelmeztetés: Az automatikus bejelentkezés Stage objektuma nem érhető el.");
            }
        }
    }

    private void loadHomeScene(String username, Stage stage) {
        if (stage == null) {
            showAlert(Alert.AlertType.ERROR, "Hiba", "Stage is null!");
            return;
        }

        try {
            // Ellenőrizzük, hogy az App osztályban megvan-e a Scene
            Scene home = App.getHomeScene();
            if (home == null) {
                // Ha ez a hiba jön, akkor az App.java nem tudta betölteni a Home.fxml-t (valószínűleg az ékezetek miatt)
                throw new RuntimeException("A Főoldal (HomeScene) nem lett előre betöltve az App.java-ban!");
            }

            HomeController hc = App.getHomeController();
            if (hc != null) {
                hc.setUsername(username);
            } else {
                throw new RuntimeException("A HomeController nem érhető el (null)!");
            }

            stage.setScene(home);

            // Ablak középre
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2);
            stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);

            // Popup kezelés hibaellenőrzéssel
            if (!remindersShown) {
                try {
                    remindersShown = true;
                    CarReminderPopup popup = new CarReminderPopup();
                    popup.showReminders(username);
                } catch (Exception popupError) {
                    // Ha a popup hibás, ne állítsa meg a belépést, csak írja ki
                    System.err.println("Popup hiba: " + popupError.getMessage());
                    // Opcionális: showAlert(Alert.AlertType.WARNING, "Popup Hiba", popupError.getMessage());
                }
            }

        } catch (Exception e) {
            // EZ A LÉNYEG: Írjuk ki a hibát ablakban!
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Betöltési Hiba",
                    "Nem sikerült betölteni a főoldalt:\n" + e.getMessage() + "\n\n" + e.toString());
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

    /**
     * VÁLTOZÁS 3: Nem-blokkoló, időzített TOAST értesítés megjelenítése (új funkció).
     */
    private void showToast(String title, String msg, Duration duration) {
        Platform.runLater(() -> {
            Stage toastStage = new Stage();
            toastStage.initStyle(StageStyle.UNDECORATED);
            toastStage.setAlwaysOnTop(true);
            toastStage.setResizable(false);

            // Stílusos Label konténer
            Label label = new Label(msg);
            label.setStyle("-fx-padding: 10 20; -fx-background-color: rgba(50, 50, 50, 0.95); -fx-text-fill: white; -fx-background-radius: 5; -fx-font-weight: bold;");

            VBox root = new VBox(label);
            root.setStyle("-fx-background-color: transparent;");

            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            toastStage.setScene(scene);

            // Pozicionálás: Képernyő tetején, középen
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            toastStage.setX((screenBounds.getWidth() - label.getWidth()) / 2);
            toastStage.setY(50); // Közel a képernyő tetejéhez

            toastStage.show();

            Timeline timeline = new Timeline(new KeyFrame(duration, e -> toastStage.close()));
            timeline.play();
        });
    }


    /**
     * VÁLTOZÁS 4: Blokkoló showAlert csak KRITIKUS HIBÁKHOZ.
     */
    private void showAlert(Alert.AlertType t, String title, String msg) {
        Platform.runLater(() -> {
            Alert a = new Alert(t);
            a.setTitle(title);
            a.setHeaderText(null);
            a.setContentText(msg);

            // Csak a hibák és a megerősítések blokkolnak
            if (t == Alert.AlertType.ERROR || t == Alert.AlertType.CONFIRMATION || t == Alert.AlertType.WARNING) {
                a.showAndWait();
            } else {
                // Az INFO ablakokat a showToast veszi át, de ha mégis hívódna, ne blokkoljon.
                a.show();
            }
        });
    }
}