package com.echchhit.habittracker.service;

import javafx.scene.Scene;

public class ThemeService {

    private static boolean darkMode = true;

    public static void toggle(Scene scene) {
        if (darkMode) {
            applyLight(scene);
        } else {
            applyDark(scene);
        }
        darkMode = !darkMode;
    }

    public static void applyDark(Scene scene) {
        scene.getStylesheets().removeIf(s -> s.contains("/theme/"));
        scene.getStylesheets().add(
                ThemeService.class
                        .getResource("/theme/dark.css")
                        .toExternalForm()
        );
    }

    public static void applyLight(Scene scene) {
        scene.getStylesheets().removeIf(s -> s.contains("/theme/"));
        scene.getStylesheets().add(
                ThemeService.class
                        .getResource("/theme/light.css")
                        .toExternalForm()
        );
    }
}
