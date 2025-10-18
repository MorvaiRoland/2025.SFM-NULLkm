package drivesync;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

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

        stage.setTitle("DriveSync");
        stage.setScene(loginScene);
        stage.setResizable(false);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/drivesync/Logók/DriveSync logo-2.png")));
        stage.show();

        // Home scene előre betöltése
        FXMLLoader homeLoader = new FXMLLoader(getClass().getResource("/drivesync/Menü/Home.fxml"));
        homeScene = new Scene(homeLoader.load(), 1200, 700);
        homeController = homeLoader.getController();

        // Automatikus login ellenőrzés
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(MainController.class);
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
}
