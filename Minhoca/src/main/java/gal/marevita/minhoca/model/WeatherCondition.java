package gal.marevita.minhoca.model;

import lombok.Builder;

@Builder(toBuilder = true)
public record WeatherCondition(
    String name,
    Double value,
    Double error
) {
}