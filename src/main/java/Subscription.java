import java.util.ArrayList;
import java.util.List;

public class Subscription {
    private int time;
    private List<City> cities;
    private boolean sent;

    public Subscription(){
        cities = new ArrayList<>();
        sent = false;
    }
    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public List<City> getCities() {
        return cities;
    }

    public void setCities(List<City> cities) {
        this.cities = cities;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }
}
