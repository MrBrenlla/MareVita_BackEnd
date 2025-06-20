package gal.marevita.anzol.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GPStoName {

  private static final String GOOGLE_API_KEY = "AIzaSyAZP7-7NSr_4R8SFKJDuVzg1ZjVA700xdA";
  private static final String GOOGLE_GEOCODE_API_URL = "https://maps.googleapis.com/maps/api/geocode/json";

  public String obterNomeLugar(double latitude, double longitude) {
    String url = String.format("%s?latlng=%s,%s&language=gl&key=%s",
        GOOGLE_GEOCODE_API_URL,
        String.valueOf(latitude).replace(",", "."),
        String.valueOf(longitude).replace(",", "."),
        GOOGLE_API_KEY);

    RestTemplate restTemplate = new RestTemplate();
    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

    if (response.getStatusCode().is2xxSuccessful()) {
      String resposta = response.getBody();
      JsonObject jsonResponse = JsonParser.parseString(resposta).getAsJsonObject();
      JsonArray results = jsonResponse.getAsJsonArray("results");

      if (results != null && results.size() > 0) {
        for (JsonElement resultElement : results) {
          JsonObject result = resultElement.getAsJsonObject();
          JsonArray addressComponents = result.getAsJsonArray("address_components");

          String[] tiposPreferidos = {
              "locality",
              "sublocality",
              "administrative_area_level_2",
              "administrative_area_level_1"
          };

          for (String tipoPreferido : tiposPreferidos) {
            for (JsonElement componentElement : addressComponents) {
              JsonObject component = componentElement.getAsJsonObject();
              JsonArray types = component.getAsJsonArray("types");

              for (JsonElement type : types) {
                if (type.getAsString().equals(tipoPreferido)) {
                  return component.get("long_name").getAsString();
                }
              }
            }
          }
        }
      }
    }

    return "Lugar descoñecido (" + latitude + ", " + longitude + ")";
  }
}
