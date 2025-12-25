package com.echchhit.habittracker.controller;

import com.echchhit.habittracker.model.Habit;
import com.echchhit.habittracker.service.HabitLogService;
import com.echchhit.habittracker.service.HabitService;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import java.time.YearMonth;

public class HabitsController {

    @FXML
    private TextField habitInput;

    @FXML
    private ListView<Habit> habitList;

    @FXML
    public void initialize() {
        habitList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Habit habit, boolean empty) {
                super.updateItem(habit, empty);

                if (empty || habit == null) {
                    setGraphic(null);
                } else {
                    CheckBox cb = new CheckBox(habit.getName());
                    cb.setOnAction(e ->
                            HabitLogService.markCompleted(habit.getId())
                    );

                    int streak = HabitLogService.getCurrentStreak(habit.getId());
                    double percent = HabitLogService.getMonthlyCompletionPercent(
                            habit.getId(), YearMonth.now());

                    Label streakLabel = new Label("🔥 " + streak + " day streak");

                    if (streak >= 7) {
                        streakLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                    } else if (streak >= 3) {
                        streakLabel.setStyle("-fx-text-fill: #FF9800;");
                    } else {
                        streakLabel.setStyle("-fx-text-fill: #bdbdbd;");
                    }

                    Label percentLabel = new Label(
                            String.format("%.0f%%", percent)
                    );

                    HBox box = new HBox(15, cb, streakLabel, percentLabel);
                    setGraphic(box);
                }
            }
        });

        loadHabits();
    }

    @FXML
    private void addHabit() {
        String name = habitInput.getText().trim();
        if (!name.isEmpty()) {
            HabitService.addHabit(name);
            habitInput.clear();
            loadHabits();
        }
    }

    private void loadHabits() {
        habitList.getItems().setAll(HabitService.getAllHabits());
    }
}
