package com.echchhit.habittracker.controller;

import com.echchhit.habittracker.model.Habit;
import com.echchhit.habittracker.service.HabitLogService;
import com.echchhit.habittracker.service.HabitService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;

public class CalendarController {

    @FXML
    private GridPane calendarGrid;

    @FXML
    private Label monthLabel;

    private YearMonth currentMonth = YearMonth.now();
    private List<Habit> habits;

    @FXML
    public void initialize() {
        habits = HabitService.getAllHabits();
        render();
    }

    @FXML
    private void prevMonth() {
        currentMonth = currentMonth.minusMonths(1);
        render();
    }

    @FXML
    private void nextMonth() {
        currentMonth = currentMonth.plusMonths(1);
        render();
    }

    private void render() {
        calendarGrid.getChildren().clear();
        monthLabel.setText(currentMonth.toString());

        for (int d = 1; d <= currentMonth.lengthOfMonth(); d++) {
            LocalDate date = currentMonth.atDay(d);
            StackPane cell = new StackPane();
            cell.setPrefSize(30, 30);

            boolean completedAny = false;
            for (Habit h : habits) {
                Set<LocalDate> completed =
                        HabitLogService.getCompletedDatesForMonth(h.getId(), currentMonth);
                if (completed.contains(date)) {
                    completedAny = true;
                    break;
                }
            }

            cell.setStyle(
                    completedAny
                            ? "-fx-background-color: #4CAF50;"
                            : "-fx-background-color: #2A2A2A;"
            );

            cell.getChildren().add(new Label(String.valueOf(d)));
            calendarGrid.add(cell, (d - 1) % 7, (d - 1) / 7);
        }
    }
}
