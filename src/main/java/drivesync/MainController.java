package drivesync;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

import java.io.IOException;
import java.util.prefs.Preferences;

public class MainController {

    @FXML private VBox loginPane, registerPane;
    @FXML private TextField loginUsername, regUsername, regEmail;
    @FXML private PasswordField loginPassword, regPassword, regPasswordConfirm;
    @FXML private TextField loginPasswordVisible, regPasswordVisible, regPasswordConfirmVisible;
    @FXML private Button loginButton, registerButton;
    @FXML private CheckBox showLoginPasswordCheck, showPasswordCheck, rememberMeCheck;
    @FXML private ImageView logoImage;

    @FXML
    private void initialize() {
        // Logo betöltése
        Image logo = new Image(getClass().getResourceAsStream("/drivesync/logo.png"));
        logoImage.setImage(logo);

        setupPasswordToggle();
    }

    private void setupPasswordToggle() {
        // ===== LOGIN jelszó =====
        loginPasswordVisible.managedProperty().bind(showLoginPasswordCheck.selectedProperty());
        loginPasswordVisible.visibleProperty().bind(showLoginPasswordCheck.selectedProperty());
        loginPassword.managedProperty().bind(showLoginPasswordCheck.selectedProperty().not());
        loginPassword.visibleProperty().bind(showLoginPasswordCheck.selectedProperty().not());
        loginPassword.textProperty().bindBidirectional(loginPasswordVisible.textProperty());

        // ===== REGISTER jelszavak =====
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

    @FXML
    private void handleLogin() {
        Login login = new Login(loginButton, loginUsername, loginPassword);
        if (login.loginUser()) {
            // "Maradjak bejelentkezve"
            if (rememberMeCheck.isSelected()) {
                Preferences prefs = Preferences.userNodeForPackage(MainController.class);
                prefs.put("username", loginUsername.getText().trim());
            }

            Stage stage = (Stage) loginButton.getScene().getWindow();
            loadHomeSceneAfterStageShown(loginUsername.getText().trim(), stage);

        } else {
            showAlert(Alert.AlertType.ERROR, "Hiba", "Helytelen felhasználónév vagy jelszó!");
        }
    }

    public void loadHomeSceneAfterStageShown(String username, Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/drivesync/Home.fxml"));
            Scene homeScene = new Scene(loader.load(), 1200, 700);

            HomeController homeController = loader.getController();
            homeController.setUsername(username);

            stage.setScene(homeScene);
            stage.setTitle("DriveSync - Főoldal");

            // Képernyő középre
            javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
            stage.setX((screenBounds.getWidth() - 1200) / 2);
            stage.setY((screenBounds.getHeight() - 700) / 2);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRegister() {
        Register reg = new Register(registerButton, regUsername, regEmail, regPassword, regPasswordConfirm);
        if (reg.registerUser()) {
            showLogin();
            showAlert(Alert.AlertType.INFORMATION, "Siker", "Sikeres regisztráció! Most bejelentkezhetsz.");
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

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
