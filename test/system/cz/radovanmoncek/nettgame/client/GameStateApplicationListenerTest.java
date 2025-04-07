package cz.radovanmoncek.nettgame.client;

import com.badlogic.gdx.Gdx;
import org.junit.jupiter.api.*;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.concurrent.Executors;

//todo: use position attributes in ALL tests !!!! !!!!
/**
 * An integration test written, in part, to test containerized vs .jar performance of the Nettgame server.
 *
 * @author Radovan Monček
 * @since 1.0
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GameStateApplicationListenerTest {

    private static Robot robot;

    @BeforeAll
    static void setup() throws AWTException {

        Executors
                .defaultThreadFactory()
                .newThread(() -> NettgameClientLauncher.main(new String[]{"--winmode", "true"}))
                .start();

        robot = new Robot();
    }

    @Test
    void testStartGame() {

        robot.delay(8000);
        robot.mouseMove(Toolkit.getDefaultToolkit().getScreenSize().width / 2, Toolkit.getDefaultToolkit().getScreenSize().height / 2 + 5);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.delay(100);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        robot.delay(3000);
        robot.mouseMove(Toolkit.getDefaultToolkit().getScreenSize().width / 2, Toolkit.getDefaultToolkit().getScreenSize().height / 2 - 30);
        robot.delay(5000);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.delay(100);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        robot.delay(3000);
        robot.setAutoDelay(200);
        robot.keyPress(KeyEvent.VK_T);
        robot.keyRelease(KeyEvent.VK_T);
        robot.keyPress(KeyEvent.VK_E);
        robot.keyRelease(KeyEvent.VK_E);
        robot.keyPress(KeyEvent.VK_S);
        robot.keyRelease(KeyEvent.VK_S);
        robot.keyPress(KeyEvent.VK_T);
        robot.keyRelease(KeyEvent.VK_T);
        robot.keyPress(KeyEvent.VK_ENTER);
        robot.keyRelease(KeyEvent.VK_ENTER);
        robot.delay(2000);
        robot.keyPress(KeyEvent.VK_W);
        robot.delay(1000);
        robot.keyRelease(KeyEvent.VK_W);
        robot.keyPress(KeyEvent.VK_A);
        robot.delay(1000);
        robot.keyRelease(KeyEvent.VK_A);
        robot.keyPress(KeyEvent.VK_S);
        robot.delay(1000);
        robot.keyRelease(KeyEvent.VK_S);
        robot.keyPress(KeyEvent.VK_D);
        robot.delay(1000);
        robot.keyRelease(KeyEvent.VK_D);
    }

    @AfterAll
    @Disabled
    static void tearDown() {

        Gdx
                .app
                .exit();
    }
}
