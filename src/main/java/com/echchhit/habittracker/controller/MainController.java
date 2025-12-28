package com.echchhit.habittracker.controller;

import com.echchhit.habittracker.service.UserStatsService;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class MainController {

    @FXML
    private StackPane contentPane;

    @FXML
    private Label levelLabel;

    @FXML
    private ProgressBar xpBar;

    private static MainController instance;

    public MainController() {
        instance = this;
    }

    @FXML
    public void initialize() {
        loadPage("/ui/dashboard.fxml");
        updateUserStats();
    }

    public static void refreshStats() {
        if (instance != null) {
            instance.updateUserStats();
        }
    }

    private void updateUserStats() {
        Platform.runLater(() -> {
            int[] stats = UserStatsService.getUserStats();
            double progress = UserStatsService.getProgressToNextLevel();

            if (levelLabel != null) levelLabel.setText(String.valueOf(stats[1]));
            if (xpBar != null) xpBar.setProgress(progress);
        });
    }

    @FXML
    private void closeApp() {
        System.exit(0);
    }

    @FXML
    private void maximizeApp(javafx.event.ActionEvent event) {
        javafx.stage.Stage stage = (javafx.stage.Stage)((javafx.scene.Node)event.getSource()).getScene().getWindow();
        stage.setMaximized(!stage.isMaximized());
    }

    @FXML
    private void minimizeApp(javafx.event.ActionEvent event) {
        ((javafx.stage.Stage)((javafx.scene.Node)event.getSource()).getScene().getWindow()).setIconified(true);
    }

    // Navigation Methods
    @FXML private void loadDashboard() { loadPage("/ui/dashboard.fxml"); }
    @FXML private void loadHabits() { loadPage("/ui/habits.fxml"); }
    @FXML private void loadStats() { loadPage("/ui/stats.fxml"); }
    @FXML private void loadCharts() { loadPage("/ui/charts.fxml"); }

    private void loadPage(String fxmlPath) {
        try {
            Node page = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentPane.getChildren().setAll(page);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), page);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}