package dataaccess;

import chess.ChessGame;
import model.GameData;
import org.junit.jupiter.api.*;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class SqlGameDAOTest {
    private SqlGameDAO gameDAO;

    @BeforeAll
    static void init() throws DataAccessException {
        DatabaseManager.initialize();
    }

    @BeforeEach
    void setUp() throws DataAccessException {
        gameDAO = new SqlGameDAO();
        gameDAO.clear();
    }

    @Test
    @DisplayName("createGame success")
    void createGameSuccess() throws DataAccessException {
        GameData game = new GameData(0, null, null, "Test Game", new ChessGame());
        int gameID = gameDAO.createGame(game);

        assertTrue(gameID > 0);
        GameData result = gameDAO.getGame(gameID);
        assertNotNull(result);
        assertEquals("Test Game", result.gameName());
    }

    @Test
    @DisplayName("createGame multiple")
    void createGameMultiple() throws DataAccessException {
        int id1 = gameDAO.createGame(new GameData(0, null, null, "Game 1", new ChessGame()));
        int id2 = gameDAO.createGame(new GameData(0, null, null, "Game 2", new ChessGame()));

        assertNotEquals(id1, id2);
    }

    @Test
    @DisplayName("getGame success")
    void getGameSuccess() throws DataAccessException {
        int gameID = gameDAO.createGame(new GameData(0, null, null, "My Game", new ChessGame()));

        GameData result = gameDAO.getGame(gameID);
        assertNotNull(result);
        assertEquals(gameID, result.gameID());
        assertEquals("My Game", result.gameName());
        assertNotNull(result.game());
    }

    @Test
    @DisplayName("getGame not found")
    void getGameNotFound() throws DataAccessException {
        assertNull(gameDAO.getGame(9999));
    }

    @Test
    @DisplayName("listGames success")
    void listGamesSuccess() throws DataAccessException {
        gameDAO.createGame(new GameData(0, null, null, "Game 1", new ChessGame()));
        gameDAO.createGame(new GameData(0, null, null, "Game 2", new ChessGame()));

        Collection<GameData> games = gameDAO.listGames();
        assertEquals(2, games.size());
    }

    @Test
    @DisplayName("listGames empty")
    void listGamesEmpty() throws DataAccessException {
        Collection<GameData> games = gameDAO.listGames();
        assertTrue(games.isEmpty());
    }

    @Test
    @DisplayName("updateGame success")
    void updateGameSuccess() throws DataAccessException {
        int gameID = gameDAO.createGame(new GameData(0, null, null, "Game", new ChessGame()));

        GameData updated = new GameData(gameID, "alice", "bob", "Game", new ChessGame());
        gameDAO.updateGame(updated);

        GameData result = gameDAO.getGame(gameID);
        assertEquals("alice", result.whiteUsername());
        assertEquals("bob", result.blackUsername());
    }

    @Test
    @DisplayName("updateGame nonexistent")
    void updateGameNonexistent() throws DataAccessException {
        GameData fake = new GameData(9999, "alice", null, "Fake", new ChessGame());
        assertDoesNotThrow(() -> gameDAO.updateGame(fake));
    }

    @Test
    @DisplayName("clear")
    void clearSuccess() throws DataAccessException {
        gameDAO.createGame(new GameData(0, null, null, "Game", new ChessGame()));
        gameDAO.clear();

        assertTrue(gameDAO.listGames().isEmpty());
    }
}
