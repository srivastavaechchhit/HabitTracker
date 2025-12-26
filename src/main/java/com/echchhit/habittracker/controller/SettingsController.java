package com.echchhit.habittracker.controller;

import com.echchhit.habittracker.service.ThemeService;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;

public class SettingsController {

    @FXML
    private ToggleButton themeToggle;

    @FXML
    public void initialize() {
        // Sync toggle with current theme
        themeToggle.setSelected(ThemeService.isDarkMode());

        // Toggle theme on user action
        themeToggle.setOnAction(e -> ThemeService.toggleTheme());
    }
}
