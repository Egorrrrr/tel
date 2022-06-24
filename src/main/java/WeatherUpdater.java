import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class WeatherUpdater implements Runnable {
    private IUpdater updater;
    private UserStorage userStorage;
    public WeatherUpdater(IUpdater updater, UserStorage userStorage){
        this.updater = updater;
        this.userStorage = userStorage;
    }
    @Override
    public void run() {
        while(true){
            try {
                updater.update();
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(1000 * 60);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
