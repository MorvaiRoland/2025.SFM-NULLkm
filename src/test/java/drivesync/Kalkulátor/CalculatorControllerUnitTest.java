package drivesync.Kalkulátor;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.util.concurrent.CountDownLatch;

public class CalculatorControllerUnitTest {
    private CalculatorController controller;

    @BeforeAll
    public static  void initJavaFX(){
        new JFXPanel();
    }

    @BeforeEach
    public void setup() throws Exception{
        controller=new CalculatorController();

        CountDownLatch latch=new CountDownLatch(1);

        Platform.runLater(()->{

        });

    }
}
