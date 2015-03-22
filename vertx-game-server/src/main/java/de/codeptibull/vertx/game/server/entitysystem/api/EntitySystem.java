package de.codeptibull.vertx.game.server.entitysystem.api;

import java.util.List;
/**
 * Created by jmader on 24.01.15.
 */
public interface EntitySystem<E extends Component> {
    void exexcute(List<E> components);
}
