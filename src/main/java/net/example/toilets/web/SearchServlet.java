package net.example.toilets.web;

import com.google.gson.Gson;
import net.example.toilets.model.Location;
import net.example.toilets.model.Toilet;
import net.example.toilets.store.ToiletQuery;
import net.example.toilets.store.ToiletStore;
import net.example.toilets.store.ToiletStoreImpl;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

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

    private static final String XML_CP = "/WEB-INF/classes/toilets.xml";

    private static final String LATITUDE = "lat";
    private static final String LONGITUDE = "lng";

    private ToiletStore store;

    @Override
    public void init() throws ServletException {
        InputStream xml = getServletContext().getResourceAsStream(XML_CP);
        if (xml == null) {
            throw new ServletException("Cannot find '" + XML_CP + "'");
        }

        LocalDateTime start = now();
        store = new ToiletStoreImpl();
        store.initialise(xml);
        log("Store initialised in " + between(start, now()).toMillis() + "ms");
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

        Gson gson = new Gson();
        resp.getOutputStream().print(gson.toJson(results));
        resp.getOutputStream().flush();
    }

    private double getAsDouble(String str) throws ServletException {
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            throw new ServletException("Failed to parse string as double '" + str + "'");
        }
    }
}
