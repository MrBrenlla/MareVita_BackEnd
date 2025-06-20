package gal.marevita.cana.service;

import gal.marevita.cana.model.User;
import gal.marevita.cana.repository.UserRepository;
import gal.marevita.cana.security.JwtUtil;
import gal.marevita.cana.service.exceptions.*;
import gal.marevita.cana.service.mappers.UserRepositoryEntityMapper;
import gal.marevita.commons.repositoryEntities.UserRepositoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.management.InstanceNotFoundException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static gal.marevita.cana.security.JwtUtil.getActualUserId;

@Component
public class UserService {

  @Autowired
  private UserRepository userRepository;
  @Autowired
  private JwtUtil jwtUtil;
  @Autowired
  private PasswordEncoder passwordEncoder;
  @Value("${IMAGES_PATH:./images/profiles}")
  private String imagesPath;

  public static Optional<Path> findFileByBaseName(Path folder, String baseName) throws IOException {
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder, baseName + ".*")) {
      for (Path entry : stream) {
        if (Files.isRegularFile(entry)) {
          return Optional.of(entry);
        }
      }
    }
    return Optional.empty();
  }

  public User register(User userData) throws UserNameAlreadyInUse, InvalidUserName, EmailAlreadyInUse, InvalidEmail, InvalidPassword {
    if (userData.userName().contains("@")) throw new InvalidUserName("O nome de usuario non pode conter @");
    if (!userData.email().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))
      throw new InvalidEmail("O email non ten un formato valido");

    if (!userData.password().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$"))
      throw new InvalidPassword("A contrasinal debe conter 1 maiúscula, 1 minúscula, 1 número e 8 caracteres en total");

    if (userRepository.existsByUserName(userData.userName())) throw new UserNameAlreadyInUse("Nome de usuario existe");
    if (userRepository.existsByEmail(userData.email()))
      throw new EmailAlreadyInUse("O correo xa está asociado a unha conta existente");

    String hashedPassword = passwordEncoder.encode(userData.password());

    User user = User.builder()
        .name(userData.name())
        .email(userData.email())
        .password(hashedPassword)
        .userName(userData.userName())
        .build();

    return UserRepositoryEntityMapper.INSTANCE.UserRepositoryEntityToUser(
        userRepository.save(UserRepositoryEntityMapper.INSTANCE.UserToUserRepositoryEntity(user)));
  }

  public String login(String userNameOrEmail, String password) throws InstanceNotFoundException, WrongPassword {

    UserRepositoryEntity user = userRepository.findByUserNameOrEmail(userNameOrEmail, userNameOrEmail)
        .orElseThrow(() -> new InstanceNotFoundException("Usuario inexistente"));
    if (!passwordEncoder.matches(password, user.password)) throw new WrongPassword("Contrasinal incorrecta");
    return jwtUtil.generateToken(user.userName, user.id);

  }

  public User updateUser(User userData) throws UserNameAlreadyInUse, InvalidUserName, EmailAlreadyInUse, InvalidEmail, InstanceNotFoundException {

    User oldUser = UserRepositoryEntityMapper.INSTANCE.UserRepositoryEntityToUser(
        userRepository.findById(getActualUserId())
            .orElseThrow(() -> new WrongToken("Token Incorrecto")));

    if (userData.userName().contains("@")) throw new InvalidUserName("O nome de usuario non pode conter @");
    if (!userData.email().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))
      throw new InvalidEmail("O email non ten un formato valido");

    if (!userData.userName().equals(oldUser.userName()) & userRepository.existsByUserName(userData.userName()))
      throw new UserNameAlreadyInUse("O nome de usuario xa existe");
    if (!userData.email().equals(oldUser.email()) & userRepository.existsByEmail(userData.email()))
      throw new EmailAlreadyInUse("O correo xa está asociado a unha conta existente");

    User newUser = oldUser.toBuilder()
        .userName(userData.userName())
        .name(userData.name())
        .email(userData.email())
        .build();

    return UserRepositoryEntityMapper.INSTANCE.UserRepositoryEntityToUser(
        userRepository.save(UserRepositoryEntityMapper.INSTANCE.UserToUserRepositoryEntity(newUser)));
  }

  public User getUser(String userName) throws InstanceNotFoundException {
    return UserRepositoryEntityMapper.INSTANCE.UserRepositoryEntityToUser(
        userRepository.findByUserName(userName).orElseThrow(() -> new InstanceNotFoundException("Usuario non atopado"))
    );
  }

  public void updatePassword(String oldPass, String newPass) throws InstanceNotFoundException, InvalidPassword {

    User user = UserRepositoryEntityMapper.INSTANCE.UserRepositoryEntityToUser(
        userRepository.findById(getActualUserId())
            .orElseThrow(() -> new WrongToken("Token Incorrecto")));

    if (!newPass.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$"))
      throw new InvalidPassword("A contrasinal debe conter 1 maiúscula, 1 minúscula, 1 número e 8 caracteres en total");

    if (!passwordEncoder.matches(oldPass, user.password())) throw new WrongPassword("Contrasinal antiga incorrecta");

    userRepository.save(UserRepositoryEntityMapper.INSTANCE.UserToUserRepositoryEntity(
        user.toBuilder().password(passwordEncoder.encode(newPass)).build()
    ));
  }

  public void updateProfilePic(MultipartFile profilePic) throws InstanceNotFoundException, IOException {
    User user = UserRepositoryEntityMapper.INSTANCE.UserRepositoryEntityToUser(
        userRepository.findById(getActualUserId())
            .orElseThrow(() -> new WrongToken("Token Incorrecto")));

    Path folder = Path.of(imagesPath);
    if (!Files.exists(folder)) {
      Files.createDirectories(folder);
    }

    try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder, user.id() + ".*")) {
      for (Path entry : stream) {
        if (Files.isRegularFile(entry)) {
          Files.delete(entry);
        }
      }
    }

    String originalFilename = profilePic.getOriginalFilename();
    String extension = "";

    if (originalFilename != null && originalFilename.contains(".")) {
      extension = originalFilename.substring(originalFilename.lastIndexOf("."));
    }

    Path destination = folder.resolve(user.id() + extension);

    Files.copy(profilePic.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
  }

  public Path getProfilePic(String userName) throws InstanceNotFoundException, IOException {

    User user = UserRepositoryEntityMapper.INSTANCE.UserRepositoryEntityToUser(
        userRepository.findByUserName(userName)
            .orElseThrow(() -> new InstanceNotFoundException("Usuario inexistente")));

    Path folder = Path.of(imagesPath);
    Path destination = findFileByBaseName(folder, user.id())
        .orElseThrow(() -> new InstanceNotFoundException("Sen imaxe de perfil"));

    return destination;
  }

  public User getSocial() throws InstanceNotFoundException {

    User user = UserRepositoryEntityMapper.INSTANCE.UserRepositoryEntityToUser(
        userRepository.findById(getActualUserId())
            .orElseThrow(() -> new WrongToken("Token Incorrecto")));

    User.UserBuilder builder = User.builder();

    builder.userName(user.userName()).email(user.email()).password(user.password()).name(user.name()).id(user.id());

    for (String id : user.friends())
      userRepository.findById(id).ifPresent(
          repositoryEntity -> builder.friend(repositoryEntity.userName));

    for (String id : user.friendPetitionsSent())
      userRepository.findById(id).ifPresent(
          repositoryEntity -> builder.friendPetitionSent(repositoryEntity.userName));

    for (String id : user.friendPetitionsReceived())
      userRepository.findById(id).ifPresent(
          repositoryEntity -> builder.friendPetitionReceived(repositoryEntity.userName));

    return builder.build();
  }

  public void sendFriendPetition(String userName) throws InstanceNotFoundException {
    User sender = UserRepositoryEntityMapper.INSTANCE.UserRepositoryEntityToUser(
        userRepository.findById(getActualUserId())
            .orElseThrow(() -> new WrongToken("Token Incorrecto")));

    if (userName.equals(sender.userName())) throw new SocialContradiction("Non podes ser amigo de ti mesmo");

    User receiver = UserRepositoryEntityMapper.INSTANCE.UserRepositoryEntityToUser(
        userRepository.findByUserName(userName)
            .orElseThrow(() -> new InstanceNotFoundException("Usuario inexistente")));

    if (receiver.friends().contains(sender.id()))
      throw new SocialContradiction("Amizade xa existente");
    if (receiver.friendPetitionsReceived().contains(sender.id()) || receiver.friendPetitionsSent().contains(sender.id()))
      throw new SocialContradiction("Solicitude xa existente");

    userRepository.save(UserRepositoryEntityMapper.INSTANCE.UserToUserRepositoryEntity(
        sender.toBuilder().friendPetitionSent(receiver.id()).build()));

    userRepository.save(UserRepositoryEntityMapper.INSTANCE.UserToUserRepositoryEntity(
        receiver.toBuilder().friendPetitionReceived(sender.id()).build()));
  }

  public void manageFriendPetition(String userName, boolean accept) throws InstanceNotFoundException {
    User receiver = UserRepositoryEntityMapper.INSTANCE.UserRepositoryEntityToUser(
        userRepository.findById(getActualUserId())
            .orElseThrow(() -> new WrongToken("Token Incorrecto")));

    if (userName.equals(receiver.userName())) throw new SocialContradiction("Non podes ser amigo de ti mesmo");

    User sender = UserRepositoryEntityMapper.INSTANCE.UserRepositoryEntityToUser(
        userRepository.findByUserName(userName)
            .orElseThrow(() -> new InstanceNotFoundException("Usuario inexistente")));

    if (receiver.friends().contains(sender.id()))
      throw new SocialContradiction("Amizade xa existente");
    if (receiver.friendPetitionsSent().contains(sender.id()))
      throw new SocialContradiction("Solicitude en sentido contrario( " + receiver.userName() + " -> " + sender.userName() + " )");

    User.UserBuilder senderBuilder = sender.toBuilder().clearFriendPetitionsSent();

    boolean erased = false;

    for (String id : sender.friendPetitionsSent()) {
      if (id.equals(receiver.id())) erased = true;
      else senderBuilder.friendPetitionSent(id);
    }

    if (!erased) throw new SocialContradiction("Solicitude inexistente");

    User.UserBuilder receiverBuilder = receiver.toBuilder().clearFriendPetitionsReceived();

    for (String id : sender.friendPetitionsSent())
      if (!id.equals(receiver.id())) receiverBuilder.friendPetitionReceived(id);

    if (accept) {
      senderBuilder.friend(receiver.id());
      receiverBuilder.friend(sender.id());
    }

    userRepository.save(UserRepositoryEntityMapper.INSTANCE.UserToUserRepositoryEntity(
        senderBuilder.build()));

    userRepository.save(UserRepositoryEntityMapper.INSTANCE.UserToUserRepositoryEntity(
        receiverBuilder.build()));
  }

  public void removeFriend(String userName) throws InstanceNotFoundException {
    User remover = UserRepositoryEntityMapper.INSTANCE.UserRepositoryEntityToUser(
        userRepository.findById(getActualUserId())
            .orElseThrow(() -> new WrongToken("Token Incorrecto")));

    if (userName.equals(remover.userName())) throw new SocialContradiction("Non podes ser amigo de ti mesmo");

    User removed = UserRepositoryEntityMapper.INSTANCE.UserRepositoryEntityToUser(
        userRepository.findByUserName(userName)
            .orElseThrow(() -> new InstanceNotFoundException("Usuario inexistente")));

    if (remover.friendPetitionsReceived().contains(removed.id()) || remover.friendPetitionsSent().contains(removed.id()))
      throw new SocialContradiction("Solicitude en espera( " + remover.userName() + " -> " + removed.userName() + " )");

    User.UserBuilder senderBuilder = removed.toBuilder().clearFriends();

    boolean erased = false;

    for (String id : removed.friends()) {
      if (id.equals(remover.id())) erased = true;
      else senderBuilder.friend(id);
    }

    if (!erased) throw new SocialContradiction("Non existe amizade");

    User.UserBuilder receiverBuilder = remover.toBuilder().clearFriends();

    for (String id : removed.friends())
      if (!id.equals(remover.id())) receiverBuilder.friend(id);


    userRepository.save(UserRepositoryEntityMapper.INSTANCE.UserToUserRepositoryEntity(
        senderBuilder.build()));

    userRepository.save(UserRepositoryEntityMapper.INSTANCE.UserToUserRepositoryEntity(
        receiverBuilder.build()));
  }

  public List<String> searchUser(String keyWord) throws InstanceNotFoundException {
    Set<User> searchResult = UserRepositoryEntityMapper.INSTANCE.UserRepositoryEntityToUser(
        userRepository.findByUserNameContainingIgnoreCase(keyWord));

    return searchResult.stream().map(User::userName).collect(Collectors.toList());
  }

}
