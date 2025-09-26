package drivesync;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class MainController {

    @FXML private ImageView logoImage, panelImage;
    @FXML private VBox loginPane, registerPane;
    @FXML private TextField loginUsername, regUsername, regEmail;
    @FXML private PasswordField loginPassword, regPassword, regPasswordConfirm;
    @FXML private TextField regPasswordVisible, regPasswordConfirmVisible;
    @FXML private CheckBox showPasswordCheck;
    @FXML private Button loginButton, registerButton;

    private List<Image> carImages = new ArrayList<>();
    private int currentImage = 0;

    @FXML
    private void initialize() {
        // Logo betöltése
        logoImage.setImage(new Image(getClass().getResourceAsStream("/drivesync/DriveSync logo-2.png")));

        // Autós képek betöltése
        carImages.add(new Image(getClass().getResourceAsStream("/drivesync/panel_car1.png")));
        carImages.add(new Image(getClass().getResourceAsStream("/drivesync/panel_car2.png")));

        panelImage.setImage(carImages.get(0));

        // Carousel animáció
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), e -> switchImage()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        // Logo animáció
        FadeTransition ft = new FadeTransition(Duration.seconds(1.5), logoImage);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();

        setupButtonAnimation(loginButton);
        setupButtonAnimation(registerButton);

        // Jelszó láthatóság kapcsoló
        showPasswordCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                regPasswordVisible.setText(regPassword.getText());
                regPasswordConfirmVisible.setText(regPasswordConfirm.getText());
                regPasswordVisible.setVisible(true);
                regPasswordConfirmVisible.setVisible(true);
                regPassword.setVisible(false);
                regPasswordConfirm.setVisible(false);
            } else {
                regPassword.setText(regPasswordVisible.getText());
                regPasswordConfirm.setText(regPasswordConfirmVisible.getText());
                regPassword.setVisible(true);
                regPasswordConfirm.setVisible(true);
                regPasswordVisible.setVisible(false);
                regPasswordConfirmVisible.setVisible(false);
            }
        });
    }

    private void switchImage() {
        currentImage = (currentImage + 1) % carImages.size();
        panelImage.setImage(carImages.get(currentImage));
    }

    private void setupButtonAnimation(Button button) {
        button.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), button);
            st.setToX(1.05); st.setToY(1.05); st.play();
        });
        button.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), button);
            st.setToX(1); st.setToY(1); st.play();
        });
    }

    @FXML
    private void handleLogin() {
        System.out.println("Login: " + loginUsername.getText());
    }

    @FXML
    private void handleRegister() {
        String username = regUsername.getText();
        String email = regEmail.getText();
        String password = showPasswordCheck.isSelected() ? regPasswordVisible.getText() : regPassword.getText();
        String confirmPassword = showPasswordCheck.isSelected() ? regPasswordConfirmVisible.getText() : regPasswordConfirm.getText();

        if (!password.equals(confirmPassword)) {
            System.out.println("Hiba: a jelszavak nem egyeznek!");
            return;
        }

        System.out.println("Register: " + username + ", " + email);
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
