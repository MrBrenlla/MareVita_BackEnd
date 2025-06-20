package gal.marevita.anzol.service;

/* imports */

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import gal.marevita.anzol.model.*;
import gal.marevita.anzol.model.Period;
import gal.marevita.anzol.repository.AlertRepository;
import gal.marevita.anzol.repository.CaptureRepository;
import gal.marevita.anzol.repository.LatestCapturesRepository;
import gal.marevita.anzol.repository.UserRepository;
import gal.marevita.anzol.service.exceptions.NotAccesible;
import gal.marevita.anzol.service.exceptions.WrongToken;
import gal.marevita.anzol.util.BaitEnum;
import gal.marevita.anzol.util.FishEnum;
import gal.marevita.anzol.util.WeatherChecker;
import org.mockito.InjectMocks;
import org.mockito.Mock;


import gal.marevita.commons.repositoryEntities.UserRepositoryEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;

import javax.management.InstanceNotFoundException;
import java.time.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static gal.marevita.anzol.util.MocksContent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest
public class AlertServiceTest {

  @Autowired
  UserRepository userRepository;
  @Autowired
  CaptureRepository captureRepository;
  @Autowired
  AlertRepository alertRepository;
  @Autowired
  LatestCapturesRepository latestCapturesRepository;
  @Autowired
  AlertService alertService;
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
  private static final int SECURITY = 0;
  private static final ZonedDateTime DATETIME = ZonedDateTime.of(2025, 5, 10,14, 33,0,0, ZoneOffset.UTC);
  private static final String IMAGE_CAPTION = "Gran captura de lubinas";
  private static final List<String> IMAGES = List.of("img1.jpg", "img2.jpg");
  private static final List<String> BAITS = List.of(BaitEnum.LURA.toString(),BaitEnum.CAMARÓN.toString());
  private static final List<Fish> FISH = List.of(
      new Fish(FishEnum.PESCADA.toString(), 3),
      new Fish(FishEnum.XURELO.toString(), 1)
  );

  private final Capture prefixCapture = Capture.builder()
      .security(SECURITY)
      .dateTime(DATETIME)
      .gpsLocation(GPS_LOC)
      .imageCaption(IMAGE_CAPTION)
      .images(IMAGES)
      .baits(BAITS)
      .fish(FISH)
      .build();

  private Capture testCapture;

  private String user1Id;
  private String user2Id;
  private String user3Id;
  private String captureId;

  // Datos dunha alerta
  private static final String ALERT_NAME = "Alerta de Proba";
  private static final List<String> BAITS_ALERT = List.of(BaitEnum.MEXILLÓN.toString(),BaitEnum.CULLER.toString());
  private static final List<Fish> FISH_ALERT = List.of(
      Fish.builder().name(FishEnum.XURELO.toString()).build());
  private static final List<WeatherCondition> WEATHER_CONDITIONS = List.of(
      WeatherCondition.builder().name("temperature").value(25.).error(5.).build(),
      WeatherCondition.builder().name("pressure").value(1529.).error(25.).build()
  );

  // Datos dunha alerta 2
  private static final String ALERT_NAME2 = "Alerta de Proba2";
  private static final List<String> BAITS_ALERT2 = List.of(BaitEnum.GAMBA.toString(),BaitEnum.VINILO.toString());
  private static final List<Fish> FISH_ALERT2 = List.of(
      Fish.builder().name(FishEnum.AGULLA.toString()).build());
  private static final List<WeatherCondition> WEATHER_CONDITIONS2 = List.of(
      WeatherCondition.builder().name("temperature").value(26.).error(8.).build(),
      WeatherCondition.builder().name("wave_high").value(3.).error(0.5).build()
  );

  private final Alert testAlert = Alert.builder()
      .name(ALERT_NAME)
      .gpsLocation(GPS_LOC)
      .baits(BAITS_ALERT)
      .fish(FISH_ALERT)
      .weatherConditions(WEATHER_CONDITIONS)
      .build();

  private final Alert testAlert2 = Alert.builder()
      .name(ALERT_NAME2)
      .gpsLocation(GPS_LOC2)
      .baits(BAITS_ALERT2)
      .fish(FISH_ALERT2)
      .weatherConditions(WEATHER_CONDITIONS2)
      .build();

  @Autowired
  private CapturesService capturesService;


  @BeforeEach
  void setUp() {
    latestCapturesRepository.deleteAll();
    alertRepository.deleteAll();
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

    setUser1Context();

    testCapture = capturesService.saveCapture(prefixCapture);

    SecurityContext context = SecurityContextHolder.createEmptyContext();
    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("None", null);
    auth.setDetails("");
    context.setAuthentication(auth);
    SecurityContextHolder.setContext(context);

    MockitoAnnotations.openMocks(this);
  }

  @AfterEach
  void tearDown() {
    alertRepository.deleteAll();
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

  @Test
  public void testNewAlert() {
    setUser1Context();

    Alert saved =
        alertService.newAlert(
            testAlert
                .toBuilder()
                .relatedCapture(testCapture.id())
                .build()
        );

    assertNotNull(saved.id());
    assertEquals(user1Id, saved.owner());
    assertEquals(testCapture.id(), saved.relatedCapture());
    assertEquals(ALERT_NAME, saved.name());
    assertEquals(GPS_LOC, saved.gpsLocation());
    assertEquals(BAITS_ALERT, saved.baits());
    assertEquals(FISH_ALERT, saved.fish());
    assertNotEquals("", saved.location());
    assertNotNull(saved.location());
    assertIterableEquals(WEATHER_CONDITIONS, saved.weatherConditions());
    System.out.println(saved);

    captureRepository.deleteAll();

    saved =
        alertService.newAlert(
            testAlert
                .toBuilder()
                .relatedCapture(testCapture.id())
                .weatherCondition(new WeatherCondition("mal1", null, null))
                .weatherCondition(new WeatherCondition("mal2", 456., null))
                .weatherCondition(new WeatherCondition("mal3", null, 478.))
                .build()
        );

    assertNotNull(saved.id());
    assertEquals(user1Id, saved.owner());
    assertNull(saved.relatedCapture());
    assertEquals(ALERT_NAME, saved.name());
    assertEquals(GPS_LOC, saved.gpsLocation());
    assertEquals(BAITS_ALERT, saved.baits());
    assertEquals(FISH_ALERT, saved.fish());
    assertNotEquals("", saved.location());
    assertNotNull(saved.location());
    assertIterableEquals(WEATHER_CONDITIONS, saved.weatherConditions());
    System.out.println(saved);
  }

  @Test
  public void testNewAlertExceptions() {
    assertThrows(WrongToken.class, () -> alertService.newAlert(testAlert));
  }

  @Test
  public void testUpdateAlert() throws InstanceNotFoundException {
    setUser1Context();
    Alert original = alertService.newAlert(testAlert);

    Alert saved = alertService.updateAlert(original.id(), testAlert2);

    assertEquals(original.id(), saved.id());
    assertEquals(user1Id, saved.owner());
    assertNull(saved.relatedCapture());
    assertEquals(ALERT_NAME2, saved.name());
    assertEquals(GPS_LOC2, saved.gpsLocation());
    assertEquals(BAITS_ALERT2, saved.baits());
    assertEquals(FISH_ALERT2, saved.fish());
    assertNotEquals("", saved.location());
    assertNotNull(saved.location());
    assertIterableEquals(WEATHER_CONDITIONS2, saved.weatherConditions());
    System.out.println(saved);

    saved =
        alertService.updateAlert(original.id(),
            testAlert
                .toBuilder()
                .relatedCapture(testCapture.id())
                .weatherCondition(new WeatherCondition("mal1", null, null))
                .weatherCondition(new WeatherCondition("mal2", 456., null))
                .weatherCondition(new WeatherCondition("mal3", null, 478.))
                .build()
        );

    assertNotNull(saved.id());
    assertEquals(user1Id, saved.owner());
    assertEquals(testCapture.id(), saved.relatedCapture());
    assertEquals(ALERT_NAME, saved.name());
    assertEquals(GPS_LOC, saved.gpsLocation());
    assertEquals(BAITS_ALERT, saved.baits());
    assertEquals(FISH_ALERT, saved.fish());
    assertNotEquals("", saved.location());
    assertNotNull(saved.location());
    assertIterableEquals(WEATHER_CONDITIONS, saved.weatherConditions());
    System.out.println(saved);
  }

  @Test
  public void testUpdateAlertExceptions() throws InstanceNotFoundException {
    assertThrows(WrongToken.class, () -> alertService.updateAlert("123", testAlert));

    setUser1Context();
    assertThrows(InstanceNotFoundException.class, () -> alertService.updateAlert("123", testAlert));
    Alert alert = alertService.newAlert(testAlert);

    setUser2Context();
    assertThrows(NotAccesible.class, () -> alertService.updateAlert(alert.id(), testAlert2));
  }

  @Test
  public void testDeleteAlert() throws InstanceNotFoundException {
    setUser1Context();
    Alert original = alertService.newAlert(testAlert);

    alertService.deleteAlert(original.id());

    assertTrue(alertRepository.findById(original.id()).isEmpty());
  }

  @Test
  public void testDeleteAlertExceptions() throws InstanceNotFoundException {
    assertThrows(WrongToken.class, () -> alertService.deleteAlert("123"));

    setUser1Context();
    assertThrows(InstanceNotFoundException.class, () -> alertService.deleteAlert("123"));
    Alert alert = alertService.newAlert(testAlert);

    setUser2Context();
    assertThrows(NotAccesible.class, () -> alertService.deleteAlert(alert.id()));
  }

  @Test
  public void testgetAlerts() {
    setUser1Context();
    Alert alert1 = alertService.newAlert(testAlert);
    Alert alert2 = alertService.newAlert(testAlert2);

    List<Alert> alerts = alertService.getAlerts();

    assertIterableEquals(List.of(alert1, alert2), alerts);
  }

  @Test
  public void testgetAlertsExceptions() {
    assertThrows(WrongToken.class, () -> alertService.getAlerts());
  }

  @Test
  public void testGetAlert() throws InstanceNotFoundException {
    setUser1Context();
    Alert alert = alertService.newAlert(testAlert);

    Alert fetchedAlert = alertService.getAlert(alert.id());

    assertEquals(alert, fetchedAlert);
  }

  @Test
  public void testGetAlertExceptions() throws InstanceNotFoundException {
    assertThrows(WrongToken.class, () -> alertService.getAlert("1"));

    setUser1Context();
    assertThrows(InstanceNotFoundException.class, () -> alertService.getAlert("fakeId"));

    Alert alert = alertService.newAlert(testAlert);

    setUser2Context();
    assertThrows(NotAccesible.class, () -> alertService.getAlert(alert.id()));
  }


  @MockitoBean
  private RestTemplate restTemplate;


  void setMocks() throws Exception {

    ObjectMapper objectMapper = new ObjectMapper();

    TypeReference<Map<String, Object>> typeRef = new TypeReference<>() {};
    Map<String, Object> mock = objectMapper.readValue(MockMeteoPast(), typeRef);

    when(restTemplate.getForObject(startsWith("https://api.open-meteo.com"), eq((Map.class))))
        .thenReturn(mock);

    typeRef = new TypeReference<>() {};
    mock = objectMapper.readValue(MockMarinePast(), typeRef);

    when(restTemplate.getForObject(startsWith("https://marine-api.open-meteo.com"), eq(Map.class)))
        .thenReturn(mock);

    TypeReference<List<Map<String, Object>>> typeRef2 = new TypeReference<>() {};
    List<Map<String, Object>> mock2 = objectMapper.readValue(MockMoonPast(), typeRef2);

    when(restTemplate.getForObject(startsWith("https://api.viewbits.com"), eq(List.class)))
        .thenReturn(mock2);

    when(restTemplate.getForObject(startsWith("http://api.geonames.org"), eq(String.class)))
        .thenReturn(MockGeoName());
  }


  @Test
  public void testWetherChecker() throws Exception {

    setMocks();
    setUser1Context();

    Alert alert = testAlert.toBuilder()
        .clearWeatherConditions()
        .weatherConditions(List.of(
            new WeatherCondition("temperature", 20., 3.),
            new WeatherCondition("cloud_cover", 0., 5.)
        ))
        .gpsLocation(GPS_LOC)
        .build();

    alert = alertService.newAlert(alert);

    List<Period> periods = alertService.checkAlert(alert.id());

    LocalDate d = LocalDate.now().plusDays(1);

    //Datas basados nos mocks
    Period period = new Period(alert,
        ZonedDateTime.of(d.getYear(),d.getMonthValue(), d.getDayOfMonth(), 1,0,0,0, ZoneOffset.UTC),
        ZonedDateTime.of(d.getYear(),d.getMonthValue(), d.getDayOfMonth(), 5,0,0,0, ZoneOffset.UTC)
    );

    d = LocalDate.now().plusDays(6);
    LocalDate d2 = LocalDate.now().plusDays(7);

    Period period2 = new Period(alert,
        ZonedDateTime.of(d.getYear(),d.getMonthValue(), d.getDayOfMonth(),18,0,0,0, ZoneOffset.UTC),
        ZonedDateTime.of(d2.getYear(),d2.getMonthValue(), d2.getDayOfMonth(),0,0,0,0, ZoneOffset.UTC)
    );
    List<Period> expected = List.of(period,period2);

    assertIterableEquals(expected, periods);

  }
}








