package drivesync;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class MainController {

    // Jobb panel – login/register
    @FXML private VBox loginPane, registerPane;
    @FXML private TextField loginUsername, regUsername, regEmail;
    @FXML private PasswordField loginPassword, regPassword, regPasswordConfirm;
    @FXML private TextField loginPasswordVisible, regPasswordVisible, regPasswordConfirmVisible;
    @FXML private Button loginButton, registerButton;
    @FXML private CheckBox showLoginPasswordCheck, showPasswordCheck;

    @FXML private ImageView logoImage;

    @FXML
    private void initialize() {
        // Logo betöltése
        Image logo = new Image(getClass().getResourceAsStream("/drivesync/logo.png"));
        logoImage.setImage(logo);

        // Jelszó mutatás beállítása
        setupPasswordToggle();
    }

    private void setupPasswordToggle() {
        // Regisztrációs panelek
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

        // Login panel
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
        System.out.println("Login: " + loginUsername.getText());
    }

    @FXML
    private void handleRegister() {
        System.out.println("Register: " + regUsername.getText() + ", " + regEmail.getText());
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
}
