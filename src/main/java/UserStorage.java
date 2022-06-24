import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class UserStorage {
    private ConcurrentHashMap<String, ArrayList<City>> userSubscriptionList;

    public UserStorage(ConcurrentHashMap<String, String> userStateMap, ConcurrentHashMap<String, ArrayList<City>> userSubscriptionList) {
        this.userSubscriptionList = userSubscriptionList;
    }
    public UserStorage(){
        userSubscriptionList = new ConcurrentHashMap<>();
    }

    public ConcurrentHashMap<String, ArrayList<City>> getUserSubscriptionList() {
        return userSubscriptionList;
    }

    public void setUserSubscriptionList(ConcurrentHashMap<String, ArrayList<City>> userSubscriptionList) {
        this.userSubscriptionList = userSubscriptionList;
    }
}
