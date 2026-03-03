package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {
    private UserService userService;
    private AuthDAO authDAO;
    private UserDAO userDAO;

    @BeforeEach
    void setUp() {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        userService = new UserService(userDAO, authDAO);
    }

    @Test
    @DisplayName("register success")
    void registerSuccess() throws DataAccessException {
        UserData user = new UserData("alice", "password123", "alice@mail.com");
        AuthData auth = userService.register(user);

        assertNotNull(auth);
        assertEquals("alice", auth.username());
        assertNotNull(auth.authToken());
    }

    @Test
    @DisplayName("register duplicate username")
    void registerDuplicate() throws DataAccessException {
        UserData user = new UserData("alice", "password123", "alice@mail.com");
        userService.register(user);

        UserData duplicate = new UserData("alice", "otherPass", "other@mail.com");
        DataAccessException ex = assertThrows(DataAccessException.class, () -> userService.register(duplicate));
        assertTrue(ex.getMessage().contains("already taken"));
    }

    @Test
    @DisplayName("login success")
    void loginSuccess() throws DataAccessException {
        UserData user = new UserData("bob", "secret", "bob@mail.com");
        userService.register(user);

        AuthData auth = userService.login(new UserData("bob", "secret", null));

        assertNotNull(auth);
        assertEquals("bob", auth.username());
        assertNotNull(auth.authToken());
    }

    @Test
    @DisplayName("login wrong password")
    void loginWrongPassword() throws DataAccessException {
        UserData user = new UserData("bob", "secret", "bob@mail.com");
        userService.register(user);

        DataAccessException ex = assertThrows(DataAccessException.class,
                () -> userService.login(new UserData("bob", "wrongpass", null)));
        assertTrue(ex.getMessage().contains("unauthorized"));
    }

    @Test
    @DisplayName("logout success")
    void logoutSuccess() throws DataAccessException {
        UserData user = new UserData("carol", "pass", "carol@mail.com");
        AuthData auth = userService.register(user);

        assertDoesNotThrow(() -> userService.logout(auth.authToken()));

        assertThrows(DataAccessException.class, () -> userService.logout(auth.authToken()));
    }

    @Test
    @DisplayName("logout invalid token")
    void logoutInvalidToken() {
        DataAccessException ex = assertThrows(DataAccessException.class,
                () -> userService.logout("bad-token"));
        assertTrue(ex.getMessage().contains("unauthorized"));
    }
}
