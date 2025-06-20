package gal.marevita.minhoca.service;

import gal.marevita.minhoca.model.Capture;
import gal.marevita.minhoca.model.Fish;
import gal.marevita.minhoca.model.User;
import gal.marevita.minhoca.model.statistics.*;
import gal.marevita.minhoca.repository.CaptureRepository;
import gal.marevita.minhoca.repository.UserRepository;
import gal.marevita.minhoca.service.exceptions.WrongToken;
import gal.marevita.minhoca.service.mappers.CaptureInfoMapper;
import gal.marevita.minhoca.service.mappers.CaptureMapper;
import gal.marevita.minhoca.service.mappers.UserRepositoryEntityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.management.InstanceNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static gal.marevita.minhoca.security.JwtUtil.getActualUserId;

@Component
public class StatisticsService {

  @Autowired
  private UserRepository userRepository;
  @Autowired
  private CaptureRepository captureRepository;

  public Statistics getStatistics(String userName) throws InstanceNotFoundException {
    List<Capture> captures = getCaptures(userName);
    return calculateStatistics(captures);
  }

  private List<Capture> getCaptures(String userName) throws InstanceNotFoundException {
    User user = UserRepositoryEntityMapper.INSTANCE.UserRepositoryEntityToUser(
        userRepository.findById(getActualUserId())
            .orElseThrow(() -> new WrongToken("Token Incorrecto")));

    User owner = UserRepositoryEntityMapper.INSTANCE.UserRepositoryEntityToUser(
        userRepository.findByUserName(userName)
            .orElseThrow(() -> new InstanceNotFoundException("Usuario inexistente")));

    List<Capture> captures = captureRepository.findByOwner(owner.id())
        .stream().map(CaptureMapper.INSTANCE::CaptureRepositoryEntityToCapture).toList();

    if (user.id().equals(owner.id())) return captures;

    int access;
    if (owner.friends().contains(user.id())) access = 1;
    else access = 0;

    return captures.stream().filter(c -> c.security() <= access).toList();
  }

  private Statistics calculateStatistics(List<Capture> captures) {

    Statistics.StatisticsBuilder builder = Statistics.builder();

    if (captures.isEmpty()) return builder.build();

    Capture biggerCapture = captures.get(0);
    int fishCount = biggerCapture.fish().stream().mapToInt(Fish::quantity).sum();
    Capture diverseCapture = captures.get(0);
    Capture likedCapture = captures.get(0);

    for (Capture c : captures) {
      if (c.fish().size() > diverseCapture.fish().size()) diverseCapture = c;
      if (c.likes().size() > likedCapture.likes().size()) likedCapture = c;
      if (c.fish().stream().mapToInt(Fish::quantity).sum() > fishCount) {
        fishCount = c.fish().stream().mapToInt(Fish::quantity).sum();
        biggerCapture = c;
      }
    }

    List<Bait> baits = calculateBaits(captures);
    List<Location> locations = calculateLocations(captures);
    List<FishCount> fishCounts = calculateFishCount(captures);

    return builder
        .baits(baits)
        .locations(locations)
        .fishes(fishCounts)
        .totalLocations(locations.size())
        .totalCaptures(captures.size())
        .totalFishCount(fishCounts
            .stream().mapToInt(FishCount::number)
            .sum())
        .biggerCapture(info(biggerCapture))
        .diverseCapture(info(diverseCapture))
        .likedCapture(info(likedCapture))
        .build();
  }

  private CaptureInfo info(Capture capture) {
    return CaptureInfoMapper.INSTANCE.toCaptureInfo(capture);
  }

  private List<Bait> calculateBaits(List<Capture> captures) {
    Map<String, List<Capture>> baitMap = new HashMap<>();

    for (Capture c : captures) {
      for (Fish f : c.fish()) {
        for (String b : c.baits()) {
          List<Capture> aux = baitMap.getOrDefault(b, new ArrayList<>());
          aux.add(c);
          baitMap.put(b, aux);
        }
      }
    }

    List<Bait> baitList = new ArrayList<>();

    baitMap.forEach((name, list) ->
        baitList.add(Bait.builder()
            .name(name)
            .times(list.size())
            .fishes(calculateFishCount(list))
            .build()));

    return baitList;
  }

  private List<Location> calculateLocations(List<Capture> captures) {
    Map<String, List<Capture>> locationMap = new HashMap<>();

    for (Capture c : captures) {
      for (Fish f : c.fish()) {
        List<Capture> aux = locationMap.getOrDefault(c.location(), new ArrayList<>());
        aux.add(c);
        locationMap.put(c.location(), aux);
      }
    }

    List<Location> locationList = new ArrayList<>();

    locationMap.forEach((name, list) ->
        locationList.add(Location.builder()
            .name(name)
            .times(list.size())
            .fishes(calculateFishCount(list))
            .build()));

    return locationList;
  }

  private List<FishCount> calculateFishCount(List<Capture> captures) {
    Map<String, Integer> fishCount = new HashMap<>();

    for (Capture c : captures) {
      for (Fish f : c.fish()) {
        fishCount.put(f.name(), fishCount.getOrDefault(f.name(), 0) + f.quantity());
      }
    }
    List<FishCount> list = new ArrayList<>();

    fishCount.forEach((name, number) ->
        list.add(FishCount.builder().name(name).number(number).build()));

    return list;
  }

}
