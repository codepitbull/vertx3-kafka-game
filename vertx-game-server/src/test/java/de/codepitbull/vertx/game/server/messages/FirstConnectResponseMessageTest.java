package de.codepitbull.vertx.game.server.messages;

import de.codeptibull.vertx.game.server.messages.FirstConnectResponseMessage;
import de.codeptibull.vertx.game.server.messages.MessageFactory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by jmader on 06.02.15.
 */
public class FirstConnectResponseMessageTest {
    @Test
    public void testEncodeAndDecode() {
        FirstConnectResponseMessage messageBuilder = new FirstConnectResponseMessage().entityId(1);
        FirstConnectResponseMessage rebuilt = (FirstConnectResponseMessage)MessageFactory.fromBuffer(messageBuilder.toBuffer());
        assertEquals(messageBuilder, rebuilt);
    }
}
