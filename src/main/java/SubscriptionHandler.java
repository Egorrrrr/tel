import org.glassfish.grizzly.utils.Pair;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

public class SubscriptionHandler {
    private HashMap<String, String> userStateMap;
    private HashMap<String, ArrayList<City>> userSubscriptionList;

    public SubscriptionHandler(HashMap<String, String> userStateMap, HashMap<String, ArrayList<City>> userSubscriptionList, WeatherGateway gate) {
        this.userStateMap = userStateMap;
        this.userSubscriptionList = userSubscriptionList;
        this.gate = gate;
    }

    private WeatherGateway gate;



    public String handleSubscription(Update update){
        String sender_username = update.getMessage().getFrom().getUserName();
        String message_text = update.getMessage().getText();
        String chat_id = String.valueOf(update.getMessage().getChatId());
        String output = "";
        if(userStateMap.get(sender_username) == "sub"){
            if(!message_text.equals("exit")) {
                City city = gate.getCityByName(message_text);
                if(city != null) {
                    if(!userSubscriptionList.containsKey(chat_id)) {
                        ArrayList<City> sub_list = new ArrayList<>();
                        sub_list.add(city);
                        userSubscriptionList.put(String.valueOf(chat_id), sub_list);
                    }
                    else {
                        userSubscriptionList.get(chat_id).add(city);
                    }
                    userStateMap.put(sender_username, "default");
                    output = new String(("Вы подписалиь на обновления по: " + city.getName()).getBytes(), StandardCharsets.UTF_8);


                }
                else {
                    output = new String("Город не найден, попробуйте еще раз".getBytes(), StandardCharsets.UTF_8);
                }
            }
            else{
                output = new String("Вы вышли из режима подписки".getBytes(), StandardCharsets.UTF_8);
                userStateMap.put(sender_username, "default");
            }
        }
        return output;
    }
    public String handleUnSubscription(Update update){
        String sender_username = update.getMessage().getFrom().getUserName();
        String message_text = update.getMessage().getText();
        String chat_id = String.valueOf(update.getMessage().getChatId());
        String output = "";
        if (message_text.equals("exit")){
            output = new String("Вы вышли из режима отписки ".getBytes(), StandardCharsets.UTF_8);
            return output;
        }
        ArrayList<City> city_array = userSubscriptionList.get(String.valueOf(chat_id));
        try{
            int index = Integer.parseInt(message_text)-1;
            String city_name = city_array.get(index).getName();
            city_array.remove(index);
            output = new String(("Вы успешно описались от: " + city_name).getBytes(), StandardCharsets.UTF_8);
            userStateMap.put(sender_username, "default");
        }
        catch (Exception e){
            return new String("Введите корректный идекс".getBytes(), StandardCharsets.UTF_8);
        }
        return output;
    }
}
