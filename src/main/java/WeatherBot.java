import com.fasterxml.jackson.annotation.JsonSetter;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class WeatherBot extends TelegramLongPollingBot {
    private String token;
    private String username;
    private WeatherGateway gate;
    public WeatherBot(){

    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {

            String message_text = update.getMessage().getText();
            long chat_id = update.getMessage().getChatId();
            double[] cord =  gate.getLatLonByCityName(message_text);
            String res = gate.getWeatherByLatLong(cord[0], cord[1]);
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chat_id));
            message.setText(res);

            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }
    @JsonSetter("token")
    public void setToken(String token) {
        this.token = token;
    }

    @JsonSetter("username")
    public void setUsername(String username) {
        this.username = username;
    }

    public WeatherGateway getGate() {
        return gate;
    }

    public void setGate(WeatherGateway gate) {
        this.gate = gate;
    }
}
