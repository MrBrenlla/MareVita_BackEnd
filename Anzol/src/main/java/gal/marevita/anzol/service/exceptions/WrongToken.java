package gal.marevita.anzol.service.exceptions;

public class WrongToken extends RuntimeException {
  public WrongToken(String message) {
    super(message);
  }
}
