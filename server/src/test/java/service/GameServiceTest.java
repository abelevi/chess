package service;

import chess.ChessGame;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTest {
    private GameService gameService;
    private UserService userService;
    private String authToken;

    @BeforeEach
    void setUp() throws DataAccessException {
        UserDAO userDAO = new MemoryUserDAO();
        AuthDAO authDAO = new MemoryAuthDAO();
        GameDAO gameDAO = new MemoryGameDAO();
        userService = new UserService(userDAO, authDAO);
        gameService = new GameService(gameDAO, authDAO);

        AuthData auth = userService.register(new UserData("alice", "pass", "a@mail.com"));
        authToken = auth.authToken();
    }

    @Test
    @DisplayName("createGame success")
    void createGameSuccess() throws DataAccessException {
        int gameID = gameService.createGame(authToken, "myGame");
        assertTrue(gameID > 0);
    }

    @Test
    @DisplayName("createGame unauthorized")
    void createGameUnauthorized() {
        DataAccessException ex = assertThrows(DataAccessException.class,
                () -> gameService.createGame("bad-token", "myGame"));
        assertTrue(ex.getMessage().contains("unauthorized"));
    }

    @Test
    @DisplayName("listGames success")
    void listGamesSuccess() throws DataAccessException {
        gameService.createGame(authToken, "game1");
        gameService.createGame(authToken, "game2");

        Collection<GameData> games = gameService.listGames(authToken);
        assertEquals(2, games.size());
    }

    @Test
    @DisplayName("listGames unauthorized")
    void listGamesUnauthorized() {
        DataAccessException ex = assertThrows(DataAccessException.class,
                () -> gameService.listGames("bad-token"));
        assertTrue(ex.getMessage().contains("unauthorized"));
    }

    @Test
    @DisplayName("joinGame success")
    void joinGameSuccess() throws DataAccessException {
        int gameID = gameService.createGame(authToken, "myGame");

        assertDoesNotThrow(() -> gameService.joinGame(authToken, ChessGame.TeamColor.WHITE, gameID));
    }

    @Test
    @DisplayName("joinGame already taken")
    void joinGameAlreadyTaken() throws DataAccessException {
        int gameID = gameService.createGame(authToken, "myGame");
        gameService.joinGame(authToken, ChessGame.TeamColor.WHITE, gameID);

        AuthData auth2 = userService.register(new UserData("bob", "pass", "b@mail.com"));

        DataAccessException ex = assertThrows(DataAccessException.class,
                () -> gameService.joinGame(auth2.authToken(), ChessGame.TeamColor.WHITE, gameID));
        assertTrue(ex.getMessage().contains("already taken"));
    }
}
