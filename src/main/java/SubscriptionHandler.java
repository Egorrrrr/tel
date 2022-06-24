import org.glassfish.grizzly.utils.Pair;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class SubscriptionHandler {
    private HashMap<String, String> userStateMap;
    private ConcurrentHashMap<String, ArrayList<City>> userSubscriptionList;

    public SubscriptionHandler(HashMap<String, String> userStateMap, ConcurrentHashMap<String, ArrayList<City>> userSubscriptionList, WeatherGateway gate) {
        this.userStateMap = userStateMap;
        this.userSubscriptionList = userSubscriptionList;
        this.gate = gate;
    }

    private WeatherGateway gate;


    public String  handleSubscription(Update update){
        return handleSubscription(update, null);
    }
    public String handleSubscription(Update update, City city){
        String output = "";
        if(update.hasCallbackQuery()){
            if(update.getCallbackQuery().getData().equals("exit")){
                output = new String("Вы вышли из режима подписки".getBytes(), StandardCharsets.UTF_8);
                String callback_username = update.getCallbackQuery().getFrom().getUserName();
                userStateMap.put(callback_username, "default");
                return output;
            }
        }
        String sender_username = update.getMessage().getFrom().getUserName();
        String message_text = update.getMessage().getText();
        String chat_id = String.valueOf(update.getMessage().getChatId());

        if(userStateMap.get(sender_username) == "sub"){
            city = city == null ? gate.getCityByName(message_text) : city;
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
                output = new String(("Вы подписалиcь на обновления по: " + city.getName()).getBytes(), StandardCharsets.UTF_8);


            }
            else {
                output = new String("Город не найден, попробуйте еще раз".getBytes(), StandardCharsets.UTF_8);
            }

        }
        return output;
    }
    public String handleUnSubscription(Update update){
        String callback_username = update.getCallbackQuery().getFrom().getUserName();
        String message_text = update.getCallbackQuery().getData();
        String chat_id = String.valueOf(update.getCallbackQuery().getMessage().getChatId());
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
            userStateMap.put(callback_username, "default");
        }
        catch (Exception e){
            return new String("Введите корректный идекс".getBytes(), StandardCharsets.UTF_8);
        }
        return output;
    }
    public SendMessage createSubMarkup(Update update){
        String sender_username = update.getMessage().getFrom().getUserName();
        String chat_id = String.valueOf(update.getMessage().getChatId());
        SendMessage message = new SendMessage();
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline = getExitButton(rowsInline);
        markupInline.setKeyboard(rowsInline);
        message.setText(new String("Введите имя города.\nЧтобы отказаться введите exit".getBytes(), StandardCharsets.UTF_8));
        message.setChatId(chat_id);
        message.setReplyMarkup(markupInline);
        return message;
    }
    public SendMessage createUnsubMarkUp(Update update){
        String sender_username = update.getMessage().getFrom().getUserName();
        String chat_id = String.valueOf(update.getMessage().getChatId());

        SendMessage message = new SendMessage();
        StringBuilder sb = new StringBuilder();
        ArrayList<City> city_array = userSubscriptionList.get(String.valueOf(chat_id));

        if(city_array.isEmpty()){
            message.setChatId(chat_id);
            message.setText(new String("У вас нет подписок".getBytes(), StandardCharsets.UTF_8));
            return message;
        }
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        userStateMap.put(sender_username, "unsub");
        for (int i = 0; i < city_array.stream().count(); i++){
            InlineKeyboardButton btn = new InlineKeyboardButton();
            btn.setText(city_array.get(i).getName());
            btn.setCallbackData(String.valueOf(i+1));
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            rowInline.add(btn);
            rowsInline.add(rowInline);
        }
        rowsInline = getExitButton(rowsInline);
        markupInline.setKeyboard(rowsInline);
        message.setChatId(chat_id);
        message.setText(new String("Выберите город, от которого хотите отписаться".getBytes(), StandardCharsets.UTF_8));
        message.setReplyMarkup(markupInline);


        return message;
    }
    public List<List<InlineKeyboardButton>> getExitButton(List<List<InlineKeyboardButton>> rowsInline){
        InlineKeyboardButton btn = new InlineKeyboardButton();
        btn.setText(new String("Выйти".getBytes(), StandardCharsets.UTF_8));
        btn.setCallbackData("exit");
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(btn);
        rowsInline.add(rowInline);
        return rowsInline;
    }
}
