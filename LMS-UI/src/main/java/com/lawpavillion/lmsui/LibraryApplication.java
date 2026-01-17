package com.lawpavillion.lmsui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main JavaFX Application for Library Management System.
 */
public class LibraryApplication extends Application {
    
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(LibraryApplication.class.getResource("library-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1400, 900);
        
        // Apply CSS stylesheet
        scene.getStylesheets().add(
            LibraryApplication.class.getResource("styles.css").toExternalForm()
        );
        
        stage.setTitle("Library Management System");
        stage.setScene(scene);
        stage.setMinWidth(1200);
        stage.setMinHeight(700);
        stage.setMaximized(true); // Start maximized to fit screen
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
