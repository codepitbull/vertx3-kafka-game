package de.codeptibull.vertx.game.server.messages;

import de.codeptibull.vertx.game.server.entitysystem.systems.ActionsEnum;
import de.codeptibull.vertx.game.server.entitysystem.systems.PositionComponent;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.buffer.Buffer;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.*;

import static de.codeptibull.vertx.game.server.messages.ProtocolConstants.NEW_ROUND_MESSAGE;
import static de.codeptibull.vertx.game.server.messages.ProtocolConstants.POS_LENGTH_INT;
import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

/**
 * Message sent to each client for every new round containing the current positions and next actions of entities.
 */
public class NewRoundMessage implements AbstractMessage {
    private Integer tick;
    private Integer gameId;
    private List<PositionComponent> actions = new ArrayList<>();
    private List<PositionComponent> immutableActions = Collections.unmodifiableList(actions);

    public NewRoundMessage() {
    }

    public NewRoundMessage(Buffer fromBuffer) {
        int length = fromBuffer.getInt(POS_LENGTH_INT);
        tick = fromBuffer.getInt(5);
        gameId = fromBuffer.getInt(9);
        int players = (length - 8) / 16;
        for (int c = 0; c < players; c++) {
            actions.add(
                    addActions(
                            new PositionComponent()
                                    .setEntityId(fromBuffer.getInt(13 + 16 * c))
                                    .setX(fromBuffer.getFloat(17 + 16 * c))
                                    .setY(fromBuffer.getFloat(21 + 16 * c))
                            , fromBuffer.getInt(25 + 16 * c)));
        }
    }

    public NewRoundMessage(JsonObject fromJson) {
        tick = fromJson.getInteger("tick");
        gameId = fromJson.getInteger("gameId");
        notNull(tick);
        notNull(gameId);
        fromJson.getJsonArray("players")
            .forEach(untypedPlayer -> {
                JsonObject player = null;
                if (untypedPlayer instanceof JsonObject) player = (JsonObject) untypedPlayer;
                else player=new JsonObject((Map)untypedPlayer);
                PositionComponent playerBuilder = new PositionComponent()
                        .setX(player.getFloat("x"))
                        .setY(player.getFloat("y"))
                        .setEntityId(player.getInteger("entityId"));

                player.getJsonArray("actions").forEach(action -> {
                    playerBuilder.addAction(ActionsEnum.valueOf(action.toString()));
                });

                add(playerBuilder);
            });
    }

    private PositionComponent addActions(PositionComponent builder, int action) {
        for (ActionsEnum actionsEnum : ActionsEnum.values()) {
            if ((actionsEnum.getValue() & action) != 0) builder.addAction(actionsEnum);
        }
        return builder;
    }

    public int getTick() {
        return tick;
    }

    public int getGameId() {
        return gameId;
    }

    public List<PositionComponent> getActions() {
        return immutableActions;
    }

    public NewRoundMessage tick(int tick) {
        this.tick = tick;
        return this;
    }

    public NewRoundMessage gameId(int gameId) {
        this.gameId = gameId;
        return this;
    }

    public NewRoundMessage add(PositionComponent action) {
        actions.add(action);
        return this;
    }

    public NewRoundMessage addAll(Collection<PositionComponent> actions) {
        this.actions.addAll(actions);
        return this;
    }

    @Override
    public Buffer toBuffer() {
        notNull(tick);
        notNull(gameId);
        notEmpty(actions);
        Buffer ret = Buffer.buffer();
        ret.appendInt(4 + 4 + 16 * actions.size());
        ret.appendByte(NEW_ROUND_MESSAGE);
        ret.appendInt(tick);
        ret.appendInt(gameId);
        actions.forEach(action -> {
            ret.appendInt(action.getEntityId());
            ret.appendFloat(action.getX());
            ret.appendFloat(action.getY());
            ret.appendInt(action.getActions().stream().map(ActionsEnum::getValue).reduce(0, (a, b) -> a | b));
        });
        return ret;
    }

    @Override
    public JsonObject toJsonObject() {
        notNull(tick);
        notNull(gameId);
        notEmpty(actions);
        JsonObject jsonObject = new JsonObject();
        jsonObject
                .put("tick", tick)
                .put("gameId", gameId)
                .put("players", buildPlayerArrayFromPositions(actions));
        return jsonObject;
    }

    public static JsonArray buildPlayerArrayFromPositions(List<PositionComponent> positionComponents) {
        JsonArray ret = new JsonArray();
        positionComponents.forEach(positionComponent -> {
                    JsonArray actions = new JsonArray();
                    positionComponent.getActions().forEach(a -> actions.add(a.name()));
                    ret.add(new JsonObject()
                                    .put("entityId", positionComponent.getEntityId())
                                    .put("x", positionComponent.getX())
                                    .put("y", positionComponent.getY())
                                    .put("actions", actions)
                    );
                }
        );
        return ret;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
