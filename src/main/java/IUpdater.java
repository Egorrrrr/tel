import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface IUpdater {
    void update() throws TelegramApiException;
}
