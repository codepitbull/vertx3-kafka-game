package de.codeptibull.vertx.game.server.messages;

import io.vertx.rxjava.core.buffer.Buffer;

import static de.codeptibull.vertx.game.server.messages.ProtocolConstants.*;
import static org.apache.commons.lang3.Validate.notNull;

/**
 * Factory to create messages from Buffers.
 */
public class MessageFactory {
    public static AbstractMessage fromBuffer(Buffer buffer) {
        notNull(buffer);
        int flags = buffer.getByte(POS_FLAG_BYTE);
        switch (flags) {
            case NEW_ROUND_MESSAGE: return new NewRoundMessage(buffer);
            case FIRST_CONNECT_MESSAGE: return new FirstConnectMessage(buffer);
            case FIRST_CONNECT_RESPONSE_MESSAGE: return new FirstConnectResponseMessage(buffer);
            case PLAYER_MESSAGE: return new PlayerMessage(buffer);
            case BEHOLDER_INIT_MESSAGE: return new InitBeholderMessage(buffer);
            default: throw new RuntimeException("Unparseable message");
        }

    }
}
