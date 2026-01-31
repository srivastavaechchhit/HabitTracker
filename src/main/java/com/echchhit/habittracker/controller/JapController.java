package com.echchhit.habittracker.controller;

import com.echchhit.habittracker.service.JapService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;

public class JapController {
    @FXML private TextField countField;
    @FXML private Label totalChantsLabel;
    @FXML private ListView<String> japHistoryList;

    private final ObservableList<String> historyData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        japHistoryList.setItems(historyData);

        // Updated Cell Factory using CSS Classes
        japHistoryList.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                        setText(null);
                        setContextMenu(null);
                        setStyle("-fx-background-color: transparent"); // Clear background for empty cells
                    } else {
                        if (!item.contains(" : ")) {
                            setText(item);
                            setGraphic(null);
                        } else {
                            String[] parts = item.split(" : ");

                            // 1. Date Label (Use CSS Class)
                            Label dateLabel = new Label(parts[0]);
                            dateLabel.getStyleClass().add("jap-date-label");

                            // 2. Count Label (Use CSS Class)
                            Label countLabel = new Label(parts[1]);
                            countLabel.getStyleClass().add("jap-count-label");

                            Region spacer = new Region();
                            HBox.setHgrow(spacer, Priority.ALWAYS);

                            // 3. Container (Use CSS Class)
                            HBox container = new HBox(dateLabel, spacer, countLabel);
                            container.setAlignment(Pos.CENTER_LEFT);
                            container.getStyleClass().add("jap-list-cell");

                            setGraphic(container);
                            setText(null);

                            // Ensure the cell background itself is transparent so the container shows
                            setStyle("-fx-background-color: transparent; -fx-padding: 5 0;");

                            // Context Menu
                            ContextMenu contextMenu = new ContextMenu();
                            MenuItem deleteItem = new MenuItem("Delete Entry");
                            deleteItem.setOnAction(event -> {
                                JapService.deleteJapLog(parts[0]);
                                refreshDisplay();
                                if (parts[0].equals(java.time.LocalDate.now().toString())) {
                                    countField.setText("0");
                                }
                            });
                            contextMenu.getItems().add(deleteItem);
                            setContextMenu(contextMenu);
                        }
                    }
                }
            };
            return cell;
        });

        refreshDisplay();
    }

    @FXML
    private void handleUpdate() {
        try {
            int val = Integer.parseInt(countField.getText().trim());
            if (val >= 0 && val <= 100000) {
                JapService.updateJapCount(val);
                refreshDisplay();
            } else {
                showAlert(Alert.AlertType.ERROR, "Limit Exceeded", "Please enter a number between 0 and 100,000.");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid numeric value.");
        }
    }

    private void refreshDisplay() {
        totalChantsLabel.setText(String.format("%,d", JapService.getTotalChants()));
        countField.setText(String.valueOf(JapService.getTodayCount()));
        historyData.clear();
        historyData.addAll(JapService.getAllJapLogs());
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }
}