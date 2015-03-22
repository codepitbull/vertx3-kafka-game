package de.codepitbull.vertx.game.server.messages;

import de.codeptibull.vertx.game.server.messages.FirstConnectMessage;
import de.codeptibull.vertx.game.server.messages.MessageFactory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by jmader on 06.02.15.
 */
public class FirstConnectMessageTest {
    @Test
    public void testEncodeAndDecode() {
        FirstConnectMessage messageBuilder = new FirstConnectMessage().name("JOCHEN").gameId(1);
        FirstConnectMessage rebuilt = (FirstConnectMessage) MessageFactory.fromBuffer(messageBuilder.toBuffer());
        assertEquals(messageBuilder, rebuilt);
    }
}
