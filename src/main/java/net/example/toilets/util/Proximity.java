package net.example.toilets.util;

import net.example.toilets.model.Distance;
import net.example.toilets.model.Location;

import static java.lang.Math.toRadians;

public final class Proximity {

    // Optimised for Australia
    private static final double RADIUS_OF_EARTH = 6364.963;

    private Proximity() {
    }

    /**
     * Calculates the distance between two points.
     * This calculation is merely an approximation, due to the way it model the earth's surface.
     * More accurate calculations are possible (e.g. by using a geodetic model of the earth),
     * but this is sufficient for our needs.
     */
    public static Distance distanceBetween(final Location pt1, final Location pt2) {
        final double lat1 = toRadians(pt1.getLatitude());
        final double lat2 = toRadians(pt2.getLatitude());
        final double lon1 = toRadians(pt1.getLongitude());
        final double lon2 = toRadians(pt2.getLongitude());

        double x = (lon2 - lon1) * Math.cos((lat1 + lat2) / 2);
        double y = (lat2 - lat1);
        double d = Math.sqrt(x * x + y * y);

        return Distance.kilometres(Math.abs(d * RADIUS_OF_EARTH));
    }
}
