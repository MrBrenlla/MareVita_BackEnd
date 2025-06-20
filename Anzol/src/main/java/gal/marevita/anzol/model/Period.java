package gal.marevita.anzol.model;

import lombok.Builder;

import java.time.ZonedDateTime;

@Builder
public record Period(
    Alert alert,
    ZonedDateTime startDate,
    ZonedDateTime endDate
) {
}

