package de.codeptibull.vertx.game.server.messages;

import de.codeptibull.vertx.game.server.entitysystem.systems.ActionsEnum;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.buffer.Buffer;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.Validate.notNull;

import static de.codeptibull.vertx.game.server.messages.ProtocolConstants.*;

/**
 * Message sent from client to server to provide the actions selected for a certain round.
 */
public class PlayerMessage implements AbstractMessage{
    private Integer tick;
    private Integer entityId;
    private Integer gameId;
    private List<ActionsEnum> actions = new ArrayList<>();
    private List<ActionsEnum> immutableActions = Collections.unmodifiableList(actions);

    public PlayerMessage() {
    }

    PlayerMessage(Buffer fromBuffer) {
        tick = fromBuffer.getInt(5);
        entityId = fromBuffer.getInt(9);
        gameId = fromBuffer.getInt(13);
        Integer action = fromBuffer.getInt(17);
        for(ActionsEnum actionsEnum:ActionsEnum.values()) {
            if((actionsEnum.getValue() & action) != 0) actions.add(actionsEnum);
        }
    }

    public PlayerMessage(JsonObject jsonObject) {
        tick = jsonObject.getInteger("tick");
        entityId = jsonObject.getInteger("entityid");
        gameId = jsonObject.getInteger("gameid");
        for(Object action:jsonObject.getJsonArray("actions")) {
            actions.add(ActionsEnum.valueOf(action.toString()));
        }
    }

    public Integer getGameId() {
        return gameId;
    }

    public PlayerMessage gameId(Integer gameId) {
        this.gameId = gameId;
        return this;
    }

    public int getEntityId() {
        return entityId;
    }

    public PlayerMessage entityId(int entityId) {
        this.entityId = entityId;
        return this;
    }

    public PlayerMessage tick(Integer tick) {
        this.tick = tick;
        return this;
    }

    public PlayerMessage addAction(ActionsEnum action) {
        actions.add(action);
        return this;
    }

    public PlayerMessage clearActions() {
        actions.clear();
        return this;
    }

    public int getTick() {
        return tick;
    }

    public List<ActionsEnum> getActions() {
        return immutableActions;
    }

    @Override
    public Buffer toBuffer() {
        notNull(tick);
        notNull(entityId);
        notNull(gameId);
        Buffer ret = Buffer.buffer();
        ret.appendInt(8);
        ret.appendByte(PLAYER_MESSAGE);
        ret.appendInt(tick);
        ret.appendInt(entityId);
        ret.appendInt(gameId);
        ret.appendInt(actions.stream().map(ActionsEnum::getValue).reduce(0, (a, b) -> a | b));
        return ret;
    }

    @Override
    public JsonObject toJsonObject() {
        notNull(tick);
        notNull(entityId);
        notNull(gameId);
        return new JsonObject()
                .put("tick", tick)
                .put("entityid", entityId)
                .put("gameid", gameId)
                .put("actions", new JsonArray(actions.stream().map(ActionsEnum::toString).collect(Collectors.toList())));
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
