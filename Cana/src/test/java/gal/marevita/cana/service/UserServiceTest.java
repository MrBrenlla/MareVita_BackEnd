package gal.marevita.cana.service;

import gal.marevita.cana.model.User;
import gal.marevita.cana.repository.UserRepository;
import gal.marevita.commons.repositoryEntities.UserRepositoryEntity;
import gal.marevita.cana.security.JwtUtil;
import gal.marevita.cana.service.exceptions.*;
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

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class UserServiceTest {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private JwtUtil jwtUtil;

  @Autowired
  private UserService userService;

  @Autowired
  private PasswordEncoder passwordEncoder;

  private final String USERNAME = "testUser";
  private final String USERNAME2 = "testUser2";
  private final String USERNAME3 = "testUser3";

  private final String EMAIL = "test@example.com";
  private final String EMAIL2 = "test@example2.com";
  private final String EMAIL3 = "test@example3.com";

  private final String PASSWORD = "securePassword123";
  private final String PASSWORD2 = "securePassword1234";
  private final String PASSWORD3 = "securePassword12345";

  private final String NAME = "name";

  private final User user1 = User.builder().userName(USERNAME).email(EMAIL).password(PASSWORD).name(NAME).build();
  private final User user2 = User.builder().userName(USERNAME2).email(EMAIL2).password(PASSWORD2).name(NAME).build();
  private final User user3 = User.builder().userName(USERNAME3).email(EMAIL3).password(PASSWORD3).name(NAME).build();

  private String user1Id;
  private String user3Id;

  Set<String> setUserName1 = Set.of(USERNAME);
  Set<String> setUserName3 = Set.of(USERNAME3);
  Set<String> setUser1;
  Set<String> setUser3;

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();

    // Usuario 1
    UserRepositoryEntity user1_1 = new UserRepositoryEntity();
    user1_1.userName = USERNAME;
    user1_1.email = EMAIL;
    user1_1.password = passwordEncoder.encode(PASSWORD);
    user1_1.name = NAME;
    user1Id = userRepository.save(user1_1).id;

    setUser1 = Set.of(user1Id);

    // Usuario 3
    UserRepositoryEntity user3_3 = new UserRepositoryEntity();
    user3_3.userName = USERNAME3;
    user3_3.email = EMAIL3;
    user3_3.password = passwordEncoder.encode(PASSWORD3);
    user3_3.name = NAME;
    user3Id = userRepository.save(user3_3).id;

    setUser3 = Set.of(user3Id);


    SecurityContext context = SecurityContextHolder.createEmptyContext();
    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("None", null);
    auth.setDetails("");
    context.setAuthentication(auth);
    SecurityContextHolder.setContext(context);
  }

  @AfterEach
  void tearDown() {
    userRepository.deleteAll();
  }

  void setUser1Context(){
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(USERNAME, null);
    auth.setDetails(user1Id);
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
  void testRegister() throws Exception {

    userService.register(user2);

    UserRepositoryEntity userRepositoryEntity = userRepository.findByUserName(user2.userName())
        .orElseThrow(() -> new InstanceNotFoundException("O usuario non se rexistrou"));

    assertEquals(USERNAME2, userRepositoryEntity.userName);
    assertEquals(EMAIL2, userRepositoryEntity.email);
    assertTrue(passwordEncoder.matches(PASSWORD2, userRepositoryEntity.password));
    assertEquals(NAME, userRepositoryEntity.name);
    assertTrue(userRepositoryEntity.friends.isEmpty());
    assertTrue(userRepositoryEntity.friendPetitionsSent.isEmpty());
    assertTrue(userRepositoryEntity.friendPetitionsReceived.isEmpty());

  }

  @Test
  void testRegisterWrongFormat() throws Exception {


    User testWrongUserName = user1.toBuilder().email(EMAIL2).userName(EMAIL2).build();
    User testWrongEmail = user1.toBuilder().email(USERNAME2).userName(USERNAME2).build();

    assertThrows(InvalidUserName.class, () -> userService.register(testWrongUserName));
    assertThrows(InvalidEmail.class, () -> userService.register(testWrongEmail));


    User testWrongPassword = user1.toBuilder().email(EMAIL2).userName(USERNAME2).password("aA1").build();
    assertThrows(InvalidPassword.class, () -> userService.register(testWrongPassword));

    User testWrongPassword2 = user1.toBuilder().email(EMAIL2).userName(USERNAME2).password("abcd1234").build();
    assertThrows(InvalidPassword.class, () -> userService.register(testWrongPassword2));

    User testWrongPassword3 = user1.toBuilder().email(EMAIL2).userName(USERNAME2).password("ABCDabcd").build();
    assertThrows(InvalidPassword.class, () -> userService.register(testWrongPassword3));

    User testWrongPassword4 = user1.toBuilder().email(EMAIL2).userName(USERNAME2).password("ABCD1234").build();
    assertThrows(InvalidPassword.class, () -> userService.register(testWrongPassword4));
  }

  @Test
  void testRegisterAlreadyInUse() throws Exception {

    User testSameUserName = user1.toBuilder().email(EMAIL2).build();
    User testSameEmail = user1.toBuilder().userName(USERNAME2).build();

    assertThrows(UserNameAlreadyInUse.class, () -> userService.register(testSameUserName));
    assertThrows(EmailAlreadyInUse.class, () -> userService.register(testSameEmail));
  }

  @Test
  void testLogIn() throws Exception {
    String token = userService.login(user1.userName(), user1.password());
    assertTrue(jwtUtil.isTokenValid(token, USERNAME));

    token = userService.login(user1.email(), user1.password());
    assertTrue(jwtUtil.isTokenValid(token, USERNAME));
  }

  @Test
  void testLogInWrongPassword() throws Exception {
    assertThrows(WrongPassword.class, () -> userService.login(USERNAME, PASSWORD2));
    assertThrows(WrongPassword.class, () -> userService.login(EMAIL, PASSWORD2));
  }

  @Test
  void testLogInUserNotFound() throws Exception {
    assertThrows(InstanceNotFoundException.class, () -> userService.login(user2.userName(), user2.password()));
    assertThrows(InstanceNotFoundException.class, () -> userService.login(user2.userName(), user2.password()));
  }

  @Test
  void testGetUser() throws Exception {
    User answer = userService.getUser(USERNAME);

    assertNotNull(answer);
    assertEquals(USERNAME, answer.userName());
    assertEquals(EMAIL, answer.email());
    assertTrue(passwordEncoder.matches(PASSWORD, answer.password()));
    assertEquals(NAME, answer.name());
    assertTrue(answer.friends().isEmpty());
    assertTrue(answer.friendPetitionsSent().isEmpty());
    assertTrue(answer.friendPetitionsReceived().isEmpty());


    assertThrows(InstanceNotFoundException.class, () -> userService.getUser(USERNAME2));
  }

  @Test
  void testUpdateUserSuccessfully() throws Exception {

    setUser1Context();

    userService.updateUser(user2);

    UserRepositoryEntity userRepositoryEntity = userRepository.findById(user1Id)
        .orElseThrow(() -> new InstanceNotFoundException("O usuario non existe"));

    assertEquals(USERNAME2, userRepositoryEntity.userName);
    assertEquals(EMAIL2, userRepositoryEntity.email);
    assertTrue(passwordEncoder.matches(PASSWORD, userRepositoryEntity.password));
    assertEquals(NAME, userRepositoryEntity.name);
    assertTrue(userRepositoryEntity.friends.isEmpty());
    assertTrue(userRepositoryEntity.friendPetitionsSent.isEmpty());
    assertTrue(userRepositoryEntity.friendPetitionsReceived.isEmpty());

  }

  @Test
  void testUpdateWrongFormat() throws Exception {

    setUser1Context();

    User testWrongUserName = user1.toBuilder().email(EMAIL2).userName(EMAIL2).build();
    User testWrongEmail = user1.toBuilder().email(USERNAME2).userName(USERNAME2).build();

    assertThrows(InvalidUserName.class, () -> userService.updateUser(testWrongUserName));
    assertThrows(InvalidEmail.class, () -> userService.updateUser(testWrongEmail));

  }

  @Test
  void testUpdateAlreadyInUse() throws Exception {

    setUser1Context();

    User testSameEmail = user1.toBuilder().email(EMAIL2).build();
    User testSameUserName = user1.toBuilder().userName(USERNAME2).build();

    userService.register(user2);

    assertThrows(UserNameAlreadyInUse.class, () -> userService.updateUser(testSameUserName));
    assertThrows(EmailAlreadyInUse.class, () -> userService.updateUser(testSameEmail));
  }

  @Test
  void testUpdateUserWrongContext() throws Exception {

    assertThrows(WrongToken.class, () -> userService.updateUser(user2));

  }

  @Test
  void testUpdatePassword() throws Exception {
    setUser1Context();

    userService.updatePassword(PASSWORD,PASSWORD2);

    UserRepositoryEntity userRepositoryEntity = userRepository.findById(user1Id)
        .orElseThrow(() -> new InstanceNotFoundException("O usuario non existe"));

    assertEquals(USERNAME, userRepositoryEntity.userName);
    assertEquals(EMAIL, userRepositoryEntity.email);
    assertTrue(passwordEncoder.matches(PASSWORD2, userRepositoryEntity.password));
    assertEquals(NAME, userRepositoryEntity.name);
    assertTrue(userRepositoryEntity.friends.isEmpty());
    assertTrue(userRepositoryEntity.friendPetitionsSent.isEmpty());
    assertTrue(userRepositoryEntity.friendPetitionsReceived.isEmpty());
  }

  @Test
  void testUpdatePasswordInvalidNewPass() throws Exception {
    setUser1Context();

    assertThrows(InvalidPassword.class, () -> userService.updatePassword(PASSWORD, "1Ab"));
    assertThrows(InvalidPassword.class, () -> userService.updatePassword(PASSWORD, "1234abcd"));
    assertThrows(InvalidPassword.class, () -> userService.updatePassword(PASSWORD, "1234ABCD"));
    assertThrows(InvalidPassword.class, () -> userService.updatePassword(PASSWORD, "ABCDabcd"));
  }

  @Test
  void testUpdatePasswordWrongOldPass() throws Exception {
    setUser1Context();

    assertThrows(WrongPassword.class, () -> userService.updatePassword(PASSWORD2, "1234abCD"));
  }

  @Test
  void testUpdatePasswordWrongContext() throws Exception {

    assertThrows(WrongToken.class, () -> userService.updatePassword(PASSWORD,PASSWORD2));

  }

  @Test
  void testSendFriendPetition() throws Exception {
    setUser1Context();

    userService.sendFriendPetition(user3.userName());

    UserRepositoryEntity userRepositoryEntity = userRepository.findById(user1Id)
        .orElseThrow(() -> new InstanceNotFoundException("O usuario non existe"));

    assertEquals(USERNAME, userRepositoryEntity.userName);
    assertEquals(EMAIL, userRepositoryEntity.email);
    assertTrue(passwordEncoder.matches(PASSWORD, userRepositoryEntity.password));
    assertEquals(NAME, userRepositoryEntity.name);
    assertTrue(userRepositoryEntity.friends.isEmpty());
    assertIterableEquals(setUser3,userRepositoryEntity.friendPetitionsSent);
    assertTrue(userRepositoryEntity.friendPetitionsReceived.isEmpty());

    userRepositoryEntity = userRepository.findById(user3Id)
        .orElseThrow(() -> new InstanceNotFoundException("O usuario non existe"));

    assertEquals(USERNAME3, userRepositoryEntity.userName);
    assertEquals(EMAIL3, userRepositoryEntity.email);
    assertTrue(passwordEncoder.matches(PASSWORD3, userRepositoryEntity.password));
    assertEquals(NAME, userRepositoryEntity.name);
    assertTrue(userRepositoryEntity.friends.isEmpty());
    assertTrue(userRepositoryEntity.friendPetitionsSent.isEmpty());
    assertIterableEquals(setUser1,userRepositoryEntity.friendPetitionsReceived);

  }

  @Test
  void testAcceptFriendPetition() throws Exception {
    setUser1Context();
    userService.sendFriendPetition(user3.userName());

    setUser3Context();
    userService.manageFriendPetition(user1.userName(),true);

    UserRepositoryEntity userRepositoryEntity = userRepository.findById(user1Id)
        .orElseThrow(() -> new InstanceNotFoundException("O usuario non existe"));

    assertEquals(USERNAME, userRepositoryEntity.userName);
    assertEquals(EMAIL, userRepositoryEntity.email);
    assertTrue(passwordEncoder.matches(PASSWORD, userRepositoryEntity.password));
    assertEquals(NAME, userRepositoryEntity.name);
    assertIterableEquals(setUser3,userRepositoryEntity.friends);
    assertTrue(userRepositoryEntity.friendPetitionsSent.isEmpty());
    assertTrue(userRepositoryEntity.friendPetitionsReceived.isEmpty());

    userRepositoryEntity = userRepository.findById(user3Id)
        .orElseThrow(() -> new InstanceNotFoundException("O usuario non existe"));

    assertEquals(USERNAME3, userRepositoryEntity.userName);
    assertEquals(EMAIL3, userRepositoryEntity.email);
    assertTrue(passwordEncoder.matches(PASSWORD3, userRepositoryEntity.password));
    assertEquals(NAME, userRepositoryEntity.name);
    assertIterableEquals(setUser1,userRepositoryEntity.friends);
    assertTrue(userRepositoryEntity.friendPetitionsSent.isEmpty());
    assertTrue(userRepositoryEntity.friendPetitionsReceived.isEmpty());

  }

  @Test
  void testDeclineFriendPetition() throws Exception {
    setUser1Context();
    userService.sendFriendPetition(user3.userName());

    setUser3Context();
    userService.manageFriendPetition(user1.userName(),false);

    UserRepositoryEntity userRepositoryEntity = userRepository.findById(user1Id)
        .orElseThrow(() -> new InstanceNotFoundException("O usuario non existe"));

    assertEquals(USERNAME, userRepositoryEntity.userName);
    assertEquals(EMAIL, userRepositoryEntity.email);
    assertTrue(passwordEncoder.matches(PASSWORD, userRepositoryEntity.password));
    assertEquals(NAME, userRepositoryEntity.name);
    assertTrue(userRepositoryEntity.friends.isEmpty());
    assertTrue(userRepositoryEntity.friendPetitionsSent.isEmpty());
    assertTrue(userRepositoryEntity.friendPetitionsReceived.isEmpty());

    userRepositoryEntity = userRepository.findById(user3Id)
        .orElseThrow(() -> new InstanceNotFoundException("O usuario non existe"));

    assertEquals(USERNAME3, userRepositoryEntity.userName);
    assertEquals(EMAIL3, userRepositoryEntity.email);
    assertTrue(passwordEncoder.matches(PASSWORD3, userRepositoryEntity.password));
    assertEquals(NAME, userRepositoryEntity.name);
    assertTrue(userRepositoryEntity.friends.isEmpty());
    assertTrue(userRepositoryEntity.friendPetitionsSent.isEmpty());
    assertTrue(userRepositoryEntity.friendPetitionsReceived.isEmpty());

  }

  @Test
  void testSendFriendPetitionExceptions() throws Exception{
    assertThrows(WrongToken.class, () -> userService.sendFriendPetition(user3.userName()));

    setUser1Context();

    userService.sendFriendPetition(user3.userName());
    assertThrows(SocialContradiction.class, () -> userService.removeFriend(user1.userName()));
    assertThrows(SocialContradiction.class, () -> userService.sendFriendPetition(user3.userName()));

    assertThrows(InstanceNotFoundException.class, () -> userService.sendFriendPetition(user2.userName()));

    setUser3Context();
    assertThrows(SocialContradiction.class, () -> userService.sendFriendPetition(user1.userName()));
    userService.manageFriendPetition(user1.userName(), true);
    assertThrows(SocialContradiction.class, () -> userService.sendFriendPetition(user1.userName()));
  }

  @Test
  void testManageFriendPetitionExceptions() throws Exception{
    assertThrows(WrongToken.class, () -> userService.manageFriendPetition(user3.userName(),true));

    setUser1Context();
    assertThrows(SocialContradiction.class, () -> userService.removeFriend(user1.userName()));

    userService.sendFriendPetition(user3.userName());

    setUser3Context();
    assertThrows(InstanceNotFoundException.class, () -> userService.manageFriendPetition(user2.userName(),true));
    userService.manageFriendPetition(user1.userName(), true);
    assertThrows(SocialContradiction.class, () -> userService.manageFriendPetition(user1.userName(),true));
  }

  @Test
  void testGetSocial() throws Exception{
    setUser1Context();

    User social = userService.getSocial();

    assertEquals(USERNAME, social.userName());
    assertEquals(EMAIL, social.email());
    assertTrue(passwordEncoder.matches(PASSWORD, social.password()));
    assertEquals(NAME, social.name());
    assertTrue(social.friends().isEmpty());
    assertTrue(social.friendPetitionsSent().isEmpty());
    assertTrue(social.friendPetitionsReceived().isEmpty());

    userService.sendFriendPetition(user3.userName());
    social = userService.getSocial();

    assertEquals(USERNAME, social.userName());
    assertEquals(EMAIL, social.email());
    assertTrue(passwordEncoder.matches(PASSWORD, social.password()));
    assertEquals(NAME, social.name());
    System.out.println(social.friends());
    assertTrue(social.friends().isEmpty());
    assertIterableEquals(setUserName3,social.friendPetitionsSent());
    assertTrue(social.friendPetitionsReceived().isEmpty());

    setUser3Context();
    social = userService.getSocial();

    assertEquals(USERNAME3, social.userName());
    assertEquals(EMAIL3, social.email());
    assertTrue(passwordEncoder.matches(PASSWORD3, social.password()));
    assertEquals(NAME, social.name());
    assertTrue(social.friends().isEmpty());
    assertIterableEquals(setUserName1,social.friendPetitionsReceived());
    assertTrue(social.friendPetitionsSent().isEmpty());

    userService.manageFriendPetition(user1.userName(), true);
    social = userService.getSocial();
    assertEquals(USERNAME3, social.userName());
    assertEquals(EMAIL3, social.email());
    assertTrue(passwordEncoder.matches(PASSWORD3, social.password()));
    assertEquals(NAME, social.name());
    assertIterableEquals(setUserName1,social.friends());
    assertTrue(social.friendPetitionsSent().isEmpty());
    assertTrue(social.friendPetitionsReceived().isEmpty());
  }

  @Test
  void testGetSocialWrongContext() throws Exception{
    assertThrows(WrongToken.class, () -> userService.getSocial());
  }

  @Test
  void testRemoveFriend() throws Exception{
    setUser1Context();
    userService.sendFriendPetition(user3.userName());

    setUser3Context();
    userService.manageFriendPetition(user1.userName(), true);

    UserRepositoryEntity userRepositoryEntity = userRepository.findById(user1Id)
        .orElseThrow(() -> new InstanceNotFoundException("O usuario non existe"));

    assertEquals(USERNAME, userRepositoryEntity.userName);
    assertEquals(EMAIL, userRepositoryEntity.email);
    assertTrue(passwordEncoder.matches(PASSWORD, userRepositoryEntity.password));
    assertEquals(NAME, userRepositoryEntity.name);
    assertIterableEquals(setUser3,userRepositoryEntity.friends);
    assertTrue(userRepositoryEntity.friendPetitionsSent.isEmpty());
    assertTrue(userRepositoryEntity.friendPetitionsReceived.isEmpty());

    userRepositoryEntity = userRepository.findById(user3Id)
        .orElseThrow(() -> new InstanceNotFoundException("O usuario non existe"));

    assertEquals(USERNAME3, userRepositoryEntity.userName);
    assertEquals(EMAIL3, userRepositoryEntity.email);
    assertTrue(passwordEncoder.matches(PASSWORD3, userRepositoryEntity.password));
    assertEquals(NAME, userRepositoryEntity.name);
    assertIterableEquals(setUser1,userRepositoryEntity.friends);
    assertTrue(userRepositoryEntity.friendPetitionsSent.isEmpty());
    assertTrue(userRepositoryEntity.friendPetitionsReceived.isEmpty());

    userService.removeFriend(user1.userName());

    userRepositoryEntity = userRepository.findById(user1Id)
        .orElseThrow(() -> new InstanceNotFoundException("O usuario non existe"));

    assertEquals(USERNAME, userRepositoryEntity.userName);
    assertEquals(EMAIL, userRepositoryEntity.email);
    assertTrue(passwordEncoder.matches(PASSWORD, userRepositoryEntity.password));
    assertEquals(NAME, userRepositoryEntity.name);
    assertTrue(userRepositoryEntity.friends.isEmpty());
    assertTrue(userRepositoryEntity.friendPetitionsSent.isEmpty());
    assertTrue(userRepositoryEntity.friendPetitionsReceived.isEmpty());

    userRepositoryEntity = userRepository.findById(user3Id)
        .orElseThrow(() -> new InstanceNotFoundException("O usuario non existe"));

    assertEquals(USERNAME3, userRepositoryEntity.userName);
    assertEquals(EMAIL3, userRepositoryEntity.email);
    assertTrue(passwordEncoder.matches(PASSWORD3, userRepositoryEntity.password));
    assertEquals(NAME, userRepositoryEntity.name);
    assertTrue(userRepositoryEntity.friends.isEmpty());
    assertTrue(userRepositoryEntity.friendPetitionsSent.isEmpty());
    assertTrue(userRepositoryEntity.friendPetitionsReceived.isEmpty());
  }

  @Test
  void testRemoveFrienExceptions() throws Exception{
    assertThrows(WrongToken.class, () -> userService.removeFriend(user1.userName()));

    setUser1Context();
    assertThrows(InstanceNotFoundException.class, () -> userService.removeFriend(user2.userName()));

    assertThrows(SocialContradiction.class, () -> userService.removeFriend(user1.userName()));
    assertThrows(SocialContradiction.class, () -> userService.removeFriend(user3.userName()));

    userService.sendFriendPetition(user3.userName());
    assertThrows(SocialContradiction.class, () -> userService.removeFriend(user3.userName()));

    setUser3Context();
    assertThrows(SocialContradiction.class, () -> userService.removeFriend(user1.userName()));

  }

  @Test
  void testSearchUser() throws Exception{
    List<String> result = userService.searchUser("user3");
    assertIterableEquals(setUserName3,result);

    List<String> aux = List.of(USERNAME,USERNAME3);

    result = userService.searchUser("test");
    assertIterableEquals(aux,result);
  }
}


