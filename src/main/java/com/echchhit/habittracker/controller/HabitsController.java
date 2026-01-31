package com.echchhit.habittracker.controller;

import com.echchhit.habittracker.service.HabitLogService;
import com.echchhit.habittracker.service.HabitService;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

public class HabitsController {

    @FXML private GridPane habitGrid;
    @FXML private ComboBox<YearMonth> monthSelector;
    @FXML private Button viewToggleBtn;

    private final Map<Integer, Integer> habitRowMap = new HashMap<>();
    private Map<Integer, String> dbHabits = new HashMap<>();
    private YearMonth selectedMonth = YearMonth.now();

    private boolean isCardView = false;

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
        Platform.runLater(this::animateGridEntrySynchronized);
    }

    @FXML
    private void onToggleView() {
        isCardView = !isCardView;
        if (viewToggleBtn != null) {
            viewToggleBtn.setText(isCardView ? "☰" : "⊞");
        }
        refreshUI();
        animateGridEntrySynchronized();
    }

    private void refreshUI() {
        habitGrid.getChildren().clear();
        habitGrid.getColumnConstraints().clear();
        habitGrid.getRowConstraints().clear();

        dbHabits = HabitService.getAllHabitsWithIds();

        if (isCardView) {
            renderCardView();
        } else {
            renderTableView();
        }
    }

    // --- CARD VIEW RENDERING ---
    private void renderCardView() {
        int columns = 3;
        for(int i=0; i<columns; i++){
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(100.0 / columns);
            habitGrid.getColumnConstraints().add(col);
        }

        int index = 0;
        LocalDate today = LocalDate.now();

        for (Map.Entry<Integer, String> entry : dbHabits.entrySet()) {
            int habitId = entry.getKey();
            String name = entry.getValue();

            int col = index % columns;
            int row = index / columns;

            boolean isCompleted = HabitLogService.getCompletedDatesForMonth(habitId, YearMonth.now()).contains(today);

            Node card = createHabitCard(habitId, name, isCompleted);
            habitGrid.add(card, col, row);

            index++;
        }
    }

    private Node createHabitCard(int habitId, String name, boolean isCompleted) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new javafx.geometry.Insets(20));

        String baseStyle = "-fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);";
        String bgStyle = isCompleted
                ? "-fx-background-color: linear-gradient(to bottom right, #6C63FF, #5a52d5);"
                : "-fx-background-color: white;";
        card.setStyle(baseStyle + bgStyle);

        Label nameLbl = new Label(name);
        nameLbl.setWrapText(true);
        nameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; " +
                (isCompleted ? "-fx-text-fill: white;" : "-fx-text-fill: #333;"));

        Label statusLbl = new Label(isCompleted ? "Completed Today!" : "Not done yet");
        statusLbl.setStyle("-fx-font-size: 12px; " + (isCompleted ? "-fx-text-fill: #e0e0e0;" : "-fx-text-fill: #888;"));

        Button actionBtn = new Button(isCompleted ? "✓" : "Mark Done");
        actionBtn.setStyle(isCompleted
                ? "-fx-background-color: white; -fx-text-fill: #6C63FF; -fx-background-radius: 20; -fx-font-weight: bold;"
                : "-fx-background-color: #f0f0f0; -fx-text-fill: #666; -fx-background-radius: 20;");
        actionBtn.setCursor(Cursor.HAND);

        actionBtn.setOnAction(e -> {
            if (isCompleted) {
                HabitLogService.markMissed(habitId, LocalDate.now());
            } else {
                HabitLogService.markCompleted(habitId, LocalDate.now());
            }
            refreshUI();
        });

        // --- CONTEXT MENU FOR CARDS ---
        ContextMenu menu = createContextMenu(habitId, name);
        card.setOnContextMenuRequested(e -> menu.show(card, e.getScreenX(), e.getScreenY()));

        card.getChildren().addAll(nameLbl, statusLbl, actionBtn);

        card.setOnMouseEntered(e -> { card.setScaleX(1.03); card.setScaleY(1.03); });
        card.setOnMouseExited(e -> { card.setScaleX(1.0); card.setScaleY(1.0); });

        return card;
    }

    // --- TABLE VIEW RENDERING ---
    private void renderTableView() {
        buildDateHeader();
        addHabitRows();
        addBottomTotalRow();
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

        List<Integer> currentOrder = new ArrayList<>(dbHabits.keySet());

        for (Map.Entry<Integer, String> entry : dbHabits.entrySet()) {
            int habitId = entry.getKey();
            String name = entry.getValue();
            habitRowMap.put(habitId, rowIndex);

            // Drag Handle
            Label dragHandle = new Label("≡");
            dragHandle.setStyle("-fx-font-size: 18px; -fx-text-fill: #aaa; -fx-padding: 0 8 0 0;");
            dragHandle.setCursor(Cursor.MOVE);
            dragHandle.setTooltip(new Tooltip("Drag to reorder"));

            // Habit Name
            Label habitLabel = new Label(name);
            habitLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
            habitLabel.setMaxWidth(Double.MAX_VALUE);

            // --- CONTEXT MENU FOR LIST ITEM ---
            habitLabel.setContextMenu(createContextMenu(habitId, name));

            HBox nameContainer = new HBox(dragHandle, habitLabel);
            nameContainer.setAlignment(Pos.CENTER_LEFT);
            nameContainer.setUserData(habitId);

            // Drag Logic
            dragHandle.setOnDragDetected(event -> {
                Dragboard db = dragHandle.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(String.valueOf(habitId));
                db.setContent(content);
                event.consume();
            });

            nameContainer.setOnDragOver(event -> {
                if (event.getGestureSource() != nameContainer && event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
                event.consume();
            });

            nameContainer.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasString()) {
                    int sourceId = Integer.parseInt(db.getString());
                    int targetId = (int) nameContainer.getUserData();
                    if (sourceId != targetId) {
                        currentOrder.remove(Integer.valueOf(sourceId));
                        int targetIndex = currentOrder.indexOf(targetId);
                        currentOrder.add(targetIndex, sourceId);
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

    private void addBottomTotalRow() {
        int bottomRow = habitRowMap.size() + 1;
        Label totalLabel = new Label("TOTAL");
        totalLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #888; -fx-font-size: 10px;");
        habitGrid.add(totalLabel, 0, bottomRow);

        Label totalValue = new Label(String.valueOf(HabitLogService.getCompletedCountToday()));
        totalValue.setStyle("-fx-font-weight: bold; -fx-text-fill: #6C63FF;");
        habitGrid.add(new StackPane(totalValue), LocalDate.now().getDayOfMonth(), bottomRow);
    }

    // --- HELPER METHODS ---

    private ContextMenu createContextMenu(int habitId, String currentName) {
        ContextMenu menu = new ContextMenu();

        MenuItem rename = new MenuItem("Rename");
        rename.setOnAction(e -> renameHabit(habitId, currentName));

        MenuItem archive = new MenuItem("Archive");
        archive.setOnAction(e -> archiveHabit(habitId));

        MenuItem delete = new MenuItem("Delete Permanently");
        delete.setStyle("-fx-text-fill: red;");
        delete.setOnAction(e -> deleteHabit(habitId));

        menu.getItems().addAll(rename, archive, new SeparatorMenuItem(), delete);
        return menu;
    }

    private void renameHabit(int habitId, String currentName) {
        TextInputDialog dialog = new TextInputDialog(currentName);
        dialog.setTitle("Rename Habit");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter new name:");
        dialog.showAndWait().ifPresent(newName -> {
            if (!newName.trim().isEmpty() && !newName.equals(currentName)) {
                HabitService.updateHabitName(habitId, newName.trim());
                refreshUI();
            }
        });
    }

    private void archiveHabit(int habitId) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Archive this habit? It will be hidden from the list.", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(type -> {
            if (type == ButtonType.YES) {
                HabitService.markHabitCompleted(habitId); // Sets status to 'COMPLETED' (Archived)
                refreshUI();
            }
        });
    }

    private void deleteHabit(int habitId) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete this habit permanently? All history will be lost.", ButtonType.YES, ButtonType.NO);
        alert.setHeaderText("Danger Zone");
        alert.showAndWait().ifPresent(type -> {
            if (type == ButtonType.YES) {
                HabitService.deleteHabit(habitId);
                refreshUI();
            }
        });
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

    private StackPane createTodayBox(int habitId, boolean initiallyCompleted) {
        StackPane box = createEmptyBox();
        int todayCol = LocalDate.now().getDayOfMonth();

        box.getStyleClass().add("today-column-cell");
        if (initiallyCompleted) {
            box.getStyleClass().add("completed-today");
            addCheckMark(box);
        }

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

    private void addCheckMark(StackPane box) {
        Label check = new Label("✓");
        check.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 14px; -fx-font-weight: bold;");
        box.getChildren().setAll(check);
        ScaleTransition st = new ScaleTransition(Duration.millis(300), check);
        st.setFromX(0.1); st.setFromY(0.1);
        st.setToX(1.0); st.setToY(1.0);
        st.play();
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
        if (isCardView) return;

        int today = LocalDate.now().getDayOfMonth();
        Node n = getNodeAt(today, habitRowMap.size() + 1);
        if (n instanceof StackPane sp && !sp.getChildren().isEmpty()) {
            ((Label)sp.getChildren().get(0)).setText(String.valueOf(HabitLogService.getCompletedCountToday()));
        }
    }

    private void refreshRowTotal(int habitId, int row) {
        if (isCardView) return;

        int count = HabitLogService.getCompletedDatesForMonth(habitId, selectedMonth).size();
        Node n = getNodeAt(selectedMonth.lengthOfMonth() + 1, row);
        if (n instanceof StackPane sp && !sp.getChildren().isEmpty()) {
            ((Label)sp.getChildren().get(0)).setText(String.valueOf(count));
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

    private void applyColumnHover(int colIndex, boolean isHovered) {
        double scale = isHovered ? 1.1 : 1.0;
        List<Node> childrenCopy = new ArrayList<>(habitGrid.getChildren());

        for (Node node : childrenCopy) {
            Integer c = GridPane.getColumnIndex(node);
            if (c != null && c == colIndex) {
                node.setScaleX(scale);
                node.setScaleY(scale);
                if (isHovered) node.toFront();
            }
        }
    }

    private void animateGridEntrySynchronized() {
        ParallelTransition masterParallel = new ParallelTransition();
        Duration animDuration = Duration.millis(400);

        int counter = 0;
        for (Node node : habitGrid.getChildren()) {
            node.setOpacity(0.0);
            FadeTransition fade = new FadeTransition(animDuration, node);
            fade.setToValue(1.0);
            TranslateTransition slide = new TranslateTransition(animDuration, node);
            slide.setFromY(20);
            slide.setToY(0);

            ParallelTransition itemAnim = new ParallelTransition(node, fade, slide);
            itemAnim.setDelay(Duration.millis(counter * 20));
            masterParallel.getChildren().add(itemAnim);
            counter++;
        }
        masterParallel.play();
    }
}