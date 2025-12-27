package com.echchhit.habittracker.controller;

import com.echchhit.habittracker.components.ToggleSwitch; // Import the new class
import com.echchhit.habittracker.service.ThemeService;
import javafx.fxml.FXML;

public class SettingsController {

    @FXML
    private ToggleSwitch themeSwitch;

    @FXML
    public void initialize() {
        // 1. Sync state with current theme
        themeSwitch.switchedOnProperty().set(ThemeService.isDarkMode());

        // 2. Listen for changes (Animation is handled inside the component!)
        themeSwitch.switchedOnProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != ThemeService.isDarkMode()) {
                ThemeService.toggleTheme();
            }
        });
    }
}