package drivesync;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.control.ScrollPane;
import javafx.util.Duration;

public class TimelineScroll {
    public static void animateScroll(ScrollPane scrollPane, double target, int durationMs) {
        Timeline timeline = new Timeline();
        KeyValue kv = new KeyValue(scrollPane.hvalueProperty(), target);
        KeyFrame kf = new KeyFrame(Duration.millis(durationMs), kv);
        timeline.getKeyFrames().add(kf);
        timeline.play();
    }
}
