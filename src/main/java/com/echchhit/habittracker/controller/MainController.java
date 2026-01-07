package com.echchhit.habittracker.controller;

import com.echchhit.habittracker.service.JapService;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.fxml.FXMLLoader;

public class MainController {

    @FXML private StackPane contentPane;
    @FXML private VBox chantOverlay;
    @FXML private BorderPane mainLayout;

    // 1. Added reference to the maximize button
    @FXML private Button maxBtn;

    @FXML
    public void initialize() {
        mainLayout.setOpacity(0.0);
        mainLayout.setScaleX(0.92);
        mainLayout.setScaleY(0.92);
        loadPage("/ui/dashboard.fxml");
    }

    @FXML
    private void dismissChantOverlay() {
        int currentToday = JapService.getTodayCount();
        JapService.updateJapCount(currentToday + 100);

        Duration slowDuration = Duration.millis(1500);
        Duration midDuration = Duration.millis(1200);

        FadeTransition fadeOutOverlay = new FadeTransition(midDuration, chantOverlay);
        fadeOutOverlay.setToValue(0.0);

        ScaleTransition scaleOutOverlay = new ScaleTransition(midDuration, chantOverlay);
        scaleOutOverlay.setToX(1.15);
        scaleOutOverlay.setToY(1.15);

        FadeTransition fadeInMain = new FadeTransition(slowDuration, mainLayout);
        fadeInMain.setToValue(1.0);
        fadeInMain.setDelay(Duration.millis(200));

        ScaleTransition scaleInMain = new ScaleTransition(slowDuration, mainLayout);
        scaleInMain.setToX(1.0);
        scaleInMain.setToY(1.0);
        scaleInMain.setInterpolator(Interpolator.EASE_BOTH);

        GaussianBlur blur = new GaussianBlur(0);
        chantOverlay.setEffect(blur);
        Timeline blurTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(blur.radiusProperty(), 0)),
                new KeyFrame(midDuration, new KeyValue(blur.radiusProperty(), 25))
        );

        ParallelTransition masterTransition = new ParallelTransition(
                fadeOutOverlay, scaleOutOverlay, fadeInMain, scaleInMain, blurTimeline
        );

        masterTransition.setOnFinished(e -> {
            chantOverlay.setVisible(false);
            chantOverlay.setManaged(false);
        });

        masterTransition.play();
    }

    @FXML private void closeApp() { System.exit(0); }
    @FXML private void loadDashboard() { loadPage("/ui/dashboard.fxml"); }
    @FXML private void loadHabits() { loadPage("/ui/habits.fxml"); }
    @FXML private void loadJap() { loadPage("/ui/jap.fxml"); }
    @FXML private void loadReading() { loadPage("/ui/reading.fxml"); }

    private void loadPage(String fxmlPath) {
        try {
            Node page = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentPane.getChildren().setAll(page);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(500), page);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void maximizeApp(javafx.event.ActionEvent event) {
        javafx.stage.Stage stage = (javafx.stage.Stage)((javafx.scene.Node)event.getSource()).getScene().getWindow();

        // Toggle the window state
        stage.setMaximized(!stage.isMaximized());

        // 2. Update the button text based on the NEW state
        if (stage.isMaximized()) {
            maxBtn.setText("❐"); // Shows the "Restore" symbol
        } else {
            maxBtn.setText("☐"); // Shows the "Maximize" symbol
        }
    }

    @FXML
    private void minimizeApp(javafx.event.ActionEvent event) {
        ((javafx.stage.Stage)((javafx.scene.Node)event.getSource()).getScene().getWindow()).setIconified(true);
    }
}