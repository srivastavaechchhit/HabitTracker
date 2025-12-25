package com.echchhit.habittracker.controller;

import com.echchhit.habittracker.service.HabitService;
import com.echchhit.habittracker.service.HabitLogService;
import com.echchhit.habittracker.service.StatsService;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;

import java.util.Map;

public class DashboardController {

    @FXML
    private Label totalHabits;

    @FXML
    private Label completedToday;

    @FXML
    private Label bestStreak;

    @FXML
    private javafx.scene.control.ProgressIndicator completionRing;

    @FXML
    private Label completionPercent;

    @FXML
    private Label todayFocus;

    @FXML
    private LineChart<String, Number> weeklyChart;

    @FXML
    private javafx.scene.layout.HBox heatmapBox;


    @FXML
    public void initialize() {
        loadSummary();
        loadWeeklyChart();

        todayFocus.setText(
                HabitService.getAllHabits().isEmpty()
                        ? "No habits yet. Add one to begin."
                        : "Focus on completing your habits today."
        );

        loadHeatmap();

        weeklyChart.setAnimated(true);

        javafx.animation.FadeTransition fade =
                new javafx.animation.FadeTransition(
                        javafx.util.Duration.millis(400),
                        weeklyChart
                );
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();

    }

    private void loadHeatmap() {

        heatmapBox.getChildren().clear();

        java.time.LocalDate today = java.time.LocalDate.now();

        for (int i = 6; i >= 0; i--) {

            java.time.LocalDate date = today.minusDays(i);
            int count = HabitLogService.getCompletedCountByDate(date);

            javafx.scene.layout.StackPane cell =
                    new javafx.scene.layout.StackPane();

            cell.setPrefSize(22, 22);
            cell.setStyle(
                    "-fx-background-radius: 6;" +
                            "-fx-background-color: " +
                            (count > 0 ? "#4CAF50" : "#2a2a2a") + ";"
            );

            heatmapBox.getChildren().add(cell);
        }
    }

    private void loadSummary() {

        int total = HabitService.getAllHabits().size();
        int completed = HabitLogService.getCompletedCountToday();

        totalHabits.setText(String.valueOf(total));
        completedToday.setText(String.valueOf(completed));

        double target = total == 0 ? 0 : (double) completed / total;
        completionPercent.setText((int)(target * 100) + "%");

        javafx.animation.Timeline timeline =
                new javafx.animation.Timeline(
                        new javafx.animation.KeyFrame(
                                javafx.util.Duration.ZERO,
                                new javafx.animation.KeyValue(
                                        completionRing.progressProperty(), 0
                                )
                        ),
                        new javafx.animation.KeyFrame(
                                javafx.util.Duration.millis(600),
                                new javafx.animation.KeyValue(
                                        completionRing.progressProperty(), target
                                )
                        )
                );

        timeline.play();

        bestStreak.setText("🔥 " + StatsService.getBestStreak());
    }

    private void loadWeeklyChart() {
        weeklyChart.setAnimated(true);

        XYChart.Series<String, Number> series =
                new XYChart.Series<>();
        series.setName("Last 7 Days");

        for (Map.Entry<String, Integer> e :
                StatsService.getWeeklyCompletion().entrySet()) {

            series.getData().add(
                    new XYChart.Data<>(e.getKey(), e.getValue())
            );
        }

        weeklyChart.getData().clear();
        weeklyChart.getData().add(series);
    }
}
