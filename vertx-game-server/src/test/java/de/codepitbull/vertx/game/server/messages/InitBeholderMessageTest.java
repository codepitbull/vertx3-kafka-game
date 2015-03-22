package de.codepitbull.vertx.game.server.messages;

import de.codeptibull.vertx.game.server.messages.FirstConnectMessage;
import de.codeptibull.vertx.game.server.messages.InitBeholderMessage;
import de.codeptibull.vertx.game.server.messages.MessageFactory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by jmader on 06.02.15.
 */
public class InitBeholderMessageTest {
    @Test
    public void testEncodeAndDecode() {
        InitBeholderMessage messageBuilder = new InitBeholderMessage().gameId(1).tick(10);
        InitBeholderMessage rebuilt = (InitBeholderMessage) MessageFactory.fromBuffer(messageBuilder.toBuffer());
        assertEquals(messageBuilder, rebuilt);
    }
}
