package de.codepitbull.vertx.game.server.messages;

import de.codeptibull.vertx.game.server.entitysystem.systems.ActionsEnum;
import de.codeptibull.vertx.game.server.messages.MessageFactory;
import de.codeptibull.vertx.game.server.messages.PlayerMessage;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by jmader on 06.02.15.
 */
public class PlayerMessageTest {
    @Test
    public void testEncodeAndDecode() {
        PlayerMessage messageBuilder = new PlayerMessage()
                .entityId(1)
                .gameId(1)
                .addAction(ActionsEnum.LEFT)
                .addAction(ActionsEnum.UP)
                .tick(3);
        PlayerMessage rebuilt = (PlayerMessage) MessageFactory.fromBuffer(messageBuilder.toBuffer());
        assertEquals(messageBuilder, rebuilt);
    }
}
