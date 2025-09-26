package drivesync;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class MainController {

    // Jobb panel login/register
    @FXML private VBox loginPane, registerPane;
    @FXML private TextField loginUsername, regUsername, regEmail;
    @FXML private PasswordField loginPassword, regPassword, regPasswordConfirm;
    @FXML private TextField regPasswordVisible, regPasswordConfirmVisible;
    @FXML private Button loginButton, registerButton;
    @FXML private CheckBox showPasswordCheck;

    // Bal panel – statikus kép
    @FXML private ImageView panelImage;

    @FXML
    private void initialize() {
        // Logó betöltése
        Image logo = loadImage("/drivesync/logo.png"); // ide a nagy kép
        if (logo != null) {
            panelImage.setImage(logo);
        }

        setupPasswordToggle();
    }

    private Image loadImage(String path) {
        try {
            return new Image(getClass().getResourceAsStream(path));
        } catch (Exception e) {
            System.out.println("Nem található a kép: " + path);
            return null;
        }
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
