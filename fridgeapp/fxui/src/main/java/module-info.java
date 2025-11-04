module gr2536.fxui {
    
    requires transitive javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires gr2536.data;

    requires transitive gr2536.core;

    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;

    opens gr2536.fxui to javafx.fxml;
    exports gr2536.fxui;
    
}