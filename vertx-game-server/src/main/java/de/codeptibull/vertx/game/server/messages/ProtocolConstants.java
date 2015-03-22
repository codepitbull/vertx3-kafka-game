package de.codeptibull.vertx.game.server.messages;

/**
 * Protocol Constants for the game protocl.
 */
public final class ProtocolConstants {

    public static final byte FIRST_CONNECT_MESSAGE = 1;
    public static final byte FIRST_CONNECT_RESPONSE_MESSAGE = 2;

    public static final byte PLAYER_MESSAGE = 3;
    public static final byte NEW_ROUND_MESSAGE = 4;

    public static final byte BEHOLDER_INIT_MESSAGE = 5;

    public static final int POS_LENGTH_INT = 0;
    public static final int POS_FLAG_BYTE = 4;

    private ProtocolConstants() {
    }
}
