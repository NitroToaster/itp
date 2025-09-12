package gr2536.fxui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class FridgeApp extends Application{

    @Override
    public void start(final Stage primaryStage) throws Exception {
        primaryStage.setTitle("FridgeApp");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("FridgeApp.fxml"));
        Scene scene = new Scene(loader.load());

        primaryStage.setScene(scene);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}