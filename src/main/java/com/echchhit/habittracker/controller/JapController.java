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
    @FXML private ListView<String> japHistoryList;

    private final ObservableList<String> historyData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        countField.setText(String.valueOf(JapService.getTodayCount()));
        japHistoryList.setItems(historyData);

        japHistoryList.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    // Check if the data matches the "Date : Count Chants" format
                    if (!item.contains(" : ")) {
                        setText(item); // Show raw text if separator is missing
                        setGraphic(null);
                    } else {
                        String[] parts = item.split(" : ");

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
                    }
                }
            }
        });

        refreshHistory();
    }

    @FXML
    private void handleUpdate() {
        try {
            int val = Integer.parseInt(countField.getText());
            if (val >= 0 && val <= 100000) {
                JapService.updateJapCount(val);
                MainController.refreshStats();
                refreshHistory();
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid input");
        }
    }

    private void refreshHistory() {
        historyData.clear();
        historyData.addAll(JapService.getAllJapLogs());
    }
}