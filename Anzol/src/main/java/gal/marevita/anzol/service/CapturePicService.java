package gal.marevita.anzol.service;

import gal.marevita.anzol.model.User;
import gal.marevita.anzol.repository.UserRepository;
import gal.marevita.anzol.service.exceptions.WrongToken;
import gal.marevita.anzol.service.mappers.UserRepositoryEntityMapper;
import gal.marevita.commons.repositoryEntities.UserRepositoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.management.InstanceNotFoundException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

import static gal.marevita.anzol.security.JwtUtil.getActualUserId;

@Component
public class CapturePicService {

  @Value("${IMAGES_PATH:./images/captures}")
  private String imagesPath;

  @Autowired
  private UserRepository userRepository;

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

  public String saveCapturePic(MultipartFile profilePic) throws InstanceNotFoundException, IOException {
    System.out.println(getActualUserId());
    Optional<UserRepositoryEntity> userO = userRepository.findById(getActualUserId());
    User user = UserRepositoryEntityMapper.INSTANCE.UserRepositoryEntityToUser(userO.orElseThrow(() -> new WrongToken("Token Incorrecto")));

    Path folder = Path.of(imagesPath + "/" + user.id());
    if (!Files.exists(folder)) {
      Files.createDirectories(folder);
    }

    String originalFilename = profilePic.getOriginalFilename();
    String extension = "";

    if (originalFilename != null && originalFilename.contains(".")) {
      extension = originalFilename.substring(originalFilename.lastIndexOf("."));
    }

    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    String uniqueID = UUID.randomUUID().toString();
    String imgId = "img_" + timestamp + "_" + uniqueID;

    Path destination = folder.resolve(imgId + extension);

    Files.copy(profilePic.getInputStream(), destination);

    return imgId;
  }

  public Path getCapturePic(String userName, String imgId) throws InstanceNotFoundException, IOException {

    User user = UserRepositoryEntityMapper.INSTANCE.UserRepositoryEntityToUser(
        userRepository.findByUserName(userName)
            .orElseThrow(() -> new InstanceNotFoundException("Usuario inexistente")));

    Path folder = Path.of(imagesPath + "/" + user.id());
    Path destination = findFileByBaseName(folder, imgId)
        .orElseThrow(() -> new InstanceNotFoundException("Imaxe inexistente"));

    return destination;
  }

}
