package de.codepitbull.vertx.game.client.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import de.codepitbull.vertx.game.client.VertxBeholder;
import de.codepitbull.vertx.game.client.VertxGame;
import scala.Int;

import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.math.NumberUtils.isNumber;

public class DesktopLauncher {
	private static final String usage = "\nexpected params:\n " +
			"player (String:hostname) (int: gameId) (String:playerName)\n" +
			"beholder (String:hostname) (int:gameId) (int:tickOffset)\n";

	public static void main (String[] arg) {

		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		isTrue(arg.length == 4, usage);
		if("player".equals(arg[0])) {
			isTrue(isNumber(arg[2]), usage);
			new LwjglApplication(new VertxGame(arg[1], Integer.valueOf(arg[2]), arg[3]), config);
		}

		else if("beholder".equals(arg[0])) {
			isTrue(isNumber(arg[2]), usage);
			isTrue(isNumber(arg[3]), usage);
			new LwjglApplication(new VertxBeholder(arg[1], Integer.parseInt(arg[2]), Integer.parseInt(arg[3])), config);
		}
	}
}
