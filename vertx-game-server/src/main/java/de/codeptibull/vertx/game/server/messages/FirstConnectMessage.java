package de.codeptibull.vertx.game.server.messages;

import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.buffer.Buffer;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import static de.codeptibull.vertx.game.server.messages.ProtocolConstants.*;
import static org.apache.commons.lang3.Validate.notNull;

/**
 * Message sent by the client the first time it connects to the server to join a game.
 */
public class FirstConnectMessage implements AbstractMessage {
    private String name;
    private Integer gameId;

    public FirstConnectMessage() {
    }

    public String getName() {
        return name;
    }

    public Integer getGameId() {
        return gameId;
    }

    FirstConnectMessage(Buffer fromBuffer) {
        int length = fromBuffer.getInt(POS_LENGTH_INT);
        gameId = fromBuffer.getInt(5);
        name = fromBuffer.getString(9, 9 + length - 4);
    }

    public FirstConnectMessage name(String name) {
        this.name = name;
        return this;
    }

    public FirstConnectMessage gameId(Integer gameId) {
        this.gameId = gameId;
        return this;
    }

    @Override
    public Buffer toBuffer() {
        notNull(name);
        notNull(gameId);
        Buffer ret = Buffer.buffer();
        ret.appendInt(4 + name.length());
        ret.appendByte(FIRST_CONNECT_MESSAGE);
        ret.appendInt(gameId);
        ret.appendString(name);
        return ret;
    }

    @Override
    public JsonObject toJsonObject() {
        notNull(name);
        notNull(gameId);
        JsonObject ret = new JsonObject();
        ret.put("name", name);
        ret.put("gameId", gameId);
        return ret;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
