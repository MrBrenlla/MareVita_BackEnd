package gal.marevita.anzol.util;

import gal.marevita.anzol.model.Alert;
import gal.marevita.anzol.model.Period;
import gal.marevita.anzol.model.WeatherCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class WeatherChecker {

  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  boolean isCurrent;
  @Autowired
  private RestTemplate restTemplate;

  public List<Period> check(Alert alert) {

    double lat = alert.gpsLocation().latitude();
    double lon = alert.gpsLocation().longitude();

    ZonedDateTime date = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC);

    Map<String, Object> responseMeteo = fetchOpenMeteoAtmospheric(lat, lon, date);

    Map<String, Object> responseMarine = fetchOpenMeteoMarine(lat, lon, date);

    ArrayList<Map<String, Object>> ResponseMoon = fetchOpenMeteoAstronomy(lat, lon, date);

    return process(responseMeteo, responseMarine, ResponseMoon, alert, date);
  }

  private Map<String, Object> fetchOpenMeteoAtmospheric(double lat, double lon, ZonedDateTime date) {
    List<WeatherCondition> conditions = new ArrayList<>();
    String baseUrl = "https://api.open-meteo.com/v1/forecast";

    Map<String, String> params = new HashMap<>();
    params.put("latitude", String.valueOf(lat));
    params.put("longitude", String.valueOf(lon));
    params.put("timezone", "auto");


    String start = date.format(DATE_FORMAT);
    String end = date.plusDays(6).format(DATE_FORMAT);
    params.put("start_date", start);
    params.put("end_date", end);
    params.put("hourly", "temperature_2m,relative_humidity_2m,wind_speed_10m,wind_direction_10m,pressure_msl,precipitation,cloud_cover");


    try {
      String url = buildUrl(baseUrl, params);

      return restTemplate.getForObject(url, Map.class);
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
    return null;
  }

  private Map<String, Object> fetchOpenMeteoMarine(double lat, double lon, ZonedDateTime date) {
    List<WeatherCondition> conditions = new ArrayList<>();

    String baseUrl = "https://marine-api.open-meteo.com/v1/marine";

    Map<String, String> params = new HashMap<>();
    params.put("latitude", String.valueOf(lat));
    params.put("longitude", String.valueOf(lon));

    params.put("hourly", "wave_height,sea_level_height_msl,sea_surface_temperature,wave_direction,wave_period,ocean_current_velocity,ocean_current_direction");
    String start = date.format(DATE_FORMAT);
    String end = date.plusDays(6).format(DATE_FORMAT);
    params.put("start_date", start);
    params.put("end_date", end);

    try {
      String url = buildUrl(baseUrl, params);
      return restTemplate.getForObject(url, Map.class);
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
    return null;
  }

  private ArrayList<Map<String, Object>> fetchOpenMeteoAstronomy(double lat, double lon, ZonedDateTime dateTime) {
    List<WeatherCondition> conditions = new ArrayList<>();
    String baseUrl = "https://api.viewbits.com/v1/moonphase";

    Map<String, String> params = new HashMap<>();

    String date = dateTime.plusDays(3).format(DATE_FORMAT);
    params.put("startdate", date);

    try {
      String url = buildUrl(baseUrl, params);
      return restTemplate.getForObject(url, ArrayList.class);
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
    return null;
  }

  private String buildUrl(String baseUrl, Map<String, String> params) {
    StringBuilder url = new StringBuilder(baseUrl + "?");
    for (Map.Entry<String, String> entry : params.entrySet()) {
      url.append(entry.getKey())
          .append("=")
          .append(entry.getValue())
          .append("&");
    }
    return url.toString();
  }

  private List<Period> process(Map<String, Object> responseMeteo, Map<String, Object> responseMarine, List<Map<String, Object>> responseMoon,
                               Alert alert, ZonedDateTime dateTime) {

    List<WeatherCondition> conditions = alert.weatherConditions();

    Map<String, List<Double>> MeteoMarine = new HashMap<>();
    if (responseMarine != null) MeteoMarine.putAll((Map<String, List<Double>>) responseMarine.get("hourly"));
    if (responseMeteo != null) MeteoMarine.putAll((Map<String, List<Double>>) responseMeteo.get("hourly"));

    List<Period> periods = new ArrayList<>();
    ZonedDateTime day00 = dateTime.with(LocalTime.of(0, 0));

    boolean isInPeriod = false;
    ZonedDateTime periodStart = null;
    double value;

    for (int i = dateTime.getHour() + 1; i < 24 * 7; i++) {
      boolean match = true;

      for (WeatherCondition condition : conditions) {
        if (condition.name().equals("illumination") || condition.name().equals("moon_age")) {
          Map<String, Object> data = responseMoon.get(i % 24);
          String str = (String) data.get(condition.name());

          if (condition.name().equals("illumination")) value = Double.parseDouble(str.replace("%", ""));
          else value = Double.parseDouble(str.split(" ")[0]);

        } else {
          List<Double> values = MeteoMarine.get(ConditionsTranslator.translate(condition.name()));

          if (values == null) break;
          Object aux = values.get(i);
          if (aux instanceof Double) value = (Double) aux;
          else if (aux instanceof Integer) value = (double) (Integer) aux;
          else value = Double.parseDouble(aux.toString());
        }

        double min = condition.value() - condition.error();
        double max = condition.value() + condition.error();
        match = (min <= value && value <= max);

        if (!match) break;
      }
      if (match && !isInPeriod) {
        periodStart = day00.plusHours(i);
        isInPeriod = true;
      } else if (!match && isInPeriod) {
        periods.add(new Period(alert, periodStart, day00.plusHours(i)));
        isInPeriod = false;
      }
    }
    if (isInPeriod)
      periods.add(new Period(alert, periodStart, day00.plusHours(24 * 7)));


    return periods;
  }
}
