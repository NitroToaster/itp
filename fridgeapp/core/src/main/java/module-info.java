module gr2536.core {
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.datatype.jdk8;

    exports gr2536.core.item;
    exports gr2536.core.fridge;
    exports gr2536.core.recipes;
    exports gr2536.core.shoppingList;
    exports gr2536.core.utils;
    
    opens gr2536.core.item to com.fasterxml.jackson.databind;
    opens gr2536.core.fridge to com.fasterxml.jackson.databind;
    opens gr2536.core.shoppingList to com.fasterxml.jackson.databind;
    opens gr2536.core.utils to com.fasterxml.jackson.databind;
    
}