package de.codepitbull.vertx.game.client;

import de.codeptibull.vertx.game.server.entitysystem.systems.ActionsEnum;
import de.codeptibull.vertx.game.server.messages.*;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetClientOptions;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.eventbus.Message;
import io.vertx.rxjava.core.net.NetSocket;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by jmader on 07.02.15.
 */
public class PlayerConnection {

    public int entityId = 0;
    public ConcurrentLinkedDeque<NewRoundMessage> queue = new ConcurrentLinkedDeque<>();

    public Vertx vertx;
    private NetSocket netSocket;
    public PlayerConnection(String hostname, int gameId, String playerName) {
        vertx = Vertx.vertx();
        vertx.createNetClient(new NetClientOptions()).connect(9000, hostname, result -> {
            if(result.failed()) {
                throw new RuntimeException("Unable to connect to "+hostname+":9000");
            }
            netSocket = result.result();
                netSocket.handler(buffer -> {
                    AbstractMessage message = MessageFactory.fromBuffer(buffer);
                    if (message instanceof NewRoundMessage) {
                        queue.add((NewRoundMessage) message);
                    } else if (message instanceof FirstConnectResponseMessage) {
                        entityId = ((FirstConnectResponseMessage) message).getEntityId();
                        vertx.eventBus().consumer("playerMessage", new Handler<Message<JsonObject>>() {
                            @Override
                            public void handle(Message<JsonObject> event) {
                                PlayerMessage playerMessage = new PlayerMessage(event.body());
                                netSocket.write(playerMessage.toBuffer());
                            }
                        });
                    }
                });
            netSocket.write(new FirstConnectMessage().gameId(gameId).name(playerName).toBuffer());
        });

    }
}
