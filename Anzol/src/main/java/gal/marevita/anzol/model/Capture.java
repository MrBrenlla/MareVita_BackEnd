package gal.marevita.anzol.model;

import lombok.Builder;
import lombok.Singular;

import java.time.ZonedDateTime;
import java.util.List;

@Builder(toBuilder = true)
public record Capture(
    String id,
    String owner,
    int security,
    @Singular List<String> likes,
    ZonedDateTime dateTime,
    GPSLocation gpsLocation,
    String location,
    String imageCaption,
    @Singular List<String> images,
    @Singular List<String> baits,
    @Singular("fish") List<Fish> fish,
    @Singular List<WeatherCondition> weatherConditions
) {
}