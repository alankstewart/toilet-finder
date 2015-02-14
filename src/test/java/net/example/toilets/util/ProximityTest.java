package net.example.toilets.util;

import net.example.toilets.model.Location;
import org.junit.Test;

import static net.example.toilets.util.Proximity.distanceBetween;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.assertThat;

public class ProximityTest {

    @Test
    public void testDistanceBetween() throws Exception {
        Location bondiBeach = new Location(-33.890755, 151.276618);
        Location operaHouse = new Location(-33.856744, 151.215134);
        Location penguinIsland = new Location(-43.349099, 147.371635);

        assertThat(distanceBetween(bondiBeach, operaHouse).inMetres(), is(closeTo(6814.3, 0.1)));
        assertThat(distanceBetween(bondiBeach, penguinIsland).inKilometres(), is(closeTo(1104, 1)));
        assertThat(distanceBetween(operaHouse, penguinIsland).inKilometres(), is(closeTo(1106, 1)));
    }
}
