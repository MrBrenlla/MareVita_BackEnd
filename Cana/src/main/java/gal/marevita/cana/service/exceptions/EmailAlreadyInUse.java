package gal.marevita.cana.service.exceptions;

public class EmailAlreadyInUse extends RuntimeException {
  public EmailAlreadyInUse(String message) {
    super(message);
  }
}
