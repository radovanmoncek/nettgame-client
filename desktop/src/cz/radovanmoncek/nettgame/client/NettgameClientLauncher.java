package cz.radovanmoncek.nettgame.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import java.net.UnknownHostException;

public class NettgameClientLauncher {

    public static void main(String[] args) throws UnknownHostException {

        if (args.length == 0 || !args[0].equals("--winmode")) {

            System.err.println("Window mode must be specified with the --winmode option");

            System.exit(1);

            return;
        }

        if (!args[1].equals("true") && !args[1].equals("false")) {

            System.err.println("Invalid argument");

            System.exit(1);

            return;
        }

        if (args.length > 2 && !args[2].equals("--server-address")) {

            System.err.println("Server address must be specified using the --server-address option");

            System.exit(1);

            return;
        }

        if (args.length < 4 || args[3] == null) {

            System.err.println("Please specify a valid IP address or hostname");

	    System.exit(1);
	    
            return;
        }
	
        final var config = new Lwjgl3ApplicationConfiguration();

        config.setForegroundFPS(60);
        config.setTitle("example nettgame client");
        config.setWindowedMode(800, 600);

        if (!Boolean.parseBoolean(args[1]))
            config.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode());

        new Lwjgl3Application(GameStateApplicationListener.returnNewInstance(args[3]), config);
    }
}
