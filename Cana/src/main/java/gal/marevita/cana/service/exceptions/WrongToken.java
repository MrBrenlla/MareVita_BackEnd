package gal.marevita.cana.service.exceptions;

public class WrongToken extends RuntimeException {
  public WrongToken(String message) {
    super(message);
  }
}
