package net.example.toilets.model;

public class Toilet {

    private final String name;
    private final String address1;
    private final String town;
    private final String state;
    private final String postcode;
    private final String addressNote;
    private final String iconUrl;
    private final Location location;

    public Toilet(String name, String address1, String town, String state, String postcode, String addressNote, Location location, String iconUrl) {
        this.name = name;
        this.address1 = address1;
        this.town = town;
        this.state = state;
        this.postcode = postcode;
        this.addressNote = addressNote;
        this.location = location;
        this.iconUrl = iconUrl;
    }

    public String getName() {
        return name;
    }

    public String getAddress1() {
        return address1;
    }

    public String getTown() {
        return town;
    }

    public String getState() {
        return state;
    }

    public String getPostcode() {
        return postcode;
    }

    public String getAddressNote() {
        return addressNote;
    }

    public Location getLocation() {
        return location;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    @Override
    public String toString() {
        return String.format("%s{name=%s,address1=%s,town=%s,state=%s,postcode=%s,addressNote=%s,iconUrl=%s,location=%s}",
                getClass().getSimpleName(), name, address1, town, state, postcode, addressNote, iconUrl, location);
    }
}
