package com.echchhit.habittracker.controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class HabitsController {

    @FXML
    private GridPane habitGrid;

    // Stores TODAY completion state per habit
    private final Map<String, Boolean> todayState = new HashMap<>();

    // Row index mapping for habits
    private final Map<String, Integer> habitRowMap = new HashMap<>();

    // Sample habits (temporary; DB later)
    private final String[] habits = {
            "Exercise",
            "Reading",
            "Meditation",
            "Revision"
    };

    @FXML
    public void initialize() {
        buildDateHeader();
        addHabitRows();
        addBottomTotalRow();
    }

    /* =====================================================
       HEADER (HABITS | 1 | 2 | ... | TODAY | TOTAL)
       ===================================================== */
    private void buildDateHeader() {

        habitGrid.getChildren().clear();
        habitGrid.getColumnConstraints().clear();

        int today = LocalDate.now().getDayOfMonth();

        // HABITS column (20%)
        ColumnConstraints habitCol = new ColumnConstraints();
        habitCol.setPercentWidth(20);
        habitGrid.getColumnConstraints().add(habitCol);
        habitGrid.add(new Label("HABITS"), 0, 0);

        // DATE columns (70% distributed)
        double datePercent = 70.0 / today;

        for (int day = 1; day <= today; day++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(datePercent);
            habitGrid.getColumnConstraints().add(cc);

            Label lbl = new Label(String.valueOf(day));
            lbl.setStyle("-fx-font-weight: bold;");
            habitGrid.add(lbl, day, 0);
        }

        // TOTAL column (10%)
        ColumnConstraints totalCol = new ColumnConstraints();
        totalCol.setPercentWidth(10);
        habitGrid.getColumnConstraints().add(totalCol);
        habitGrid.add(new Label("TOTAL"), today + 1, 0);
    }

    /* =====================================================
       HABIT ROWS + BOXES
       ===================================================== */
    private void addHabitRows() {

        int today = LocalDate.now().getDayOfMonth();

        for (int i = 0; i < habits.length; i++) {

            String habit = habits[i];
            int row = i + 1;

            habitRowMap.put(habit, row);
            todayState.put(habit, false);

            // Habit name
            Label habitLabel = new Label(habit);
            habitLabel.setStyle("-fx-font-weight: bold;");
            habitGrid.add(habitLabel, 0, row);

            // Date boxes
            for (int day = 1; day <= today; day++) {

                if (day == today) {
                    habitGrid.add(createTodayBox(habit), day, row);
                } else {
                    habitGrid.add(createLockedBox(), day, row);
                }
            }

            // Right-side TOTAL (per habit)
            Label rowTotal = new Label("0");
            rowTotal.setStyle("-fx-font-weight: bold;");
            habitGrid.add(rowTotal, today + 1, row);
        }
    }

    /* =====================================================
       BOTTOM TOTAL ROW
       ===================================================== */
    private void addBottomTotalRow() {

        int bottomRow = habits.length + 1;
        int today = LocalDate.now().getDayOfMonth();

        Label totalLabel = new Label("TOTAL");
        totalLabel.setStyle("-fx-font-weight: bold;");
        habitGrid.add(totalLabel, 0, bottomRow);

        Label totalValue = new Label("0");
        totalValue.setStyle("-fx-font-weight: bold;");
        habitGrid.add(totalValue, today, bottomRow);
    }

    /* =====================================================
       TODAY BOX (CLICKABLE)
       ===================================================== */
    private StackPane createTodayBox(String habit) {

        StackPane box = createEmptyBox();

        box.setOnMouseClicked(e -> {

            boolean done = todayState.get(habit);
            todayState.put(habit, !done);

            if (!done) {
                box.getChildren().setAll(new Label("✓"));
            } else {
                box.getChildren().clear();
            }

            updateRowTotal(habit);
            updateBottomTotal();
        });

        return box;
    }

    /* =====================================================
       LOCKED BOX (PAST DAYS)
       ===================================================== */
    private StackPane createLockedBox() {

        StackPane box = createEmptyBox();
        box.setStyle("""
            -fx-border-color: #333;
            -fx-background-color: #1e1e1e;
            -fx-border-radius: 4;
            -fx-background-radius: 4;
        """);

        return box;
    }

    /* =====================================================
       EMPTY SQUARE BOX
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
            -fx-background-color: transparent;
        """);

        return box;
    }

    /* =====================================================
       TOTAL UPDATES
       ===================================================== */
    private void updateRowTotal(String habit) {

        int row = habitRowMap.get(habit);
        int today = LocalDate.now().getDayOfMonth();

        Label label = (Label) getNodeAt(today + 1, row);
        label.setText(todayState.get(habit) ? "1" : "0");
    }

    private void updateBottomTotal() {

        int total = 0;
        for (boolean done : todayState.values()) {
            if (done) total++;
        }

        int bottomRow = habits.length + 1;
        int today = LocalDate.now().getDayOfMonth();

        Label label = (Label) getNodeAt(today, bottomRow);
        label.setText(String.valueOf(total));
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
