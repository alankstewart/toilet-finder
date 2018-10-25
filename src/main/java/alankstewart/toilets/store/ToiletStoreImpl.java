package alankstewart.toilets.store;

import alankstewart.toilets.model.Distance;
import alankstewart.toilets.model.Toilet;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static alankstewart.toilets.util.Proximity.distanceBetween;
import static java.util.stream.Collectors.toList;

/**
 * Created by alanstewart on 6/02/15.
 */
public final class ToiletStoreImpl extends AbstractToiletStoreImpl {

    private final List<Toilet> toilets = new ArrayList<>();

    @Override
    public List<Toilet> search(ToiletQuery query) {
        var location = query.getLocation();
        return toilets.parallelStream()
                .filter(t -> distanceBetween(t.getLocation(), location).compareTo(Distance.kilometres(5)) <= 0)
                .sorted(Comparator.comparing(t2 -> distanceBetween(t2.getLocation(), location)))
                .limit(query.getLimit())
                .collect(toList());
    }

    @Override
    protected long storeToilets(InputStream toiletXml) {
        toilets.clear();
        readToiletXml(toiletXml);
        return toilets.size();
    }

    @Override
    protected void addToilet(Toilet toilet) {
        toilets.add(toilet);
    }
}
