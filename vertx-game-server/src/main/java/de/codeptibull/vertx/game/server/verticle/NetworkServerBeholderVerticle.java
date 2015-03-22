package de.codeptibull.vertx.game.server.verticle;

import de.codeptibull.vertx.game.server.messages.AbstractMessage;
import de.codeptibull.vertx.game.server.messages.InitBeholderMessage;
import de.codeptibull.vertx.game.server.messages.MessageFactory;
import de.codeptibull.vertx.game.server.messages.NewRoundMessage;
import de.codeptibull.vertx.kafka.simple.KafkaSimpleConsumerVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.core.net.NetServerOptions;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.net.NetServer;
import io.vertx.rxjava.core.net.NetSocket;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.HashMap;
import java.util.Map;

import static de.codeptibull.vertx.kafka.simple.KafkaSimpleConsumerVerticle.*;
import static org.apache.commons.lang3.Validate.notNull;

/**
 * This verticle listens to a specified port for beholders.
 * They can decide which game they want to watch and where to start watching.
 */
public class NetworkServerBeholderVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkServerBeholderVerticle.class);

    private long beholderCounter = 0;

    public static final String CONFIG_NET_PORT_BEHOLDER = "netPortBeholder";
    public static final String CONFIG_KAFKA_PORT = "kafkaPort";
    public static final String CONFIG_KAFKA_BROKERS = "kafkaBrokers";

    private Map<NetSocket, String> socketToDeploymentIdMap = new HashMap<>();

    private StringDeserializer stringDeserializer = new StringDeserializer();
    @Override
    public void start(Future<Void> startFuture) throws Exception {
        notNull(config().getString(CONFIG_KAFKA_BROKERS), CONFIG_KAFKA_BROKERS+" not specified");
        notNull(config().getInteger(CONFIG_KAFKA_PORT), CONFIG_KAFKA_PORT+" not specified");
        LOGGER.info("Creating " + NetworkServerBeholderVerticle.class.getSimpleName() + " for config " + config().toString());

        NetServer netServer = vertx.createNetServer(new NetServerOptions().setPort(config().getInteger(CONFIG_NET_PORT_BEHOLDER, 9001)));

        registerProtocolHandlers(netServer);

        netServer.listen(listening -> {
            if (listening.succeeded()) {
                startFuture.complete();
            } else startFuture.fail(listening.cause());
        });
    }


    private void registerProtocolHandlers(NetServer netServer) {

        netServer.connectHandler(connected -> {
            socketToDeploymentIdMap.put(connected, null);
            connected.closeHandler(closeevent -> {
                        String deplId = socketToDeploymentIdMap.remove(connected);
                        if (deplId == null) LOGGER.info("Closed unassociated socket");
                        else vertx.undeploy(deplId);
                    }
            );
            connected.exceptionHandler(exception -> {
                LOGGER.warn("Unexepcted exception", exception);
            });

            connected.handler(buffer -> {
                //TODO: length-checks (e.g.: sending simple strings will give problems)
                AbstractMessage parsedMessage = MessageFactory.fromBuffer(buffer);
                if (parsedMessage instanceof InitBeholderMessage) {
                    InitBeholderMessage message = (InitBeholderMessage) parsedMessage;
                    long beholderCounterCurrent = beholderCounter++;
                    vertx.deployVerticle(KafkaSimpleConsumerVerticle.class.getName(),
                            new DeploymentOptions().setConfig(new JsonObject()
                                            .put(PARTITION, 0)
                                            .put(PORT, config().getInteger(CONFIG_KAFKA_PORT))
                                            .put(TOPIC, "game-" + message.getGameId())
                                            .put(BROKERS, config().getString(CONFIG_KAFKA_BROKERS))
                                            .put(LISTEN_ADDRESS, "request-more-" + beholderCounterCurrent)
                                            .put(TARGET_ADDRESS, "process-" + beholderCounterCurrent)
                                            .put(OFFSET, message.getTick())
                            ), res -> {
                                if (socketToDeploymentIdMap.containsKey(connected)) {
                                    socketToDeploymentIdMap.put(connected, res.result());
                                    vertx.eventBus().<byte[]>consumer("process-" + beholderCounterCurrent, handle -> {
                                        JsonObject body = new JsonObject(stringDeserializer.deserialize("", handle.body()));
                                        NewRoundMessage newRoundMessage = new NewRoundMessage(body);
                                        connected.write(newRoundMessage.toBuffer());
                                    }).completionHandler(done -> {
                                        vertx.eventBus().send("request-more-" + beholderCounterCurrent, Integer.MAX_VALUE);
                                    });
                                } else vertx.undeploy(res.result());
                            });
                }
            });
        });

    }
}
