package com.echchhit.habittracker.app;

import com.echchhit.habittracker.database.DatabaseInitializer;
import com.echchhit.habittracker.service.ThemeService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {


    @Override
    public void start(Stage stage) throws Exception {

        DatabaseInitializer.initialize();

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/ui/root.fxml")
        );

        Scene scene = new Scene(loader.load(), 1000, 650);
        stage.setTitle("Habit Tracker");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
