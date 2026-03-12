package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class SqlAuthDAOTest {
    private SqlAuthDAO authDAO;

    @BeforeAll
    static void init() throws DataAccessException {
        DatabaseManager.initialize();
    }

    @BeforeEach
    void setUp() throws DataAccessException {
        authDAO = new SqlAuthDAO();
        authDAO.clear();
    }

    @Test
    @DisplayName("createAuth success")
    void createAuthSuccess() throws DataAccessException {
        AuthData auth = new AuthData("token123", "alice");
        authDAO.createAuth(auth);

        AuthData result = authDAO.getAuth("token123");
        assertNotNull(result);
        assertEquals("token123", result.authToken());
        assertEquals("alice", result.username());
    }

    @Test
    @DisplayName("createAuth duplicate token")
    void createAuthDuplicate() throws DataAccessException {
        AuthData auth = new AuthData("token123", "alice");
        authDAO.createAuth(auth);

        assertThrows(DataAccessException.class, () -> authDAO.createAuth(auth));
    }

    @Test
    @DisplayName("getAuth success")
    void getAuthSuccess() throws DataAccessException {
        authDAO.createAuth(new AuthData("mytoken", "bob"));

        AuthData result = authDAO.getAuth("mytoken");
        assertNotNull(result);
        assertEquals("bob", result.username());
    }

    @Test
    @DisplayName("getAuth not found")
    void getAuthNotFound() throws DataAccessException {
        assertNull(authDAO.getAuth("nonexistent"));
    }

    @Test
    @DisplayName("deleteAuth success")
    void deleteAuthSuccess() throws DataAccessException {
        authDAO.createAuth(new AuthData("token1", "alice"));
        authDAO.deleteAuth("token1");

        assertNull(authDAO.getAuth("token1"));
    }

    @Test
    @DisplayName("deleteAuth nonexistent")
    void deleteAuthNonexistent() throws DataAccessException {
        assertDoesNotThrow(() -> authDAO.deleteAuth("notoken"));
    }

    @Test
    @DisplayName("clear")
    void clearSuccess() throws DataAccessException {
        authDAO.createAuth(new AuthData("t1", "alice"));
        authDAO.createAuth(new AuthData("t2", "bob"));
        authDAO.clear();

        assertNull(authDAO.getAuth("t1"));
        assertNull(authDAO.getAuth("t2"));
    }
}
