import com.fasterxml.jackson.annotation.JsonSetter;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.lang.invoke.SwitchPoint;
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

        UserEvent event = new UserEvent(update);

        String sender_username = event.getName();
        String chat_id = event.getChat_id();
        String message_text = event.getMessage();
        MessageType type = event.getType();

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chat_id));

        if(!userStateMap.containsKey(sender_username)){
            userStateMap.put(sender_username,"default");
        }

        switch (type){
            case CALLBACK:
                AnswerCallbackQuery ans_callback = new AnswerCallbackQuery();
                ans_callback.setCallbackQueryId(update.getCallbackQuery().getId());


                if(userStateMap.get(sender_username) == "default"){
                    return;
                }
                if(userStateMap.get(sender_username) == "unsub"){
                    message.setText(subHandler.handleUnSubscription(update));
                }
                if(userStateMap.get(sender_username) == "sub"){
                    message.setText(subHandler.handleSubscription(update));
                }
                try {
                    execute(ans_callback);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                break;
            case LOCATION:
                City city = new City();
                try {
                    city.setLat(event.getLat());
                    city.setLon(event.getLon());
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                message.setText(gate.getWeatherByCity(city));
                break;
            case COMMAND:
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
                break;
            case TEXT:
                if(userStateMap.get(sender_username) == "default"){

                    message.setText(getWeather(message_text));
                }
                if(userStateMap.get(sender_username) == "sub"){
                    message.setText(subHandler.handleSubscription(update));
                }
                if(userStateMap.get(sender_username) == "unsub"){
                    message.setText(subHandler.handleUnSubscription(update));
                }
            break;
        }
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
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
