package com.echchhit.habittracker.controller;

import com.echchhit.habittracker.service.HabitLogService;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HabitsController {

    @FXML
    private GridPane habitGrid;

    private final Map<Integer, Integer> habitRowMap = new HashMap<>();

    // TEMP: habitId mapping (later from DB)
    private final Map<String, Integer> habitIdMap = new HashMap<>();

    private final String[] habits = {
            "Exercise",
            "Reading",
            "Meditation",
            "Revision"
    };

    @FXML
    public void initialize() {

        // TEMP habit IDs
        for (int i = 0; i < habits.length; i++) {
            habitIdMap.put(habits[i], i + 1);
        }

        // Auto-cross yesterday (ONLY at startup = day rollover)
        LocalDate yesterday = LocalDate.now().minusDays(1);
        for (int id : habitIdMap.values()) {
            HabitLogService.autoCrossIfMissed(id, yesterday);
        }

        buildDateHeader();
        addHabitRows();
        addBottomTotalRow();
    }

    /* =====================================================
       HEADER
       ===================================================== */
    private void buildDateHeader() {

        habitGrid.getChildren().clear();
        habitGrid.getColumnConstraints().clear();

        int today = LocalDate.now().getDayOfMonth();

        ColumnConstraints habitCol = new ColumnConstraints();
        habitCol.setPercentWidth(20);
        habitGrid.getColumnConstraints().add(habitCol);
        habitGrid.add(new Label("HABITS"), 0, 0);

        double datePercent = 70.0 / today;

        for (int day = 1; day <= today; day++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(datePercent);
            habitGrid.getColumnConstraints().add(cc);

            Label lbl = new Label(String.valueOf(day));
            lbl.setStyle("-fx-font-weight: bold;");
            habitGrid.add(lbl, day, 0);
        }

        ColumnConstraints totalCol = new ColumnConstraints();
        totalCol.setPercentWidth(10);
        habitGrid.getColumnConstraints().add(totalCol);
        habitGrid.add(new Label("TOTAL"), today + 1, 0);
    }

    /* =====================================================
       HABIT ROWS
       ===================================================== */
    private void addHabitRows() {

        int today = LocalDate.now().getDayOfMonth();
        YearMonth month = YearMonth.now();

        for (int i = 0; i < habits.length; i++) {

            String habit = habits[i];
            int habitId = habitIdMap.get(habit);
            int row = i + 1;
            habitRowMap.put(habitId, row);


            Label habitLabel = new Label(habit);
            habitLabel.setStyle("-fx-font-weight: bold;");
            habitGrid.add(habitLabel, 0, row);

            Set<LocalDate> completedDates =
                    HabitLogService.getCompletedDatesForMonth(habitId, month);

            for (int day = 1; day <= today; day++) {

                LocalDate date = LocalDate.now().withDayOfMonth(day);

                if (day == today) {
                    habitGrid.add(
                            createTodayBox(habitId, completedDates.contains(date)),
                            day,
                            row
                    );
                } else {
                    habitGrid.add(
                            createLockedBox(completedDates.contains(date)),
                            day,
                            row
                    );
                }
            }

            // RIGHT SIDE TOTAL (days completed this month)
            Label rowTotal = new Label(
                    String.valueOf(completedDates.size())
            );
            rowTotal.setStyle("-fx-font-weight: bold;");
            habitGrid.add(rowTotal, today + 1, row);
        }
    }

    private int getHabitRow(int habitId) {
        return habitRowMap.getOrDefault(habitId, -1);
    }

    /* =====================================================
       BOTTOM TOTAL
       ===================================================== */
    private void addBottomTotalRow() {

        int bottomRow = habits.length + 1;
        int today = LocalDate.now().getDayOfMonth();

        Label totalLabel = new Label("TOTAL");
        totalLabel.setStyle("-fx-font-weight: bold;");
        habitGrid.add(totalLabel, 0, bottomRow);

        Label totalValue = new Label(
                String.valueOf(HabitLogService.getCompletedCountToday())
        );
        totalValue.setStyle("-fx-font-weight: bold;");
        habitGrid.add(totalValue, today, bottomRow);
    }

    /* =====================================================
       TODAY BOX (EDITABLE ALL DAY)
       ===================================================== */
    private StackPane createTodayBox(int habitId, boolean initiallyCompleted) {

        StackPane box = createEmptyBox();

        if (initiallyCompleted) {
            box.getChildren().setAll(new Label("✓"));
        }

        box.setOnMouseClicked(e -> {

            boolean isChecked = box.getChildren().stream()
                    .anyMatch(n -> n instanceof Label);

            LocalDate today = LocalDate.now();

            if (isChecked) {
                // UNCHECK → update DB
                HabitLogService.markMissed(habitId, today);
                box.getChildren().clear();
            } else {
                // CHECK → update DB
                HabitLogService.markCompleted(habitId);
                box.getChildren().setAll(new Label("✓"));
            }

            refreshTotals();
            refreshRowTotal(habitId, getHabitRow(habitId));
        });

        return box;
    }

    /* =====================================================
       LOCKED BOX (PAST DAYS)
       ===================================================== */
    private StackPane createLockedBox(boolean completed) {

        StackPane box = createEmptyBox();

        if (completed) {
            box.getChildren().setAll(new Label("✓"));
        } else {
            Label cross = new Label("✗");
            cross.setStyle("-fx-text-fill: #888;");
            box.getChildren().setAll(cross);
        }

        // NO BLACK BACKGROUND
        box.setStyle("""
            -fx-border-color: #555;
            -fx-border-radius: 4;
            -fx-background-color: transparent;
        """);

        return box;
    }

    /* =====================================================
       EMPTY BOX
       ===================================================== */
    private StackPane createEmptyBox() {

        StackPane box = new StackPane();
        box.setPrefSize(22, 22);
        box.setMinSize(22, 22);
        box.setMaxSize(22, 22);

        box.setStyle("""
            -fx-border-color: #555;
            -fx-border-radius: 4;
            -fx-background-radius: 4;
        """);

        return box;
    }

    /* =====================================================
       TOTAL REFRESH
       ===================================================== */
    private void refreshTotals() {

        int today = LocalDate.now().getDayOfMonth();
        int bottomRow = habits.length + 1;

        Label bottom = (Label) getNodeAt(today, bottomRow);
        if (bottom != null) {
            bottom.setText(
                    String.valueOf(HabitLogService.getCompletedCountToday())
            );
        }
    }

    private void refreshRowTotal(int habitId, int row) {

        int today = LocalDate.now().getDayOfMonth();
        YearMonth month = YearMonth.now();

        int completedDays =
                HabitLogService.getCompletedDatesForMonth(habitId, month).size();

        Label rowTotal = (Label) getNodeAt(today + 1, row);
        if (rowTotal != null) {
            rowTotal.setText(String.valueOf(completedDays));
        }
    }


    /* =====================================================
       GRID HELPER
       ===================================================== */
    private Node getNodeAt(int col, int row) {
        for (Node node : habitGrid.getChildren()) {
            Integer c = GridPane.getColumnIndex(node);
            Integer r = GridPane.getRowIndex(node);
            if (c != null && r != null && c == col && r == row) {
                return node;
            }
        }
        return null;
    }
}
