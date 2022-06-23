import org.telegram.telegrambots.meta.api.objects.Update;



public class UserEvent {
    private String chat_id;
    private String name;
    private String message;
    private MessageType type;
    private double lat;
    private double lon;


    public UserEvent(Update update){
        if(update.hasCallbackQuery()){
            this.name = update.getCallbackQuery().getFrom().getUserName();
            this.chat_id = String.valueOf(update.getCallbackQuery().getMessage().getChatId());
            this.message = update.getCallbackQuery().getData();
            this.type = MessageType.CALLBACK;
            return;
        }
        this.name = update.getMessage().getFrom().getUserName();
        this.chat_id = String.valueOf(update.getMessage().getChatId());
        if(update.getMessage().getLocation() != null){
            this.lat = update.getMessage().getLocation().getLatitude();
            this.lon = update.getMessage().getLocation().getLongitude();
            this.type = MessageType.LOCATION;
            return;
        }

        if(update.hasMessage() && update.getMessage().hasText()){
            this.message = update.getMessage().getText();
            this.type = message.startsWith("/") ? MessageType.COMMAND : MessageType.TEXT;

        }


    }

    public String getChat_id() {
        return chat_id;
    }

    public void setChat_id(String chat_id) {
        this.chat_id = chat_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }
    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }



}
