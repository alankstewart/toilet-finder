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

    private static ToiletStore toiletStore = new MongoDBToiletStoreImpl();

    @BeforeClass
    public static void onlyOnce() {
        InputStream inputStream = ToiletStoreTest.class.getResourceAsStream("/toilets.xml");
        assertNotNull(inputStream);
        LocalDateTime start = now();
        toiletStore.initialise(inputStream);
        System.out.format("Store initialised in %d ms\n", Duration.between(start, now()).toMillis());
    }

    @Test
    public void shouldFindTenToiletsNear55LimeStreetSydney() {
        Location location = new Location(-33.868654, 151.201854);
        ToiletQuery query = new ToiletQuery(location, 10);
        List<String> toiletNames = toiletStore.search(query).stream().map(t -> t.getName()).collect(toList());
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
    public void shouldFindFiveToiletsNearHome() {
        Location location = new Location(-33.707452, 151.113031);
        ToiletQuery query = new ToiletQuery(location, 5);
        List<String> toiletNames = toiletStore.search(query).stream().map(t -> t.getName()).collect(toList());
        assertEquals(5, toiletNames.size());
        assertThat(toiletNames, hasItems(
                "Carrington Oval",
                "Waitara Park",
                "Willow Park",
                "PA James Park",
                "Waitara Train Station"));
    }

    @Test
    public void shouldFindThreeToiletsNearReserveBank() {
        Location location = new Location(-33.867960, 151.211745);
        ToiletQuery query = new ToiletQuery(location, 3);
        List<String> toiletNames = toiletStore.search(query).stream().map(t -> t.getName()).collect(toList());
        assertEquals(3, toiletNames.size());
        assertThat(toiletNames, hasItems(
                "Martin Place Train Station",
                "Colonial Centre",
                "Hyde Park - North 1"));
    }

    @Test
    public void shouldFindZeroToilets() {
        Location location = new Location(-35, 155);
        ToiletQuery query = new ToiletQuery(location, 10);
        List<Toilet> toilets = toiletStore.search(query);
        assertTrue(toilets.isEmpty());
    }
}
