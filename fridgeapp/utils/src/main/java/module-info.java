module gr2536.utils {
    requires transitive gr2536.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.datatype.jdk8;
    
    exports gr2536.utils;
    opens gr2536.utils to com.fasterxml.jackson.databind;
}
