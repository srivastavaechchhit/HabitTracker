package com.echchhit.habittracker.service;

import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.time.LocalTime;
import java.util.Timer;
import java.util.TimerTask;

public class ReminderService {

    private static Timer timer;

    public static void startDailyReminder(LocalTime time) {
        if (timer != null) timer.cancel();
        timer = new Timer(true);

        long delay = java.time.Duration.between(
                LocalTime.now(), time).toMillis();
        if (delay < 0) delay += 24 * 60 * 60 * 1000;

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Habit Reminder");
                    alert.setHeaderText("Time to check your habits");
                    alert.setContentText("Don’t break your streak today.");
                    alert.show();
                });
            }
        }, delay, 24 * 60 * 60 * 1000);
    }
}
