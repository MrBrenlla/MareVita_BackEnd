package gal.marevita.anzol.model;

import lombok.Builder;

@Builder(toBuilder = true)
public record WeatherCondition(
    String name,
    Double value,
    Double error
) {
}