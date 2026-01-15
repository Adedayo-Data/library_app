module com.lawpavillion.lmsui {
    requires javafx.controls;
    requires javafx.fxml;
    requires spring.web;
    requires com.google.gson;
    requires spring.data.commons;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.materialdesign2;

    opens com.lawpavillion.lmsui to javafx.fxml;
    opens com.lawpavillion.lmsui.controller to javafx.fxml;
    opens com.lawpavillion.lmsui.model to com.google.gson;
    
    exports com.lawpavillion.lmsui;
    exports com.lawpavillion.lmsui.controller;
    exports com.lawpavillion.lmsui.model;
}