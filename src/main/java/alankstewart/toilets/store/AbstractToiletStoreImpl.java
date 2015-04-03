package alankstewart.toilets.store;

import alankstewart.toilets.model.Location;
import alankstewart.toilets.model.Toilet;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.logging.Logger;

import static java.lang.Double.parseDouble;
import static java.time.LocalDateTime.now;

/**
 * Created by alanstewart on 18/02/15.
 */
abstract class AbstractToiletStoreImpl implements ToiletStore {

    private static final Logger LOGGER = Logger.getLogger(AbstractToiletStoreImpl.class.getName());

    @Override
    public void initialise(InputStream toiletXml) {
        LocalDateTime start = now();
        LOGGER.info("Initialized toilet store with " + storeToilets(toiletXml) + " toilets in " +
                Duration.between(start, now()).toMillis() + " ms");
    }

    final void readToiletXml(InputStream toiletXml) {
        Toilet.Builder builder = new Toilet.Builder();
        String tagContent = null;
        XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
        try {
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
                                addToilet(builder.build());
                                break;
                        }
                        tagContent = "";
                        break;
                }
            }
            xmlStreamReader.close();
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract long storeToilets(InputStream toiletXml);

    protected abstract void addToilet(Toilet toilet);
}
