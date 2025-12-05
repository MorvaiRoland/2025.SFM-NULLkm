package drivesync;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.prefs.Preferences;

public class App extends Application {

    private static Scene loginScene;
    private static Scene homeScene;
    private static HomeController homeController;

    @Override
    public void start(Stage stage) throws Exception {
        // Login scene betöltése
        FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("/drivesync/LOG/REG/main.fxml"));
        loginScene = new Scene(loginLoader.load(), 900, 600);
        loginScene.getStylesheets().add(getClass().getResource("/drivesync/CSS/style.css").toExternalForm());

        // Globális beállítások alkalmazása (téma, betűméret)
        applyGlobalPreferences(loginScene);

        stage.setTitle("DriveSync");
        stage.setScene(loginScene);
        stage.setResizable(false);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/drivesync/Logok/DriveSync logo-2.png")));
        stage.show();

        // Finom belépő animáció (első benyomás)
        if (loginScene.getRoot() != null) {
            loginScene.getRoot().setOpacity(0);
            FadeTransition ft = new FadeTransition(Duration.millis(220), loginScene.getRoot());
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();
        }

        // Home scene előre betöltése
        FXMLLoader homeLoader = new FXMLLoader(getClass().getResource("/drivesync/Menü/Home.fxml"));
        homeScene = new Scene(homeLoader.load(), 1200, 700);
        homeScene.getStylesheets().add(getClass().getResource("/drivesync/CSS/style.css").toExternalForm());
        homeController = homeLoader.getController();
        applyGlobalPreferences(homeScene);

        // Automatikus login ellenőrzés
        Preferences prefs = Preferences.userNodeForPackage(MainController.class);
        String rememberedUser = prefs.get("username", null);
        if (rememberedUser != null) {
            homeController.setUsername(rememberedUser);
            stage.setScene(homeScene);
        }
    }

    public static Scene getLoginScene() { return loginScene; }

    public static Scene getHomeScene() { return homeScene; }

    public static HomeController getHomeController() { return homeController; }

    public static void main(String[] args) {
        launch();
    }

    /**
     * Alkalmazza a globális felhasználói beállításokat (téma és betűméret)
     * Ha nincs tárolva téma a Preferences-ben, alapból világos ("light") témát alkalmaz.
     */
    private void applyGlobalPreferences(Scene scene) {
        if (scene == null || scene.getRoot() == null) return;

        Preferences prefs = Preferences.userNodeForPackage(App.class);
        prefs.remove("theme");

        // Ha nincs mentett téma, alapból világos
        String theme = prefs.get("theme", "Világos");
        double fontSize = prefs.getDouble("fontSize", 14.0);

        // Mindig töröljük az előző téma-osztályokat
        scene.getRoot().getStyleClass().removeAll("theme-dark", "theme-light");

        // Alkalmazzuk a témát
        if ("Sötét".equalsIgnoreCase(theme)) {
            scene.getRoot().getStyleClass().add("theme-dark");
        } else {
            scene.getRoot().getStyleClass().add("theme-light");
        }

        // Betűméret
        scene.getRoot().setStyle(String.format("-fx-font-size: %.0fpx;", fontSize));
    }


    /**
     * Egységes stílus alkalmazása JavaFX dialógusokra (Alert, Dialog)
     */
    public static void styleDialog(Dialog<?> dialog) {
        if (dialog == null || dialog.getDialogPane() == null) return;
        try {
            var pane = dialog.getDialogPane();
            String css = App.class.getResource("/drivesync/CSS/style.css").toExternalForm();
            if (!pane.getStylesheets().contains(css)) {
                pane.getStylesheets().add(css);
            }

            Preferences prefs = Preferences.userNodeForPackage(App.class);
            String theme = prefs.get("theme", "Világos");
            double fontSize = prefs.getDouble("fontSize", 14.0);

            pane.getStyleClass().removeAll("theme-dark", "theme-light");
            if ("Sötét".equalsIgnoreCase(theme)) {
                pane.getStyleClass().add("theme-dark");
            } else {
                pane.getStyleClass().add("theme-light");
            }

            pane.setStyle(String.format("-fx-font-size: %.0fpx;", fontSize));
        } catch (Exception ignored) {}
    }
}
