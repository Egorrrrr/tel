import com.fasterxml.jackson.databind.ObjectMapper;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.IOException;
import java.nio.file.Paths;

public class main {
    public static void main(String args[]) throws IOException, TelegramApiException {
        ObjectMapper mapper = new ObjectMapper();
        WeatherBot bot = mapper.readValue(Paths.get("cred.json").toFile(), WeatherBot.class);
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(bot);

    }
}
