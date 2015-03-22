package de.codepitbull.vertx.game.server.verticle;

import de.codeptibull.vertx.game.server.verticle.RestApiVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.test.core.VertxTestBase;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

import static de.codeptibull.vertx.game.server.verticle.NetworkServerBeholderVerticle.CONFIG_KAFKA_BROKERS;
import static de.codeptibull.vertx.game.server.verticle.NetworkServerBeholderVerticle.CONFIG_KAFKA_PORT;
import static de.codeptibull.vertx.game.server.verticle.RestApiVerticle.CONFIG_PORT;
import static io.vertx.core.http.HttpHeaders.CONTENT_LENGTH;
import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;
import static io.vertx.core.http.HttpMethod.*;

/**
 * Created by jmader on 18.01.15.
 */
public class RestApiVerticleTest extends VertxTestBase {

    public Integer port() {
        return 9090;
    }

    @Before
    public void setUpTest() {
        vertx.deployVerticle(RestApiVerticle.class.getName(), new DeploymentOptions()
                .setConfig(new JsonObject()
                                .put(CONFIG_PORT, port())
                                .put(CONFIG_KAFKA_BROKERS, "127.0.0.1")
                                .put(CONFIG_KAFKA_PORT, 9092)
                ));
        waitUntil(() -> vertx.deploymentIDs().size() >= 1);
    }

    @Test
    public void testGetFail() throws Exception{
        HttpClient httpClient = vertx.createHttpClient(new HttpClientOptions());
        httpClient.request(GET, port(), "localhost", "/games/123565645", response -> {
            assertEquals(404, response.statusCode());
            testComplete();
        }).end();
        await();
    }

    @Test
    @Ignore
    public void testCreateGame() throws Exception{
        HttpClient httpClient = vertx.createHttpClient(new HttpClientOptions());
        String json = vertx.fileSystem().readFileBlocking("creategame.json").toString();

        final CountDownLatch latch = new CountDownLatch(1);
        httpClient.request(POST, port(), "localhost", "/games", response -> {
            response.bodyHandler(body -> {
                assertEquals("Wrong response code", 303, response.statusCode());
                assertEquals("Problem with Location-header", "/games/1", response.headers().get("Location"));
                latch.countDown();
            });
        }).setChunked(true).putHeader(CONTENT_TYPE, "application/json").putHeader(CONTENT_LENGTH, ""+json.length()).write(json).end();
        latch.await();

        final CountDownLatch latch2 = new CountDownLatch(1);
        httpClient.request(GET, port(), "localhost", "/games/1", response -> {
            response.bodyHandler(body -> {
                JsonObject jsonObject = new JsonObject(body.toString());
                assertTrue(jsonObject.getJsonArray("playerNames").contains("player1"));
                assertTrue(jsonObject.getJsonArray("playerNames").contains("player2"));
                assertTrue(jsonObject.getJsonArray("playerNames").contains("player3"));
                latch2.countDown();
            });
        }).end();
        latch2.await();

        final CountDownLatch latch3 = new CountDownLatch(1);
        httpClient.request(DELETE, port(), "localhost", "/games/1", response -> {
            assertEquals(200, response.statusCode());
            latch3.countDown();
        }).end();
        latch3.await();
    }

}
