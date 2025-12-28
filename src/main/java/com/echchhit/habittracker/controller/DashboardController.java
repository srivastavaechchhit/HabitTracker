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
    @FXML private javafx.scene.control.ProgressIndicator completionRing;
    @FXML private Label completionPercent;
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

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            int count = HabitLogService.getCompletedCountByDate(date);

            StackPane cell = new StackPane();
            cell.setPrefSize(28, 28);
            cell.getStyleClass().add("heatmap-cell");

            // Premium coloring logic
            String color = count > 0 ? "#6C63FF" : "#2a2a32";
            double opacity = count > 1 ? 1.0 : 0.6;
            if (count == 0) opacity = 1.0;

            cell.setStyle("-fx-background-color: " + color + "; -fx-opacity: " + opacity + ";");

            Tooltip tip = new Tooltip(date.toString() + ": " + count + " completed");
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

        double target = total == 0 ? 0 : (double) completed / total;
        completionPercent.setText((int)(target * 100) + "%");

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(completionRing.progressProperty(), 0)),
                new KeyFrame(Duration.millis(1000), new KeyValue(completionRing.progressProperty(), target))
        );
        timeline.play();
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