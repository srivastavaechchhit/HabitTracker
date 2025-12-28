package com.echchhit.habittracker.controller;

import com.echchhit.habittracker.service.HabitLogService;
import com.echchhit.habittracker.service.HabitService;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
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
import java.util.Set;

public class HabitsController {

    @FXML private GridPane habitGrid;
    @FXML private ComboBox<YearMonth> monthSelector;

    private final Map<Integer, Integer> habitRowMap = new HashMap<>();
    private Map<Integer, String> dbHabits = new HashMap<>();
    private YearMonth selectedMonth = YearMonth.now();

    @FXML
    public void initialize() {
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

        dbHabits = HabitService.getAllHabitsWithIds();

        LocalDate yesterday = LocalDate.now().minusDays(1);
        for (int id : dbHabits.keySet()) {
            HabitLogService.autoCrossIfMissed(id, yesterday);
        }

        refreshUI();

        // Restores the entrance animation every time the section is loaded
        Platform.runLater(this::animateGridDiagonal);
    }

    private void refreshUI() {
        habitGrid.getChildren().clear();
        habitGrid.getColumnConstraints().clear();
        dbHabits = HabitService.getAllHabitsWithIds();

        buildDateHeader();
        addHabitRows();
        addBottomTotalRow();
    }

    private void animateGridDiagonal() {
        ParallelTransition mainParallel = new ParallelTransition();
        double delayFactor = 15.0; // Adjust this to speed up/slow down the diagonal wave

        for (Node node : habitGrid.getChildren()) {
            Integer col = GridPane.getColumnIndex(node);
            Integer row = GridPane.getRowIndex(node);

            if (col == null) col = 0;
            if (row == null) row = 0;

            // Initialize node as invisible and slightly smaller
            node.setOpacity(0.0);
            node.setScaleX(0.8);
            node.setScaleY(0.8);

            // Create Fade Animation
            FadeTransition fade = new FadeTransition(Duration.millis(300), node);
            fade.setToValue(1.0);

            // Create Scale Animation (Pop effect)
            ScaleTransition scale = new ScaleTransition(Duration.millis(300), node);
            scale.setToX(1.0);
            scale.setToY(1.0);

            ParallelTransition cellAnim = new ParallelTransition(node, fade, scale);

            // Calculate diagonal delay: nodes with same (row + col) animate together
            cellAnim.setDelay(Duration.millis((row + col) * delayFactor));

            mainParallel.getChildren().add(cellAnim);
        }

        mainParallel.play();
    }

    // Standard Grid Building Logic
    private void buildDateHeader() {
        int daysInMonth = selectedMonth.lengthOfMonth();
        LocalDate today = LocalDate.now();
        ColumnConstraints habitCol = new ColumnConstraints();
        habitCol.setPercentWidth(20);
        habitGrid.getColumnConstraints().add(habitCol);
        Label header = new Label("HABITS");
        header.setStyle("-fx-text-fill: #888; -fx-font-size: 10px;");
        habitGrid.add(header, 0, 0);

        double datePercent = 70.0 / daysInMonth;
        for (int day = 1; day <= daysInMonth; day++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(datePercent);
            habitGrid.getColumnConstraints().add(cc);
            Label lbl = new Label(String.valueOf(day));
            boolean isActualToday = selectedMonth.equals(YearMonth.now()) && day == today.getDayOfMonth();
            lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: " + (isActualToday ? "#6C63FF" : "#888") + ";");
            StackPane container = new StackPane(lbl);
            container.setPrefHeight(30);
            habitGrid.add(container, day, 0);
        }

        ColumnConstraints totalCol = new ColumnConstraints();
        totalCol.setPercentWidth(10);
        habitGrid.getColumnConstraints().add(totalCol);
        Label totalHeader = new Label("TOTAL");
        totalHeader.setStyle("-fx-text-fill: #888; -fx-font-size: 10px;");
        habitGrid.add(new StackPane(totalHeader), daysInMonth + 1, 0);
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
            habitLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
            habitGrid.add(habitLabel, 0, rowIndex);

            Set<LocalDate> completedDates = HabitLogService.getCompletedDatesForMonth(habitId, selectedMonth);

            for (int day = 1; day <= daysInMonth; day++) {
                LocalDate date = selectedMonth.atDay(day);
                if (date.isAfter(today)) {
                    habitGrid.add(createFutureBox(), day, rowIndex);
                } else if (date.equals(today)) {
                    habitGrid.add(createTodayBox(habitId, completedDates.contains(date)), day, rowIndex);
                } else {
                    habitGrid.add(createLockedBox(completedDates.contains(date)), day, rowIndex);
                }
            }

            Label rowTotal = new Label(String.valueOf(completedDates.size()));
            rowTotal.setStyle("-fx-font-weight: bold; -fx-text-fill: #666;");
            habitGrid.add(new StackPane(rowTotal), daysInMonth + 1, rowIndex);
            rowIndex++;
        }
    }

    private StackPane createTodayBox(int habitId, boolean initiallyCompleted) {
        StackPane box = createEmptyBox();
        box.setStyle("-fx-cursor: hand; -fx-background-color: transparent; -fx-border-color: #6C63FF; -fx-border-radius: 4; -fx-border-width: 1.5;");
        if (initiallyCompleted) addCheckMark(box);

        box.setOnMouseClicked(e -> {
            boolean isChecked = !box.getChildren().isEmpty();
            LocalDate today = LocalDate.now();
            if (isChecked) {
                HabitLogService.markMissed(habitId, today);
                box.getChildren().clear();
                box.setStyle("-fx-cursor: hand; -fx-background-color: transparent; -fx-border-color: #6C63FF; -fx-border-radius: 4; -fx-border-width: 1.5;");
            } else {
                HabitLogService.markCompleted(habitId, today);
                addCheckMark(box);
                box.setStyle("-fx-cursor: hand; -fx-background-color: rgba(108, 99, 255, 0.1); -fx-border-color: #6C63FF; -fx-border-radius: 4; -fx-border-width: 1.5;");
            }
            refreshTotals();
            refreshRowTotal(habitId, habitRowMap.get(habitId));
            MainController.refreshStats();
        });
        return box;
    }

    // REQUIRED: Fixes the FXML LoadException
    @FXML
    private void onAddHabit() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Habit");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter habit name:");
        dialog.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                HabitService.addHabit(name.trim());
                refreshUI();
            }
        });
    }

    private void addCheckMark(StackPane box) {
        Label check = new Label("✓");
        check.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 14px; -fx-font-weight: bold;");
        box.getChildren().setAll(check);
        ScaleTransition st = new ScaleTransition(Duration.millis(200), check);
        st.setFromX(0.1); st.setFromY(0.1);
        st.setToX(1.0); st.setToY(1.0);
        st.play();
    }

    private void addBottomTotalRow() {
        int bottomRow = habitRowMap.size() + 1;
        Label totalLabel = new Label("TOTAL");
        totalLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #888; -fx-font-size: 10px;");
        habitGrid.add(totalLabel, 0, bottomRow);

        Label totalValue = new Label(String.valueOf(HabitLogService.getCompletedCountToday()));
        totalValue.setStyle("-fx-font-weight: bold; -fx-text-fill: #6C63FF;");
        habitGrid.add(new StackPane(totalValue), LocalDate.now().getDayOfMonth(), bottomRow);
    }

    private StackPane createLockedBox(boolean completed) {
        StackPane box = createEmptyBox();
        Label icon = new Label(completed ? "✓" : "✗");
        icon.setStyle(completed ? "-fx-text-fill: #666;" : "-fx-text-fill: #444; -fx-font-size: 10px;");
        box.getChildren().setAll(icon);
        return box;
    }

    private StackPane createFutureBox() {
        StackPane box = createEmptyBox();
        box.setStyle("-fx-border-color: #dcdcdc; -fx-border-radius: 4; -fx-opacity: 0.3;");
        return box;
    }

    private StackPane createEmptyBox() {
        StackPane box = new StackPane();
        box.setPrefSize(24, 24);
        box.setMinSize(24, 24);
        box.setMaxSize(24, 24);
        box.setStyle("-fx-border-color: #dcdcdc; -fx-border-radius: 4;");
        return box;
    }

    private void refreshTotals() {
        int today = LocalDate.now().getDayOfMonth();
        StackPane container = (StackPane) getNodeAt(today, habitRowMap.size() + 1);
        if (container != null) {
            ((Label)container.getChildren().get(0)).setText(String.valueOf(HabitLogService.getCompletedCountToday()));
        }
    }

    private void refreshRowTotal(int habitId, int row) {
        int count = HabitLogService.getCompletedDatesForMonth(habitId, selectedMonth).size();
        StackPane container = (StackPane) getNodeAt(selectedMonth.lengthOfMonth() + 1, row);
        if (container != null) {
            ((Label)container.getChildren().get(0)).setText(String.valueOf(count));
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