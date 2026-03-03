package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class ClearServiceTest {
    private ClearService clearService;
    private UserService userService;
    private GameService gameService;
    private AuthDAO authDAO;

    @BeforeEach
    void setUp() {
        UserDAO userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        GameDAO gameDAO = new MemoryGameDAO();
        clearService = new ClearService(userDAO, authDAO, gameDAO);
        userService = new UserService(userDAO, authDAO);
        gameService = new GameService(gameDAO, authDAO);
    }

    @Test
    @DisplayName("clear success")
    void clearSuccess() throws DataAccessException {
        AuthData auth = userService.register(new UserData("alice", "pass", "a@mail.com"));
        gameService.createGame(auth.authToken(), "myGame");

        assertDoesNotThrow(() -> clearService.clear());

        assertNull(authDAO.getAuth(auth.authToken()));
        assertThrows(DataAccessException.class, () -> userService.login(new UserData("alice", "pass", null)));
    }
}
