import com.fasterxml.jackson.annotation.JsonSetter;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.lang.invoke.SwitchPoint;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class WeatherBot extends TelegramLongPollingBot implements IUpdater {
    private String token;
    private String username;
    private WeatherGateway gate;

    private HashMap<String, String> userStateMap;
    private ConcurrentHashMap<String, Subscription> userSubscriptionList;
    private SubscriptionHandler subHandler;

    public void setUserStorage(UserStorage userStorage) {
        userSubscriptionList = userStorage.getUserSubscriptionList();
    }

    public WeatherBot(){
        userStateMap = new HashMap<>();
        userSubscriptionList = new ConcurrentHashMap<>();
    }

    public WeatherBot(UserStorage userStorage){
        userStateMap = new HashMap<>();

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

        String user_state = "";
        if(!userStateMap.containsKey(sender_username)){
            userStateMap.put(sender_username,"default");
            user_state = "default";
        }
        else {
            user_state = userStateMap.get(sender_username);
        }

        switch (type){
            case CALLBACK:
                AnswerCallbackQuery ans_callback = new AnswerCallbackQuery();
                ans_callback.setCallbackQueryId(update.getCallbackQuery().getId());
                try {
                    execute(ans_callback);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                DeleteMessage del = new DeleteMessage();
                del.setChatId(event.getChat_id());
                del.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
                if(user_state.equals("default")){
                    return;
                }
                if(user_state.equals("unsub")){
                    message.setText(subHandler.handleUnSubscription(update));
                }
                else
                if(user_state.equals("sub")){
                    message.setText(subHandler.handleSubscription(update));
                }
                if(user_state.equals("time")){
                    if(message_text.equals(">") || message_text.equals("<")){
                        EditMessageReplyMarkup edit = subHandler.changeTimeMarkup(update);
                        try {
                            if(edit!=null){
                                execute(edit);
                            }
                            return;
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }
                    else if(message_text.equals("exit") ){
                        userStateMap.put(sender_username, "default");
                        message.setText(new String("???? ?????????? ???? ???????????? ?????????????????? ??????????????".getBytes(),StandardCharsets.UTF_8));
                    }
                    else {
                        message.setText(subHandler.SetSubTime(chat_id, Integer.parseInt(message_text)));
                        userStateMap.put(sender_username, "default");
                    }

                }
                try {
                    execute(del);
                    if(message.getText() != null){
                        execute(message);
                        return;
                    }
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                break;
            case LOCATION:

                City city;
                if(user_state == "sub"){
                    city = gate.getCityByCord(event.getLat(), event.getLon());
                    message.setText(subHandler.handleSubscription(update,city));
                }
                else{
                    city = new City();
                    city.setLat(event.getLat());
                    city.setLon(event.getLon());
                    message.setText(gate.getWeatherByCity(city));
                }
                break;
            case COMMAND:
                if(message_text.startsWith("/subscribe")) {
                    userStateMap.put(sender_username, "sub");
                    message = subHandler.createSubMarkup(update);
                }
                else
                if(message_text.startsWith("/unsubscribe")) {
                    message = subHandler.createUnsubMarkUp(update);
                }
                else
                if(message_text.startsWith("/start")){
                    String ans = new String(String.format("?????????? ???????????????????? ?? ???????????????? ??????, %s.\n?????????? ???????????? ????????????, ?????????????? ????????????????, ???????????????????????? ??????, ????????????", sender_username).getBytes(), StandardCharsets.UTF_8);
                    message.setText(ans);
                }
                else
                if(message_text.startsWith("/settime")){
                    if(user_state == "default"){
                        message = subHandler.createTimeMarkup(update);
                    }
                }
                break;
            case TEXT:
                if(user_state == "default"){

                    message.setText(getWeather(message_text));
                }
                else
                if(user_state == "sub"){
                    message.setText(subHandler.handleSubscription(update));
                }
                else
                if(user_state == "unsub"){
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
            res = new String( "?????????? ???? ????????????".getBytes(), StandardCharsets.UTF_8);
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
    public void update(String chat_id, List<City> cities) throws TelegramApiException {

        for (City city: cities
             ) {
            String forecast = gate.getWeatherByCity(city);
            SendMessage message = new SendMessage();
            message.setChatId(chat_id);
            message.setText(forecast);
            execute(message);
        }

    }
}
