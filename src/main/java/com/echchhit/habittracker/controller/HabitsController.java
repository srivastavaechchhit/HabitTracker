package com.echchhit.habittracker.controller;

import com.echchhit.habittracker.service.HabitLogService;
import com.echchhit.habittracker.service.HabitService;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.control.TextInputDialog;
import java.util.Optional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HabitsController {

    @FXML
    private GridPane habitGrid;

    @FXML
    private javafx.scene.control.ComboBox<java.time.YearMonth> monthSelector;

    private final Map<Integer, Integer> habitRowMap = new HashMap<>();

    // TEMP: habitId mapping (later from DB)
    private final Map<String, Integer> habitIdMap = new HashMap<>();

    private Map<Integer, String> dbHabits = new HashMap<>();

    @FXML
    public void initialize() {

        java.time.YearMonth currentMonth = java.time.YearMonth.now();

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
            habitLabel.setStyle("-fx-font-weight: bold;");
            habitGrid.add(habitLabel, 0, rowIndex);

            Set<LocalDate> completedDates =
                    HabitLogService.getCompletedDatesForMonth(habitId, month);

            for (int day = 1; day <= today; day++) {
                LocalDate date = LocalDate.now().withDayOfMonth(day);

                if (day == today) {
                    habitGrid.add(
                            createTodayBox(habitId, completedDates.contains(date)),
                            day,
                            rowIndex
                    );
                } else {
                    habitGrid.add(
                            createLockedBox(completedDates.contains(date)),
                            day,
                            rowIndex
                    );
                }
            }

            Label rowTotal = new Label(String.valueOf(completedDates.size()));
            rowTotal.setStyle("-fx-font-weight: bold;");
            habitGrid.add(rowTotal, today + 1, rowIndex);

            rowIndex++;
        }
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

    private void buildDateHeaderForMonth(java.time.YearMonth month) {

        int days =
                month.equals(java.time.YearMonth.now())
                        ? java.time.LocalDate.now().getDayOfMonth()
                        : month.lengthOfMonth();

        ColumnConstraints habitCol = new ColumnConstraints();
        habitCol.setPercentWidth(20);
        habitGrid.getColumnConstraints().add(habitCol);
        habitGrid.add(new Label("HABITS"), 0, 0);

        double datePercent = 70.0 / days;

        for (int day = 1; day <= days; day++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(datePercent);
            habitGrid.getColumnConstraints().add(cc);

            habitGrid.add(new Label(String.valueOf(day)), day, 0);
        }

        ColumnConstraints totalCol = new ColumnConstraints();
        totalCol.setPercentWidth(10);
        habitGrid.getColumnConstraints().add(totalCol);
        habitGrid.add(new Label("TOTAL"), days + 1, 0);
    }

    private void addBottomTotalRowForMonth(java.time.YearMonth month) {

        int days =
                month.equals(java.time.YearMonth.now())
                        ? java.time.LocalDate.now().getDayOfMonth()
                        : month.lengthOfMonth();

        int bottomRow = habitRowMap.size() + 1;

        habitGrid.add(new Label("TOTAL"), 0, bottomRow);

        int total = 0;
        for (int day = 1; day <= days; day++) {
            total += HabitLogService.getCompletedCountByDate(
                    month.atDay(day)
            );
        }

        habitGrid.add(new Label(String.valueOf(total)), days, bottomRow);
    }

    private int getHabitRow(int habitId) {
        return habitRowMap.getOrDefault(habitId, -1);
    }

    /* =====================================================
       BOTTOM TOTAL
       ===================================================== */
    private void addBottomTotalRow() {

        int bottomRow = habitRowMap.size() + 1;
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
        int bottomRow = habitRowMap.size() + 1;

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

    private void reloadGridForMonth(java.time.YearMonth month) {
        habitGrid.getChildren().clear();
        habitGrid.getColumnConstraints().clear();
        buildDateHeaderForMonth(month);
        addHabitRowsFromDb();
        addBottomTotalRowForMonth(month);
    }

}
