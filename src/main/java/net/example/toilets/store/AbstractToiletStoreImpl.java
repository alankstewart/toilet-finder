package net.example.toilets.store;

import net.example.toilets.model.Location;
import net.example.toilets.model.Toilet;
import net.example.toilets.model.ToiletBuilder;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;

import static java.lang.Double.parseDouble;

/**
 * Created by alanstewart on 18/02/15.
 */
public abstract class AbstractToiletStoreImpl implements ToiletStore {

    protected void readToiletXml(InputStream toiletXml) throws XMLStreamException {
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
                            add(toiletBuilder.createToilet());
                            break;
                    }
                    break;
            }
        }
    }

    protected abstract void add(Toilet toilet);
}
