package de.codeptibull.vertx.game.server.entitysystem.systems;

import de.codeptibull.vertx.game.server.entitysystem.api.EntitySystem;

import java.util.List;


/**
 * Created by jmader on 24.01.15.
 */
public class PositonSystem implements EntitySystem<PositionComponent> {

    private final float distPerRound;

    public PositonSystem(float distPerRound) {
        this.distPerRound = distPerRound;
    }

    @Override
    public void exexcute(List<PositionComponent> components) {
        components.forEach(component -> {
            component.getActions().forEach(action -> {

            switch (action) {
                case RIGHT: component.translate(distPerRound, 0); break;
                case LEFT: component.translate(-distPerRound, 0); break;
                case UP: component.translate(0, distPerRound); break;
                case DOWN: component.translate(0, -distPerRound); break;
                default: break;
            }});

            component.clearActions();
        });
    }

    private float rotateBy(float rotation, float amount) {
        float ret = rotation + amount;
        if(ret > 360) return 90;
        if(ret < 0) return 270;
        return ret;
    }
}
