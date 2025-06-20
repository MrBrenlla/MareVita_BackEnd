package gal.marevita.anzol.util;

import gal.marevita.anzol.model.WeatherCondition;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class WeatherRetriever {

  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter ISO_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
  private final RestTemplate restTemplate = new RestTemplate();
  boolean isCurrent;

  public List<WeatherCondition> getWeatherData(double lat, double lon, ZonedDateTime dateTime) {
    List<WeatherCondition> conditions = new ArrayList<>();
    ZonedDateTime now = ZonedDateTime.now();
    ZonedDateTime lastOClock = now.withMinute(0).withSecond(0).withNano(0);

    isCurrent = dateTime.isAfter(lastOClock);

    conditions.addAll(fetchOpenMeteoAtmospheric(lat, lon, dateTime.withZoneSameInstant(ZoneOffset.UTC)));
    System.out.println(conditions);

    conditions.addAll(fetchOpenMeteoMarine(lat, lon, dateTime.withZoneSameInstant(ZoneOffset.UTC)));
    System.out.println(conditions);

    conditions.addAll(fetchOpenMeteoAstronomy(lat, lon, dateTime.withZoneSameInstant(ZoneOffset.UTC)));
    System.out.println(conditions);

    return conditions;
  }

  private List<WeatherCondition> fetchOpenMeteoAtmospheric(double lat, double lon, ZonedDateTime dateTime) {
    List<WeatherCondition> conditions = new ArrayList<>();
    String baseUrl = "https://api.open-meteo.com/v1/forecast";

    Map<String, String> params = new HashMap<>();
    params.put("latitude", String.valueOf(lat));
    params.put("longitude", String.valueOf(lon));
    params.put("timezone", "auto");

    if (isCurrent) {
      params.put("current", "temperature_2m,relative_humidity_2m,wind_speed_10m,wind_direction_10m,pressure_msl,precipitation,cloud_cover");

    } else {
      String date = dateTime.format(DATE_FORMAT);
      params.put("start_date", date);
      params.put("end_date", date);
      params.put("hourly", "temperature_2m,relative_humidity_2m,wind_speed_10m,wind_direction_10m,pressure_msl,precipitation,cloud_cover");
    }

    try {
      String url = buildUrl(baseUrl, params);
      Map<String, Object> response = restTemplate.getForObject(url, Map.class);

      processHistoricalData(response, conditions, dateTime);

    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
    return conditions;
  }

  private List<WeatherCondition> fetchOpenMeteoMarine(double lat, double lon, ZonedDateTime dateTime) {
    List<WeatherCondition> conditions = new ArrayList<>();

    String baseUrl = "https://marine-api.open-meteo.com/v1/marine";

    Map<String, String> params = new HashMap<>();
    params.put("latitude", String.valueOf(lat));
    params.put("longitude", String.valueOf(lon));


    if (!isCurrent) {
      params.put("hourly", "wave_height,sea_level_height_msl,sea_surface_temperature,wave_direction,wave_period,ocean_current_velocity,ocean_current_direction");
      String date = dateTime.format(DATE_FORMAT);
      params.put("start_date", date);
      params.put("end_date", date);
    } else {
      params.put("current", "wave_height,sea_level_height_msl,sea_surface_temperature,wave_direction,wave_period,ocean_current_velocity,ocean_current_direction");
    }

    try {
      String url = buildUrl(baseUrl, params);
      Map<String, Object> response = restTemplate.getForObject(url, Map.class);
      processMarineData(response, conditions, dateTime);
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
    return conditions.stream().filter(c -> c.value() != null).toList();
  }

  private List<WeatherCondition> fetchOpenMeteoAstronomy(double lat, double lon, ZonedDateTime dateTime) {
    List<WeatherCondition> conditions = new ArrayList<>();
    String baseUrl = "https://api.viewbits.com/v1/moonphase";

    Map<String, String> params = new HashMap<>();

    String date = dateTime.format(DATE_FORMAT);
    params.put("startdate", date);

    try {
      String url = buildUrl(baseUrl, params);
      ArrayList<Map<String, Object>> response = restTemplate.getForObject(url, ArrayList.class);
      processAstronomyData(response, conditions, dateTime);
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
    return conditions;
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

  private void processHistoricalData(Map<String, Object> response, List<WeatherCondition> conditions, ZonedDateTime dateTime) {

    if (isCurrent) {
      Map<String, Object> current = (Map<String, Object>) response.get("current");

      conditions.add(createCondition("temperature", current.get("temperature_2m")));
      conditions.add(createCondition("humidity", current.get("relative_humidity_2m")));
      conditions.add(createCondition("wind_speed", current.get("wind_speed_10m")));
      conditions.add(createCondition("wind_direction", current.get("wind_direction_10m")));
      conditions.add(createCondition("pressure", current.get("pressure_msl")));
      conditions.add(createCondition("precipitation", current.get("precipitation")));
      conditions.add(createCondition("cloud_cover", current.get("cloud_cover")));

    } else {
      Map<String, Object> hourly = (Map<String, Object>) response.get("hourly");
      int hour = dateTime.getHour();

      conditions.add(createCondition("temperature", ((List<Double>) hourly.get("temperature_2m")).get(hour)));
      conditions.add(createCondition("humidity", ((List<Double>) hourly.get("relative_humidity_2m")).get(hour)));
      conditions.add(createCondition("wind_speed", ((List<Double>) hourly.get("wind_speed_10m")).get(hour)));
      conditions.add(createCondition("wind_direction", ((List<Double>) hourly.get("wind_direction_10m")).get(hour)));
      conditions.add(createCondition("pressure", ((List<Double>) hourly.get("pressure_msl")).get(hour)));
      conditions.add(createCondition("precipitation", ((List<Double>) hourly.get("precipitation")).get(hour)));
      conditions.add(createCondition("cloud_cover", ((List<Double>) hourly.get("cloud_cover")).get(hour)));
    }
  }

  private void processMarineData(Map<String, Object> response, List<WeatherCondition> conditions, ZonedDateTime dateTime) {
    if (isCurrent) {
      Map<String, Object> current = (Map<String, Object>) response.get("current");

      conditions.add(createCondition("wave_height", current.get("wave_height")));
      conditions.add(createCondition("wave_direction", current.get("wave_direction")));
      conditions.add(createCondition("wave_period", current.get("wave_period")));

      conditions.add(createCondition("sea_level", current.get("sea_level_height_msl")));
      conditions.add(createCondition("sea_temperature", current.get("sea_surface_temperature")));
      conditions.add(createCondition("current_velocity", current.get("ocean_current_velocity")));
      conditions.add(createCondition("current_direction", current.get("ocean_current_direction")));

    } else {
      Map<String, Object> hourly = (Map<String, Object>) response.get("hourly");
      int hour = dateTime.getHour();

      conditions.add(createCondition("wave_height", ((List<Double>) hourly.get("wave_height")).get(hour)));
      conditions.add(createCondition("wave_direction", ((List<Double>) hourly.get("wave_direction")).get(hour)));
      conditions.add(createCondition("wave_period", ((List<Double>) hourly.get("wave_period")).get(hour)));

      conditions.add(createCondition("sea_level", ((List<Double>) hourly.get("sea_level_height_msl")).get(hour)));
      conditions.add(createCondition("sea_temperature", ((List<Double>) hourly.get("sea_surface_temperature")).get(hour)));
      conditions.add(createCondition("current_velocity", ((List<Double>) hourly.get("ocean_current_velocity")).get(hour)));
      conditions.add(createCondition("current_direction", ((List<Double>) hourly.get("ocean_current_direction")).get(hour)));

    }
  }

  private void processAstronomyData(ArrayList<Map<String, Object>> response, List<WeatherCondition> conditions, ZonedDateTime dateTime) {
    Map<String, Object> data = response.get(3);

    String i = (String) data.get("illumination");
    String a = (String) data.get("moon_age");

    Double ilu = Double.parseDouble(i.replace("%", ""));
    Double age = Double.parseDouble(a.split(" ")[0]);

    conditions.add(createCondition("illumination", ilu));
    conditions.add(createCondition("moon_age", age));
  }

  private WeatherCondition createCondition(String name, Object value) {
    return WeatherCondition.builder()
        .name(name)
        .value(value != null ? Double.parseDouble(value.toString()) : null)
        .error(null)
        .build();
  }
}
