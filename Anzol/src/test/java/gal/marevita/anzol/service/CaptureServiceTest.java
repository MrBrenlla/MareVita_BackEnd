package gal.marevita.anzol.service;

import gal.marevita.anzol.model.*;
import gal.marevita.anzol.repository.CaptureRepository;
import gal.marevita.anzol.repository.LatestCapturesRepository;
import gal.marevita.anzol.repository.UserRepository;
import gal.marevita.anzol.service.exceptions.NotAccesible;
import gal.marevita.commons.repositoryEntities.UserRepositoryEntity;
import gal.marevita.anzol.service.exceptions.WrongDataTime;
import gal.marevita.anzol.service.exceptions.WrongToken;
import gal.marevita.anzol.util.BaitEnum;
import gal.marevita.anzol.util.FishEnum;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import javax.management.InstanceNotFoundException;

import static org.junit.jupiter.api.Assertions.*;


import java.time.*;
import java.util.List;
import java.util.Set;

@SpringBootTest
@ActiveProfiles("test")
public class CaptureServiceTest {


  @Autowired
  UserRepository userRepository;
  @Autowired
  CaptureRepository captureRepository;
  @Autowired
  CapturesService capturesService;
  @Autowired
  LatestCapturesRepository latestCapturesRepository;

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

  private static final int SECURITY = 0;
  private static final ZonedDateTime DATETIME = ZonedDateTime.of(2025, 5, 10,14, 33,0,0, ZoneOffset.UTC);
  private static final GPSLocation GPS_LOC = new GPSLocation(42.694108, -9.029924);
  private static final String IMAGE_CAPTION = "Gran captura de lubinas";
  private static final List<String> IMAGES = List.of("img1.jpg", "img2.jpg");
  private static final List<String> BAITS = List.of(BaitEnum.LURA.toString(),BaitEnum.CAMARÓN.toString());
  private static final List<Fish> FISH = List.of(
      new Fish(FishEnum.PESCADA.toString(), 3),
      new Fish(FishEnum.XURELO.toString(), 1)
  );

  private final Capture testCapture = Capture.builder()
      .security(SECURITY)
      .dateTime(DATETIME)
      .gpsLocation(GPS_LOC)
      .imageCaption(IMAGE_CAPTION)
      .images(IMAGES)
      .baits(BAITS)
      .fish(FISH)
      .build();

  private String user1Id;
  private String user2Id;
  private String user3Id;

  @BeforeEach
  void setUp() {
    latestCapturesRepository.deleteAll();
    userRepository.deleteAll();
    captureRepository.deleteAll();

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

    SecurityContext context = SecurityContextHolder.createEmptyContext();
    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("None", null);
    auth.setDetails("");
    context.setAuthentication(auth);
    SecurityContextHolder.setContext(context);
  }

  @AfterEach
  void tearDown() {
    latestCapturesRepository.deleteAll();
    userRepository.deleteAll();
    captureRepository.deleteAll();
  }

  void setUser1Context(){
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(USERNAME, null);
    auth.setDetails(user1Id);
    context.setAuthentication(auth);
    SecurityContextHolder.setContext(context);
  }

  void setUser2Context(){
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(USERNAME2, null);
    auth.setDetails(user2Id);
    context.setAuthentication(auth);
    SecurityContextHolder.setContext(context);
  }

  void setUser3Context(){
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(USERNAME3, null);
    auth.setDetails(user3Id);
    context.setAuthentication(auth);
    SecurityContextHolder.setContext(context);
  }

  @Test
  public void testCapture() {
    setUser1Context();

    Capture saved = capturesService.saveCapture(testCapture);

    assertNotNull(saved.id());
    assertEquals(user1Id,saved.owner());
    assertEquals(SECURITY,saved.security());
    assertIterableEquals(List.of(),saved.likes());
    assertEquals(DATETIME,saved.dateTime());
    assertEquals(GPS_LOC,saved.gpsLocation());
    assertEquals(IMAGE_CAPTION,saved.imageCaption());
    assertEquals(IMAGES,saved.images());
    assertEquals(BAITS,saved.baits());
    assertEquals(FISH,saved.fish());
    assertNotEquals("",saved.location());
    assertFalse(saved.weatherConditions().isEmpty());
    System.out.println(saved);

    assertTrue(latestCapturesRepository.findById(saved.id()).isEmpty());

    ZonedDateTime now = ZonedDateTime.now();

    saved = capturesService.saveCapture(
        testCapture.toBuilder()
            .dateTime(now)
            .build());

    assertEquals(user1Id,saved.owner());
    assertEquals(SECURITY,saved.security());
    assertIterableEquals(List.of(),saved.likes());
    assertEquals(now,saved.dateTime());
    assertEquals(GPS_LOC,saved.gpsLocation());
    assertEquals(IMAGE_CAPTION,saved.imageCaption());
    assertEquals(IMAGES,saved.images());
    assertEquals(BAITS,saved.baits());
    assertEquals(FISH,saved.fish());
    assertNotEquals("",saved.location());
    assertFalse(saved.weatherConditions().isEmpty());
    System.out.println(saved);
    System.out.println(saved);

    assertTrue(latestCapturesRepository.findById(saved.id()).isPresent());
  }

  @Test
  public void testCaptureExceptions() {

    assertThrows(WrongToken.class, () -> capturesService.saveCapture(testCapture));

    setUser1Context();

    assertThrows(WrongDataTime.class, () -> capturesService.saveCapture(
        testCapture.toBuilder().dateTime(ZonedDateTime.now().plusDays(3)).build()));


  }

  @Test
  public void testGetUserCaptures() {
    setUser1Context();

    Capture c =capturesService.saveCapture(testCapture).toBuilder().owner(USERNAME).build();

    List<Capture> userCaptures = capturesService.getCaptures();

    assertIterableEquals(List.of(c),userCaptures);
  }

 @Test
  public void testGetCapturesExceptions() {
    assertThrows(WrongToken.class, () -> capturesService.getCaptures());
 }

 @Test
  public void testGetFriendCaptures() {

   setUser1Context();
   Capture c0 =capturesService.saveCapture(testCapture).toBuilder().owner(USERNAME).build();
   Capture c1 =capturesService.saveCapture(testCapture.toBuilder().security(1).build()).toBuilder().owner(USERNAME).build();
   Capture c2 =capturesService.saveCapture(testCapture.toBuilder().security(2).build()).toBuilder().owner(USERNAME).build();

   List<Capture> userCaptures = capturesService.getFriendCaptures();
   assertIterableEquals(List.of(),userCaptures);

   setUser2Context();
   userCaptures = capturesService.getFriendCaptures();
   assertIterableEquals(List.of(c0,c1),userCaptures);

   setUser3Context();
   userCaptures = capturesService.getFriendCaptures();
   assertIterableEquals(List.of(),userCaptures);
  }

  @Test
  public void testGetFriendCapturesExceptions() {
    assertThrows(WrongToken.class, () -> capturesService.getCaptures());
  }

  @Test
  public void testGetCapture() throws Exception {

    setUser1Context();
    Capture c0 =capturesService.saveCapture(testCapture).toBuilder().owner(USERNAME).build();
    Capture c1 =capturesService.saveCapture(testCapture.toBuilder().security(1).build()).toBuilder().owner(USERNAME).build();
    Capture c2 =capturesService.saveCapture(testCapture.toBuilder().security(2).build()).toBuilder().owner(USERNAME).build();

    assertEquals(c0,capturesService.getCapture(c0.id()));
    assertEquals(c1,capturesService.getCapture(c1.id()));
    assertEquals(c2,capturesService.getCapture(c2.id()));

    setUser2Context();
    assertEquals(c0,capturesService.getCapture(c0.id()));
    assertEquals(c1,capturesService.getCapture(c1.id()));
    assertThrows(NotAccesible.class, () -> capturesService.getCapture(c2.id()));

    setUser3Context();
    assertEquals(c0,capturesService.getCapture(c0.id()));
    assertThrows(NotAccesible.class, () -> capturesService.getCapture(c1.id()));
    assertThrows(NotAccesible.class, () -> capturesService.getCapture(c2.id()));
  }

  @Test
  public void testGetCaptureExceptions() throws Exception {
    assertThrows(WrongToken.class, () -> capturesService.getCapture("123"));

    setUser1Context();

    assertThrows(InstanceNotFoundException.class, () -> capturesService.getCapture("123"));
  }

  @Test
  public void testDeleteCapture() throws Exception {
    setUser1Context();
    Capture c0 =capturesService.saveCapture(testCapture).toBuilder().owner(USERNAME).build();
    Capture c1 =capturesService.saveCapture(testCapture.toBuilder().security(1).build()).toBuilder().owner(USERNAME).build();
    Capture c2 =capturesService.saveCapture(testCapture.toBuilder().security(2).build()).toBuilder().owner(USERNAME).build();

    capturesService.deleteCapture(c0.id());
    assertEquals(List.of(c1,c2),capturesService.getCaptures());

    capturesService.deleteCapture(c1.id());
    assertEquals(List.of(c2),capturesService.getCaptures());

    capturesService.deleteCapture(c2.id());
    assertEquals(List.of(),capturesService.getCaptures());

  }

  @Test
  public void testDeleteCaptureExceptions() throws Exception {
    assertThrows(WrongToken.class, () -> capturesService.deleteCapture("123"));

    setUser1Context();
    Capture c0 = capturesService.saveCapture(testCapture);

    assertThrows(InstanceNotFoundException.class, () -> capturesService.deleteCapture("123"));

    setUser2Context();

    assertThrows(NotAccesible.class, () -> capturesService.deleteCapture(c0.id()));
  }



  @Test
  public void testLikeCapture() throws Exception {
    setUser1Context();
    Capture c0 =capturesService.saveCapture(testCapture);

    assertIterableEquals(List.of(),c0.likes());

    capturesService.toggleLike(c0.id());
    assertIterableEquals(List.of(USERNAME),capturesService.getCapture(c0.id()).likes());

    setUser2Context();
    capturesService.toggleLike(c0.id());
    assertIterableEquals(List.of(USERNAME,USERNAME2),capturesService.getCapture(c0.id()).likes());
    capturesService.toggleLike(c0.id());
    assertIterableEquals(List.of(USERNAME),capturesService.getCapture(c0.id()).likes());
  }

  @Test
  public void testLikeCaptureExceptions() throws Exception {
    assertThrows(WrongToken.class, () -> capturesService.toggleLike("123"));

    setUser1Context();
    Capture c1 =capturesService.saveCapture(testCapture.toBuilder().security(1).build());
    Capture c2 =capturesService.saveCapture(testCapture.toBuilder().security(2).build());

    assertThrows(InstanceNotFoundException.class, () -> capturesService.toggleLike("123"));

    setUser2Context();
    assertIterableEquals(List.of(USERNAME2),capturesService.toggleLike(c1.id()).likes());
    assertThrows(NotAccesible.class, () -> capturesService.toggleLike(c2.id()));

    setUser3Context();
    assertThrows(NotAccesible.class, () -> capturesService.toggleLike(c1.id()));
    assertThrows(NotAccesible.class, () -> capturesService.toggleLike(c2.id()));
  }

  @Test
  public void testGetUserCapturesByUserName() throws Exception {
    setUser1Context();
    Capture c1 = capturesService.saveCapture(testCapture).toBuilder().owner(USERNAME).build();

    List<Capture> userCaptures = capturesService.getUserCaptures(USERNAME);
    assertIterableEquals(List.of(c1), userCaptures);

    setUser2Context();
    userCaptures = capturesService.getUserCaptures(USERNAME);
    assertIterableEquals(List.of(c1), userCaptures);

    userCaptures = capturesService.getUserCaptures(USERNAME2);
    assertTrue(userCaptures.isEmpty());
  }
  
}
