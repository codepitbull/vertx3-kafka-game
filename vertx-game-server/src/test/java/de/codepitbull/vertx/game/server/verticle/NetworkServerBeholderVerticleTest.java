package de.codepitbull.vertx.game.server.verticle;

import de.codepitbull.vertx.game.server.util.KafkaProducerVerticle;
import de.codeptibull.vertx.game.server.verticle.NetworkServerBeholderVerticle;
import de.codeptibull.vertx.game.server.entitysystem.systems.ActionsEnum;
import de.codeptibull.vertx.game.server.entitysystem.systems.PositionComponent;
import de.codeptibull.vertx.game.server.messages.*;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetClientOptions;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.net.NetClient;
import io.vertx.test.core.VertxTestBase;
import kafka.admin.AdminUtils;
import kafka.server.KafkaConfig;
import kafka.server.KafkaServer;
import kafka.utils.*;
import org.I0Itec.zkclient.ZkClient;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import static de.codeptibull.vertx.game.server.verticle.NetworkServerBeholderVerticle.CONFIG_KAFKA_BROKERS;
import static de.codeptibull.vertx.game.server.verticle.NetworkServerBeholderVerticle.CONFIG_KAFKA_PORT;
import static de.codeptibull.vertx.game.server.verticle.NetworkServerBeholderVerticle.CONFIG_NET_PORT_BEHOLDER;
import static java.util.stream.IntStream.range;
import static org.hamcrest.Matchers.*;

/**
 *
 * Created by jmader on 08.01.15.
 */
public class NetworkServerBeholderVerticleTest extends VertxTestBase {
    private int brokerId = 0;

    private ZkClient zkClient;
    private kafka.zk.EmbeddedZookeeper zkServer;
    private KafkaServer kafkaServer;
    private int port = 0;

    public static final String GAME_TOPIC = "game-1";


    Vertx vertxRx;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        vertxRx = new Vertx(vertx);
    }


    @Before
    public void setUpTest() throws Exception{

        String zkConnect = TestZKUtils.zookeeperConnect();
        zkServer = new kafka.zk.EmbeddedZookeeper(zkConnect);
        zkClient = new ZkClient(zkServer.connectString(), 30000, 30000, ZKStringSerializer$.MODULE$);

        // setup Broker
        port = TestUtils.choosePort();
        Properties props = TestUtils.createBrokerConfig(brokerId, port, true);

        KafkaConfig config = new KafkaConfig(props);
        Time mock = new MockTime();
        kafkaServer = TestUtils.createServer(config, mock);

        AdminUtils.createTopic(zkClient, GAME_TOPIC, 1, 1, new Properties());
        List<KafkaServer> servers = new ArrayList<>();
        servers.add(kafkaServer);
        TestUtils.waitUntilMetadataIsPropagated(scala.collection.JavaConversions.asScalaBuffer(servers), GAME_TOPIC, 0, 500);

        TestUtils.waitUntilLeaderIsElectedOrChanged(zkClient, GAME_TOPIC, 0, 500, scala.Option.apply(null), scala.Option.apply(null));


        vertx.deployVerticle(KafkaProducerVerticle.class.getName(),
                new DeploymentOptions().setConfig(new JsonObject().put("bootstrap.server", "127.0.0.1:" + port)));

        waitUntil(() -> vertx.deploymentIDs().size() == 1);
    }

    @Test
    public void testTwoBeholdersConnectingWithDifferentOffsets() throws Exception{

        Thread.sleep(1000);

        range(0, 40).forEach(val -> {
            String msg = new NewRoundMessage().gameId(1).tick(val).add(new PositionComponent().setEntityId(1).addAction(ActionsEnum.DOWN)).toJsonObject().toString();
            vertx.eventBus().send("outgoing", new JsonObject().put("topic", GAME_TOPIC).put("msg", msg));
        });

        vertx.setPeriodic(100, lo -> {
            String msg = new NewRoundMessage().gameId(1).tick(100).add(new PositionComponent().setEntityId(1).addAction(ActionsEnum.DOWN)).toJsonObject().toString();
            vertx.eventBus().send("outgoing", new JsonObject().put("topic", GAME_TOPIC).put("msg", msg));
        });

        Thread.sleep(1000);

        CountDownLatch cl = new CountDownLatch(1);

        vertxRx.deployVerticle(NetworkServerBeholderVerticle.class.getName(),
                new DeploymentOptions().setConfig(
                        new JsonObject()
                                .put(CONFIG_NET_PORT_BEHOLDER, 9001)
                                .put(CONFIG_KAFKA_PORT, kafkaServer.config().port())
                                .put(CONFIG_KAFKA_BROKERS, "127.0.0.1")
                ), suc -> {

                    cl.countDown();
                });
        cl.await();



        waitForMore(1);

        MutableBoolean client1Done= new MutableBoolean(false);
        NetClient netClient1 = vertxRx.createNetClient(new NetClientOptions()).connect(9001, "localhost", result -> {
            assertTrue(result.succeeded());
            result.result().handler(msg -> {
                AbstractMessage parsedMessage = MessageFactory.fromBuffer(msg);
                if (parsedMessage instanceof NewRoundMessage) {
                    if(!client1Done.getValue()) {
                        client1Done.setTrue();
                        NewRoundMessage message = (NewRoundMessage) parsedMessage;
                        assertThat(message.getTick(), equalTo(10));
                        result.result().close();
                        complete();
                    }

                }
            });
            result.result().write(new InitBeholderMessage().tick(10).gameId(1).toBuffer());
        });

        MutableBoolean client2Done= new MutableBoolean(false);
        NetClient netClient2 = vertxRx.createNetClient(new NetClientOptions()).connect(9001, "localhost", result -> {
            assertTrue(result.succeeded());
            result.result().handler(msg -> {
                AbstractMessage parsedMessage = MessageFactory.fromBuffer(msg);
                if (parsedMessage instanceof NewRoundMessage) {
                    if(!client2Done.getValue()) {
                        client2Done.setTrue();
                        NewRoundMessage message = (NewRoundMessage) parsedMessage;
                        assertThat(message.getTick(), equalTo(14));
                        result.result().close();
                        complete();
                    }
                }
            });
            result.result().write(new InitBeholderMessage().tick(14).gameId(1).toBuffer());
        });

        await();
        netClient1.close();
        netClient2.close();
    }

}
