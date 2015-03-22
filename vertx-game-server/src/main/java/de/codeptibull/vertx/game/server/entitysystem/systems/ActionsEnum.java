package de.codeptibull.vertx.game.server.entitysystem.systems;

/**
 * Created by jmader on 16.01.15.
 */
public enum ActionsEnum {
    LEFT(0b1), RIGHT(0b10), UP(0b100), DOWN(0b1000);

    private int value;
    private ActionsEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
