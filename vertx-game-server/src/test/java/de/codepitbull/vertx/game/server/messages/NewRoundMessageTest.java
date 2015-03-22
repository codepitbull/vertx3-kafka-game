package de.codepitbull.vertx.game.server.messages;

import de.codeptibull.vertx.game.server.entitysystem.systems.ActionsEnum;
import de.codeptibull.vertx.game.server.entitysystem.systems.PositionComponent;
import de.codeptibull.vertx.game.server.messages.MessageFactory;
import de.codeptibull.vertx.game.server.messages.NewRoundMessage;
import io.vertx.rxjava.core.buffer.Buffer;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Created by jmader on 06.02.15.
 */
public class NewRoundMessageTest {

    @Test
    public void testEncodeAndDecode() {
        NewRoundMessage message = new NewRoundMessage();
        message
                .tick(3)
                .gameId(1)
                .add(new PositionComponent()
                        .addAction(ActionsEnum.DOWN)
                        .setEntityId(1)
                        .setX(1.0f)
                        .setY(2.0f))
                .add(new PositionComponent()
                        .addAction(ActionsEnum.UP)
                        .setEntityId(2)
                        .setX(3.0f)
                        .setY(5.0f));

        Buffer build = message.toBuffer();

        NewRoundMessage rebuilt = (NewRoundMessage)MessageFactory.fromBuffer(build);
        assertEquals(message, rebuilt);
        assertThat(rebuilt.getActions().size(), is(2));
        assertThat(rebuilt.getActions().get(0).getActions().get(0), is(ActionsEnum.DOWN));

    }
}
