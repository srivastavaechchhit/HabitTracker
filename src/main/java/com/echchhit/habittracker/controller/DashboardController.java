package com.echchhit.habittracker.controller;

import com.echchhit.habittracker.service.HabitService;
import com.echchhit.habittracker.service.HabitLogService;
import com.echchhit.habittracker.service.StatsService;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import javafx.animation.FadeTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;

import java.time.LocalDate;
import java.util.Map;

public class DashboardController {

    @FXML private Label totalHabits;
    @FXML private Label completedToday;
    @FXML private Label bestStreak;
    @FXML private Label todayFocus;
    @FXML private LineChart<String, Number> weeklyChart;
    @FXML private HBox heatmapBox;

    @FXML
    public void initialize() {
        loadSummary();
        loadWeeklyChart();
        loadHeatmap();

        todayFocus.setText(
                HabitService.getAllHabits().isEmpty()
                        ? "No habits yet. Add one to begin."
                        : "Stay consistent! Every small step counts toward your goals."
        );

        weeklyChart.setAnimated(true);
        FadeTransition fade = new FadeTransition(Duration.millis(800), weeklyChart);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private void loadHeatmap() {
        heatmapBox.getChildren().clear();
        LocalDate today = LocalDate.now();

        // 1. Pass 1: Find the maximum completions in the last 7 days for relative scaling
        int maxCompletions = 0;
        java.util.Map<LocalDate, Integer> weeklyData = new java.util.HashMap<>();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            int count = HabitLogService.getCompletedCountByDate(date);
            weeklyData.put(date, count);
            if (count > maxCompletions) {
                maxCompletions = count;
            }
        }

        // 2. Pass 2: Create cells with relative opacity and smooth edges
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            int count = weeklyData.get(date);

            StackPane cell = new StackPane();
            cell.setPrefSize(28, 28);
            cell.getStyleClass().add("heatmap-cell"); // Uses base styles from dark.css

            // Color Logic: Purple for active, dark grey for empty
            String color = count > 0 ? "#6C63FF" : "#2a2a32";

            // Relative Opacity Calculation
            double opacity;
            if (count == 0) {
                opacity = 1.0;
            } else if (maxCompletions == 0) {
                opacity = 0.6;
            } else {
                // Scales opacity: minimum 0.3 to maximum 1.0 based on the highest completion day
                opacity = 0.3 + (0.7 * ((double) count / maxCompletions));
            }

            // Apply style with both relative opacity and rounded corners
            cell.setStyle("-fx-background-color: " + color + "; " +
                    "-fx-opacity: " + opacity + "; " +
                    "-fx-background-radius: 8px; " +
                    "-fx-border-radius: 8px;");

            // Tooltip for interaction
            Tooltip tip = new Tooltip(date.toString() + ": " + count + " habits completed");
            tip.setShowDelay(Duration.millis(100));
            Tooltip.install(cell, tip);

            heatmapBox.getChildren().add(cell);
        }
    }

    private void loadSummary() {
        int total = HabitService.getAllHabits().size();
        int completed = HabitLogService.getCompletedCountToday();

        totalHabits.setText(String.valueOf(total));
        completedToday.setText(String.valueOf(completed));
        bestStreak.setText("🔥 " + StatsService.getBestStreak());
    }

    private void loadWeeklyChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Completion Trend");

        for (Map.Entry<String, Integer> e : StatsService.getWeeklyCompletion().entrySet()) {
            series.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
        }

        weeklyChart.getData().clear();
        weeklyChart.getData().add(series);
    }
}