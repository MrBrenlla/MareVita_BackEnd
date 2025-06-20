package gal.marevita.anzol.model;

import lombok.Builder;

@Builder(toBuilder = true)
public record GPSLocation(
    Double latitude,
    Double longitude
) {
}