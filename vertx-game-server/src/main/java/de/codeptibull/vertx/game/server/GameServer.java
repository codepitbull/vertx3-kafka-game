package de.codeptibull.vertx.game.server;

import de.codeptibull.vertx.game.server.verticle.RestApiVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import static de.codeptibull.vertx.game.server.verticle.GameMainLoopVerticle.CONFIG_BOOTSTRAP_SERVERS;
import static de.codeptibull.vertx.game.server.verticle.NetworkServerBeholderVerticle.CONFIG_KAFKA_BROKERS;
import static de.codeptibull.vertx.game.server.verticle.NetworkServerBeholderVerticle.CONFIG_KAFKA_PORT;
import static de.codeptibull.vertx.game.server.verticle.NetworkServerPlayerVerticle.CONFIG_NET_PORT;
import static de.codeptibull.vertx.game.server.verticle.NetworkServerPlayerVerticle.CONFIG_ZOOKEEPER_CONNECT_STRING;
import static de.codeptibull.vertx.game.server.verticle.RestApiVerticle.CONFIG_PORT;
import static de.codeptibull.vertx.game.server.verticle.RestApiVerticle.CONFIG_ZOOKEEPER;
import static org.apache.commons.lang3.Validate.isTrue;

/**
 * Created by jmader on 05.02.15.
 */
public class GameServer {

    public static void main(String[] args) {
        isTrue(args.length == 1, "Please provide the hostname where Zookeeper and Kafka are running!");
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(RestApiVerticle.class.getName(),
                new DeploymentOptions().setConfig(
                        new JsonObject()
                                .put(CONFIG_PORT, 8090)
                                .put(CONFIG_NET_PORT, 9000)
                                .put(CONFIG_KAFKA_BROKERS, args[0])
                                .put(CONFIG_KAFKA_PORT, 9092)
                                .put(CONFIG_ZOOKEEPER, args[0] +":2181")
                                .put(CONFIG_ZOOKEEPER_CONNECT_STRING, args[0] +":2181")
                                .put(CONFIG_BOOTSTRAP_SERVERS, args[0] +":9092")));
    }
}
