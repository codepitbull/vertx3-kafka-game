package de.codeptibull.vertx.game.server.verticle;

import io.vertx.core.*;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.ext.apex.Router;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.ext.apex.handler.BodyHandler;
import kafka.admin.AdminUtils;
import kafka.common.TopicExistsException;
import org.I0Itec.zkclient.ZkClient;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static de.codeptibull.vertx.game.server.verticle.GameMainLoopVerticle.CONFIG_BOOTSTRAP_SERVERS;
import static io.vertx.core.http.HttpHeaders.LOCATION;
import static io.vertx.core.http.HttpMethod.*;
import static org.apache.commons.lang3.StringUtils.isNumeric;

/**
 * Verticle providing a very basic game management REST-API.
 */
public class RestApiVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestApiVerticle.class);

    public static final String HTTP_PARAM_PORT = "param0";
    public static final String CONFIG_PORT = "port";
    public static final String CONFIG_PLAYER_NAMES = "playerNames";
    public static final String CONFIG_GAME_ID = "gameId";
    public static final String CONFIG_ROUND_LENGHT = "roundLength";
    public static final String CONFIG_PLAYER_SPEED = "playerSpeed";
    public static final String CONFIG_ZOOKEEPER = "zookeeper";
    private Integer gameId = 0;

    private Map<Integer, JsonObject> idToGameMap = new HashMap<>();
    private Integer httpPort = 0;
    private String zookeeper = null;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        LOGGER.info("Creating " + RestApiVerticle.class.getSimpleName() + " for config " + config().toString());

        httpPort = config().getInteger(CONFIG_PORT, 8090);
        zookeeper = config().getString(CONFIG_ZOOKEEPER);

        Router apiRouter = Router.router(vertx);

        addRouteForRetrievingGameInfo(apiRouter);

        addRouteForDeletingGames(apiRouter);

        addRouteForCreatingGames(apiRouter);

        vertx.createHttpServer(new HttpServerOptions().setPort(httpPort)).requestHandler(apiRouter::accept).listen(asyncResult -> {
            //TODO: Apex isn't yet RX-compatible, change this as soon as Apex got RX
            vertx.deployVerticle(NetworkServerPlayerVerticle.class.getName(), new DeploymentOptions().setConfig(config()), donePlayer -> {
                if (donePlayer.succeeded()) {
                    vertx.deployVerticle(NetworkServerBeholderVerticle.class.getName(), new DeploymentOptions().setConfig(config()), doneBeholder -> {
                        if (doneBeholder.succeeded()) {
                            startFuture.complete();
                        } else startFuture.fail(doneBeholder.cause());
                    });
                } else startFuture.fail(donePlayer.cause());
            });

        });
    }

    private void addRouteForRetrievingGameInfo(Router apiRouter) {
        apiRouter.routeWithRegex("/games/(\\d+)")
                .method(GET)
                .handler(context -> {
                    if (isNumeric(context.request().params().get(HTTP_PARAM_PORT)) && idToGameMap.containsKey(getParam0(context))) {
                        context.response()
                                .end(idToGameMap.get(getParam0(context)).toString());
                        return;
                    }
                    context.response().setStatusCode(404).end();
                });
    }

    private void addRouteForDeletingGames(Router apiRouter) {
        apiRouter.routeWithRegex("/games/(\\d+)")
                .method(DELETE)
                .handler(context -> {
                    if (isNumeric(context.request().params().get(HTTP_PARAM_PORT)) && idToGameMap.containsKey(getParam0(context))) {
                        JsonObject jsonObject = idToGameMap.remove(getParam0(context));

                        //TODO: send event to end game

                        context.response().end();
                        return;
                    }
                    context.response().setStatusCode(404).end();
                });
    }

    private void addRouteForCreatingGames(Router apiRouter) {
        apiRouter.route("/games").handler(BodyHandler.create());
        apiRouter.route("/games")
                .method(POST)
                .consumes("application/json")
                .handler(context -> {
                    int newGameId = gameId++;
                    JsonObject gameToCreate = context.getBodyAsJson().put(CONFIG_GAME_ID, newGameId);
                    LOGGER.info("Creating game for request " + gameToCreate.toString());
                    vertx.eventBus().send("newGame", gameToCreate, response -> {
                        if (response.succeeded()) {
                            deployGameMainLoopVerticleForNewGame(context, newGameId, gameToCreate);
                        } else {
                            LOGGER.info("Failed creating game.", response.cause());
                            context.response().setStatusCode(503).end();
                        }
                    });
                });
    }

    private void deployGameMainLoopVerticleForNewGame(RoutingContext context, int newGameId, JsonObject gameToCreate) {
        vertx.deployVerticle(GameMainLoopVerticle.class.getName(),
                new DeploymentOptions().setConfig(new JsonObject()
                        .put(CONFIG_BOOTSTRAP_SERVERS, config().getString(CONFIG_BOOTSTRAP_SERVERS))
                        .put(CONFIG_GAME_ID, newGameId)
                        .put(CONFIG_PLAYER_NAMES, gameToCreate.getJsonArray(CONFIG_PLAYER_NAMES))),
                deploymentResult -> {
                    //TODO: Deployment ID speichern
                    if (deploymentResult.succeeded()) {
                        idToGameMap.put(newGameId, gameToCreate);
                        if (zookeeper != null) {
                            createKafkaTopic(newGameId, result ->
                                    context.response()
                                            .putHeader(LOCATION, "/games/" + newGameId)
                                            .setStatusCode(303).end(gameToCreate.toString()));
                        } else context.response()
                                .putHeader(LOCATION, "/games/" + newGameId)
                                .setStatusCode(303).end(gameToCreate.toString());
                    } else
                        LOGGER.error("Failed deploying " + GameMainLoopVerticle.class.getSimpleName(), deploymentResult.cause());
                });
    }

    private void createKafkaTopic(int newGameId, Handler<AsyncResult<Object>> resultHandler) {
        LOGGER.info("Creating topic game-" + newGameId);
        vertx.executeBlocking(
                exec -> {
                    ZkClient zkClient = new ZkClient(zookeeper);
                    try {
                        AdminUtils.createTopic(zkClient, "game-" + newGameId, 1, 1, new Properties());
                        LOGGER.info("Created topic game-" + newGameId);
                    } catch (TopicExistsException te) {
                        LOGGER.info(te);
                    }
                    exec.complete();
                }, resultHandler);
    }


    private Integer getParam0(RoutingContext context) {
        return Integer.valueOf(context.request().params().get(HTTP_PARAM_PORT));
    }
}
