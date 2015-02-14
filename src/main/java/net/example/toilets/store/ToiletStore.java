package net.example.toilets.store;

import net.example.toilets.model.Toilet;

import java.io.InputStream;
import java.util.List;

/**
 * A searchable store of Toilets.
 */
public interface ToiletStore {

    /**
     * Search for Toilets.
     *
     * @param query The query to use
     * @return A {@link List} of {@link Toilet} objects. Never null, but may be empty
     */
    List<Toilet> search(ToiletQuery query);

    /**
     * Called by the Servlet on initialisation.
     *
     * @param toiletXml An InputStream of Toilet XML
     */
    void initialise(InputStream toiletXml);
}
