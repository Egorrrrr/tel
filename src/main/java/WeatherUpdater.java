import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class WeatherUpdater implements Runnable {
    private IUpdater updater;
    private UserStorage userStorage;
    public WeatherUpdater(IUpdater updater, UserStorage userStorage){
        this.updater = updater;
        this.userStorage = userStorage;
    }
    @Override
    public void run() {
        ConcurrentHashMap<String, Subscription> map = userStorage.getUserSubscriptionList();
        while(true){
            try {
                for (String user: map.keySet()
                     ) {
                    Subscription sub = map.get(user);
                    int y = LocalTime.now().getHour();
                    if(sub.getCities().isEmpty()){
                        break;
                    }
                    if(sub.getTime()== LocalTime.now().getHour() && !sub.isSent()){
                        updater.update(user, sub.getCities());
                        sub.setSent(true);
                    }
                    else if(sub.getTime() != LocalTime.now().getHour() && sub.isSent()){
                        sub.setSent(false);
                    }
                }
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(1000 * 5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
