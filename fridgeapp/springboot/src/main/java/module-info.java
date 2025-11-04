module gr2536.springboot {

    requires gr2536.core;
    requires gr2536.data;

    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;

    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.beans;
    requires spring.web;
    requires spring.core;

    requires jakarta.annotation;  
    requires jakarta.validation;

    requires io.swagger.v3.oas.models;

    requires static lombok;

    opens gr2536.springboot to spring.core, spring.beans, spring.context, spring.web, spring.aop;
}