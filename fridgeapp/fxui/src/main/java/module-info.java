module gr2536.fxui {
    requires transitive javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;

    opens gr2536.fxui to javafx.fxml;
    exports gr2536.fxui;
    
}