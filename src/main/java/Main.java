import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.IOException;
import java.nio.file.Paths;

public class Main {
    public static void main(String args[]) throws IOException, TelegramApiException {
        ObjectMapper mapper = new ObjectMapper();
        WeatherGateway gate = mapper.readValue(Paths.get("cfg/openw.json").toFile(), WeatherGateway.class);
        WeatherBot bot = mapper.readValue(Paths.get("cfg/cred.json").toFile(), WeatherBot.class);
        WeatherUpdater updater = new WeatherUpdater(bot);
        bot.setGate(gate);
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(bot);
        updater.run();


    }
}
