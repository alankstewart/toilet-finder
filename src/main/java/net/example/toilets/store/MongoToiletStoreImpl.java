package net.example.toilets.store;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.QueryBuilder;
import net.example.toilets.model.Location;
import net.example.toilets.model.Toilet;
import net.example.toilets.model.ToiletBuilder;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;

import static java.lang.String.valueOf;
import static java.util.stream.Collectors.toList;
import static net.example.toilets.util.Proximity.RADIUS_OF_EARTH;

/**
 * Created by alanstewart on 15/02/15.
 */
public final class MongoToiletStoreImpl extends AbstractToiletStoreImpl {

    private DBCollection coll;

    @Override
    public List<Toilet> search(ToiletQuery query) {
        if (coll == null) {
            return Collections.<Toilet>emptyList();
        }

        Location location = query.getLocation();
        return coll.find(QueryBuilder.start("location")
                .nearSphere(location.getLongitude(), location.getLatitude(), 5 / RADIUS_OF_EARTH)
                .get()).limit(query.getLimit()).toArray()
                .stream().map(this::createToilet)
                .collect(toList());
    }

    @Override
    public void initialise(InputStream toiletXml) {
        try {
            MongoClient mongoClient = new MongoClient();
            coll = mongoClient.getDB("toiletdb").getCollection("toilets");
            coll.remove(new BasicDBObject());
            coll.createIndex(new BasicDBObject("location", "2dsphere"));
            readToiletXml(toiletXml);
        } catch (UnknownHostException | XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void add(Toilet toilet) {
        Location location = toilet.getLocation();
        BasicDBList coordinates = new BasicDBList();
        coordinates.put(0, location.getLongitude());
        coordinates.put(1, location.getLatitude());
        coll.insert(new BasicDBObject("name", toilet.getName())
                .append("address1", toilet.getAddress1())
                .append("town", toilet.getTown())
                .append("state", toilet.getState())
                .append("postcode", toilet.getPostcode())
                .append("addressNote", toilet.getAddressNote())
                .append("iconUrl", toilet.getIconUrl())
                .append("location", new BasicDBObject("type", "Point").append("coordinates", coordinates)));
    }

    private Toilet createToilet(DBObject dbObject) {
        DBObject locationDbObject = (DBObject) dbObject.get("location");
        BasicDBList coordinates = (BasicDBList) locationDbObject.get("coordinates");
        return new ToiletBuilder()
                .setName(valueOf(dbObject.get("name")))
                .setAddress1(valueOf(dbObject.get("address1")))
                .setTown(valueOf(dbObject.get("town")))
                .setState(valueOf(dbObject.get("state")))
                .setPostcode(valueOf(dbObject.get("postcode")))
                .setAddressNote(valueOf(dbObject.get("addressNote")))
                .setIconUrl(valueOf(dbObject.get("iconUrl")))
                .setLocation(new Location((Double) coordinates.get(1), (Double) coordinates.get(0)))
                .createToilet();
    }
}
