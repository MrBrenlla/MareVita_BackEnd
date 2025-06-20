package gal.marevita.cana.api;


import gal.marevita.cana.api.mappers.SocialDTOMapper;
import gal.marevita.cana.api.mappers.UserDTOMapper;
import gal.marevita.cana.api.mappers.UserDataDTOMapper;
import gal.marevita.cana.apigenerator.openapi.api.UserApi;
import gal.marevita.cana.apigenerator.openapi.api.model.*;
import gal.marevita.cana.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.management.InstanceNotFoundException;
import java.util.List;

@RestController
public class UserApiImp implements UserApi {

  @Autowired
  private UserService userService;

  @Override
  public ResponseEntity register(UserDTO userDTO) {
    try {
      return ResponseEntity.ok(UserDataDTOMapper.INSTANCE.UserToUserDataDTO(
          userService.register(UserDTOMapper.INSTANCE.UserDTOToUser(userDTO))));
    } catch (Exception e) {
      return ExceptionManager.manage(e);
    }
  }

  @Override
  public ResponseEntity logIn(LogInDTO login) {
    try {
      System.out.printf("Logged in: %s\n", login);
      TokenDTO token = TokenDTO.builder().token(userService.login(login.getUserNameOrEmail(), login.getPassword())).build();
      return ResponseEntity.ok(token);
    } catch (Exception e) {
      return ExceptionManager.manage(e);
    }
  }

  @Override
  public ResponseEntity updateUser(UserDataDTO userDTO) {
    try {
      return ResponseEntity.ok(UserDataDTOMapper.INSTANCE.UserToUserDataDTO(
          userService.updateUser(UserDataDTOMapper.INSTANCE.UserDataDTOToUser(userDTO))
      ));
    } catch (Exception e) {
      return ExceptionManager.manage(e);
    }
  }

  @Override
  public ResponseEntity getUser(String userName) {
    try {
      return ResponseEntity.ok(UserDataDTOMapper.INSTANCE.UserToUserDataDTO(
          userService.getUser(userName)));
    } catch (Exception e) {
      return ExceptionManager.manage(e);
    }
  }

  @Override
  public ResponseEntity updatePassword(PassChangeDTO passChangeDTO) {
    try {
      userService.updatePassword(passChangeDTO.getOldPass(), passChangeDTO.getNewPass());
      return ResponseEntity.ok("Cambiada correctamente");
    } catch (Exception e) {
      return ExceptionManager.manage(e);
    }
  }

  @Override
  public ResponseEntity getSocial() {
    try {
      return ResponseEntity.ok(SocialDTOMapper.INSTANCE.UserToSocialDTO(
          userService.getSocial()
      ));
    } catch (InstanceNotFoundException e) {
      return ExceptionManager.manage(e);
    }
  }

  @Override
  public ResponseEntity sendFriendPetition(String username) {
    try {
      userService.sendFriendPetition(username);
      return getSocial();
    } catch (Exception e) {
      return ExceptionManager.manage(e);
    }
  }


  @Override
  public ResponseEntity AcceptFriendPetition(String username) {
    try {
      userService.manageFriendPetition(username, true);
      return getSocial();
    } catch (Exception e) {
      return ExceptionManager.manage(e);
    }
  }


  @Override
  public ResponseEntity DeclineFriendPetition(String username) {
    try {
      userService.manageFriendPetition(username, false);
      return getSocial();
    } catch (Exception e) {
      return ExceptionManager.manage(e);
    }
  }


  @Override
  public ResponseEntity RemoveFriend(String username) {
    try {
      userService.removeFriend(username);
      return getSocial();
    } catch (Exception e) {
      return ExceptionManager.manage(e);
    }
  }


  @Override
  public ResponseEntity searchUser(String keyword) {
    try {
      List<String> result = userService.searchUser(keyword);
      return ResponseEntity.ok(SearchResultDTO.builder().userList(result).build());
    } catch (Exception e) {
      return ExceptionManager.manage(e);
    }
  }

}
