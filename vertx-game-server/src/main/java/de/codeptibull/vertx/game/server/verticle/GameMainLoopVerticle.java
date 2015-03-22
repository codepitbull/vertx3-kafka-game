package de.codeptibull.vertx.game.server.verticle;

import de.codeptibull.vertx.game.server.entitysystem.systems.PositionComponent;
import de.codeptibull.vertx.game.server.entitysystem.systems.PositonSystem;
import de.codeptibull.vertx.game.server.messages.NewRoundMessage;
import de.codeptibull.vertx.game.server.messages.PlayerMessage;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.eventbus.Message;
import io.vertx.rxjava.core.eventbus.MessageConsumer;
import org.apache.commons.lang3.Validate;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notEmpty;

/**
 * Each game has its own GameMainLoopVerticle. It contains the (possibly heavy weight) game logic.
 *
 * TODO: It also contains the logic for persisting to Kafka, should be moved to a separate verticle.
 */
public class GameMainLoopVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameMainLoopVerticle.class);
    public static final String CONFIG_BOOTSTRAP_SERVERS = "bootstrap.servers";

    private int tick = 0;
    private int gameId = 0;
    private String bootstrapServes = null;
    private KafkaProducer<String, String> producer;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        Validate.isTrue(config().containsKey(RestApiVerticle.CONFIG_PLAYER_NAMES), "The property " + RestApiVerticle.CONFIG_PLAYER_NAMES + " is not set");
        Validate.isTrue(config().containsKey(RestApiVerticle.CONFIG_GAME_ID), "The property " + RestApiVerticle.CONFIG_GAME_ID + " is not set");
        LOGGER.info("Creating " + GameMainLoopVerticle.class.getSimpleName()+" for config "+config().toString());

        bootstrapServes = config().getString(CONFIG_BOOTSTRAP_SERVERS);
        gameId = config().getInteger(RestApiVerticle.CONFIG_GAME_ID);

        PositonSystem positonSystem = new PositonSystem(config().getFloat(RestApiVerticle.CONFIG_PLAYER_SPEED, 10f));

        MessageConsumer<JsonObject> commandConsumer = vertx.eventBus().<JsonObject>consumer("command-" + gameId);
        commandConsumer.completionHandler(completed -> {
            if (completed.failed()) startFuture.fail(completed.cause());
            else startFuture.complete();
        });

        if (bootstrapServes != null)
            producer = createProducer();
        observableForProcessingGameRounds(config().getInteger(RestApiVerticle.CONFIG_ROUND_LENGHT, 20),
                positonSystem,
                createInitialPositionComponentsForPlayers(config().getJsonArray(RestApiVerticle.CONFIG_PLAYER_NAMES).getList()),
                commandConsumer.toObservable());
    }

    private List<PositionComponent> createInitialPositionComponentsForPlayers(List playerNames) {
        final List<PositionComponent> positionComponentList = new ArrayList<>();
        playerNames.forEach(playerName -> {
            int entityId = playerNames.indexOf(playerName);
            positionComponentList.add(new PositionComponent()
                    .setEntityId(entityId)
                    .setX(100f * entityId)
                    .setY(10f));
        });
        return positionComponentList;
    }

    private void observableForProcessingGameRounds(int roundLength, PositonSystem positonSystem, List<PositionComponent> positionComponentList, Observable<Message<JsonObject>> gameeventObservable) {
        gameeventObservable.
                filter(msg -> msg.body().containsKey("tick") && msg.body().getInteger("tick").intValue() == tick).
                buffer(roundLength, TimeUnit.MILLISECONDS).
                subscribe(msgList -> {
                    positonSystem.exexcute(positionComponentList);
                    msgList.forEach(msg -> {
                                PlayerMessage playerMessage = new PlayerMessage(msg.body());
                                playerMessage.getActions().forEach(action -> {
                                            positionComponentList.get(playerMessage.getEntityId()).addAction(action);
                                        }
                                );
                            }
                    );
                    tick++;
                    JsonObject newRoundMessage = new NewRoundMessage().tick(tick).gameId(gameId).addAll(positionComponentList).toJsonObject();
                    if (producer == null) vertx.eventBus().send("newRound", newRoundMessage);
                    else producer.send(new ProducerRecord<>("game-" + gameId, newRoundMessage.toString()));
                });
    }

    ;

    public KafkaProducer createProducer() throws Exception {
        Properties props = new Properties();
        props.put("bootstrap.servers", config().getString(CONFIG_BOOTSTRAP_SERVERS));
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("producer.type", "async");
        props.put("request.required.acks", "1");
        props.put("batch.size", "0");

        return new KafkaProducer<>(props);
    }

}
