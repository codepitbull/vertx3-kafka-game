package de.codeptibull.vertx.game.server.messages;

import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.buffer.Buffer;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import static de.codeptibull.vertx.game.server.messages.ProtocolConstants.BEHOLDER_INIT_MESSAGE;
import static org.apache.commons.lang3.Validate.notNull;

/**
 * First message sent by a beholder client to indicate what vgame should be replayed and where to start.
 */
public class InitBeholderMessage implements AbstractMessage {
    private Integer tick;
    private Integer gameId;

    public InitBeholderMessage() {
    }

    InitBeholderMessage(Buffer fromBuffer) {
        tick = fromBuffer.getInt(5);
        gameId = fromBuffer.getInt(9);
    }

    public InitBeholderMessage(JsonObject jsonObject) {
        tick = jsonObject.getInteger("tick");
        gameId = jsonObject.getInteger("gameid");
    }

    public Integer getGameId() {
        return gameId;
    }

    public InitBeholderMessage gameId(Integer gameId) {
        this.gameId = gameId;
        return this;
    }

    public InitBeholderMessage tick(Integer tick) {
        this.tick = tick;
        return this;
    }

    public int getTick() {
        return tick;
    }

    @Override
    public Buffer toBuffer() {
        notNull(tick);
        notNull(gameId);
        Buffer ret = Buffer.buffer();
        ret.appendInt(8);
        ret.appendByte(BEHOLDER_INIT_MESSAGE);
        ret.appendInt(tick);
        ret.appendInt(gameId);
        return ret;
    }

    @Override
    public JsonObject toJsonObject() {
        notNull(tick);
        notNull(gameId);
        return new JsonObject()
                .put("tick", tick)
                .put("gameid", gameId);
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
