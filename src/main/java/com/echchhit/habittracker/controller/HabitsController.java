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

    @FXML
    private GridPane habitGrid;

    @FXML
    private ComboBox<YearMonth> monthSelector;

    private final Map<Integer, Integer> habitRowMap = new HashMap<>();
    private final Map<String, Integer> habitIdMap = new HashMap<>();
    private Map<Integer, String> dbHabits = new HashMap<>();

    @FXML
    public void initialize() {

        YearMonth currentMonth = YearMonth.now();

        monthSelector.getItems().clear();
        monthSelector.getItems().add(currentMonth);
        monthSelector.setValue(currentMonth);
        monthSelector.setOnAction(e -> reloadGridForMonth(monthSelector.getValue()));

        dbHabits = HabitService.getAllHabitsWithIds();
        habitIdMap.clear();
        dbHabits.forEach((id, name) -> habitIdMap.put(name, id));

        // Auto-cross yesterday (ONLY at startup = day rollover)
        LocalDate yesterday = LocalDate.now().minusDays(1);
        for (int id : habitIdMap.values()) {
            HabitLogService.autoCrossIfMissed(id, yesterday);
        }

        buildDateHeader();
        addHabitRowsFromDb();
        addBottomTotalRow();
    }

    @FXML
    private void onAddHabit() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Habit");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter habit name:");
        // Minimal styling for the dialog to match theme slightly better
        dialog.getDialogPane().setStyle("-fx-background-color: #2a2a2a;");
        dialog.getDialogPane().lookup(".content").setStyle("-fx-text-fill: white;");

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            String habitName = result.get().trim();
            if (!habitName.isEmpty()) {
                HabitService.addHabit(habitName);
                reloadHabits();
            }
        }
    }

    private void reloadHabits() {
        habitGrid.getChildren().clear();
        habitGrid.getColumnConstraints().clear();
        dbHabits = HabitService.getAllHabitsWithIds();
        buildDateHeader();
        addHabitRowsFromDb();
        addBottomTotalRow();
    }

    private void addHabitRowsFromDb() {
        int today = LocalDate.now().getDayOfMonth();
        YearMonth month = YearMonth.now();
        int rowIndex = 1;

        for (Map.Entry<Integer, String> entry : dbHabits.entrySet()) {
            int habitId = entry.getKey();
            String habit = entry.getValue();

            habitRowMap.put(habitId, rowIndex);

            Label habitLabel = new Label(habit);
            habitLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

            // Context Menu for finishing/deleting a habit
            ContextMenu menu = new ContextMenu();
            MenuItem completeItem = new MenuItem("Archive / Mark Complete");
            completeItem.setOnAction(e -> confirmAndCompleteHabit(habitId));
            menu.getItems().add(completeItem);
            habitLabel.setContextMenu(menu);

            habitGrid.add(habitLabel, 0, rowIndex);

            Set<LocalDate> completedDates = HabitLogService.getCompletedDatesForMonth(habitId, month);

            for (int day = 1; day <= today; day++) {
                LocalDate date = LocalDate.now().withDayOfMonth(day);

                if (day == today) {
                    // This is the interactive box for TODAY
                    habitGrid.add(
                            createTodayBox(habitId, completedDates.contains(date)),
                            day,
                            rowIndex
                    );
                } else {
                    // Past days (locked)
                    habitGrid.add(
                            createLockedBox(completedDates.contains(date)),
                            day,
                            rowIndex
                    );
                }
            }

            // Row Total Count
            Label rowTotal = new Label(String.valueOf(completedDates.size()));
            rowTotal.setStyle("-fx-font-weight: bold; -fx-text-fill: #666;");
            habitGrid.add(rowTotal, today + 1, rowIndex);

            rowIndex++;
        }
    }

    private void confirmAndCompleteHabit(int habitId) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Archive Habit");
        alert.setHeaderText("Finish this habit?");
        alert.setContentText("You will stop tracking this habit, but data will be saved.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            HabitService.markHabitCompleted(habitId);
            reloadHabits();
        }
    }

    /* =====================================================
       GRID HEADER & TOTALS
       ===================================================== */
    private void buildDateHeader() {
        habitGrid.getChildren().clear();
        habitGrid.getColumnConstraints().clear();

        int today = LocalDate.now().getDayOfMonth();

        // Habit Name Column
        ColumnConstraints habitCol = new ColumnConstraints();
        habitCol.setPercentWidth(20);
        habitGrid.getColumnConstraints().add(habitCol);

        Label habitsHeader = new Label("HABITS");
        habitsHeader.setStyle("-fx-text-fill: #888; -fx-font-size: 10px;");
        habitGrid.add(habitsHeader, 0, 0);

        // Date Columns
        double availableWidth = 70.0; // 70% of space for days
        double datePercent = availableWidth / today;

        for (int day = 1; day <= today; day++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(datePercent);
            habitGrid.getColumnConstraints().add(cc);

            Label lbl = new Label(String.valueOf(day));
            lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: " + (day == today ? "#6C63FF" : "#888") + ";");
            habitGrid.add(lbl, day, 0);
        }

        // Total Column
        ColumnConstraints totalCol = new ColumnConstraints();
        totalCol.setPercentWidth(10);
        habitGrid.getColumnConstraints().add(totalCol);

        Label totalHeader = new Label("TOTAL");
        totalHeader.setStyle("-fx-text-fill: #888; -fx-font-size: 10px;");
        habitGrid.add(totalHeader, today + 1, 0);
    }

    private void reloadGridForMonth(YearMonth month) {
        // Simple reload for now (same logic)
        habitGrid.getChildren().clear();
        habitGrid.getColumnConstraints().clear();
        // NOTE: For full historic browsing, you'd adapt buildDateHeader to 'month.lengthOfMonth()'
        // calling buildDateHeader() which defaults to 'today' for simplicity in this snippet
        initialize();
    }

    private void addBottomTotalRow() {
        int bottomRow = habitRowMap.size() + 1;
        int today = LocalDate.now().getDayOfMonth();

        Label totalLabel = new Label("TOTAL");
        totalLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #888; -fx-font-size: 10px;");
        habitGrid.add(totalLabel, 0, bottomRow);

        Label totalValue = new Label(String.valueOf(HabitLogService.getCompletedCountToday()));
        totalValue.setStyle("-fx-font-weight: bold; -fx-text-fill: #6C63FF;");
        habitGrid.add(totalValue, today, bottomRow);
    }

    /* =====================================================
       INTERACTIVE TODAY BOX (WITH XP & ANIMATION)
       ===================================================== */
    private StackPane createTodayBox(int habitId, boolean initiallyCompleted) {
        StackPane box = createEmptyBox();
        box.setStyle("-fx-cursor: hand; -fx-background-color: transparent; -fx-border-color: #555; -fx-border-radius: 4;");

        if (initiallyCompleted) {
            addCheckMark(box);
        }

        box.setOnMouseClicked(e -> {
            boolean isChecked = !box.getChildren().isEmpty(); // logic: empty = unchecked

            LocalDate today = LocalDate.now();

            if (isChecked) {
                // UNCHECK
                HabitLogService.markMissed(habitId, today);
                box.getChildren().clear();
                box.setStyle("-fx-cursor: hand; -fx-background-color: transparent; -fx-border-color: #555; -fx-border-radius: 4;");
            } else {
                // CHECK
                HabitLogService.markCompleted(habitId);
                addCheckMark(box);
                // Highlight border
                box.setStyle("-fx-cursor: hand; -fx-background-color: rgba(76, 175, 80, 0.1); -fx-border-color: #4CAF50; -fx-border-radius: 4;");
            }

            // 1. Refresh Totals in Grid
            refreshTotals();
            refreshRowTotal(habitId, getHabitRow(habitId));

            // 2. TRIGGER XP UPDATE IN SIDEBAR (The crucial new part!)
            MainController.refreshStats();
        });

        return box;
    }

    private void addCheckMark(StackPane box) {
        Label check = new Label("✓");
        check.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 14px; -fx-font-weight: bold;");
        box.getChildren().setAll(check);

        // ANIMATION: Pop effect
        ScaleTransition st = new ScaleTransition(Duration.millis(200), check);
        st.setFromX(0.1);
        st.setFromY(0.1);
        st.setToX(1.0);
        st.setToY(1.0);
        st.play();
    }

    private StackPane createLockedBox(boolean completed) {
        StackPane box = createEmptyBox();
        if (completed) {
            Label check = new Label("✓");
            check.setStyle("-fx-text-fill: #666;"); // Greyed out for past
            box.getChildren().setAll(check);
        } else {
            Label cross = new Label("✗");
            cross.setStyle("-fx-text-fill: #444; -fx-font-size: 10px;");
            box.getChildren().setAll(cross);
        }
        return box;
    }

    private StackPane createEmptyBox() {
        StackPane box = new StackPane();
        box.setPrefSize(24, 24);
        box.setMinSize(24, 24);
        box.setMaxSize(24, 24);
        box.setStyle("-fx-border-color: #333; -fx-border-radius: 4;");
        return box;
    }

    /* =====================================================
       REFRESH HELPERS
       ===================================================== */
    private void refreshTotals() {
        int today = LocalDate.now().getDayOfMonth();
        int bottomRow = habitRowMap.size() + 1;
        Label bottom = (Label) getNodeAt(today, bottomRow);
        if (bottom != null) {
            bottom.setText(String.valueOf(HabitLogService.getCompletedCountToday()));
        }
    }

    private void refreshRowTotal(int habitId, int row) {
        YearMonth month = YearMonth.now();
        int completedDays = HabitLogService.getCompletedDatesForMonth(habitId, month).size();
        int colIndex = LocalDate.now().getDayOfMonth() + 1;

        Label rowTotal = (Label) getNodeAt(colIndex, row);
        if (rowTotal != null) {
            rowTotal.setText(String.valueOf(completedDays));
        }
    }

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

    private int getHabitRow(int habitId) {
        return habitRowMap.getOrDefault(habitId, -1);
    }
}