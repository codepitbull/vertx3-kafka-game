package de.codepitbull.vertx.game.client;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import de.codeptibull.vertx.game.server.entitysystem.systems.ActionsEnum;
import de.codeptibull.vertx.game.server.messages.NewRoundMessage;
import de.codeptibull.vertx.game.server.messages.PlayerMessage;

import java.util.ArrayList;
import java.util.List;


public class VertxBeholder extends ApplicationAdapter {
	SpriteBatch batch;
	Texture img;
    List<Sprite> sprites;
    NewRoundMessage currentRound;
    BitmapFont font;
    float roundLength = 0.02f;
    float playerSpeed = 10f;

    BeholderConnection beholderConnection;

    public VertxBeholder(String host, int gameId, int tick) {
        beholderConnection = new BeholderConnection(host, gameId, tick);
    }

    @Override
	public void create () {
		batch = new SpriteBatch();
		img = new Texture("ghost.png");
        font = new BitmapFont();
        font.setColor(Color.BLACK);
    }

	@Override
	public void render () {
        NewRoundMessage nextRound = beholderConnection.queue.poll();
        if(nextRound != null) {
            currentRound = nextRound;
            if(sprites != null) {
                currentRound.getActions().forEach(action ->
                    sprites.get(action.getEntityId()).setPosition(action.getX(), action.getY()));
            }
        }
        if(currentRound == null) return;

		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if(sprites == null) {
            sprites = new ArrayList<>();
            for(int i=0; i < currentRound.getActions().size(); i++) {
                Sprite sprite = new Sprite(img);
                sprite.setPosition(0, 0);
                sprites.add(sprite);
            }
        }
        currentRound.getActions().forEach(playerAction -> {
            float delta = playerSpeed * (roundLength / Gdx.graphics.getDeltaTime());

            playerAction.getActions().forEach(action -> {
                if (action == ActionsEnum.LEFT)
                    sprites.get(playerAction.getEntityId()).translateX(-delta);
                else if (action == ActionsEnum.RIGHT)
                    sprites.get(playerAction.getEntityId()).translateX(delta);
                else if (action == ActionsEnum.UP)
                    sprites.get(playerAction.getEntityId()).translateY(delta);
                else if (action == ActionsEnum.DOWN)
                    sprites.get(playerAction.getEntityId()).translateY(-delta);
            });
        });

        batch.begin();
        sprites.forEach(sprite -> sprite.draw(batch));
        font.draw(batch, "Tick "+currentRound.getTick(), 10, 20);
        batch.end();
	}
}
