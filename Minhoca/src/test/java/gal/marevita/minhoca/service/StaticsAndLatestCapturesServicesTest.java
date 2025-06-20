package gal.marevita.minhoca.service;

import gal.marevita.commons.repositoryEntities.GPSLocationRepositoryEntity;
import gal.marevita.commons.repositoryEntities.LatestCapturesRepositoryEntity;
import gal.marevita.commons.repositoryEntities.UserRepositoryEntity;
import gal.marevita.minhoca.api.mappers.LatestCaptureDTOMapper;
import gal.marevita.minhoca.model.*;
import gal.marevita.minhoca.model.statistics.*;
import gal.marevita.minhoca.repository.CaptureRepository;
import gal.marevita.minhoca.repository.LatestCapturesRepository;
import gal.marevita.minhoca.repository.UserRepository;
import gal.marevita.minhoca.service.exceptions.WrongToken;
import gal.marevita.minhoca.service.mappers.CaptureMapper;
import gal.marevita.minhoca.service.mappers.LatestCaptureMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.management.InstanceNotFoundException;
import java.time.*;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class StaticsAndLatestCapturesServicesTest {

  @Autowired
  UserRepository userRepository;
  @Autowired
  CaptureRepository captureRepository;
  @Autowired
  LatestCapturesRepository latestCapturesRepository;
  @Autowired
  StatisticsService statisticsService;
  @Autowired
  LatestCapturesService latestCapturesService;
  @Autowired
  PasswordEncoder passwordEncoder;

  private final String USERNAME = "testUser";
  private final String EMAIL = "test@example.com";
  private final String PASSWORD = "securePassword123";
  private final String NAME = "name";
  private final User user1 = User.builder().userName(USERNAME).email(EMAIL).password(PASSWORD).name(NAME).build();

  private final String USERNAME2 = "testUser2";
  private final String EMAIL2 = "test2@example.com";
  private final User user2 = User.builder().userName(USERNAME2).email(EMAIL2).password(PASSWORD).name(NAME).build();

  private final String USERNAME3 = "testUser3";
  private final String EMAIL3 = "test3@example.com";
  private final User user3 = User.builder().userName(USERNAME3).email(EMAIL3).password(PASSWORD).name(NAME).build();

  // GPS para o test
  private static final GPSLocation GPS_LOC = new GPSLocation(42.694108, -9.029924);

  private static final GPSLocation GPS_LOC2 = new GPSLocation(42.694105, -9.029928);

  // Datos dunha captura
  private static final ZonedDateTime DATETIME = ZonedDateTime.of(2025, 5, 1,0, 11,0,0, ZoneOffset.UTC);
  private static final String IMAGE_CAPTION = "Gran captura de lubinas";
  private static final List<String> IMAGES = List.of("img1.jpg", "img2.jpg");
  private static final String LOCATION1 = "Location 1";
  private static final String LOCATION2 = "Location 2";
  private static final String LOCATION3 = "Location 3";
  private static final List<String> BAITS1 = List.of("MIÑOCA");
  private static final List<String> BAITS2 = List.of("AMEIXA");
  private static final List<String> BAITS3 = List.of("GAMBA");
  private static final List<Fish> FISH1 = List.of(new Fish("LUBIÑA", 1));
  private static final List<Fish> FISH2 = List.of(new Fish("OLLOMOL", 2));
  private static final List<Fish> FISH3 = List.of(new Fish("POLBO", 3));

  private final Capture.CaptureBuilder prefixCapture0 = Capture.builder()
      .security(0)
      .dateTime(DATETIME)
      .gpsLocation(GPS_LOC)
      .imageCaption(IMAGE_CAPTION)
      .images(IMAGES)
      .baits(BAITS1)
      .fish(FISH1)
      .location(LOCATION1);

  private final Capture.CaptureBuilder prefixCapture1 = Capture.builder()
      .security(1)
      .dateTime(DATETIME.plusDays(1).plusHours(1))
      .gpsLocation(GPS_LOC)
      .imageCaption(IMAGE_CAPTION)
      .images(IMAGES)
      .baits(BAITS2)
      .fish(FISH2)
      .location(LOCATION2);

  private final Capture.CaptureBuilder prefixCapture2 = Capture.builder()
      .security(2)
      .dateTime(DATETIME.plusDays(2).plusHours(2))
      .gpsLocation(GPS_LOC)
      .imageCaption(IMAGE_CAPTION)
      .images(IMAGES)
      .baits(BAITS3)
      .fish(FISH3)
      .location(LOCATION3);


  private Capture testCapture;

  private String user1Id;
  private String user2Id;
  private String user3Id;
  private String captureId;

  //Posibles valores Statistics
  CaptureInfo capture0Info;
  CaptureInfo capture1Info;
  CaptureInfo capture2Info;

  // Crear instancias de FishCount
  private final FishCount fish0 = new FishCount("LUBIÑA", 1);
  private final FishCount fish1 = new FishCount("OLLOMOL", 2);
  private final FishCount fish2 = new FishCount("POLBO", 3);

  // Crear instancias de Location
  private final Location location0 = new Location(LOCATION1, 1, List.of(fish0));
  private final Location location1 = new Location(LOCATION2, 1, List.of(fish1));
  private final Location location2 = new Location(LOCATION3, 1, List.of(fish2));
  
  // Crear instancias de Bait
  private final Bait bait0 = new Bait("MIÑOCA", 1, List.of(fish0));
  private final Bait bait1 = new Bait("AMEIXA", 1, List.of(fish1));
  private final Bait bait2 = new Bait("GAMBA", 1, List.of(fish2));



  @BeforeEach
  void setUp() {
    latestCapturesRepository.deleteAll();
    captureRepository.deleteAll();
    userRepository.deleteAll();

    // Usuario 1
    UserRepositoryEntity user1_1 = new UserRepositoryEntity();
    user1_1.userName = USERNAME;
    user1_1.email = EMAIL;
    user1_1.password = passwordEncoder.encode(PASSWORD);
    user1_1.name = NAME;
    user1_1 = userRepository.save(user1_1);
    user1Id = user1_1.id;

    // Usuario 2
    UserRepositoryEntity user2_1 = new UserRepositoryEntity();
    user2_1.userName = USERNAME2;
    user2_1.email = EMAIL2;
    user2_1.password = passwordEncoder.encode(PASSWORD);
    user2_1.name = NAME;
    user2_1.friends = Set.of(user1Id);
    user2Id = userRepository.save(user2_1).id;

    //Engadir user2 como amigo de user1
    user1_1.friends = Set.of(user2Id);
    userRepository.save(user1_1);

    // Usuario 3
    UserRepositoryEntity user3_1 = new UserRepositoryEntity();
    user3_1.userName = USERNAME3;
    user3_1.email = EMAIL3;
    user3_1.password = passwordEncoder.encode(PASSWORD);
    user3_1.name = NAME;
    user3Id = userRepository.save(user3_1).id;

    prefixCapture0.owner(user1Id).likes(Set.of(user3Id));
    prefixCapture1.owner(user1Id).likes(Set.of(user2Id, user1Id));
    prefixCapture2.owner(user1Id).likes(Set.of());

    String aux = captureRepository.save(CaptureMapper.INSTANCE.CaptureToCaptureRepositoryEntity(prefixCapture0.build())).id;
    capture0Info = new CaptureInfo(aux, DATETIME);

    aux =captureRepository.save(CaptureMapper.INSTANCE.CaptureToCaptureRepositoryEntity(prefixCapture1.build())).id;
    capture1Info = new CaptureInfo(aux,DATETIME.plusDays(1).plusHours(1));

    aux =captureRepository.save(CaptureMapper.INSTANCE.CaptureToCaptureRepositoryEntity(prefixCapture2.build())).id;
    capture2Info = new CaptureInfo(aux, DATETIME.plusDays(2).plusHours(2));
    
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("None", null);
    auth.setDetails("");
    context.setAuthentication(auth);
    SecurityContextHolder.setContext(context);
  }

  @AfterEach
  void tearDown() {
    latestCapturesRepository.deleteAll();
    captureRepository.deleteAll();
    userRepository.deleteAll();
  }

  void setUser1Context() {
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(USERNAME, null);
    auth.setDetails(user1Id);
    context.setAuthentication(auth);
    SecurityContextHolder.setContext(context);
  }

  void setUser2Context() {
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(USERNAME2, null);
    auth.setDetails(user2Id);
    context.setAuthentication(auth);
    SecurityContextHolder.setContext(context);
  }

  void setUser3Context() {
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(USERNAME3, null);
    auth.setDetails(user3Id);
    context.setAuthentication(auth);
    SecurityContextHolder.setContext(context);
  }


  @Test
  public void testGetStatistics() throws InstanceNotFoundException {
    setUser1Context();

    Statistics stats = statisticsService.getStatistics(user1.userName());

    assertEquals(capture0Info,stats.diverseCapture());
    assertEquals(capture1Info,stats.likedCapture());
    assertEquals(capture2Info,stats.biggerCapture());

    assertEquals(3,stats.totalCaptures());
    assertEquals(6,stats.totalFishCount());
    assertEquals(3,stats.totalLocations());

    assertIterableEquals(List.of(bait0,bait1,bait2).stream().sorted().toList(),stats.baits().stream().sorted().toList() );
    assertIterableEquals(List.of(location0,location1,location2).stream().sorted().toList(),stats.locations().stream().sorted().toList());
    assertIterableEquals(List.of(fish0,fish1,fish2).stream().sorted().toList(),stats.fishes().stream().sorted().toList());

    setUser2Context();

    stats = statisticsService.getStatistics(user1.userName());

    assertEquals(capture0Info,stats.diverseCapture());
    assertEquals(capture1Info,stats.likedCapture());
    assertEquals(capture1Info,stats.biggerCapture());

    assertEquals(2,stats.totalCaptures());
    assertEquals(3,stats.totalFishCount());
    assertEquals(2,stats.totalLocations());

    assertIterableEquals(List.of(bait0,bait1).stream().sorted().toList(),stats.baits().stream().sorted().toList());
    assertIterableEquals(List.of(location0,location1).stream().sorted().toList(),stats.locations().stream().sorted().toList());
    assertIterableEquals(List.of(fish0,fish1).stream().sorted().toList(),stats.fishes().stream().sorted().toList());

    setUser3Context();

    stats = statisticsService.getStatistics(user1.userName());

    assertEquals(capture0Info,stats.diverseCapture());
    assertEquals(capture0Info,stats.likedCapture());
    assertEquals(capture0Info,stats.biggerCapture());

    assertEquals(1,stats.totalCaptures());
    assertEquals(1,stats.totalFishCount());
    assertEquals(1,stats.totalLocations());

    assertIterableEquals(List.of(bait0),stats.baits());
    assertIterableEquals(List.of(location0),stats.locations());
    assertIterableEquals(List.of(fish0),stats.fishes());

  }

  @Test
  public void testGetStatisticExceptions() throws InstanceNotFoundException {
    assertThrows(WrongToken.class, () -> statisticsService.getStatistics(user1.userName()));

    setUser1Context();

    assertThrows(InstanceNotFoundException.class, () -> statisticsService.getStatistics("1234"));
  }

  @Test
  public void testGetLatestCaptures() {
    ZonedDateTime now = ZonedDateTime.now();
    ZonedDateTime validDate1 = now.minusDays(1).withNano(0);
    ZonedDateTime validDate2 = now.minusDays(3).withNano(0);

    LatestCapturesRepositoryEntity latest1 = new LatestCapturesRepositoryEntity();
    latest1.id="capture1";
    latest1.owner=user1Id;
    latest1.security=0;
    GPSLocationRepositoryEntity GPS_LOC_rep = new GPSLocationRepositoryEntity();
    GPS_LOC_rep.latitude=GPS_LOC.latitude();
    GPS_LOC_rep.longitude=GPS_LOC.longitude();
    latest1.gpsLocation = GPS_LOC_rep;
    latest1.dateTime = validDate1.toInstant();


    LatestCapturesRepositoryEntity latest2 = new LatestCapturesRepositoryEntity();
    latest2.id="capture2";
    latest2.owner=user2Id;
    latest2.security=0;
    GPSLocationRepositoryEntity GPS_LOC_rep2 = new GPSLocationRepositoryEntity();
    GPS_LOC_rep2.latitude=GPS_LOC2.latitude();
    GPS_LOC_rep2.longitude=GPS_LOC2.longitude();
    latest2.gpsLocation = GPS_LOC_rep2;
    latest2.dateTime = validDate2.toInstant();

    latestCapturesRepository.save(latest1);
    latestCapturesRepository.save(latest2);

    latest1.owner=USERNAME;
    latest2.owner=USERNAME2;

    List<LatestCapture> list = LatestCaptureMapper.INSTANCE.LatestCaptureRepositoryEntityToLatestCapture(List.of(latest1,latest2));


    setUser1Context();

    List<LatestCapture> latestCaptures = latestCapturesService.getLatestCaptures();
    assertIterableEquals(list,latestCaptures);

  }
  
  

}
