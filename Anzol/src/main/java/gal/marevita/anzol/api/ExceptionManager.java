package gal.marevita.anzol.api;

import gal.marevita.anzol.service.exceptions.NotAccesible;
import gal.marevita.anzol.service.exceptions.WrongDataTime;
import gal.marevita.anzol.service.exceptions.WrongToken;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.management.InstanceNotFoundException;

public class ExceptionManager {

  public static ResponseEntity manage(final Exception e) {
    Class<? extends Exception> ex = e.getClass();

    if (ex == InstanceNotFoundException.class) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    if (ex == WrongToken.class) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    if (ex == NotAccesible.class) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    if (ex == WrongDataTime.class) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
  }
}
