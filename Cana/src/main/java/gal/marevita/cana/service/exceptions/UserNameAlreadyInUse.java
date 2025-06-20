package gal.marevita.cana.service.exceptions;

public class UserNameAlreadyInUse extends RuntimeException {
  public UserNameAlreadyInUse(String message) {
    super(message);
  }
}
