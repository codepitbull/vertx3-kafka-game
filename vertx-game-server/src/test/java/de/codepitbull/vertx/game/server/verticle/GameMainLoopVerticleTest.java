package de.codepitbull.vertx.game.server.verticle;

import de.codeptibull.vertx.game.server.verticle.GameMainLoopVerticle;
import de.codeptibull.vertx.game.server.entitysystem.systems.ActionsEnum;
import de.codeptibull.vertx.game.server.messages.NewRoundMessage;
import de.codeptibull.vertx.game.server.messages.PlayerMessage;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.test.core.VertxTestBase;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableLong;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static de.codeptibull.vertx.game.server.verticle.RestApiVerticle.CONFIG_GAME_ID;
import static de.codeptibull.vertx.game.server.verticle.RestApiVerticle.CONFIG_PLAYER_NAMES;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

/**
 * Created by jmader on 25.01.15.
 */
public class GameMainLoopVerticleTest extends VertxTestBase {

    @Before
    public void setUpTest() {
        vertx.deployVerticle(GameMainLoopVerticle.class.getName(),
                new DeploymentOptions().setConfig(
                        new JsonObject()
                                .put(CONFIG_PLAYER_NAMES, new JsonArray().add("JOCHEN"))
                                .put(CONFIG_GAME_ID, 1)
                ));
        waitUntil(() -> vertx.deploymentIDs().size() == 1);
    }

    @Test
    public void testThatEachTickIncrementsByOne() throws Exception {
        AtomicLong lastTick = new AtomicLong(0);
        vertx.eventBus().<JsonObject>consumer("newRound", res -> {
            NewRoundMessage newRoundMessage = new NewRoundMessage(res.body());
            assertThat(newRoundMessage.getActions().get(0).getEntityId(), is(0));
            if (lastTick.get() == 0){
                lastTick.set(newRoundMessage.getTick());
            }
            else {
                assertEquals(1, newRoundMessage.getTick() - lastTick.get());
                testComplete();
            }
        });
        await();
    }

    @Test
    public void testThatActionsHaveAnEffect() throws Exception {
        MutableLong lastTick = new MutableLong(0);
        MutableFloat lastX = new MutableFloat(0);
        MutableBoolean receivedFirst = new MutableBoolean(false);
        vertx.eventBus().<JsonObject>consumer("newRound", res -> {
            NewRoundMessage newRoundMessage = new NewRoundMessage(res.body());
            if(receivedFirst.getValue()) {
                if (lastTick.getValue() == 0) {
                    assertThat(newRoundMessage.getActions().size(), is(1));
                    assertThat(newRoundMessage.getActions().get(0).getEntityId(), is(0));
                    lastX.setValue(newRoundMessage.getActions().get(0).getX());
                    vertx.eventBus().send("command-1", new PlayerMessage()
                            .tick(res.body().getInteger("tick"))
                            .entityId(0)
                            .gameId(1)
                            .addAction(ActionsEnum.LEFT)
                            .toJsonObject());
                    lastTick.setValue(newRoundMessage.getTick());
                }
                else if (newRoundMessage.getTick() == lastTick.getValue()+1){
                    assertEquals(0, newRoundMessage.getActions().get(0).getEntityId());
                    assertThat(newRoundMessage.getActions().get(0).getX(), is(lastX.getValue()));
                    assertThat(newRoundMessage.getActions().get(0).getActions(), contains(ActionsEnum.LEFT));
                }
                else if (newRoundMessage.getTick() == lastTick.getValue()+2){
                    assertEquals(0, newRoundMessage.getActions().get(0).getEntityId());
                    assertThat(newRoundMessage.getActions().get(0).getActions(), empty());
                    assertTrue(newRoundMessage.getActions().get(0).getX() < lastX.getValue());
                    testComplete();
                }
            }
            receivedFirst.setValue(true);
        });
        await();
    }

    @Test
    public void testThatActionIsShown() throws Exception {
        AtomicInteger received = new AtomicInteger(0);
        vertx.eventBus().<JsonObject>consumer("newRound", res -> {
            NewRoundMessage newRoundMessage = new NewRoundMessage(res.body());
            assertEquals(1, newRoundMessage.getActions().size());
            assertEquals(0, newRoundMessage.getActions().get(0).getEntityId());
            received.incrementAndGet();
            //we might receive the first one close to its timer running out, ignore it and pick the 2nd one
            if((received.get() == 2)) {
                assertEquals(0, newRoundMessage.getActions().get(0).getEntityId());
                vertx.eventBus().send("command-1", new PlayerMessage()
                                .tick(res.body().getInteger("tick"))
                                .entityId(0)
                                .gameId(1)
                                .addAction(ActionsEnum.LEFT)
                                .toJsonObject()
                );
            }
            else if(received.get() == 3){
                assertThat(newRoundMessage.getActions().get(0).getActions(), contains(ActionsEnum.LEFT));
                testComplete();
            }
        });
        await();
    }
}
