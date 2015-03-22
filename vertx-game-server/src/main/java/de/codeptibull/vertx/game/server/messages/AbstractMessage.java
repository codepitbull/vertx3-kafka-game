package de.codeptibull.vertx.game.server.messages;

import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.buffer.Buffer;

/**
 * Base class for network messages.
 */
public interface AbstractMessage {
    /**
     * Convert the message to a Buffer suitable for network transmission.
     * @return a Buffer containing the message in binary form
     */
    Buffer toBuffer();

    /**
     * Convert the message to a JsonObject suitable for transmission over the Vert.x event bus.
     * @return the message encoded as JSON
     */
    JsonObject toJsonObject();
}
