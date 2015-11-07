package alankstewart.toilets.model;

/**
 * Created by alanstewart on 8/11/2015.
 */
public final class ToiletBuilder {

    private String name;
    private String address1;
    private String town;
    private String state;
    private String postcode;
    private String addressNote;
    private Location location;
    private String iconUrl;

    public ToiletBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public ToiletBuilder setAddress1(String address1) {
        this.address1 = address1;
        return this;
    }

    public ToiletBuilder setTown(String town) {
        this.town = town;
        return this;
    }

    public ToiletBuilder setState(String state) {
        this.state = state;
        return this;
    }

    public ToiletBuilder setPostcode(String postcode) {
        this.postcode = postcode;
        return this;
    }

    public ToiletBuilder setAddressNote(String addressNote) {
        this.addressNote = addressNote;
        return this;
    }

    public ToiletBuilder setLocation(Location location) {
        this.location = location;
        return this;
    }

    public ToiletBuilder setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
        return this;
    }

    public Toilet build() {
        return new Toilet(name, address1, town, state, postcode, addressNote, location, iconUrl);
    }
}
