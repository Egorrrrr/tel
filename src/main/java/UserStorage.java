import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class UserStorage {
    private ConcurrentHashMap<String, Subscription> userSubscriptionList;

    public UserStorage(ConcurrentHashMap<String, String> userStateMap, ConcurrentHashMap<String, Subscription> userSubscriptionList) {
        this.userSubscriptionList = userSubscriptionList;
    }
    public UserStorage(){
        userSubscriptionList = new ConcurrentHashMap<>();
    }

    public ConcurrentHashMap<String, Subscription> getUserSubscriptionList() {
        return userSubscriptionList;
    }

    public void setUserSubscriptionList(ConcurrentHashMap<String, Subscription> userSubscriptionList) {
        this.userSubscriptionList = userSubscriptionList;
    }
}
