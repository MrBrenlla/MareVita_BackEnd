package gal.marevita.anzol.util;

public class ConditionsTranslator {

  public static String translate(String condition) {
    switch (condition) {
      case "temperature":
        return "temperature_2m";
      case "humidity":
        return "relative_humidity_2m";
      case "wind_speed":
        return "wind_speed_10m";
      case "wind_direction":
        return "wind_direction_10m";
      case "pressure":
        return "pressure_msl";
      case "precipitation":
        return "precipitation";
      case "cloud_cover":
        return "cloud_cover";
      case "wave_height":
        return "wave_height";
      case "wave_direction":
        return "wave_direction";
      case "wave_period":
        return "wave_period";
      case "sea_level":
        return "sea_level_height_msl";
      case "sea_temperature":
        return "sea_surface_temperature";
      case "current_velocity":
        return "ocean_current_velocity";
      case "current_direction":
        return "ocean_current_direction";
      default:
        return condition;
    }
  }

}
