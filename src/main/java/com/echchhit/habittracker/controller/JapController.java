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
        // Link the ObservableList to the ListView
        japHistoryList.setItems(historyData);

        // Custom Cell Factory for a modern look and Deletion feature
        japHistoryList.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                        setText(null);
                        setContextMenu(null);
                    } else {
                        // Safety check for the "Date : Count Chants" format
                        if (!item.contains(" : ")) {
                            setText(item);
                            setGraphic(null);
                        } else {
                            String[] parts = item.split(" : ");

                            // UI elements for the list row
                            Label dateLabel = new Label(parts[0]);
                            dateLabel.setStyle("-fx-text-fill: #888; -fx-font-weight: bold;");

                            Label countLabel = new Label(parts[1]);
                            countLabel.setStyle("-fx-text-fill: #6C63FF; -fx-font-weight: 800;");

                            Region spacer = new Region();
                            HBox.setHgrow(spacer, Priority.ALWAYS);

                            HBox container = new HBox(dateLabel, spacer, countLabel);
                            container.setAlignment(Pos.CENTER_LEFT);
                            container.setStyle("-fx-padding: 10; -fx-background-color: white; -fx-background-radius: 5;");

                            setGraphic(container);
                            setText(null);

                            // Create Context Menu for Deletion
                            ContextMenu contextMenu = new ContextMenu();
                            MenuItem deleteItem = new MenuItem("Delete Entry");
                            deleteItem.setOnAction(event -> {
                                JapService.deleteJapLog(parts[0]); // Delete by date
                                refreshDisplay();
                                // Reset today's field if the deleted entry was for today
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
            // Validating the 100,000 limit
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

    /**
     * Synchronizes all UI components with the latest data from the database.
     */
    private void refreshDisplay() {
        // Update lifetime total with formatting
        totalChantsLabel.setText(String.format("%,d", JapService.getTotalChants()));

        // Update today's entry field
        countField.setText(String.valueOf(JapService.getTodayCount()));

        // Update the history list
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