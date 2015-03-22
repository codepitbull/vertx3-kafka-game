package de.codepitbull.vertx.game.server.entitysystem.systems;

import de.codeptibull.vertx.game.server.entitysystem.systems.ActionsEnum;
import de.codeptibull.vertx.game.server.entitysystem.systems.PositionComponent;
import de.codeptibull.vertx.game.server.entitysystem.systems.PositonSystem;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 * Created by jmader on 07.02.15.
 */
public class PositionSystemTest {

    @Test
    public void testRotate() {
        PositonSystem positonSystem = new PositonSystem(10);
        PositionComponent positionComponent1 = new PositionComponent().setEntityId(1).addAction(ActionsEnum.LEFT).setX(10).setY(10);
        PositionComponent positionComponent2 = new PositionComponent().setEntityId(2).addAction(ActionsEnum.RIGHT).setX(30).setY(10);
        ArrayList<PositionComponent> positionComponents = new ArrayList<>();
        positionComponents.add(positionComponent1);
        positionComponents.add(positionComponent2);
        
        positonSystem.exexcute(positionComponents);
        assertThat(positionComponent1.getX(), is(0f));
        assertThat(positionComponent1.getActions(), empty());
        assertThat(positionComponent2.getX(), is(40f));
        assertThat(positionComponent2.getActions(), empty());
    }

}
