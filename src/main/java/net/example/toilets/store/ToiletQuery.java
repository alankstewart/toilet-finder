package net.example.toilets.store;

import net.example.toilets.model.Location;

public class ToiletQuery {

    private final Location location;
    private final int limit;

    public ToiletQuery(final Location location, final int limit) {
        this.location = location;
        this.limit = limit;
    }

    public Location getLocation() {
        return location;
    }

    public int getLimit() {
        return limit;
    }

    @Override
    public String toString() {
        return String.format("%s{location=%s,limit=%d}", getClass().getSimpleName(), location, limit);
    }
}
