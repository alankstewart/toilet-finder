package alankstewart.toilets.store;

import alankstewart.toilets.model.Location;
import alankstewart.toilets.model.Toilet;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.QueryBuilder;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.StreamSupport;

import static alankstewart.toilets.model.Toilet.KEY_ADDR;
import static alankstewart.toilets.model.Toilet.KEY_ICON;
import static alankstewart.toilets.model.Toilet.KEY_LOC;
import static alankstewart.toilets.model.Toilet.KEY_NAME;
import static alankstewart.toilets.model.Toilet.KEY_NOTE;
import static alankstewart.toilets.model.Toilet.KEY_PCODE;
import static alankstewart.toilets.model.Toilet.KEY_STATE;
import static alankstewart.toilets.model.Toilet.KEY_TOWN;
import static alankstewart.toilets.util.Proximity.RADIUS_OF_EARTH;
import static java.lang.String.valueOf;
import static java.util.stream.Collectors.toList;

/**
 * Created by alanstewart on 15/02/15.
 */
public final class MongoDBToiletStoreImpl extends AbstractToiletStoreImpl {

    private final MongoCollection<Document> collection;
    private final List<Toilet> toilets = new ArrayList<>();

    public MongoDBToiletStoreImpl() {
        MongoClient mongoClient = new MongoClient();
        collection = mongoClient.getDatabase("toiletdb").getCollection("toilets");
        collection.createIndex(new BasicDBObject(KEY_LOC, "2dsphere"));
    }

    @Override
    public List<Toilet> search(ToiletQuery query) {
        Location location = query.getLocation();
        return StreamSupport.stream(collection.find((Bson) QueryBuilder.start(KEY_LOC)
                .nearSphere(location.getLongitude(), location.getLatitude(), 5 / RADIUS_OF_EARTH)
                .get()).limit(query.getLimit()).map(this::createToilet).spliterator(), false)
                .collect(toList());
    }

    @Override
    protected long storeToilets(InputStream toiletXml) {
        collection.deleteMany(new Document());
        readToiletXml(toiletXml);
        if (!toilets.isEmpty()) {
            addToiletsToCollection();
        }
        return collection.count();
    }

    @Override
    protected void addToilet(Toilet toilet) {
        if (toilets.size() == 1000) {
            addToiletsToCollection();
        }
        toilets.add(toilet);
    }

    private void addToiletsToCollection() {
        collection.insertMany(toilets.stream().map(toilet -> {
            Location location = toilet.getLocation();
            List<Double> coordinates = Arrays.asList(location.getLongitude(), location.getLatitude());
            return new Document(KEY_NAME, toilet.getName())
                    .append(KEY_ADDR, toilet.getAddress1())
                    .append(KEY_TOWN, toilet.getTown())
                    .append(KEY_STATE, toilet.getState())
                    .append(KEY_PCODE, toilet.getPostcode())
                    .append(KEY_NOTE, toilet.getAddressNote())
                    .append(KEY_ICON, toilet.getIconUrl())
                    .append(KEY_LOC, new Document("type", "Point").append("coordinates", coordinates));
        }).collect(toList()));
        toilets.clear();
    }

    @SuppressWarnings("unchecked")
    private Toilet createToilet(Document document) {
        Document locationDocument = (Document) document.get(KEY_LOC);
        List<Double> coordinates = (List<Double>) locationDocument.get("coordinates");
        return new Toilet.Builder()
                .setName(valueOf(document.get(KEY_NAME)))
                .setAddress1(valueOf(document.get(KEY_ADDR)))
                .setTown(valueOf(document.get(KEY_TOWN)))
                .setState(valueOf(document.get(KEY_STATE)))
                .setPostcode(valueOf(document.get(KEY_PCODE)))
                .setAddressNote(valueOf(document.get(KEY_NOTE)))
                .setIconUrl(valueOf(document.get(KEY_ICON)))
                .setLocation(new Location(coordinates.get(1), coordinates.get(0)))
                .build();
    }
}
