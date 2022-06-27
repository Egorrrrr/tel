import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class SubscriptionHandler {
    private HashMap<String, String> userStateMap;
    private HashMap<Long, Integer> timeUserState;
    private ConcurrentHashMap<String, Subscription> userSubscriptionList;

    public SubscriptionHandler(HashMap<String, String> userStateMap, ConcurrentHashMap<String, Subscription> userSubscriptionList, WeatherGateway gate) {
        this.userStateMap = userStateMap;
        this.userSubscriptionList = userSubscriptionList;
        this.gate = gate;
        this.timeUserState = new HashMap<>();
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
            String message_to_send = "";
            if(city != null) {
                if(!userSubscriptionList.containsKey(chat_id)) {
                    ArrayList<City> sub_list = new ArrayList<>();
                    sub_list.add(city);
                    Subscription sub = new Subscription();
                    sub.setCities(sub_list);
                    sub.setTime(9);
                    sub.setSent(false);
                    userSubscriptionList.put(String.valueOf(chat_id), sub);
                    message_to_send = "Вы подписалиcь на обновления по: " + city.getName() +"\nПо умолчанию погода высылается в 9 утра.\n/settime - для настройки";
                }
                else {
                    userSubscriptionList.get(chat_id).getCities().add(city);
                    message_to_send = "Вы подписалиcь на обновления по: " + city.getName();
                }
                userStateMap.put(sender_username, "default");
                output = new String((message_to_send).getBytes(), StandardCharsets.UTF_8);


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
            userStateMap.put(callback_username, "default");
            return output;
        }
        ArrayList<City> city_array = (ArrayList<City>) userSubscriptionList.get(String.valueOf(chat_id)).getCities();
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
        String chat_id = String.valueOf(update.getMessage().getChatId());
        SendMessage message = new SendMessage();
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline = getTextButton(rowsInline, "Выйти", "exit");
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
        ArrayList<City> city_array = (ArrayList<City>) userSubscriptionList.get(String.valueOf(chat_id)).getCities();

        if(city_array.isEmpty()){
            message.setChatId(chat_id);
            message.setText(new String("У вас нет подписок".getBytes(), StandardCharsets.UTF_8));
            userStateMap.put(sender_username, "default");
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
        rowsInline = getTextButton(rowsInline, "Выйти", "exit");
        markupInline.setKeyboard(rowsInline);
        message.setChatId(chat_id);
        message.setText(new String("Выберите город, от которого хотите отписаться".getBytes(), StandardCharsets.UTF_8));
        message.setReplyMarkup(markupInline);
        return message;
    }
    public String SetSubTime(String chat_id, int time){
        if(userSubscriptionList.containsKey(chat_id)){
            userSubscriptionList.get(chat_id).setTime(time);
        }
        else{
            Subscription sub = new Subscription();
            sub.setTime(time);
            userSubscriptionList.put(chat_id, sub);
        }
        return new String(("Ваше время подписки измененно на: " + time).getBytes(), StandardCharsets.UTF_8);
    }
    public EditMessageReplyMarkup changeTimeMarkup(Update update){
        String chat_id = String.valueOf(update.getCallbackQuery().getMessage().getChatId());
        String data = update.getCallbackQuery().getData();
        Long user_id = update.getCallbackQuery().getFrom().getId();
        EditMessageReplyMarkup edit = new EditMessageReplyMarkup();
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline = getTextButton(rowsInline, "<", "<");
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        int current_option = timeUserState.get(user_id);
        int change = data.equals(">") ? 3 : -3;
        int ind =current_option+change;
        if((current_option == 0 && change<0) || (current_option==21 && change>0)){
            return null;
        }
        for (int i = ind; i < ind+3; i++){

            InlineKeyboardButton btn = new InlineKeyboardButton();

            btn.setText(String.valueOf(i));

            btn.setCallbackData(String.valueOf(i));

            rowInline.add(btn);
        }
        timeUserState.put(user_id, current_option+change);
        rowsInline.add(rowInline);
        rowsInline = getTextButton(rowsInline, ">", ">");
        rowsInline = getTextButton(rowsInline, "Выйти","exit");
        markupInline.setKeyboard(rowsInline);
        edit.setReplyMarkup(markupInline);
        edit.setChatId(chat_id);
        edit.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        return edit;
    }
    public SendMessage createTimeMarkup(Update update){
        String username = update.getMessage().getFrom().getUserName();
        String chat_id = String.valueOf(update.getMessage().getChatId());
        SendMessage message = new SendMessage();
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline = getTextButton(rowsInline, "<", "<");
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        for (int i = 0; i < 3; i++){

            InlineKeyboardButton btn = new InlineKeyboardButton();
            btn.setText(String.valueOf(i));
            btn.setCallbackData(String.valueOf(i));
            rowInline.add(btn);
        }
        rowsInline.add(rowInline);
        rowsInline = getTextButton(rowsInline, ">", ">");
        rowsInline = getTextButton(rowsInline, "Выйти","exit");
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);
        message.setText(new String("Выберите время рассылки".getBytes(), StandardCharsets.UTF_8));
        message.setChatId(chat_id);
        userStateMap.put(username,"time");
        timeUserState.put(update.getMessage().getFrom().getId(), 0);
        return message;
    }
    public List<List<InlineKeyboardButton>> getTextButton(List<List<InlineKeyboardButton>> rowsInline, String text, String data){
        InlineKeyboardButton btn = new InlineKeyboardButton();
        btn.setText(new String(text.getBytes(), StandardCharsets.UTF_8));
        btn.setCallbackData(data);
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(btn);
        rowsInline.add(rowInline);
        return rowsInline;
    }
}
