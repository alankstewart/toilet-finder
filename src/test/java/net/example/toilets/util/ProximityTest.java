package net.example.toilets.util;

import net.example.toilets.model.Location;
import org.junit.Test;

import static net.example.toilets.util.Proximity.distanceBetween;
import static org.junit.Assert.assertEquals;

public class ProximityTest {

    @Test
    public void testDistanceBetween() throws Exception {
        Location bondiBeach = new Location(-33.890755, 151.276618);
        Location operaHouse = new Location(-33.856744, 151.215134);
        Location penguinIsland = new Location(-43.349099, 147.371635);

        assertEquals(6814.3, distanceBetween(bondiBeach, operaHouse).inMetres(), 0.1);
        assertEquals(1104, distanceBetween(bondiBeach, penguinIsland).inKilometres(), 1);
        assertEquals(1106, distanceBetween(operaHouse, penguinIsland).inKilometres(), 1);
    }
}
