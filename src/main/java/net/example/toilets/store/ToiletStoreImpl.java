package net.example.toilets.store;

import net.example.toilets.model.Distance;
import net.example.toilets.model.Location;
import net.example.toilets.model.Toilet;
import net.example.toilets.model.ToiletBuilder;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
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
                .filter(t -> distanceBetween(t.getLocation(), location).compareTo(Distance.metres(5000)) <= 0)
                .sorted((t1, t2) -> distanceBetween(t1.getLocation(), location).compareTo(distanceBetween(t2.getLocation(), location)))
                .limit(query.getLimit())
                .collect(toList());
    }

    @Override
    public void initialise(InputStream toiletXml) {
        try {
            toilets.clear();
            toilets.addAll(readToiletsFromToiletXml(toiletXml));
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Toilet> readToiletsFromToiletXml(InputStream toiletXml) throws XMLStreamException {
        List<Toilet> toilets = new ArrayList<>();
        ToiletBuilder toiletBuilder = null;
        XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
        XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(toiletXml);
        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();
            if (xmlEvent.isStartElement()) {
                StartElement startElement = xmlEvent.asStartElement();
                switch (startElement.getName().getLocalPart()) {
                    case "ToiletDetails":
                        toiletBuilder = new ToiletBuilder().setLocation(new Location(
                                parseDouble(getAttributeValue(startElement, "Latitude")),
                                parseDouble(getAttributeValue(startElement, "Longitude"))));
                        break;
                    case "Name":
                        toiletBuilder.setName(readCharacters(xmlEventReader.nextEvent()));
                        break;
                    case "Address1":
                        toiletBuilder.setAddress1(readCharacters(xmlEventReader.nextEvent()));
                        break;
                    case "Town":
                        toiletBuilder.setTown(readCharacters(xmlEventReader.nextEvent()));
                        break;
                    case "State":
                        toiletBuilder.setState(readCharacters(xmlEventReader.nextEvent()));
                        break;
                    case "Postcode":
                        toiletBuilder.setPostcode(readCharacters(xmlEventReader.nextEvent()));
                        break;
                    case "IconURL":
                        toiletBuilder.setIconUrl(readCharacters(xmlEventReader.nextEvent()));
                        break;
                    case "Icon":
                        toiletBuilder.setAddressNote(getAttributeValue(startElement, "IconAltText"));
                        break;
                }
            }
            if (xmlEvent.isEndElement() && xmlEvent.asEndElement().getName().getLocalPart().equals("ToiletDetails")) {
                toilets.add(toiletBuilder.createToilet());
            }
        }
        return toilets;
    }

    private String getAttributeValue(StartElement startElement, String attributeName) {
        return startElement.getAttributeByName(new QName(attributeName)).getValue();
    }

    private String readCharacters(XMLEvent xmlEvent) throws XMLStreamException {
        return xmlEvent.getEventType() == XMLStreamReader.CHARACTERS ? xmlEvent.asCharacters().getData() : "";
    }
}
