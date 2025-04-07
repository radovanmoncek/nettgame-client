package cz.radovanmoncek.nettgame.client;

import com.badlogic.gdx.Gdx;
import org.junit.jupiter.api.*;

import java.awt.*;
import java.awt.event.InputEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

        NettgameClientLauncher.main(new String[]{"--winmode", "true"});

        robot = new Robot();
        robot.setAutoDelay(20);
    }

    @Test
    void testStartGame() {

        robot.mouseMove(Toolkit.getDefaultToolkit().getScreenSize().width / 2, Toolkit.getDefaultToolkit().getScreenSize().height / 2);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    @AfterAll
    @Disabled
    static void tearDown() {

        Gdx
                .app
                .exit();
    }
}
