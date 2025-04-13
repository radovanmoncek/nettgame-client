package cz.radovanmoncek.nettgame.client;

import com.badlogic.gdx.Gdx;
import org.junit.jupiter.api.*;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.net.UnknownHostException;
import java.util.Random;
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
    private static Random random = new Random();

    @BeforeAll
    static void setup() throws AWTException {

        Executors
                .defaultThreadFactory()
	    .newThread(() -> {
		    try {
			NettgameClientLauncher.main(new String[]{"--winmode", "true"});
		       }
		catch(UnknownHostException exception){
		    Gdx.app.error("setup", "Unknown Host Exception", exception);
		}
		})
                .start();

        robot = new Robot();
        random = new Random();
    }

    @Test
    void testStartGame() {

        robot.delay(60000);
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
    }

    @RepeatedTest(60)
    void testMovement() {

        final var movementPossibilities = new int[]{KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D};
        final var randomMove = movementPossibilities[random.nextInt(movementPossibilities.length)];

        robot.keyPress(randomMove);
        robot.delay(100);
        robot.keyRelease(randomMove);
    }

    @AfterAll
    @Disabled
    static void tearDown() {

        Gdx
                .app
                .exit();
    }
}
