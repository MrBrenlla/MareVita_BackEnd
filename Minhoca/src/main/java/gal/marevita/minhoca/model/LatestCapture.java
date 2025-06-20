package gal.marevita.minhoca.model;

import lombok.Builder;

import java.time.ZonedDateTime;

@Builder(toBuilder = true)
public record LatestCapture(


    String id,
    String owner,
    int security,
    GPSLocation gpsLocation,
    ZonedDateTime dateTime

) {
}
