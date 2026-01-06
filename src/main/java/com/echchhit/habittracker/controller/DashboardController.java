package com.echchhit.habittracker.controller;

import com.echchhit.habittracker.service.HabitService;
import com.echchhit.habittracker.service.HabitLogService;
import com.echchhit.habittracker.service.StatsService;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import java.time.LocalDate;

public class DashboardController {

    @FXML private Label totalHabits;
    @FXML private Label completedToday;
    @FXML private Label bestStreak;
    @FXML private Label todayFocus;
    @FXML private LineChart<String, Number> weeklyChart;
    @FXML private HBox heatmapBox;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;

    @FXML
    public void initialize() {
        // 1. Load basic summary stats (Total habits, Today's completions, Best streak)
        loadSummary();

        // 2. Build the heatmap with relative opacity and smooth edges
        loadHeatmap();

        // 3. Set the motivational focus text
        todayFocus.setText(
                HabitService.getAllHabits().isEmpty()
                        ? "No habits yet. Add one to begin."
                        : "Stay consistent! Every small step counts toward your goals."
        );

        // 4. Trigger the "moving forward" line chart animation.
        // We use Platform.runLater to ensure the chart is visible before the animation starts.
        javafx.application.Platform.runLater(this::loadWeeklyChart);
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
        // 1. Reset chart and fetch completion data
        weeklyChart.getData().clear();
        var completionData = com.echchhit.habittracker.service.StatsService.getWeeklyCompletion().entrySet().stream().toList();

        if (completionData.isEmpty()) return;

        // 2. Calculate Dynamic Y-Axis Range: Maximum completions + 5
        int maxCompletions = completionData.stream()
                .mapToInt(java.util.Map.Entry::getValue)
                .max()
                .orElse(0);

        // Force every integer label (like 1 and 14) to be visible
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(maxCompletions + 5);
        yAxis.setAutoRanging(false);
        yAxis.setTickUnit(1);
        yAxis.setMinorTickVisible(false);
        yAxis.setTickMarkVisible(true);

        // 3. Create the series and add data points
        javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();
        series.setName("Completion Trend");
        for (var entry : completionData) {
            series.getData().add(new javafx.scene.chart.XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        weeklyChart.getData().add(series);

        // 4. Setup traveler and hide static symbols initially
        javafx.scene.layout.StackPane traveler = new javafx.scene.layout.StackPane();
        traveler.setStyle("-fx-background-color: white; -fx-background-radius: 50%; " +
                "-fx-min-width: 10px; -fx-min-height: 10px; " +
                "-fx-border-color: #6C63FF; -fx-border-width: 2px;");

        traveler.setVisible(false);
        for (javafx.scene.chart.XYChart.Data<String, Number> data : series.getData()) {
            if (data.getNode() != null) data.getNode().setVisible(false);
        }

        // 5. Wait for layout to stabilize, then run the horizontal crawl
        javafx.animation.PauseTransition layoutWait = new javafx.animation.PauseTransition(javafx.util.Duration.millis(100));
        layoutWait.setOnFinished(e -> {
            javafx.scene.Group plotContent = (javafx.scene.Group) series.getNode().getParent();
            if (!plotContent.getChildren().contains(traveler)) plotContent.getChildren().add(traveler);

            javafx.scene.shape.Rectangle clipRect = new javafx.scene.shape.Rectangle();
            clipRect.setHeight(weeklyChart.getHeight() + 100);
            clipRect.heightProperty().bind(weeklyChart.heightProperty());
            series.getNode().setClip(clipRect);

            double startX = xAxis.getDisplayPosition(series.getData().get(0).getXValue());
            double startY = yAxis.getDisplayPosition(series.getData().get(0).getYValue());

            traveler.setLayoutX(startX - 5);
            traveler.setLayoutY(startY - 5);
            traveler.setVisible(true);
            clipRect.setWidth(startX);

            javafx.animation.SequentialTransition sequentialTransition = new javafx.animation.SequentialTransition();

            for (int i = 0; i < series.getData().size() - 1; i++) {
                final int nextIndex = i + 1;
                var currentPoint = series.getData().get(i);
                var nextPoint = series.getData().get(nextIndex);

                double nextX = xAxis.getDisplayPosition(nextPoint.getXValue());
                double nextY = yAxis.getDisplayPosition(nextPoint.getYValue());

                javafx.animation.Timeline segment = new javafx.animation.Timeline(
                        new javafx.animation.KeyFrame(javafx.util.Duration.millis(400),
                                event -> {
                                    // "Deposit" the permanent circle at the tip's location
                                    if (nextPoint.getNode() != null) nextPoint.getNode().setVisible(true);

                                    // Remove traveler once the journey is complete
                                    if (nextIndex == series.getData().size() - 1) {
                                        plotContent.getChildren().remove(traveler);
                                    }
                                },
                                new javafx.animation.KeyValue(clipRect.widthProperty(), nextX),
                                new javafx.animation.KeyValue(traveler.layoutXProperty(), nextX - 5),
                                new javafx.animation.KeyValue(traveler.layoutYProperty(), nextY - 5)
                        )
                );

                if (i == 0 && currentPoint.getNode() != null) currentPoint.getNode().setVisible(true);
                sequentialTransition.getChildren().add(segment);
            }
            sequentialTransition.play();
        });
        layoutWait.play();
    }
}