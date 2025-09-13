package gr2536.fxui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;


/**
 * JavaFX entrypoint for FridgeApp.
 */
public class FridgeApp extends Application{


    /**
     * Sets up and displays primary stage.
     * 
     * @param primaryStage the main window provided by JavaFX
     * @throws Exception if the FXML or scene creation fails
     */
    @Override
    public void start(final Stage primaryStage) throws Exception {
        primaryStage.setTitle("FridgeApp");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("FridgeApp.fxml"));
        Scene scene = new Scene(loader.load());

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Standard JavaFX launcher.
     * 
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}