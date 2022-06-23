import com.fasterxml.jackson.annotation.JsonSetter;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

public class WeatherBot extends TelegramLongPollingBot implements IUpdater {
    private String token;
    private String username;
    private WeatherGateway gate;
    private HashMap<String, String> userStateMap;
    private HashMap<String, ArrayList<City>> userSubscriptionList;
    SubscriptionHandler subHandler;
    public WeatherBot(){
        userStateMap = new HashMap<>();
        userSubscriptionList = new HashMap<>();

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
        SendMessage message = new SendMessage();
        if(update.hasCallbackQuery()){
            String callback_username = update.getCallbackQuery().getFrom().getUserName();
            message.setChatId(String.valueOf(update.getCallbackQuery().getMessage().getChatId()));
            if(userStateMap.get(callback_username) == "unsub"){

                message.setText(subHandler.handleUnSubscription(update));


            }
            if(userStateMap.get(callback_username) == "sub"){
                message.setText(subHandler.handleSubscription(update));
            }
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            return;
        }

        String chat_id = String.valueOf(update.getMessage().getChatId());
        message.setChatId(String.valueOf(chat_id));
        if(update.getMessage().getLocation() != null){
                City city = new City();
                try {
                    city.setLat(update.getMessage().getLocation().getLatitude());
                    city.setLon(update.getMessage().getLocation().getLongitude());
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                message.setText(gate.getWeatherByCity(city));
                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                return;
        }
        if (update.hasMessage() && update.getMessage().hasText()) {
            String message_text = update.getMessage().getText();

            String sender_username = update.getMessage().getFrom().getUserName();
            if(!userStateMap.containsKey(sender_username)){
                userStateMap.put(sender_username,"default");
            }



            if(message_text.startsWith("/me")) {

            }
            if(message_text.startsWith("/subscribe")) {
                userStateMap.put(sender_username, "sub");
                message = subHandler.createSubMarkup(update);
            }
            if(message_text.startsWith("/unsubscribe")) {
                message = subHandler.createUnsubMarkUp(update);
            }
            if(message_text.startsWith("/start")){
                String ans = new String(String.format("Добро пожаловать в погодный бот, %s.\nЧтобы узнать погоду, введите название, интерсуещего вас, города", sender_username).getBytes(), StandardCharsets.UTF_8);
                message.setText(ans);
            }
            if(!message_text.startsWith("/")){
                if(userStateMap.get(sender_username) == "default"){

                        message.setText(getWeather(message_text));
                }
                if(userStateMap.get(sender_username) == "sub"){
                    message.setText(subHandler.handleSubscription(update));
                }
                if(userStateMap.get(sender_username) == "unsub"){
                    message.setText(subHandler.handleUnSubscription(update));
                }
            }
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    private String getWeather(String city_name){
        City city =  gate.getCityByName(city_name);
        String res = "";
        if (city == null){
            res = new String( "Город не найден".getBytes(), StandardCharsets.UTF_8);
            return res;
        }
        res = gate.getWeatherByCity(city);
        return res;
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
        subHandler = new SubscriptionHandler(userStateMap, userSubscriptionList, gate);
    }

    @Override
    public void update() throws TelegramApiException {

        for (String chat_id: userSubscriptionList.keySet()
             ) {
            for (City city: userSubscriptionList.get(chat_id)
                 ) {
                String forecast = gate.getWeatherByCity(city);
                SendMessage message = new SendMessage();
                message.setChatId(chat_id);
                message.setText(forecast);
                execute(message);
            }

        }

    }
}
