package net.example.toilets.store;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.QueryBuilder;
import net.example.toilets.model.Location;
import net.example.toilets.model.Toilet;

import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.valueOf;
import static java.util.stream.Collectors.toList;
import static net.example.toilets.model.Toilet.KEY_ADDR;
import static net.example.toilets.model.Toilet.KEY_ICON;
import static net.example.toilets.model.Toilet.KEY_LOC;
import static net.example.toilets.model.Toilet.KEY_NAME;
import static net.example.toilets.model.Toilet.KEY_NOTE;
import static net.example.toilets.model.Toilet.KEY_PCODE;
import static net.example.toilets.model.Toilet.KEY_STATE;
import static net.example.toilets.model.Toilet.KEY_TOWN;
import static net.example.toilets.util.Proximity.RADIUS_OF_EARTH;

/**
 * Created by alanstewart on 15/02/15.
 */
public final class MongoToiletStoreImpl extends AbstractToiletStoreImpl {

    private final DBCollection coll;
    private final List<Toilet> toilets = new ArrayList<>();

    public MongoToiletStoreImpl() {
        try {
            MongoClient mongoClient = new MongoClient();
            coll = mongoClient.getDB("toiletdb").getCollection("toilets");
            coll.createIndex(new BasicDBObject(KEY_LOC, "2dsphere"));
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Toilet> search(ToiletQuery query) {
        Location location = query.getLocation();
        return coll.find(QueryBuilder.start(KEY_LOC)
                .nearSphere(location.getLongitude(), location.getLatitude(), 5 / RADIUS_OF_EARTH)
                .get()).limit(query.getLimit()).toArray()
                .stream().map(this::createToilet)
                .collect(toList());
    }

    @Override
    public void initialise(InputStream toiletXml) {
        coll.remove(new BasicDBObject());
        readToiletXml(toiletXml);
        if (!toilets.isEmpty()) {
            addToiletsToCollection();
        }
    }

    @Override
    protected void add(Toilet toilet) {
        if (toilets.size() == 1000) {
            addToiletsToCollection();
        }
        toilets.add(toilet);
    }

    private void addToiletsToCollection() {
        BulkWriteOperation bulkWriteOperation = coll.initializeOrderedBulkOperation();
        for (Toilet toilet : toilets) {
            Location location = toilet.getLocation();
            BasicDBList coordinates = new BasicDBList();
            coordinates.put(0, location.getLongitude());
            coordinates.put(1, location.getLatitude());
            DBObject dbObject = new BasicDBObject(KEY_NAME, toilet.getName())
                    .append(KEY_ADDR, toilet.getAddress1())
                    .append(KEY_TOWN, toilet.getTown())
                    .append(KEY_STATE, toilet.getState())
                    .append(KEY_PCODE, toilet.getPostcode())
                    .append(KEY_NOTE, toilet.getAddressNote())
                    .append(KEY_ICON, toilet.getIconUrl())
                    .append(KEY_LOC, new BasicDBObject("type", "Point").append("coordinates", coordinates));
            bulkWriteOperation.insert(dbObject);
        }
        if (bulkWriteOperation.execute().getInsertedCount() != toilets.size()) {
            throw new IllegalStateException("Failed to add toilets to collection");
        }
        toilets.clear();
    }

    private Toilet createToilet(DBObject dbObject) {
        DBObject locationDbObject = (DBObject) dbObject.get(KEY_LOC);
        BasicDBList coordinates = (BasicDBList) locationDbObject.get("coordinates");
        return new Toilet.Builder()
                .setName(valueOf(dbObject.get(KEY_NAME)))
                .setAddress1(valueOf(dbObject.get(KEY_ADDR)))
                .setTown(valueOf(dbObject.get(KEY_TOWN)))
                .setState(valueOf(dbObject.get(KEY_STATE)))
                .setPostcode(valueOf(dbObject.get(KEY_PCODE)))
                .setAddressNote(valueOf(dbObject.get(KEY_NOTE)))
                .setIconUrl(valueOf(dbObject.get(KEY_ICON)))
                .setLocation(new Location((Double) coordinates.get(1), (Double) coordinates.get(0)))
                .build();
    }
}
