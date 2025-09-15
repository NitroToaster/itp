module gr2536.fxui {
    
    requires transitive javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;

    requires gr2536.core;

    opens gr2536.fxui to javafx.fxml;
    exports gr2536.fxui;
    
}