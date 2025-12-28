package com.echchhit.habittracker.controller;

import com.echchhit.habittracker.service.HabitLogService;
import com.echchhit.habittracker.service.HabitService;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class HabitsController {

    @FXML private GridPane habitGrid;
    @FXML private ComboBox<YearMonth> monthSelector;

    private final Map<Integer, Integer> habitRowMap = new HashMap<>();
    private Map<Integer, String> dbHabits = new HashMap<>();
    private YearMonth selectedMonth = YearMonth.now();

    @FXML
    public void initialize() {
        // Populate Month Selector with current and last 5 months
        if (monthSelector.getItems().isEmpty()) {
            for (int i = 0; i < 6; i++) {
                monthSelector.getItems().add(YearMonth.now().minusMonths(i));
            }
            monthSelector.setValue(selectedMonth);
            monthSelector.setOnAction(e -> {
                selectedMonth = monthSelector.getValue();
                refreshUI();
            });
        }

        // Auto-cross missed habits for yesterday on startup
        dbHabits = HabitService.getAllHabitsWithIds();
        LocalDate yesterday = LocalDate.now().minusDays(1);
        for (int id : dbHabits.keySet()) {
            HabitLogService.autoCrossIfMissed(id, yesterday);
        }

        refreshUI();
    }

    private void refreshUI() {
        habitGrid.getChildren().clear();
        habitGrid.getColumnConstraints().clear();
        dbHabits = HabitService.getAllHabitsWithIds();

        buildDateHeader();
        addHabitRows();
        addBottomTotalRow();
    }

    private void buildDateHeader() {
        int daysInMonth = selectedMonth.lengthOfMonth();
        LocalDate today = LocalDate.now();

        // Habit Name Column
        ColumnConstraints habitCol = new ColumnConstraints();
        habitCol.setPercentWidth(20);
        habitGrid.getColumnConstraints().add(habitCol);

        Label header = new Label("HABITS");
        header.getStyleClass().add("stat-label");
        habitGrid.add(header, 0, 0);

        // Date Columns
        double datePercent = 70.0 / daysInMonth;
        for (int day = 1; day <= daysInMonth; day++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(datePercent);
            habitGrid.getColumnConstraints().add(cc);

            Label lbl = new Label(String.valueOf(day));
            boolean isActualToday = selectedMonth.equals(YearMonth.now()) && day == today.getDayOfMonth();
            lbl.setStyle("-fx-text-fill: " + (isActualToday ? "#6C63FF" : "#888") + "; -fx-font-weight: bold;");
            habitGrid.add(lbl, day, 0);
        }

        // Total Column
        ColumnConstraints totalCol = new ColumnConstraints();
        totalCol.setPercentWidth(10);
        habitGrid.getColumnConstraints().add(totalCol);
        Label totalHeader = new Label("TOTAL");
        totalHeader.getStyleClass().add("stat-label");
        habitGrid.add(totalHeader, daysInMonth + 1, 0);
    }

    private void addHabitRows() {
        int rowIndex = 1;
        habitRowMap.clear();
        int daysInMonth = selectedMonth.lengthOfMonth();
        LocalDate today = LocalDate.now();

        for (Map.Entry<Integer, String> entry : dbHabits.entrySet()) {
            int habitId = entry.getKey();
            String name = entry.getValue();
            habitRowMap.put(habitId, rowIndex);

            Label habitLabel = new Label(name);
            habitLabel.getStyleClass().add("label");
            habitLabel.setStyle("-fx-font-weight: bold;");

            // Double click to rename
            habitLabel.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) showEditDialog(habitId, name);
            });

            // Context Menu for Archiving
            ContextMenu menu = new ContextMenu();
            MenuItem archive = new MenuItem("Archive Habit");
            archive.setOnAction(e -> archiveHabit(habitId));
            menu.getItems().add(archive);
            habitLabel.setContextMenu(menu);

            habitGrid.add(habitLabel, 0, rowIndex);

            Set<LocalDate> completedDates = HabitLogService.getCompletedDatesForMonth(habitId, selectedMonth);

            for (int day = 1; day <= daysInMonth; day++) {
                LocalDate date = selectedMonth.atDay(day);
                if (date.isAfter(today)) {
                    habitGrid.add(createFutureBox(), day, rowIndex);
                } else {
                    habitGrid.add(createInteractiveBox(habitId, date, completedDates.contains(date)), day, rowIndex);
                }
            }

            // Row Total
            Label rowTotalLabel = new Label(String.valueOf(completedDates.size()));
            rowTotalLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #888;");
            habitGrid.add(rowTotalLabel, daysInMonth + 1, rowIndex);

            rowIndex++;
        }
    }

    private StackPane createInteractiveBox(int habitId, LocalDate date, boolean completed) {
        StackPane box = new StackPane();
        box.setPrefSize(24, 24);
        box.getStyleClass().add("habit-cell");

        if (date.equals(LocalDate.now())) {
            box.getStyleClass().add("habit-cell-today");
        } else {
            box.getStyleClass().add("habit-cell-locked");
        }

        if (completed) {
            addCheck(box);
        } else if (date.isBefore(LocalDate.now())) {
            addCross(box);
        }

        box.setOnMouseClicked(e -> {
            boolean isCurrentlyChecked = !box.getChildren().isEmpty() &&
                    box.getChildren().get(0).getStyleClass().contains("habit-check");

            if (isCurrentlyChecked) {
                HabitLogService.markMissed(habitId, date);
                box.getChildren().clear();
                if (date.isBefore(LocalDate.now())) addCross(box);
            } else {
                // Modified markCompleted to accept specific date for history
                HabitLogService.markCompleted(habitId, date);
                addCheck(box);
            }
            refreshRowAndColumnTotals(habitId, date);
            MainController.refreshStats();
        });
        return box;
    }

    private void addCheck(StackPane box) {
        box.getChildren().clear();
        Label check = new Label("✓");
        check.getStyleClass().add("habit-check");
        box.getChildren().add(check);
        ScaleTransition st = new ScaleTransition(Duration.millis(200), check);
        st.setFromX(0.5); st.setFromY(0.5); st.setToX(1); st.setToY(1); st.play();
    }

    private void addCross(StackPane box) {
        box.getChildren().clear();
        Label cross = new Label("✗");
        cross.getStyleClass().add("habit-cross");
        box.getChildren().add(cross);
    }

    private StackPane createFutureBox() {
        StackPane box = new StackPane();
        box.setPrefSize(24, 24);
        box.getStyleClass().addAll("habit-cell", "habit-cell-future");
        return box;
    }

    private void showEditDialog(int id, String oldName) {
        TextInputDialog dialog = new TextInputDialog(oldName);
        dialog.setTitle("Rename Habit");
        dialog.setHeaderText("Change habit name:");
        dialog.showAndWait().ifPresent(newName -> {
            if (!newName.isBlank()) {
                HabitService.updateHabitName(id, newName.trim());
                refreshUI();
            }
        });
    }

    private void archiveHabit(int id) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Archive this habit?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(type -> {
            if (type == ButtonType.YES) {
                HabitService.markHabitCompleted(id);
                refreshUI();
            }
        });
    }

    @FXML
    private void onAddHabit() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Habit");
        dialog.setHeaderText("Enter habit name:");
        dialog.showAndWait().ifPresent(name -> {
            if (!name.isBlank()) {
                HabitService.addHabit(name.trim());
                refreshUI();
            }
        });
    }

    private void refreshRowAndColumnTotals(int habitId, LocalDate date) {
        int row = habitRowMap.get(habitId);
        int daysInMonth = selectedMonth.lengthOfMonth();

        // Update Row Total
        Label rTotal = (Label) getNodeAt(daysInMonth + 1, row);
        if (rTotal != null) {
            int count = HabitLogService.getCompletedDatesForMonth(habitId, selectedMonth).size();
            rTotal.setText(String.valueOf(count));
        }

        // Update Column Total at Bottom
        Label bTotal = (Label) getNodeAt(date.getDayOfMonth(), dbHabits.size() + 1);
        if (bTotal != null) {
            bTotal.setText(String.valueOf(HabitLogService.getCompletedCountByDate(date)));
        }
    }

    private void addBottomTotalRow() {
        int row = dbHabits.size() + 1;
        Label lbl = new Label("TOTAL");
        lbl.getStyleClass().add("stat-label");
        habitGrid.add(lbl, 0, row);

        for (int d = 1; d <= selectedMonth.lengthOfMonth(); d++) {
            Label val = new Label(String.valueOf(HabitLogService.getCompletedCountByDate(selectedMonth.atDay(d))));
            val.setStyle("-fx-text-fill: #6C63FF; -fx-font-weight: bold;");
            habitGrid.add(val, d, row);
        }
    }

    private Node getNodeAt(int col, int row) {
        for (Node node : habitGrid.getChildren()) {
            Integer c = GridPane.getColumnIndex(node);
            Integer r = GridPane.getRowIndex(node);
            if (c != null && r != null && c == col && r == row) return node;
        }
        return null;
    }
}