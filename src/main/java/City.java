import com.fasterxml.jackson.annotation.JsonSetter;

public class City {
    private String name;
    private String state;
    private String country;
    private double lat;
    private double lon;

    public City() {
    }

    public String getName() {
        return name;
    }

    @JsonSetter("name")
    public void setName(String name) {
        this.name = name;
    }

    public String getState() {
        return state;
    }

    @JsonSetter("state")
    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    @JsonSetter("country")
    public void setCountry(String country) {
        this.country = country;
    }

    public double getLat() {
        return lat;
    }

    @JsonSetter("lat")
    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    @JsonSetter("lon")
    public void setLon(double lon) {
        this.lon = lon;
    }
}
