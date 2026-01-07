package com.echchhit.habittracker.app;

import com.echchhit.habittracker.database.DatabaseInitializer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MainApp extends Application {

    private double xOffset = 0;
    private double yOffset = 0;

    @Override
    public void start(Stage stage) throws Exception {
        DatabaseInitializer.initialize();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/root.fxml"));
        Parent root = loader.load();

        // 1. Remove standard OS title bar
        stage.initStyle(StageStyle.UNDECORATED);

        // 2. Make the window draggable ONLY via the title bar
        // We look up the HBox with the ID 'titleBar' defined in root.fxml
        Node titleBar = root.lookup("#titleBar");
        if (titleBar != null) {
            titleBar.setOnMousePressed(event -> {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            });
            titleBar.setOnMouseDragged(event -> {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            });
        }

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/theme/light.css").toExternalForm());

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}