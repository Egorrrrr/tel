import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class WeatherGateway {

    private String token;

    public WeatherGateway(){

    }

    public City getCityByName(String name){
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            name = name.replaceAll(" ","-");
            String url = String.format("http://api.openweathermap.org/geo/1.0/direct?q=%s&limit=5&appid=%s", name, token);
            HttpGet request = new HttpGet(url);
            Object result = client.execute(request, httpResponse -> httpResponse.getEntity().getContent().readAllBytes());
            String jsonString = new String((byte[])result);
            if(jsonString.equals("[]")){
                return null;
            }
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature
                            .FAIL_ON_UNKNOWN_PROPERTIES,
                    false);
            JSONArray json = new JSONArray(jsonString);
            City city = mapper.readValue(json.get(0).toString(), City.class);
            return city;


        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public City getCityByCord(double lat, double lon){
        City city = new City();
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String url = String.format("https://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&units=metric&appid=%s", lat, lon, token);
            HttpGet request = new HttpGet(url);
            Object result = client.execute(request, httpResponse -> httpResponse.getEntity().getContent().readAllBytes());
            String jsonString = new String((byte[])result);
            JSONObject json = new JSONObject(jsonString);
            city.setLon(lon);
            city.setLat(lat);
            city.setCountry(json.getJSONObject("sys").getString("country"));
            city.setName(json.getString("name"));
            return city;
        } catch (IOException e) {
            e.printStackTrace();
            return city;
        }
    }

    public String getWeatherByCity(City city){

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String url = String.format("https://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&units=metric&appid=%s", city.getLat(), city.getLon(), token);
            HttpGet request = new HttpGet(url);
            Object result = client.execute(request, httpResponse -> httpResponse.getEntity().getContent().readAllBytes());
            String jsonString = new String((byte[])result);
            JSONObject json = new JSONObject(jsonString);
            String res_string = new String(String.format("%s, %s, %s\nТемпература: %.1f, %s\nДавление: %s\nВлажность: %s%%",
                    city.getName() != null ? city.getName() : json.getString("name"),
                    city.getState() != null ? city.getState() : city.getName() != null ? city.getName() : json.get("name"),
                    city.getCountry() != null ? city.getCountry() : json.getJSONObject("sys").getString("country"),
                    json.getJSONObject("main").getDouble("temp"),
                    json.getJSONArray("weather").getJSONObject(0).getString("description"),
                    json.getJSONObject("main").getDouble("pressure"),
                    json.getJSONObject("main").getDouble("humidity")).getBytes(), StandardCharsets.UTF_8);

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
