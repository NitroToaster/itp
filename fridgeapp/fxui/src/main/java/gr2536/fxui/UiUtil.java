package gr2536.fxui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

final class UiUtil {
    private UiUtil() {}

    static void showError(String title, String header, String content) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }

    static void switchScene(AnchorPane root, String fxmlPath, String title) throws Exception {
        FXMLLoader loader = new FXMLLoader(UiUtil.class.getResource(fxmlPath));
        Parent newRoot = loader.load();
        Scene scene = new Scene(newRoot);
        scene.getStylesheets().add(UiUtil.class.getResource("/gr2536/fxui/FridgeApp.css").toExternalForm());
        Stage stage = (Stage) root.getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle(title);
    }
}


