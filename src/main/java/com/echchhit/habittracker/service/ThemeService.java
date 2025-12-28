package com.echchhit.habittracker.service;

import javafx.scene.Scene;

public class ThemeService {

    private static Scene scene;

    /**
     * Set the scene reference and immediately apply the light theme.
     */
    public static void setScene(Scene s) {
        scene = s;
        applyTheme();
    }

    /**
     * Hardcoded to apply only the light theme.
     */
    public static void applyTheme() {
        if (scene == null) {
            System.out.println("Scene is NULL — theme not applied");
            return;
        }

        scene.getStylesheets().clear();

        // Forcefully load only the light theme
        String css = ThemeService.class.getResource("/theme/light.css").toExternalForm();
        scene.getStylesheets().add(css);
    }

    /**
     * Concept of dark mode removed; always returns false.
     */
    public static boolean isDarkMode() {
        return false;
    }

    /**
     * Toggle logic removed to ensure application stays in light mode.
     */
    public static void toggleTheme() {
        // No operation
    }
}