package de.codepitbull.vertx.game.client;

import de.codeptibull.vertx.game.server.messages.*;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetClientOptions;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.eventbus.Message;
import io.vertx.rxjava.core.net.NetClient;
import io.vertx.rxjava.core.net.NetSocket;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by jmader on 07.02.15.
 */
public class BeholderConnection {
    public ConcurrentLinkedDeque<NewRoundMessage> queue = new ConcurrentLinkedDeque<>();
    public Vertx vertx;
    public BeholderConnection(String host, int gameId, int tick) {
        vertx = Vertx.vertx();

        vertx.createNetClient(new NetClientOptions()).connect(9001, host, result -> {
            if(result.failed()) {
                throw new RuntimeException("Unable to connect to "+host+":9001");
            }
            else {
                result.result().handler(msg -> {
                    AbstractMessage parsedMessage = MessageFactory.fromBuffer(msg);
                    if (parsedMessage instanceof NewRoundMessage) {
                        queue.add((NewRoundMessage) parsedMessage);
                    }
                });
                result.result().write(new InitBeholderMessage().tick(tick).gameId(gameId).toBuffer());
            }
        });
    }
}
