package cz.radovanmoncek.nettgame.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public class NettgameClientLauncher {

    public static void main(String[] args) {

        if (args.length == 0 || !args[0].equals("--winmode")) {

            System.err.println("Window mode must be specified with the --winmode option");

            Gdx
                    .app
                    .exit();

            return;
        }

        if (!args[1].equals("true") && !args[1].equals("false")) {

            System.err.println("Invalid argument");

            Gdx
                    .app
                    .exit();

            return;
        }

        final var config = new Lwjgl3ApplicationConfiguration();

        config.setForegroundFPS(60);
        config.setTitle("example nettgame client");
        config.setWindowedMode(800, 600);

        if (!Boolean.parseBoolean(args[1]))
            config.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode());

        new Lwjgl3Application(GameStateApplicationListener.returnNewInstance(), config);
    }
}
