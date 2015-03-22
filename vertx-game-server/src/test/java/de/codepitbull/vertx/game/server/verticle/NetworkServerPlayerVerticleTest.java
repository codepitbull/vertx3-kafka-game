package de.codepitbull.vertx.game.server.verticle;

import de.codeptibull.vertx.game.server.verticle.NetworkServerPlayerVerticle;
import de.codeptibull.vertx.game.server.entitysystem.systems.ActionsEnum;
import de.codeptibull.vertx.game.server.entitysystem.systems.PositionComponent;
import de.codeptibull.vertx.game.server.messages.*;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetClientOptions;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.net.NetClient;
import io.vertx.test.core.VertxTestBase;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

import static de.codeptibull.vertx.game.server.verticle.NetworkServerPlayerVerticle.CONFIG_NET_PORT;
import static org.hamcrest.Matchers.*;

/**
 * Created by jmader on 08.01.15.
 */
public class NetworkServerPlayerVerticleTest extends VertxTestBase {

    Vertx vertxRx;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        vertxRx = new Vertx(vertx);
    }

    @Before
    public void setUpTest() throws Exception {
        vertxRx.deployVerticle(NetworkServerPlayerVerticle.class.getName(),
                new DeploymentOptions().setConfig(
                        new JsonObject()
                                .put(CONFIG_NET_PORT, 9000)
                ));
        waitUntil(() -> vertx.deploymentIDs().size() == 1);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        vertxRx.eventBus().send("newGame", new JsonObject().put("gameId", 1).put("playerNames", new JsonArray().add("JOCHEN")), response -> {
            if ("OK".equals(response.result().body())) {
                countDownLatch.countDown();
            }
        });
        awaitLatch(countDownLatch);
    }

    @Test
    public void testInitialPlayerConnection() {
        NetClient netClient = vertxRx.createNetClient(new NetClientOptions()).connect(9000, "localhost", result -> {
            assertTrue(result.succeeded());
            result.result().handler(buffer -> {
                assertEquals("This should be the first player to connect.", 0, ((FirstConnectResponseMessage) MessageFactory.fromBuffer(buffer)).getEntityId());
                testComplete();
                result.result().close();
            });
            result.result().write(new FirstConnectMessage().name("JOCHEN").gameId(1).toBuffer());
        });
        await();
        netClient.close();
    }

    @Test
    public void testNewRoundEvent() throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        NetClient netClient = vertxRx.createNetClient(new NetClientOptions()).connect(9000, "localhost", result -> {
            assertTrue(result.succeeded());
            result.result().handler(buffer -> {

                if (countDownLatch.getCount() == 1)
                    countDownLatch.countDown();
                AbstractMessage parsedMessage = MessageFactory.fromBuffer(buffer);
                if (parsedMessage instanceof NewRoundMessage) {
                    NewRoundMessage message = (NewRoundMessage) parsedMessage;
                    assertThat(message.getTick(), is(2));
                    assertThat(message.getActions(), hasSize(1));
                    assertThat(message.getActions(), hasItem(
                            new PositionComponent()
                                    .addAction(ActionsEnum.LEFT)
                                    .setEntityId(0)
                                    .setX(10.0f)
                                    .setY(20.0f)));

                    result.result().close();
                    testComplete();
                }
            });
            result.result().write(new FirstConnectMessage().name("JOCHEN").gameId(1).toBuffer());
        });
        countDownLatch.await();

        vertxRx.eventBus().send("newRound", new NewRoundMessage()
                .tick(2)
                .gameId(1)
                .add(new PositionComponent()
                        .setEntityId(0)
                        .setX(10.0f)
                        .setY(20.0f)
                        .addAction(ActionsEnum.LEFT)).toJsonObject());
        await();
        netClient.close();
    }

    @Test
    public void testPlayerMoves() throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        NetClient netClient = vertxRx.createNetClient(new NetClientOptions()).connect(9000, "localhost", result -> {
            assertTrue(result.succeeded());
            result.result().handler(buffer -> {

                if (countDownLatch.getCount() == 1)
                    countDownLatch.countDown();
                AbstractMessage parsedMessage = MessageFactory.fromBuffer(buffer);
                if (parsedMessage instanceof NewRoundMessage) {
                    NewRoundMessage message = (NewRoundMessage) parsedMessage;
                    assertThat(message.getTick(), is(2));
                    assertThat(message.getActions(), hasSize(1));
                    assertThat(message.getActions(), hasItem(
                            new PositionComponent()
                                    .addAction(ActionsEnum.LEFT)
                                    .setEntityId(0)
                                    .setX(10.0f)
                                    .setY(20.0f)));

                    result.result().close();
                    testComplete();
                }
            });
            result.result().write(new FirstConnectMessage().name("JOCHEN").gameId(1).toBuffer());
        });
        countDownLatch.await();

        vertxRx.eventBus().send("newRound", new NewRoundMessage()
                .tick(2)
                .gameId(1)
                .add(new PositionComponent()
                        .setEntityId(0)
                        .setX(10.0f)
                        .setY(20.0f)
                        .addAction(ActionsEnum.LEFT)).toJsonObject());
        await();
        netClient.close();
    }
}
