package com.echchhit.habittracker.controller;

import com.echchhit.habittracker.service.JapService;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class MainController {

    @FXML
    private StackPane contentPane;

    @FXML
    private VBox chantOverlay; // Reference to the mandatory overlay in root.fxml

    private static MainController instance;

    public MainController() {
        instance = this;
    }

    @FXML
    public void initialize() {
        // Load the dashboard as the default starting page
        loadPage("/ui/dashboard.fxml");
    }

    /**
     * Dismisses the mandatory chant overlay and logs progress.
     * This allows the user to interact with the rest of the application.
     */
    @FXML
    private void dismissChantOverlay() {
        // 1. Automatically update today's Jap count by 100
        int currentToday = JapService.getTodayCount();
        JapService.updateJapCount(currentToday + 100);

        // 2. Hide the overlay
        chantOverlay.setVisible(false);
        chantOverlay.setManaged(false);
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
    @FXML private void loadJap() { loadPage("/ui/jap.fxml"); }
    @FXML private void loadReading() { loadPage("/ui/reading.fxml"); }

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