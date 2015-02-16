package net.example.toilets.store;

import net.example.toilets.model.Distance;
import net.example.toilets.model.Location;
import net.example.toilets.model.Toilet;
import net.example.toilets.model.ToiletBuilder;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Double.parseDouble;
import static java.util.stream.Collectors.toList;
import static net.example.toilets.util.Proximity.distanceBetween;

/**
 * Created by alanstewart on 6/02/15.
 */
public final class ToiletStoreImpl implements ToiletStore {

    private List<Toilet> toilets = new ArrayList<>();

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
        try {
            toilets.clear();
            readToilets(toiletXml);
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    private void readToilets(InputStream toiletXml) throws XMLStreamException {
        ToiletBuilder toiletBuilder = null;

        String tagContent = null;
        XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
        XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(toiletXml);
        while (xmlStreamReader.hasNext()) {
            int event = xmlStreamReader.next();
            switch (event) {
                case XMLStreamConstants.START_ELEMENT:
                    switch (xmlStreamReader.getLocalName()) {
                        case "ToiletDetails":
                            toiletBuilder = new ToiletBuilder().setLocation(new Location(
                                    parseDouble(xmlStreamReader.getAttributeValue(null, "Latitude")),
                                    parseDouble(xmlStreamReader.getAttributeValue(null, "Longitude"))));
                            break;
                        case "Icon":
                            toiletBuilder.setAddressNote(xmlStreamReader.getAttributeValue(null, "IconAltText"));
                            break;
                    }
                    break;
                case XMLStreamConstants.CHARACTERS:
                    tagContent = xmlStreamReader.getText();
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    switch (xmlStreamReader.getLocalName()) {
                        case "Name":
                            toiletBuilder.setName(tagContent);
                            break;
                        case "Address1":
                            toiletBuilder.setAddress1(tagContent);
                            break;
                        case "Town":
                            toiletBuilder.setTown(tagContent);
                            break;
                        case "State":
                            toiletBuilder.setState(tagContent);
                            break;
                        case "Postcode":
                            toiletBuilder.setPostcode(tagContent);
                            break;
                        case "IconURL":
                            toiletBuilder.setIconUrl(tagContent);
                            break;
                        case "ToiletDetails":
                            toilets.add(toiletBuilder.createToilet());
                            break;
                    }
                    break;
            }
        }
    }
}
