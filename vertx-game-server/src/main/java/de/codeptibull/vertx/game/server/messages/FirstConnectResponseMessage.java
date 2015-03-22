package de.codeptibull.vertx.game.server.messages;

import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.buffer.Buffer;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notNull;

import static de.codeptibull.vertx.game.server.messages.ProtocolConstants.*;

/**
 * Reply to the {@link FirstConnectMessage} containing the entity id for the connected player.
 */
public class FirstConnectResponseMessage implements AbstractMessage {
    private int entityId;

    public FirstConnectResponseMessage() {
    }

    public int getEntityId() {
        return entityId;
    }

    FirstConnectResponseMessage(Buffer fromBuffer) {
        notNull(fromBuffer);
        isTrue(FIRST_CONNECT_RESPONSE_MESSAGE == fromBuffer.getByte(POS_FLAG_BYTE));
        entityId = fromBuffer.getInt(5);
    }

    public FirstConnectResponseMessage entityId(int entityId) {
        this.entityId = entityId;
        return this;
    }

    @Override
    public Buffer toBuffer() {
        notNull(entityId);
        Buffer ret = Buffer.buffer();
        ret.appendInt(4);
        ret.appendByte(FIRST_CONNECT_RESPONSE_MESSAGE);
        ret.appendInt(entityId);
        return ret;
    }

    @Override
    public JsonObject toJsonObject() {
        notNull(entityId);
        JsonObject ret = new JsonObject();
        ret.put("entityid", entityId);
        return ret;
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }


}
