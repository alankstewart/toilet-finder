package net.example.toilets.web;

import net.example.toilets.model.Location;
import net.example.toilets.model.Toilet;
import net.example.toilets.store.MongoToiletStoreImpl;
import net.example.toilets.store.ToiletQuery;
import net.example.toilets.store.ToiletStore;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collector;

import static java.time.Duration.between;
import static java.time.LocalDateTime.now;

/**
 * A Servlet to search for Toilets.
 * <p>
 * Currently wired up to respond to GET requests with the following parameters:
 * <ul>
 * <li><code>lat</code>: The latitude of the search centre</li>
 * <li><code>lng</code>: The longitude of the search centre</li>
 * </ul>
 */
public class SearchServlet extends HttpServlet {

    private static final String XML_CP = "/toilets.xml";
    private static final String LATITUDE = "lat";
    private static final String LONGITUDE = "lng";

    private ToiletStore store;

    @Override
    public void init() throws ServletException {
        InputStream xml = getClass().getResourceAsStream(XML_CP);
        if (xml == null) {
            throw new ServletException("Cannot find '" + XML_CP + "'");
        }

        LocalDateTime start = now();
        store = new MongoToiletStoreImpl();
        store.initialise(xml);
        log("Toilet store initialised in " + between(start, now()).toMillis() + " ms");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String latitude = req.getParameter(LATITUDE);
        String longitude = req.getParameter(LONGITUDE);
        if (latitude == null || latitude.isEmpty() || longitude == null || longitude.isEmpty()) {
            return;
        }

        Location location = new Location(getAsDouble(latitude), getAsDouble(longitude));
        ToiletQuery query = new ToiletQuery(location, 10);
        LocalDateTime start = now();
        List<Toilet> results = store.search(query);
        log("Search: " + query + " took " + between(start, now()).toMillis() + "ms");
        writeJson(resp.getOutputStream(), results);
        resp.getOutputStream().flush();
    }

    private double getAsDouble(String str) throws ServletException {
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            throw new ServletException("Failed to parse string as double '" + str + "'");
        }
    }

    private void writeJson(OutputStream outputStream, List<Toilet> results) {
        Json.createWriter(outputStream).write(results.stream().map(Toilet::getJsonStructure)
                .collect(Collector.of(Json::createArrayBuilder, JsonArrayBuilder::add, (left, right) -> {
                    left.add(right);
                    return left;
                })).build());
    }
}
