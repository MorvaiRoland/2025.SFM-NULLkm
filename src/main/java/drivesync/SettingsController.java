package drivesync;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

public class SettingsController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label regDateLabel;

    @FXML private CheckBox darkModeCheck;
    @FXML private CheckBox notificationsCheck;
    @FXML private CheckBox autoSyncCheck;

    @FXML private TextField pdfPathField;

    @FXML private Button saveBtn;
    @FXML private Button resetBtn;

    private Stage primaryStage;

    // Például a beállításokat tárolhatjuk változókban
    private String savedName = "példa";
    private String savedEmail = "példa@example.com";
    private String savedPassword = "";
    private boolean savedDarkMode = false;
    private boolean savedNotifications = true;
    private boolean savedAutoSync = true;
    private String savedPdfPath = System.getProperty("user.home");

    @FXML
    private void initialize() {
        loadSettings();
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    private void loadSettings() {
        nameField.setText(savedName);
        emailField.setText(savedEmail);
        passwordField.setText(savedPassword);
        regDateLabel.setText("2025-01-01"); // példa

        darkModeCheck.setSelected(savedDarkMode);
        notificationsCheck.setSelected(savedNotifications);
        autoSyncCheck.setSelected(savedAutoSync);
        pdfPathField.setText(savedPdfPath);
    }

    @FXML
    private void handleSave() {
        savedName = nameField.getText();
        savedEmail = emailField.getText();
        savedPassword = passwordField.getText();
        savedDarkMode = darkModeCheck.isSelected();
        savedNotifications = notificationsCheck.isSelected();
        savedAutoSync = autoSyncCheck.isSelected();
        savedPdfPath = pdfPathField.getText();

        // Itt ténylegesen lehet menteni fájlba vagy adatbázisba
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Mentés");
        alert.setHeaderText(null);
        alert.setContentText("Beállítások elmentve!");
        alert.showAndWait();
    }

    @FXML
    private void handleReset() {
        // Alapértelmezett értékek visszaállítása
        savedName = "";
        savedEmail = "";
        savedPassword = "";
        savedDarkMode = false;
        savedNotifications = false;
        savedAutoSync = false;
        savedPdfPath = System.getProperty("user.home");

        loadSettings();

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Alapértelmezett");
        alert.setHeaderText(null);
        alert.setContentText("Beállítások visszaállítva az alapértelmezett értékekre!");
        alert.showAndWait();
    }

    @FXML
    private void handleBrowsePdfPath() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("PDF mentési útvonal kiválasztása");
        chooser.setInitialDirectory(new File(pdfPathField.getText()));
        File selectedDir = chooser.showDialog(primaryStage);

        if (selectedDir != null) {
            pdfPathField.setText(selectedDir.getAbsolutePath());
        }
    }
}
