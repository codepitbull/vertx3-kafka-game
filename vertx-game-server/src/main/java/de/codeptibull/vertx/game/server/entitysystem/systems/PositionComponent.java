package de.codeptibull.vertx.game.server.entitysystem.systems;

import de.codeptibull.vertx.game.server.entitysystem.api.Component;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.lang3.Validate.notNull;

/**
 * Created by jmader on 24.01.15.
 */
public class PositionComponent implements Component {
    private int entityId;
    private float x;
    private float y;
    private List<ActionsEnum> actions = new ArrayList<>();
    private List<ActionsEnum> immutableActions = Collections.unmodifiableList(actions);

    public float getX() {
        return x;
    }

    public PositionComponent setX(float x) {
        this.x = x;
        return this;
    }

    public float getY() {
        return y;
    }

    public PositionComponent setY(float y) {
        this.y = y;
        return this;
    }

    public List<ActionsEnum> getActions() {
        return immutableActions;
    }

    public PositionComponent addAction(ActionsEnum action) {
        this.actions.add(action);
        return this;
    }

    public PositionComponent clearActions() {
        this.actions.clear();
        return this;
    }

    public void translate(float x, float y) {
        this.x += x;
        this.y += y;
    }

    public int getEntityId() {
        return entityId;
    }

    public PositionComponent setEntityId(int entityId) {
        notNull(entityId);
        this.entityId = entityId;
        return this;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }
}
