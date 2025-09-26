package drivesync;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {

    @FXML private VBox loginPane, registerPane;
    @FXML private TextField loginUsername, regUsername, regEmail;
    @FXML private PasswordField loginPassword, regPassword, regPasswordConfirm;
    @FXML private TextField loginPasswordVisible, regPasswordVisible, regPasswordConfirmVisible;
    @FXML private Button loginButton, registerButton;
    @FXML private CheckBox showLoginPasswordCheck, showPasswordCheck;
    @FXML private ImageView logoImage;

    @FXML
    private void initialize() {
        Image logo = new Image(getClass().getResourceAsStream("/drivesync/logo.png"));
        logoImage.setImage(logo);
        setupPasswordToggle();
    }

    private void setupPasswordToggle() {
        showPasswordCheck.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                regPasswordVisible.setText(regPassword.getText());
                regPasswordVisible.setVisible(true);
                regPassword.setVisible(false);

                regPasswordConfirmVisible.setText(regPasswordConfirm.getText());
                regPasswordConfirmVisible.setVisible(true);
                regPasswordConfirm.setVisible(false);
            } else {
                regPassword.setText(regPasswordVisible.getText());
                regPassword.setVisible(true);
                regPasswordVisible.setVisible(false);

                regPasswordConfirm.setText(regPasswordConfirmVisible.getText());
                regPasswordConfirm.setVisible(true);
                regPasswordConfirmVisible.setVisible(false);
            }
        });

        showLoginPasswordCheck.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                loginPasswordVisible.setText(loginPassword.getText());
                loginPasswordVisible.setVisible(true);
                loginPassword.setVisible(false);
            } else {
                loginPassword.setText(loginPasswordVisible.getText());
                loginPassword.setVisible(true);
                loginPasswordVisible.setVisible(false);
            }
        });
    }

    @FXML
    private void handleLogin() {
        Login login = new Login(loginButton, loginUsername, loginPassword);
        if (login.loginUser()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/drivesync/Home.fxml"));
                Scene homeScene = new Scene(loader.load(), 1200, 700); // megnövelt méret

                HomeController homeController = loader.getController();
                homeController.setUsername(loginUsername.getText().trim());

                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.setScene(homeScene);
                stage.setTitle("DriveSync - Főoldal");

                // Home oldal középre
                javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
                stage.setX((screenBounds.getWidth() - 1200) / 2);
                stage.setY((screenBounds.getHeight() - 700) / 2);

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Hiba", "Helytelen felhasználónév vagy jelszó!");
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
