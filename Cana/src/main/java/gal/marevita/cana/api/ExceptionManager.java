package gal.marevita.cana.api;

import gal.marevita.cana.apigenerator.openapi.api.model.Error400DTO;
import gal.marevita.cana.apigenerator.openapi.api.model.Error409DTO;
import gal.marevita.cana.service.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.management.InstanceNotFoundException;

public class ExceptionManager {

  public static ResponseEntity manage(final Exception e) {
    Class<? extends Exception> ex = e.getClass();

    if (ex == EmailAlreadyInUse.class)
      return ResponseEntity.status(HttpStatus.CONFLICT).body(new Error409DTO.Error409DTOBuilder().conflict("Email").build());
    if (ex == UserNameAlreadyInUse.class)
      return ResponseEntity.status(HttpStatus.CONFLICT).body(new Error409DTO.Error409DTOBuilder().conflict("UserName").build());

    if (ex == InvalidEmail.class)
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Error400DTO.Error400DTOBuilder().badValue("Email").build());
    if (ex == InvalidUserName.class)
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Error400DTO.Error400DTOBuilder().badValue("UserName").build());
    if (ex == InvalidPassword.class)
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Error400DTO.Error400DTOBuilder().badValue("Password").build());

    if (ex == InstanceNotFoundException.class) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

    if (ex == WrongToken.class) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    if (ex == WrongPassword.class) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
  }
}
