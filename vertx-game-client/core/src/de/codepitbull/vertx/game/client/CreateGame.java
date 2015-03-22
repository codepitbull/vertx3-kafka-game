package de.codepitbull.vertx.game.client;

import de.codeptibull.vertx.game.server.GameServer;
import de.codeptibull.vertx.game.server.messages.*;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.net.NetClientOptions;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.http.HttpClient;
import io.vertx.rxjava.core.net.NetClient;

import static io.vertx.core.http.HttpHeaders.CONTENT_LENGTH;
import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;
import static io.vertx.core.http.HttpMethod.POST;

/**
 * Created by jmader on 07.02.15.
 */
public class CreateGame {
    public static void main(String[] args) throws Exception{
        HttpClient httpClient = Vertx.vertx().createHttpClient(new HttpClientOptions());

        String json = "{\"playerNames\" : [\"player1\",\"player2\",\"player3\"]}";
        httpClient.request(POST, 8090, "localhost", "/games", response -> {
            response.bodyHandler(body -> {
//                new ClientConnection("player1");
//                new ClientConnection("player2");
            });
        }).setChunked(true).putHeader(CONTENT_TYPE.toString(), "application/json").putHeader(CONTENT_LENGTH.toString(), ""+json.length()).write(json).end();

    }
}
