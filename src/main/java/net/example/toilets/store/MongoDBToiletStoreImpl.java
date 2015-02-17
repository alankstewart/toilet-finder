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

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.List;

import static java.lang.Double.parseDouble;
import static java.lang.String.valueOf;
import static java.util.stream.Collectors.toList;
import static net.example.toilets.util.Proximity.RADIUS_OF_EARTH;

/**
 * Created by alanstewart on 15/02/15.
 */
public final class MongoDBToiletStoreImpl implements ToiletStore {

    private DBCollection coll;

    @Override
    public List<Toilet> search(ToiletQuery query) {
        Location location = query.getLocation();
        DBObject filter = new QueryBuilder().start("location")
                .nearSphere(location.getLongitude(), location.getLatitude(), 5 / RADIUS_OF_EARTH).get();
        return coll.find(filter)
                .limit(query.getLimit()).toArray()
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
            readToilets(toiletXml);
        } catch (UnknownHostException | XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    private void readToilets(InputStream toiletXml) throws XMLStreamException {
        BasicDBObject doc = null;

        String tagContent = null;
        XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
        XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(toiletXml);
        while (xmlStreamReader.hasNext()) {
            int event = xmlStreamReader.next();
            switch (event) {
                case XMLStreamConstants.START_ELEMENT:
                    switch (xmlStreamReader.getLocalName()) {
                        case "ToiletDetails":
                            BasicDBList coordinates = new BasicDBList();
                            coordinates.put(0, parseDouble(xmlStreamReader.getAttributeValue(null, "Longitude")));
                            coordinates.put(1, parseDouble(xmlStreamReader.getAttributeValue(null, "Latitude")));
                            doc = new BasicDBObject("location", new BasicDBObject("type", "Point").append("coordinates", coordinates));
                            break;
                        case "Icon":
                            doc.append("addressNote", xmlStreamReader.getAttributeValue(null, "IconAltText"));
                            break;
                    }
                    break;
                case XMLStreamConstants.CHARACTERS:
                    tagContent = xmlStreamReader.getText();
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    switch (xmlStreamReader.getLocalName()) {
                        case "Name":
                            doc.append("name", tagContent);
                            break;
                        case "Address1":
                            doc.append("address1", tagContent);
                            break;
                        case "Town":
                            doc.append("town", tagContent);
                            break;
                        case "State":
                            doc.append("state", tagContent);
                            break;
                        case "Postcode":
                            doc.append("postcode", tagContent);
                            break;
                        case "IconURL":
                            doc.append("iconUrl", tagContent);
                            break;
                        case "ToiletDetails":
                            coll.insert(doc);
                            break;
                    }
                    break;
            }
        }
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
