package alankstewart.toilets.store;

import alankstewart.toilets.model.Location;
import alankstewart.toilets.model.Toilet;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.util.List;

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

    private static final ToiletStore TOILET_STORE = new ToiletStoreImpl();

    @BeforeClass
    public static void onlyOnce() {
        var inputStream = ToiletStoreTest.class.getResourceAsStream("/toilets.xml");
        assertNotNull(inputStream);
        TOILET_STORE.initialise(inputStream);
    }

    @Test
    public void shouldFindTenToiletsNear55LimeStreetSydney() {
        var toiletNames = searchToiletStore(-33.868654, 151.201854, 10);
        toiletNames.forEach((System.out::println));
        assertEquals(10, toiletNames.size());
        assertThat(toiletNames, hasItems(
                "Darling Harbour - Harbourside East",
                "Darling Harbour - Harbourside West",
                "Cockle Bay Restaurant Precinct",
                "Darling Walk",
                "Wynyard Park",
                "Wynyard Train Station",
                "InterPark",
                "Town Hall House Public Toilet",
                "Metcentre",
                "Lang Park"));
    }

    @Test
    public void shouldFindTenToiletsNearHome() {
        var toiletNames = searchToiletStore(-33.707452, 151.113031, 10);
        assertEquals(10, toiletNames.size());
        assertThat(toiletNames, hasItems(
                "Carrington Oval",
                "Waitara Park",
                "Willow Park",
                "PA James Park",
                "Waitara Train Station",
                "Caltex Woolworths",
                "Wahroonga Park",
                "Wahroonga Train Station",
                "Westfield Hornsby",
                "Westfield Hornsby"));
    }

    @Test
    public void shouldFindThreeToiletsNearReserveBank() {
        var toiletNames = searchToiletStore(-33.867960, 151.211745, 3);
        assertEquals(3, toiletNames.size());
        assertThat(toiletNames, hasItems(
                "Martin Place Train Station",
                "Colonial Centre",
                "Hyde Park - North 1"));
    }

    @Test
    public void shouldFindZeroToilets() {
        var toiletNames = searchToiletStore(-35, 35, 10);
        assertTrue(toiletNames.isEmpty());
    }

    private List<String> searchToiletStore(double latitude, double longitude, int limit) {
        var location = new Location(latitude, longitude);
        var query = new ToiletQuery(location, limit);
        return TOILET_STORE.search(query).stream().map(Toilet::getName).collect(toList());
    }
}
