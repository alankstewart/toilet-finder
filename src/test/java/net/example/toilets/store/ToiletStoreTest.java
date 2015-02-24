package net.example.toilets.store;

import net.example.toilets.model.Location;
import net.example.toilets.model.Toilet;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static java.time.LocalDateTime.now;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by alanstewart on 6/02/15.
 */
public class ToiletStoreTest {

    private static final ToiletStore TOILET_STORE = new JdbcToiletStoreImpl();

    @BeforeClass
    public static void onlyOnce() {
        InputStream inputStream = ToiletStoreTest.class.getResourceAsStream("/toilets.xml");
        assertNotNull(inputStream);
        LocalDateTime start = now();
        TOILET_STORE.initialise(inputStream);
        System.out.println("Initialised toilet store in " + Duration.between(start, now()).toMillis() + " ms");
    }

    @Test
    public void shouldFindTenToiletsNear55LimeStreetSydney() {
        List<String> toiletNames = searchToiletStore(-33.868654, 151.201854, 10);
        toiletNames.forEach((System.out::println));
        assertEquals(10, toiletNames.size());
        assertThat(toiletNames, hasItems(
                "Darling Harbour - Harbourside East",
                "Darling Harbour - Harbourside West",
                "Darling Walk",
                "Wynyard Park",
                "Wynyard Train Station",
                "Metcentre",
                "Lang Park",
                "Town Hall Square",
                "Town Hall Train Station",
                "David Jones - Castlereagh Street"));
    }

    @Test
    public void shouldFindTenToiletsNearHome() {
        List<String> toiletNames = searchToiletStore(-33.707452, 151.113031, 10);
        assertEquals(10, toiletNames.size());
        assertThat(toiletNames, hasItems(
                "Carrington Oval",
                "Waitara Park",
                "Willow Park",
                "PA James Park",
                "Waitara Train Station",
                "Caltex Australia",
                "Wahroonga Park",
                "Wahroonga Train Station",
                "Wahroonga Car Park",
                "BP Express Hornsby"));
    }

    @Test
    public void shouldFindThreeToiletsNearReserveBank() {
        List<String> toiletNames = searchToiletStore(-33.867960, 151.211745, 3);
        assertEquals(3, toiletNames.size());
        assertThat(toiletNames, hasItems(
                "Martin Place Train Station",
                "Colonial Centre",
                "Hyde Park - North 1"));
    }

    @Test
    public void shouldFindZeroToilets() {
        List<String> toiletNames = searchToiletStore(-35, 35, 10);
        assertTrue(toiletNames.isEmpty());
    }

    private List<String> searchToiletStore(double latitude, double longitude, int limit) {
        Location location = new Location(latitude, longitude);
        ToiletQuery query = new ToiletQuery(location, limit);
        return TOILET_STORE.search(query).stream().map(Toilet::getName).collect(toList());
    }
}
