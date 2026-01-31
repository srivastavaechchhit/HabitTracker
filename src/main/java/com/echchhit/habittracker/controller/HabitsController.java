package com.echchhit.habittracker.controller;

import com.echchhit.habittracker.service.HabitLogService;
import com.echchhit.habittracker.service.HabitService;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.GridPane;
import javafx.application.Platform;
import javafx.scene.layout.HBox;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.util.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.animation.*;
import java.util.HashMap;
import javafx.scene.Node;
import javafx.fxml.FXML;
import java.util.List;
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

        // Trigger the synchronized entrance animation every time the page loads
        Platform.runLater(this::animateGridEntrySynchronized);
    }

    private void refreshUI() {
        habitGrid.getChildren().clear();
        habitGrid.getColumnConstraints().clear();

        // This fetches habits in the correct 'sort_order' thanks to your HabitService update
        dbHabits = HabitService.getAllHabitsWithIds();

        buildDateHeader();
        addHabitRows();
        addBottomTotalRow();
    }

    private void animateGridEntrySynchronized() {
        ParallelTransition masterParallel = new ParallelTransition();

        double gridDelayFactor = 20.0; // Speed of diagonal reveal
        double listDelayFactor = 40.0; // Stagger between habit names
        Duration animDuration = Duration.millis(400); // Slightly longer for "settling" feel

        // 1. ANIMATE GRID BOXES & HEADERS
        for (Node node : habitGrid.getChildren()) {
            Integer col = GridPane.getColumnIndex(node);
            Integer row = GridPane.getRowIndex(node);
            if (col == null) col = 0;
            if (row == null) row = 0;

            // Skip the first column (Habit Names) handled in step 2
            if (col == 0) continue;

            node.setOpacity(0.0);

            // SPECIAL ANIMATION FOR DATE HEADERS (Row 0, Columns > 0)
            if (row == 0 && col > 0) {
                node.setScaleX(3.5); // Start big
                node.setScaleY(3.5);

                ScaleTransition settleScale = new ScaleTransition(animDuration, node);
                settleScale.setToX(1.0);
                settleScale.setToY(1.0);

                FadeTransition fade = new FadeTransition(animDuration, node);
                fade.setToValue(1.0);

                ParallelTransition headerAnim = new ParallelTransition(node, settleScale, fade);
                headerAnim.setDelay(Duration.millis(col * gridDelayFactor));
                masterParallel.getChildren().add(headerAnim);
            } else {
                // Diagonal reveal for boxes and totals
                FadeTransition fade = new FadeTransition(animDuration, node);
                fade.setToValue(1.0);
                fade.setDelay(Duration.millis((row + col) * gridDelayFactor));
                masterParallel.getChildren().add(fade);
            }
        }

        // 2. ANIMATE HABIT LIST (Staggered Slide-in)
        int habitRowCounter = 0;
        for (Node node : habitGrid.getChildren()) {
            Integer col = GridPane.getColumnIndex(node);
            Integer row = GridPane.getRowIndex(node);

            if (col != null && col == 0 && row != null && row > 0) {
                node.setOpacity(0.0);

                TranslateTransition slide = new TranslateTransition(animDuration, node);
                slide.setFromX(-30);
                slide.setToX(0);

                FadeTransition fade = new FadeTransition(animDuration, node);
                fade.setToValue(1.0);

                ParallelTransition combine = new ParallelTransition(node, slide, fade);
                combine.setDelay(Duration.millis(habitRowCounter * listDelayFactor));

                masterParallel.getChildren().add(combine);
                habitRowCounter++;
            }
        }

        masterParallel.play();
    }

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

        // 1. Get the current ordered list of IDs to help with index calculations
        List<Integer> currentOrder = new ArrayList<>(dbHabits.keySet());

        for (Map.Entry<Integer, String> entry : dbHabits.entrySet()) {
            int habitId = entry.getKey();
            String name = entry.getValue();
            habitRowMap.put(habitId, rowIndex);

            // --- DRAG HANDLE ---
            Label dragHandle = new Label("≡");
            dragHandle.setStyle("-fx-font-size: 18px; -fx-text-fill: #aaa; -fx-padding: 0 8 0 0;");
            dragHandle.setCursor(Cursor.MOVE);
            dragHandle.setTooltip(new Tooltip("Drag to reorder"));

            // --- HABIT NAME ---
            Label habitLabel = new Label(name);
            habitLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
            habitLabel.setMaxWidth(Double.MAX_VALUE);

            // Context Menu
            ContextMenu menu = new ContextMenu();
            MenuItem archive = new MenuItem("Archive Habit");
            archive.setOnAction(e -> archiveHabit(habitId));
            menu.getItems().add(archive);
            habitLabel.setContextMenu(menu);

            // --- CONTAINER (Handle + Name) ---
            HBox nameContainer = new HBox(dragHandle, habitLabel);
            nameContainer.setAlignment(Pos.CENTER_LEFT);
            nameContainer.setUserData(habitId); // Store ID for drop logic

            // --- DRAG EVENTS ---

            // A. START DRAG (On the Handle only)
            dragHandle.setOnDragDetected(event -> {
                Dragboard db = dragHandle.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(String.valueOf(habitId)); // Carry the ID
                db.setContent(content);
                event.consume();
            });

            // B. DRAG OVER (On the whole row name area)
            nameContainer.setOnDragOver(event -> {
                if (event.getGestureSource() != nameContainer && event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
                event.consume();
            });

            // C. DROP (Reorder Logic)
            nameContainer.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasString()) {
                    int sourceId = Integer.parseInt(db.getString());
                    int targetId = (int) nameContainer.getUserData();

                    if (sourceId != targetId) {
                        // Reorder the local list
                        currentOrder.remove(Integer.valueOf(sourceId));
                        int targetIndex = currentOrder.indexOf(targetId);
                        currentOrder.add(targetIndex, sourceId);

                        // Save to DB and Refresh UI
                        HabitService.updateHabitOrder(currentOrder);
                        refreshUI();
                        success = true;
                    }
                }
                event.setDropCompleted(success);
                event.consume();
            });

            habitGrid.add(nameContainer, 0, rowIndex);

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
        int todayCol = LocalDate.now().getDayOfMonth();

        box.getStyleClass().add("today-column-cell");
        if (initiallyCompleted) {
            box.getStyleClass().add("completed-today");
            addCheckMark(box);
        }

        // Column-wide hover listeners
        box.setOnMouseEntered(e -> applyColumnHover(todayCol, true));
        box.setOnMouseExited(e -> applyColumnHover(todayCol, false));

        box.setOnMouseClicked(e -> {
            boolean isChecked = !box.getChildren().isEmpty();
            LocalDate today = LocalDate.now();

            if (isChecked) {
                HabitLogService.markMissed(habitId, today);
                box.getChildren().clear();
                box.getStyleClass().remove("completed-today");
            } else {
                HabitLogService.markCompleted(habitId, today);
                addCheckMark(box);
                box.getStyleClass().add("completed-today");
            }

            refreshTotals();
            refreshRowTotal(habitId, getHabitRow(habitId));
        });

        return box;
    }

    private void applyColumnHover(int colIndex, boolean isHovered) {
        double scale = isHovered ? 1.1 : 1.0;
        java.util.ArrayList<Node> childrenCopy = new java.util.ArrayList<>(habitGrid.getChildren());

        for (Node node : childrenCopy) {
            Integer c = GridPane.getColumnIndex(node);
            if (c != null && c == colIndex) {
                node.setScaleX(scale);
                node.setScaleY(scale);
                if (isHovered) {
                    node.toFront();
                }
            }
        }
    }

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
                Platform.runLater(this::animateGridEntrySynchronized);
            }
        });
    }

    private void addCheckMark(StackPane box) {
        Label check = new Label("✓");
        check.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 14px; -fx-font-weight: bold;");
        box.getChildren().setAll(check);
        ScaleTransition st = new ScaleTransition(Duration.millis(300), check);
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

    private void archiveHabit(int habitId) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Archive this habit?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(type -> {
            if (type == ButtonType.YES) {
                HabitService.markHabitCompleted(habitId);
                refreshUI();
            }
        });
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

    private int getHabitRow(int habitId) {
        return habitRowMap.getOrDefault(habitId, -1);
    }
}