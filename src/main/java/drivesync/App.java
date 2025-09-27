package drivesync;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.prefs.Preferences;

public class App extends Application {

    private static Scene loginScene; // eltároljuk a login scene-t

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/drivesync/Main.fxml"));
        loginScene = new Scene(loader.load(), 900, 600);

        // CSS betöltése
        loginScene.getStylesheets().add(getClass().getResource("/drivesync/style.css").toExternalForm());

        stage.setTitle("DriveSync");
        stage.setScene(loginScene);
        stage.setResizable(false);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/drivesync/DriveSync logo-2.png")));
        stage.show();

        // Ellenőrizzük, van-e mentett felhasználó
        Preferences prefs = Preferences.userNodeForPackage(MainController.class);
        String rememberedUser = prefs.get("username", null);
        if (rememberedUser != null) {
            MainController controller = loader.getController();
            controller.loadHomeSceneAfterStageShown(rememberedUser, stage);
        }
    }

    public static Scene getLoginScene() {
        return loginScene;
    }

    public static void main(String[] args) {
        launch();
    }
}
