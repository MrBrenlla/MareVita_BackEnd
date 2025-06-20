package gal.marevita.minhoca.service;

import gal.marevita.minhoca.model.LatestCapture;
import gal.marevita.minhoca.model.User;
import gal.marevita.minhoca.repository.LatestCapturesRepository;
import gal.marevita.minhoca.repository.UserRepository;
import gal.marevita.minhoca.service.exceptions.WrongToken;
import gal.marevita.minhoca.service.mappers.LatestCaptureMapper;
import gal.marevita.minhoca.service.mappers.UserRepositoryEntityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.management.InstanceNotFoundException;
import java.util.List;
import java.util.Objects;

import static gal.marevita.minhoca.security.JwtUtil.getActualUserId;

@Component
public class LatestCapturesService {

  @Autowired
  LatestCapturesRepository latestCapturesRepository;
  @Autowired
  UserRepository userRepository;

  public List<LatestCapture> getLatestCaptures() {
    User user = UserRepositoryEntityMapper.INSTANCE.UserRepositoryEntityToUser(
        userRepository.findById(getActualUserId())
            .orElseThrow(() -> new WrongToken("Token Incorrecto")));

    List<LatestCapture> latestCaptures = LatestCaptureMapper.INSTANCE.LatestCaptureRepositoryEntityToLatestCapture(
        latestCapturesRepository.findAll());

    return latestCaptures.stream().map(latestCapture -> adapt(latestCapture, user))
        .filter(Objects::nonNull).toList();
  }

  private LatestCapture adapt(LatestCapture latestCapture, User user) {

    User owner;
    try {
      owner = UserRepositoryEntityMapper.INSTANCE.UserRepositoryEntityToUser(
          userRepository.findById(latestCapture.owner())
              .orElseThrow(() -> new InstanceNotFoundException("Token Incorrecto")));
    } catch (InstanceNotFoundException e) {
      return null;
    }

    if (!owner.id().equals(user.id())) {
      switch (latestCapture.security()) {
        case 2:
          return null;
        case 1:
          if (!owner.friends().contains(user.id())) return null;
        default:
      }
    }

    return latestCapture.toBuilder().owner(owner.userName()).build();
  }
}
