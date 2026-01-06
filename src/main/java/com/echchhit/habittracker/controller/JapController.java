package com.echchhit.habittracker.controller;

import com.echchhit.habittracker.service.JapService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.ListView;
import javafx.scene.control.Alert;

public class JapController {
    @FXML private TextField countField;
    @FXML private ListView<String> japHistoryList;

    private final ObservableList<String> logsData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Load today's count
        countField.setText(String.valueOf(JapService.getTodayCount()));

        // Setup ListView data
        japHistoryList.setItems(logsData);
        refreshHistory();
    }

    @FXML
    private void handleUpdate() {
        try {
            int val = Integer.parseInt(countField.getText());
            if (val >= 0 && val <= 100000) {
                JapService.updateJapCount(val);
                MainController.refreshStats(); // Update global XP if needed
                refreshHistory();              // Update the list on the right
            } else {
                new Alert(Alert.AlertType.ERROR, "Enter a number between 0 and 100,000").show();
            }
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Invalid input. Please enter a number.").show();
        }
    }

    private void refreshHistory() {
        logsData.clear();
        logsData.addAll(JapService.getAllJapLogs());
    }
}