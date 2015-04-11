package alankstewart.toilets.util;

import alankstewart.toilets.model.Location;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ProximityTest {

    @Test
    public void testDistanceBetween() throws Exception {
        Location bondiBeach = new Location(-33.890755, 151.276618);
        Location operaHouse = new Location(-33.856744, 151.215134);
        Location penguinIsland = new Location(-43.349099, 147.371635);

        assertEquals(6814.3, Proximity.distanceBetween(bondiBeach, operaHouse).inMetres(), 0.1);
        assertEquals(1104, Proximity.distanceBetween(bondiBeach, penguinIsland).inKilometres(), 1);
        assertEquals(1106, Proximity.distanceBetween(operaHouse, penguinIsland).inKilometres(), 1);
    }
}
