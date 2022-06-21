import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class WeatherGateway {

    private String token;

    public WeatherGateway(){

    }

    public double[] getLatLonByCityName(String name){
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String url = String.format("http://api.openweathermap.org/geo/1.0/direct?q=%s&limit=5&appid=%s", name, token);
            HttpGet request = new HttpGet(url);
            Object result = client.execute(request, httpResponse -> httpResponse.getEntity().getContent().readAllBytes());
            String jsonString = new String((byte[])result);

            JSONArray json = new JSONArray(jsonString);
            JSONObject city  = (JSONObject) json.get(0);
            return new double[] {city.getDouble("lat"), city.getDouble("lon")};

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public String getWeatherByLatLong(double lat, double lon){

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String url = String.format("https://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&units=metric&appid=%s", lat, lon, token);
            HttpGet request = new HttpGet(url);
            Object result = client.execute(request, httpResponse -> httpResponse.getEntity().getContent().readAllBytes());
            String jsonString = new String((byte[])result);
            JSONObject json = new JSONObject(jsonString);
            String res_string = new String(String.format("%s, %s | Температура: %.1f, %s",
                    json.getString("name"),
                    json.getJSONObject("sys").getString("country"),
                    json.getJSONObject("main").getDouble("temp"),
                    json.getJSONArray("weather").getJSONObject(0).getString("description")).getBytes(), StandardCharsets.UTF_8);
            return res_string;

        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
    public String getToken() {
        return token;
    }

    @JsonSetter("token")
    public void setToken(String token) {
        this.token = token;
    }
}
