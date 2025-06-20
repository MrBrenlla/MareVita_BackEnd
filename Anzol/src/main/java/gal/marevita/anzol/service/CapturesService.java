package gal.marevita.anzol.service;

import gal.marevita.anzol.model.Capture;
import gal.marevita.anzol.model.LatestCapture;
import gal.marevita.anzol.model.User;
import gal.marevita.anzol.repository.CaptureRepository;
import gal.marevita.anzol.repository.LatestCapturesRepository;
import gal.marevita.anzol.repository.UserRepository;
import gal.marevita.anzol.service.exceptions.NotAccesible;
import gal.marevita.anzol.service.exceptions.WrongDataTime;
import gal.marevita.anzol.service.exceptions.WrongParameter;
import gal.marevita.anzol.service.exceptions.WrongToken;
import gal.marevita.anzol.service.mappers.CaptureMapper;
import gal.marevita.anzol.service.mappers.LatestCaptureMapper;
import gal.marevita.anzol.service.mappers.UserRepositoryEntityMapper;
import gal.marevita.anzol.util.GPStoName;
import gal.marevita.anzol.util.WeatherRetriever;
import gal.marevita.commons.repositoryEntities.CaptureRepositoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.management.InstanceNotFoundException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static gal.marevita.anzol.security.JwtUtil.getActualUserId;

@Component
public class CapturesService {

  @Autowired
  private UserRepository userRepository;
  @Autowired
  private CaptureRepository captureRepository;
  @Autowired
  private WeatherRetriever weatherRetriever;
  @Autowired
  private GPStoName gpstoName;
  @Autowired
  private LatestCapturesRepository latestCapturesRepository;

  @Value("${IMAGES_PATH:./images/profiles}")
  private String imagesPath;

  public Capture saveCapture(Capture capture) {
    User user = UserRepositoryEntityMapper.INSTANCE.UserRepositoryEntityToUser(
        userRepository.findById(getActualUserId())
            .orElseThrow(() -> new WrongToken("Token Incorrecto")));

    if (capture.dateTime() == null) {
      throw new WrongDataTime("A data e hora non poden ser nulas");
    }

    ZoneId zonaMadrid = ZoneId.of("Europe/Madrid");

    ZonedDateTime zonedDateTime = ZonedDateTime.now(zonaMadrid);

    if (capture.dateTime().isAfter(zonedDateTime)) {
      throw new WrongDataTime("A data e hora( " + capture.dateTime() +
          " ) > momento actual(" + zonedDateTime + ")");
    }

    if (capture.gpsLocation() == null || capture.gpsLocation().latitude() == null || capture.gpsLocation().longitude() == null) {
      throw new WrongParameter("A localizacion non poden ser nulas");
    }

    Capture.CaptureBuilder builder = Capture.builder()
        .security(capture.security())
        .likes(new ArrayList<>())
        .dateTime(capture.dateTime())
        .fish(capture.fish())
        .baits(capture.baits())
        .images(capture.images())
        .gpsLocation(capture.gpsLocation())
        .imageCaption(capture.imageCaption())
        .owner(getActualUserId())
        .weatherConditions(weatherRetriever
            .getWeatherData(
                capture.gpsLocation().latitude(),
                capture.gpsLocation().longitude(),
                capture.dateTime()
            ))
        .location(gpstoName.obterNomeLugar(
            capture.gpsLocation().latitude(),
            capture.gpsLocation().longitude()
        ));

    CaptureRepositoryEntity entity = CaptureMapper.INSTANCE.CaptureToCaptureRepositoryEntity(builder.build());

    Capture response = CaptureMapper.INSTANCE.CaptureRepositoryEntityToCapture(captureRepository.save(
        entity)
    );

    if (response.dateTime().isBefore(zonedDateTime.minusDays(7))) return response;

    latestCapturesRepository.save(LatestCaptureMapper.INSTANCE.LatestCaptureToLatestCaptureRepositoryEntity(
        LatestCapture.builder()
            .id(response.id())
            .owner(response.owner())
            .gpsLocation(response.gpsLocation())
            .security(response.security())
            .dateTime(response.dateTime()
                .toInstant()
            )
            .build()
    ));

    return response;
  }


  public List<Capture> getCaptures() {
    User user = UserRepositoryEntityMapper.INSTANCE.UserRepositoryEntityToUser(
        userRepository.findById(getActualUserId())
            .orElseThrow(() -> new WrongToken("Token Incorrecto")));

    return captureRepository.findByOwner(user.id()).stream()
        .map(CaptureMapper.INSTANCE::CaptureRepositoryEntityToCapture)
        .map(c -> c.toBuilder().owner(user.userName()).build())
        .map(this::translateLikes)
        .toList();
  }

  public List<Capture> getFriendCaptures() {
    User user = UserRepositoryEntityMapper.INSTANCE.UserRepositoryEntityToUser(
        userRepository.findById(getActualUserId())
            .orElseThrow(() -> new WrongToken("Token Incorrecto")));

    List<Capture> list = new ArrayList<>();

    for (String friendId : user.friends()) {
      try {
        User owner = userRepository.findById(friendId)
            .map(UserRepositoryEntityMapper.INSTANCE::UserRepositoryEntityToUser)
            .orElseThrow(() -> new InstanceNotFoundException("Usuario inexistente"));
        list.addAll(captureRepository.findByOwner(friendId).stream()
            .map(CaptureMapper.INSTANCE::CaptureRepositoryEntityToCapture)
            .filter(c -> c.security() < 2)
            .map(c -> c.toBuilder().owner(owner.userName()).build())
            .map(this::translateLikes)
            .toList());
      } catch (Exception e) {
      }
    }

    list.sort(Comparator.comparing(Capture::dateTime));

    return list;
  }

  public Capture getCapture(String id) throws InstanceNotFoundException {
    User user = UserRepositoryEntityMapper.INSTANCE.UserRepositoryEntityToUser(
        userRepository.findById(getActualUserId())
            .orElseThrow(() -> new WrongToken("Token Incorrecto")));

    Capture c = CaptureMapper.INSTANCE.CaptureRepositoryEntityToCapture(
        captureRepository.findById(id)
            .orElseThrow(() -> new InstanceNotFoundException("Capture inexistente")));

    User owner = UserRepositoryEntityMapper.INSTANCE.UserRepositoryEntityToUser(
        userRepository.findById(c.owner())
            .orElseThrow(() -> new IllegalStateException("Captura sen usuario existente")));

    if (!c.owner().equals(user.id()) && c.security() > 0)
      if (c.security() == 1) {
        if (!owner.friends().contains(user.id())) throw new NotAccesible("Captura non publica");
      } else throw new NotAccesible("Captura privada");

    return translateLikes(c.toBuilder().owner(owner.userName()).build());
  }

  public List<Capture> getUserCaptures(String userName) throws InstanceNotFoundException {
    User currentUser = UserRepositoryEntityMapper.INSTANCE.UserRepositoryEntityToUser(
        userRepository.findById(getActualUserId())
            .orElseThrow(() -> new WrongToken("Token Incorrecto"))
    );

    User owner = userRepository.findByUserName(userName)
        .map(UserRepositoryEntityMapper.INSTANCE::UserRepositoryEntityToUser)
        .orElseThrow(() -> new InstanceNotFoundException("Usuario inexistente"));

    return captureRepository.findByOwner(owner.id()).stream()
        .map(CaptureMapper.INSTANCE::CaptureRepositoryEntityToCapture)
        .filter(c -> {
          if (owner.id().equals(currentUser.id())) return true;
          if (c.security() == 0) return true;
          if (c.security() == 1) return owner.friends().contains(currentUser.id());
          return false;
        })
        .map(c -> c.toBuilder().owner(owner.userName()).build())
        .map(this::translateLikes)
        .sorted(Comparator.comparing(Capture::dateTime))
        .toList();
  }

  public void deleteCapture(String captureId) throws InstanceNotFoundException {
    User user = UserRepositoryEntityMapper.INSTANCE.UserRepositoryEntityToUser(
        userRepository.findById(getActualUserId())
            .orElseThrow(() -> new WrongToken("Token Incorrecto"))
    );

    Capture capture = CaptureMapper.INSTANCE.CaptureRepositoryEntityToCapture(
        captureRepository.findById(captureId)
            .orElseThrow(() -> new InstanceNotFoundException("Capture inexistente"))
    );

    if (!capture.owner().equals(user.id())) {
      throw new NotAccesible("Non se poden borrar capturas axenas");
    }

    captureRepository.deleteById(captureId);
    latestCapturesRepository.deleteById(captureId);
  }

  public Capture toggleLike(String captureId) throws InstanceNotFoundException {
    User user = UserRepositoryEntityMapper.INSTANCE.UserRepositoryEntityToUser(
        userRepository.findById(getActualUserId())
            .orElseThrow(() -> new WrongToken("Token Incorrecto"))
    );
    Capture capture = CaptureMapper.INSTANCE.CaptureRepositoryEntityToCapture(
        captureRepository.findById(captureId)
            .orElseThrow(() -> new InstanceNotFoundException("Capture inexistente"))
    );

    if (!capture.owner().equals(user.id()) && capture.security() > 0)
      if (capture.security() == 1) {
        User owner = UserRepositoryEntityMapper.INSTANCE.UserRepositoryEntityToUser(
            userRepository.findById(capture.owner())
                .orElseThrow(() -> new IllegalStateException("Captura sen usuario existente")));
        if (!owner.friends().contains(user.id())) throw new NotAccesible("Captura non publica");
      } else throw new NotAccesible("Captura privada");


    Capture.CaptureBuilder builder = capture.toBuilder().clearLikes();
    boolean found = false;

    for (String userId : capture.likes()) {
      if (userId.equals(user.id())) found = true;
      else builder.like(userId);
    }

    if (!found) builder.like(user.id());

    capture = CaptureMapper.INSTANCE.CaptureRepositoryEntityToCapture(captureRepository.save(
        CaptureMapper.INSTANCE.CaptureToCaptureRepositoryEntity(builder.build())));

    Capture c = translateLikes(capture);

    return c;
  }

  private Capture translateLikes(Capture capture) {

    return capture.toBuilder().clearLikes()
        .likes(capture.likes().stream().map(
            id -> userRepository.findById(id).orElseThrow().userName).toList())
        .build();
  }

}
