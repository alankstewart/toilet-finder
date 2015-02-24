package net.example.toilets.store;

import net.example.toilets.model.Distance;
import net.example.toilets.model.Location;
import net.example.toilets.model.Toilet;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static net.example.toilets.util.Proximity.distanceBetween;

/**
 * Created by alanstewart on 6/02/15.
 */
public final class ToiletStoreImpl extends AbstractToiletStoreImpl {

    private final List<Toilet> toilets = new ArrayList<>();

    @Override
    public List<Toilet> search(ToiletQuery query) {
        Location location = query.getLocation();
        return toilets.parallelStream()
                .filter(t -> distanceBetween(t.getLocation(), location).compareTo(Distance.kilometres(5)) <= 0)
                .sorted((t1, t2) -> distanceBetween(t1.getLocation(), location).compareTo(distanceBetween(t2.getLocation(), location)))
                .limit(query.getLimit())
                .collect(toList());
    }

    @Override
    public void initialise(InputStream toiletXml) {
        toilets.clear();
        readToiletXml(toiletXml);
    }

    @Override
    protected void add(Toilet toilet) {
        toilets.add(toilet);
    }
}
