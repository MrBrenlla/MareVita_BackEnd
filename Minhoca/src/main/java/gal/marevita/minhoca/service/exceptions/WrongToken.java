package gal.marevita.minhoca.service.exceptions;

public class WrongToken extends RuntimeException {
  public WrongToken(String message) {
    super(message);
  }
}
