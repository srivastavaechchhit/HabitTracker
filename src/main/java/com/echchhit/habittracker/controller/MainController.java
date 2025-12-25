package com.echchhit.habittracker.controller;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class MainController {

    @FXML
    private StackPane contentPane;

    @FXML
    public void initialize() {
        loadPage("/ui/dashboard.fxml");
    }

    @FXML
    private void loadDashboard() {
        loadPage("/ui/dashboard.fxml");
    }

    @FXML
    private void loadHabits() {
        loadPage("/ui/habits.fxml");
    }

    @FXML
    private void loadStats() {
        loadPage("/ui/stats.fxml");
    }

    @FXML
    private void loadSettings() {
        loadPage("/ui/settings.fxml");
    }

    @FXML
    private void loadCalendar() {
        loadPage("/ui/calendar.fxml");
    }

    @FXML
    private void loadCharts() {
        loadPage("/ui/charts.fxml");
    }

    private void loadPage(String fxmlPath) {
        try {
            Node page = FXMLLoader.load(getClass().getResource(fxmlPath));

            FadeTransition fadeOut = new FadeTransition(Duration.millis(150), contentPane);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            fadeOut.setOnFinished(e -> {
                contentPane.getChildren().setAll(page);

                // SLIDE-IN ANIMATION (NEW PAGE)
                TranslateTransition slide =
                        new TranslateTransition(Duration.millis(250), page);
                slide.setFromX(30);
                slide.setToX(0);
                slide.play();

                // FADE-IN ANIMATION
                FadeTransition fadeIn =
                        new FadeTransition(Duration.millis(200), contentPane);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });


            fadeOut.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
