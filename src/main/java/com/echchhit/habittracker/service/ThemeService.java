package com.echchhit.habittracker.service;

import javafx.scene.Scene;

public class ThemeService {

    private static Scene scene;
    private static boolean darkMode = false;

    public static void setScene(Scene s) {
        scene = s;
    }

    public static void toggleTheme() {
        darkMode = !darkMode;
        applyTheme();
    }

    public static void applyTheme() {
        if (scene == null) {
            System.out.println("Scene is NULL — theme not applied");
            return;
        }

        scene.getStylesheets().clear();

        String css = darkMode
                ? ThemeService.class.getResource("/theme/dark.css").toExternalForm()
                : ThemeService.class.getResource("/theme/light.css").toExternalForm();

        scene.getStylesheets().add(css);
    }

    public static boolean isDarkMode() {
        return darkMode;
    }
}
