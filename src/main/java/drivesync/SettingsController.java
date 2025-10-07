package drivesync;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class SettingsController {

    // Felhasználói adatok
    @FXML
    private TextField nameField;

    @FXML
    private TextField emailField;

    @FXML
    private Label regDateLabel;

    // Alapértelmezett opciók
    @FXML
    private CheckBox notificationsCheck;

    @FXML
    private CheckBox darkModeCheck;

    @FXML
    private CheckBox autoSyncCheck;

    // Extra opciók
    @FXML
    private CheckBox showTooltipsCheck;

    @FXML
    private CheckBox enableLogsCheck;

    // Gombok
    @FXML
    private Button saveBtn;

    @FXML
    private Button deleteBtn;

    /**
     * Inicializálás: például betöltéskor felhasználói adatok beállítása
     */
    @FXML
    private void initialize() {
        // Példaértékek betöltése
        nameField.setText("Morva Péter");
        emailField.setText("morva@example.com");
        regDateLabel.setText("2025-01-01");

        notificationsCheck.setSelected(true);
        darkModeCheck.setSelected(false);
        autoSyncCheck.setSelected(true);

        showTooltipsCheck.setSelected(true);
        enableLogsCheck.setSelected(false);
    }

    /**
     * Mentés gomb kezelő
     */
    @FXML
    private void handleSave() {
        System.out.println("Beállítások mentése:");
        System.out.println("Név: " + nameField.getText());
        System.out.println("Email: " + emailField.getText());
        System.out.println("Értesítések: " + notificationsCheck.isSelected());
        System.out.println("Sötét mód: " + darkModeCheck.isSelected());
        System.out.println("Automatikus szinkron: " + autoSyncCheck.isSelected());
        System.out.println("Tooltipok: " + showTooltipsCheck.isSelected());
        System.out.println("Naplózás: " + enableLogsCheck.isSelected());

        // Ide lehet tenni tényleges mentést fájlba vagy adatbázisba
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Mentés");
        alert.setHeaderText(null);
        alert.setContentText("Beállítások elmentve!");
        alert.showAndWait();
    }

    /**
     * Törlés gomb kezelő
     */
    @FXML
    private void handleDelete() {
        // Alapértelmezett értékek visszaállítása
        nameField.setText("");
        emailField.setText("");
        notificationsCheck.setSelected(false);
        darkModeCheck.setSelected(false);
        autoSyncCheck.setSelected(false);
        showTooltipsCheck.setSelected(false);
        enableLogsCheck.setSelected(false);

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Törlés");
        alert.setHeaderText(null);
        alert.setContentText("Beállítások törölve!");
        alert.showAndWait();
    }
}
