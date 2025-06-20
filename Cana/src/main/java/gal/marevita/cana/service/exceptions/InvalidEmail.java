package gal.marevita.cana.service.exceptions;

public class InvalidEmail extends RuntimeException {
  public InvalidEmail(String message) {
    super(message);
  }
}
