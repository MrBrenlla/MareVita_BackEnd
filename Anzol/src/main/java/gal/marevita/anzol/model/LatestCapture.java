package gal.marevita.anzol.model;

import lombok.Builder;

import java.time.Instant;

@Builder(toBuilder = true)
public record LatestCapture(


    String id,
    String owner,
    int security,
    GPSLocation gpsLocation,
    Instant dateTime

) {
}
