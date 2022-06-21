import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class WeatherUpdater implements Runnable {
    IUpdater updater;
    public WeatherUpdater(IUpdater updater){
        this.updater = updater;

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
