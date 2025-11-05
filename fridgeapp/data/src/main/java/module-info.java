module gr2536.data {
    requires transitive gr2536.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.datatype.jdk8;
    
    exports gr2536.data;
    opens gr2536.data to com.fasterxml.jackson.databind;
}
