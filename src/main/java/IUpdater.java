import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

public interface IUpdater {
    void update(String user, List<City> cities) throws TelegramApiException;
}
