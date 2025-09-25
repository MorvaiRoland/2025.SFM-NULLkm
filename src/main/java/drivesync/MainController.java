package drivesync;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

public class MainController {

    @FXML
    private ImageView logoImage;

    @FXML
    private TextField loginUsername, regUsername, regEmail, regVnev, regKnev;

    @FXML
    private PasswordField loginPassword, regPassword;

    @FXML
    private void initialize() {
        // Logo betöltése
        Image logo = new Image(getClass().getResourceAsStream("/drivesync/DriveSync logo-2.png"));
        logoImage.setImage(logo);

        // Animáció: finom fade-in a logo
        FadeTransition ft = new FadeTransition(Duration.seconds(2), logoImage);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    @FXML
    private void handleLogin() {
        String username = loginUsername.getText();
        String password = loginPassword.getText();

        // Ide jöhet a DB ellenőrzés
        System.out.println("Login próbálkozás: " + username);
    }

    @FXML
    private void handleRegister() {
        String username = regUsername.getText();
        String email = regEmail.getText();
        String password = regPassword.getText();
        String vnev = regVnev.getText();
        String knev = regKnev.getText();

        // Ide jöhet a DB insert
        System.out.println("Regisztráció: " + username + ", " + email);
    }
}
