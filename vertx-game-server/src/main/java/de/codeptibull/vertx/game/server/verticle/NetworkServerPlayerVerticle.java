package de.codeptibull.vertx.game.server.verticle;

import de.codeptibull.vertx.game.server.messages.*;
import de.codeptibull.vertx.kafka.highlevel.KafkaHighLevelConsumerVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.core.net.NetServerOptions;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.buffer.Buffer;
import io.vertx.rxjava.core.net.NetServer;
import io.vertx.rxjava.core.net.NetSocket;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.*;

import static de.codeptibull.vertx.game.server.verticle.RestApiVerticle.CONFIG_GAME_ID;
import static de.codeptibull.vertx.game.server.verticle.RestApiVerticle.CONFIG_PLAYER_NAMES;
import static de.codeptibull.vertx.kafka.highlevel.KafkaHighLevelConsumerVerticle.*;

/**
 * Created by jmader on 08.01.15.
 */
public class NetworkServerPlayerVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkServerPlayerVerticle.class);

    public static final String CONFIG_NET_PORT = "netPort";
    public static final String CONFIG_ZOOKEEPER_CONNECT_STRING = "zookeeperConnect";

    private Map<Integer, Map<String, NetSocket>> gameIdToPlayerNameToSocketMap = new HashMap<>();
    private Set<NetSocket> socketsNotYetAssociatedWithPlayerSet = new HashSet<>();
    private Map<Integer, List<String>> gameIdToPlayerNamesMap = new HashMap<>();

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        LOGGER.info("Creating " + NetworkServerPlayerVerticle.class.getSimpleName() + " for config " + config().toString());
        NetServer netServer = vertx.createNetServer(new NetServerOptions().setPort(config().getInteger(CONFIG_NET_PORT, 9000)));

        registerProtocolHandlers(netServer);

        netServer.listen(listening -> {
            if (listening.succeeded()) {
                vertx.eventBus().<JsonObject>consumer("newGame").handler(event -> {
                    Integer gameId = event.body().getInteger(CONFIG_GAME_ID);
                    LOGGER.info("Registering new game: gamid-" + gameId);
                    Map<String, NetSocket> playerToSocketMap = new HashMap<>();
                    List<String> playerNamesList = new ArrayList<String>();
                    event.body().getJsonArray(CONFIG_PLAYER_NAMES).getList().forEach(playerName -> {
                        playerToSocketMap.put(playerName.toString(), null);
                        playerNamesList.add(playerName.toString());
                    });

                    if (config().containsKey(CONFIG_ZOOKEEPER_CONNECT_STRING)) {
                        LOGGER.info("Deploying " + KafkaHighLevelConsumerVerticle.class.getName() + " for topic gamid-" + gameId);
                        vertx.deployVerticle(KafkaHighLevelConsumerVerticle.class.getName(),
                                new DeploymentOptions().setConfig(new JsonObject()
                                                .put(ZOOKEEPER_CONNECT, config().getString(CONFIG_ZOOKEEPER_CONNECT_STRING))
                                                .put(TOPIC, "game-" + gameId)
                                                .put(ADDRESS, "newRoundBytes")
                                                .put(GROUP_ID, "gameConsumer" + gameId)
                                ));
                    }

                    gameIdToPlayerNameToSocketMap.put(gameId, playerToSocketMap);
                    gameIdToPlayerNamesMap.put(gameId, playerNamesList);
                    event.reply("OK");

                }).completionHandler(completed -> {
                    if (completed.succeeded()) startFuture.complete();
                    else startFuture.fail(completed.cause());
                });
            } else startFuture.fail(listening.cause());
        });
    }

    private void registerProtocolHandlers(NetServer netServer) {

        netServer.connectHandler(connected -> {
            socketsNotYetAssociatedWithPlayerSet.add(connected);
            connected.closeHandler(closeevent -> {
                        if (socketsNotYetAssociatedWithPlayerSet.remove(connected)) {
                            LOGGER.info("Closed unassociated socket");
                        }
                        gameIdToPlayerNameToSocketMap.values().forEach(playerNameToSocketMap -> {
                            playerNameToSocketMap.entrySet().removeIf(entry -> connected.equals(entry.getValue()));
                        });
                    }
            );
            connected.exceptionHandler(exception -> {
                LOGGER.warn("Unexepcted exception", exception);
            });

            connected.handler(buffer -> {
                //TODO: length-checks (e.g.: sending simple strings will give problems)
                AbstractMessage parsedMessage = MessageFactory.fromBuffer(buffer);
                if (parsedMessage instanceof FirstConnectMessage) {
                    FirstConnectMessage message = (FirstConnectMessage) parsedMessage;
                    if (socketsNotYetAssociatedWithPlayerSet.remove(connected)) {
                        LOGGER.info("Init message for player " + message.getName());
                        Map<String, NetSocket> playerToSocketMap = gameIdToPlayerNameToSocketMap.get(message.getGameId());
                        playerToSocketMap.put(message.getName(), connected);
                        FirstConnectResponseMessage msg = new FirstConnectResponseMessage().entityId(
                                gameIdToPlayerNamesMap.get(message.getGameId()).indexOf(message.getName()));
                        LOGGER.info("SENDING BACK "+msg.toString());
                        connected.write(msg.toBuffer());
                    } else {
                        LOGGER.warn("Init message for already connected player " + message.getName());
                    }
                } else if (parsedMessage instanceof PlayerMessage) {
                    PlayerMessage message = (PlayerMessage) parsedMessage;



                    LOGGER.info("Received PlayerMessage: " + message.toString());
                    vertx.eventBus().send("command-"+message.getGameId(), message.toJsonObject());
                }

            });
        });

        vertx.eventBus().<JsonObject>consumer("newRound", newRound -> {
            NewRoundMessage newRoundMessage = new NewRoundMessage(newRound.body());
            LOGGER.debug("Sending NewRoundMessage: " + newRound.body());
            Buffer message = new NewRoundMessage(newRound.body()).toBuffer();
            gameIdToPlayerNameToSocketMap.get(newRoundMessage.getGameId()).entrySet().forEach(socket -> {
                if(socket.getValue() != null)
                    socket.getValue().write(message);
            });
        });

        StringDeserializer stringDeserializer = new StringDeserializer();
        vertx.eventBus().<byte[]>consumer("newRoundBytes", newRound -> {
            JsonObject body = new JsonObject(stringDeserializer.deserialize("", newRound.body()));
            NewRoundMessage newRoundMessage = new NewRoundMessage(body);
            LOGGER.debug("Sending NewRoundMessage (from Kafka): " + newRound.body());
            Buffer message = new NewRoundMessage(body).toBuffer();
            gameIdToPlayerNameToSocketMap.get(newRoundMessage.getGameId()).entrySet().forEach(socket -> {
                if (socket.getValue() != null)
                    socket.getValue().write(message);
            });
        });

    }
}
