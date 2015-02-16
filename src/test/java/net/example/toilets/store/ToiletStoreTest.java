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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;

/**
 * Created by alanstewart on 6/02/15.
 */
public class ToiletStoreTest {

    private static final String NAME = "name";

    private static ToiletStore toiletStore = new MongoToiletStoreImpl();

    @BeforeClass
    public static void onlyOnce() {
        InputStream inputStream = ToiletStoreTest.class.getResourceAsStream("/toilets.xml");
        assertThat(inputStream, is(notNullValue()));
        LocalDateTime start = now();
        toiletStore.initialise(inputStream);
        System.out.format("Store initialised in %d ms\n", Duration.between(start, now()).toMillis());
    }

    @Test
    public void shouldFindTenToiletsNear55LimeStreetSydney() {
        Location location = new Location(-33.868654, 151.201854);
        ToiletQuery query = new ToiletQuery(location, 10);
        List<Toilet> toilets = toiletStore.search(query);
        toilets.forEach((System.out::println));
        assertThat(toilets, hasSize(10));
        assertThat(toilets, contains(
                hasProperty(NAME, is("Darling Harbour - Harbourside East")),
                hasProperty(NAME, is("Darling Harbour - Harbourside West")),
                hasProperty(NAME, is("Darling Walk")),
                hasProperty(NAME, is("Wynyard Park")),
                hasProperty(NAME, is("Wynyard Train Station")),
                hasProperty(NAME, is("Metcentre")),
                hasProperty(NAME, is("Lang Park")),
                hasProperty(NAME, is("Town Hall Square")),
                hasProperty(NAME, is("Town Hall Train Station")),
                hasProperty(NAME, is("David Jones - Castlereagh Street"))));
    }

    @Test
    public void shouldFindFiveToiletsNearHome() {
        Location location = new Location(-33.707452, 151.113031);
        ToiletQuery query = new ToiletQuery(location, 5);
        List<Toilet> toilets = toiletStore.search(query);
        assertThat(toilets, hasSize(5));
        assertThat(toilets, contains(
                hasProperty(NAME, is("Carrington Oval")),
                hasProperty(NAME, is("Waitara Park")),
                hasProperty(NAME, is("Willow Park")),
                hasProperty(NAME, is("PA James Park")),
                hasProperty(NAME, is("Waitara Train Station"))));
    }

    @Test
    public void shouldFindThreeToiletsNearReserveBank() {
        Location location = new Location(-33.867960, 151.211745);
        ToiletQuery query = new ToiletQuery(location, 3);
        List<Toilet> toilets = toiletStore.search(query);
        assertThat(toilets, hasSize(3));
        assertThat(toilets, contains(
                hasProperty(NAME, is("Martin Place Train Station")),
                hasProperty(NAME, is("Colonial Centre")),
                hasProperty(NAME, is("Hyde Park - North 1"))));
    }

    @Test
    public void shouldFindZeroToilets() {
        Location location = new Location(-35, 155);
        ToiletQuery query = new ToiletQuery(location, 10);
        List<Toilet> toilets = toiletStore.search(query);
        toilets.forEach(System.out::println);
        assertThat(toilets, is(notNullValue()));
        assertThat(toilets, is(empty()));
    }
}
