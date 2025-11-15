package drivesync.Bejelentkezés;

import drivesync.Adatbázis.Database;
import drivesync.App;
import drivesync.HomeController;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.prefs.Preferences;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisibleField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private CheckBox rememberMeCheck;
    @FXML private CheckBox showPasswordCheck;
    @FXML private MediaView sidebarVideo;
    @FXML private Label videoCaption;

    @FXML
    private void initialize() {
        setupPasswordToggle();
        setupSidebarVideo();

        // Automatikus login
        Preferences prefs = Preferences.userNodeForPackage(LoginController.class);
        String rememberedUser = prefs.get("username", null);
        if (rememberedUser != null) {
            openHome(rememberedUser);
        }
    }

    private void setupPasswordToggle() {
        if (passwordVisibleField != null && passwordField != null && showPasswordCheck != null) {
            showPasswordCheck.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (isSelected) {
                    passwordVisibleField.setText(passwordField.getText());
                    passwordVisibleField.setVisible(true);
                    passwordField.setVisible(false);
                } else {
                    passwordField.setText(passwordVisibleField.getText());
                    passwordField.setVisible(true);
                    passwordVisibleField.setVisible(false);
                }
            });
        }
    }

    private void setupSidebarVideo() {
        try {
            // Videó betöltése resources mappából
            String videoPath = getClass().getResource("/drivesync/Videók/intro.mp4").toExternalForm();
            Media media = new Media(videoPath);
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setAutoPlay(true);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE); // loop
            sidebarVideo.setMediaPlayer(mediaPlayer);

            // Felirat
            videoCaption.setText("Üdvözlünk a DriveSync-ben!");
        } catch (Exception e) {
            e.printStackTrace();
            if (videoCaption != null) {
                videoCaption.setText("Videó betöltése sikertelen!");
            }
        }
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = showPasswordCheck.isSelected() ? passwordVisibleField.getText().trim() : passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Kérlek, töltsd ki az összes mezőt!");
            return;
        }

        if (loginUser(username, password)) {
            if (rememberMeCheck.isSelected()) {
                Preferences prefs = Preferences.userNodeForPackage(LoginController.class);
                prefs.put("username", username);
            }
            openHome(username);
        } else {
            errorLabel.setText("Hibás felhasználónév vagy jelszó!");
        }
    }

    private boolean loginUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = SHA2(?, 256)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void openHome(String username) {
        try {
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(App.getHomeScene());
            stage.setTitle("DriveSync");

            HomeController controller = App.getHomeController();
            if (controller != null) {
                controller.setUsername(username);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
