package alankstewart.toilets.model;

import javax.json.Json;
import javax.json.JsonStructure;

public class Toilet {

    public static final String KEY_NAME = "name";
    public static final String KEY_ADDR = "address1";
    public static final String KEY_TOWN = "town";
    public static final String KEY_STATE = "state";
    public static final String KEY_PCODE = "postcode";
    public static final String KEY_NOTE = "addressNote";
    public static final String KEY_ICON = "iconUrl";
    public static final String KEY_LOC = "location";

    private final String name;
    private final String address1;
    private final String town;
    private final String state;
    private final String postcode;
    private final String addressNote;
    private final String iconUrl;
    private final Location location;

    private Toilet(String name, String address1, String town, String state, String postcode, String addressNote, Location location, String iconUrl) {
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

    public JsonStructure toJson() {
        return Json.createObjectBuilder()
                .add(KEY_NAME, name)
                .add(KEY_ADDR, address1)
                .add(KEY_TOWN, town)
                .add(KEY_STATE, state)
                .add(KEY_PCODE, postcode)
                .add(KEY_NOTE, addressNote)
                .add(KEY_ICON, iconUrl)
                .add(KEY_LOC, Json.createObjectBuilder()
                        .add("type", "Point")
                        .add("coordinates", Json.createArrayBuilder()
                                .add(location.getLongitude())
                                .add(location.getLatitude())))
                .build();
    }

    @Override
    public String toString() {
        return String.format("%s{name=%s,address1=%s,town=%s,state=%s,postcode=%s,addressNote=%s,iconUrl=%s,location=%s}",
                getClass().getSimpleName(), name, address1, town, state, postcode, addressNote, iconUrl, location);
    }

    public static class Builder {

        private String name;
        private String address1;
        private String town;
        private String state;
        private String postcode;
        private String addressNote;
        private Location location;
        private String iconUrl;

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setAddress1(String address1) {
            this.address1 = address1;
            return this;
        }

        public Builder setTown(String town) {
            this.town = town;
            return this;
        }

        public Builder setState(String state) {
            this.state = state;
            return this;
        }

        public Builder setPostcode(String postcode) {
            this.postcode = postcode;
            return this;
        }

        public Builder setAddressNote(String addressNote) {
            this.addressNote = addressNote;
            return this;
        }

        public Builder setLocation(Location location) {
            this.location = location;
            return this;
        }

        public Builder setIconUrl(String iconUrl) {
            this.iconUrl = iconUrl;
            return this;
        }

        public Toilet build() {
            return new Toilet(name, address1, town, state, postcode, addressNote, location, iconUrl);
        }
    }
}
