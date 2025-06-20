package gal.marevita.minhoca.model.statistics;


import lombok.Builder;

import java.time.ZonedDateTime;

@Builder(toBuilder = true)
public record CaptureInfo(
    String id,
    ZonedDateTime dateTime
) {
}