package com.echchhit.habittracker.controller;

import com.echchhit.habittracker.service.ThemeService;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;

public class SettingsController {

    @FXML
    private ToggleButton themeToggle;

    @FXML
    private void toggleTheme() {
        ThemeService.toggle(themeToggle.getScene());
    }
}
