package com.echchhit.habittracker.components;

import javafx.animation.FillTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class ToggleSwitch extends StackPane {

    private final BooleanProperty switchedOn = new SimpleBooleanProperty(false);
    private final TranslateTransition translateAnimation = new TranslateTransition(Duration.seconds(0.25));
    private final FillTransition fillAnimation = new FillTransition(Duration.seconds(0.25));
    private final ParallelTransition animation = new ParallelTransition(translateAnimation, fillAnimation);

    public BooleanProperty switchedOnProperty() {
        return switchedOn;
    }

    public ToggleSwitch() {
        // 1. The Background (Pill shape)
        Rectangle background = new Rectangle(50, 26);
        background.setArcWidth(26);
        background.setArcHeight(26);
        background.setFill(Color.WHITE);
        background.setStroke(Color.LIGHTGRAY);

        // 2. The Circle (Thumb)
        Circle trigger = new Circle(12);
        trigger.setCenterX(12);
        trigger.setCenterY(12);
        trigger.setFill(Color.WHITE);
        trigger.setStroke(Color.LIGHTGRAY);

        // Drop shadow effect for depth
        trigger.setEffect(new javafx.scene.effect.DropShadow(3, Color.color(0,0,0,0.2)));

        // 3. Setup Animations
        translateAnimation.setNode(trigger);
        fillAnimation.setShape(background);

        getChildren().addAll(background, trigger);

        // 4. Click Listener
        setOnMouseClicked(event -> {
            switchedOn.set(!switchedOn.get());
        });

        // 5. Logic to move the circle when state changes
        switchedOn.addListener((obs, oldState, newState) -> {
            boolean isOn = newState;

            // Move circle: Right (25) if ON, Left (-25) if OFF (relative to center)
            // Adjusting generic translation values for this size
            translateAnimation.setToX(isOn ? 12 : -12);

            // Change color: Green/Blue if ON, White/Grey if OFF
            fillAnimation.setFromValue((Color) background.getFill());
            fillAnimation.setToValue(isOn ? Color.web("#4CAF50") : Color.WHITE);

            animation.play();
        });
    }
}