package net.example.toilets.store;

import net.example.toilets.model.Location;
import net.example.toilets.model.Toilet;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;

import static java.lang.Double.parseDouble;

/**
 * Created by alanstewart on 18/02/15.
 */
abstract class AbstractToiletStoreImpl implements ToiletStore {

    protected void readToiletXml(InputStream toiletXml) throws XMLStreamException {
        Toilet.Builder builder = new Toilet.Builder();
        String tagContent = null;
        XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
        XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(toiletXml);
        while (xmlStreamReader.hasNext()) {
            int event = xmlStreamReader.next();
            switch (event) {
                case XMLStreamConstants.START_ELEMENT:
                    switch (xmlStreamReader.getLocalName()) {
                        case "ToiletDetails":
                            builder = new Toilet.Builder().setLocation(new Location(
                                    parseDouble(xmlStreamReader.getAttributeValue(null, "Latitude")),
                                    parseDouble(xmlStreamReader.getAttributeValue(null, "Longitude"))));
                            break;
                        case "Icon":
                            builder.setAddressNote(xmlStreamReader.getAttributeValue(null, "IconAltText"));
                            break;
                    }
                    break;
                case XMLStreamConstants.CHARACTERS:
                    tagContent = xmlStreamReader.getText().trim();
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    switch (xmlStreamReader.getLocalName()) {
                        case "Name":
                            builder.setName(tagContent);
                            break;
                        case "Address1":
                            builder.setAddress1(tagContent);
                            break;
                        case "Town":
                            builder.setTown(tagContent);
                            break;
                        case "State":
                            builder.setState(tagContent);
                            break;
                        case "Postcode":
                            builder.setPostcode(tagContent);
                            break;
                        case "IconURL":
                            builder.setIconUrl(tagContent);
                            break;
                        case "ToiletDetails":
                            add(builder.build());
                            break;
                    }
                    tagContent = "";
                    break;
            }
        }
        xmlStreamReader.close();
    }

    protected abstract void add(Toilet toilet);
}
