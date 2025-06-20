package gal.marevita.cana.service.exceptions;

public class WrongPassword extends RuntimeException {
  public WrongPassword(String message) {
    super(message);
  }
}
