package com.echchhit.habittracker.controller;

import com.echchhit.habittracker.service.StatsService;
import javafx.fxml.FXML;
import javafx.scene.chart.*;

import java.util.Map;

public class ChartsController {

    @FXML
    private LineChart<String, Number> weeklyChart;

    @FXML
    private BarChart<String, Number> habitChart;

    @FXML
    public void initialize() {

        // Enable animations
        weeklyChart.setAnimated(true);
        habitChart.setAnimated(true);

        // Load data
        loadWeeklyChart();
        loadHabitChart();
    }


    private void loadWeeklyChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
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

    private void loadHabitChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Habits");

        for (Map.Entry<String, Integer> e :
                StatsService.getHabitCompletionCount().entrySet()) {
            series.getData().add(
                    new XYChart.Data<>(e.getKey(), e.getValue())
            );
        }

        habitChart.getData().clear();
        habitChart.getData().add(series);
    }
}
