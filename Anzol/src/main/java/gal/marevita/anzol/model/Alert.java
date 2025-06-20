package gal.marevita.anzol.model;

import lombok.Builder;
import lombok.Singular;

import java.util.List;

@Builder(toBuilder = true)
public record Alert(

    String id,
    String name,
    String owner,
    String relatedCapture,
    GPSLocation gpsLocation,
    String location,
    @Singular List<String> baits,
    @Singular("fish") List<Fish> fish,
    @Singular List<WeatherCondition> weatherConditions
) {
}
