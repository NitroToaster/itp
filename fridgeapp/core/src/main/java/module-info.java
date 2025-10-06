//* Domain model and persistence utility for FridgeApp */
module gr2536.core {
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.datatype.jdk8;

    exports gr2536.core;
    opens gr2536.core to com.fasterxml.jackson.databind;
    
}