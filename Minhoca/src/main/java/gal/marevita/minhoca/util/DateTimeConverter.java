package gal.marevita.minhoca.util;

import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
@Named("DateTimeConverter")
public class DateTimeConverter {

  @Named("localDateTimeToInstant")
  public Instant toInstant(LocalDateTime value) {
    return value != null ?
        value.atZone(ZoneId.systemDefault()).toInstant() :
        null;
  }

  @Named("instantToLocalDateTime")
  public LocalDateTime toLocalDateTime(Instant value) {
    return value != null ?
        value.atZone(ZoneId.systemDefault()).toLocalDateTime() :
        null;
  }
}