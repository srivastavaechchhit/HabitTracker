package com.echchhit.habittracker.controller;

import com.echchhit.habittracker.service.HabitService;
import com.echchhit.habittracker.service.ExportService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class StatsController {

    @FXML
    private Label totalHabits;

    @FXML
    public void initialize() {
        totalHabits.setText(
                "Total habits: " + HabitService.getAllHabits().size()
        );
    }

    @FXML
    private void export() {
        ExportService.exportToCSV("habit_report.csv");
    }

}
