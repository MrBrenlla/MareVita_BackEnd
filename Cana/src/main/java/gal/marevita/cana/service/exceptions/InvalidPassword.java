package gal.marevita.cana.service.exceptions;

public class InvalidPassword extends RuntimeException {
  public InvalidPassword(String message) {
    super(message);
  }
}
