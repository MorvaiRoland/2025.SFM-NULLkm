package drivesync;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class HomeController {

    @FXML private Label welcomeLabel;
    @FXML private Button logoutButton;

    public void setUsername(String username) {
        welcomeLabel.setText("Üdv, " + username + "!");
    }

    @FXML
    private void handleLogout() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        stage.setScene(App.getLoginScene());
        stage.setTitle("DriveSync");

        // opcionálisan törölhetjük a login mezőket
        // ((TextField)App.getLoginScene().lookup("#loginUsername")).clear();
        // ((PasswordField)App.getLoginScene().lookup("#loginPassword")).clear();
    }
}
