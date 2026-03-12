package dataaccess;

import model.UserData;
import org.junit.jupiter.api.*;
import org.mindrot.jbcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.*;

public class SqlUserDAOTest {
    private SqlUserDAO userDAO;

    @BeforeAll
    static void init() throws DataAccessException {
        DatabaseManager.initialize();
    }

    @BeforeEach
    void setUp() throws DataAccessException {
        userDAO = new SqlUserDAO();
        userDAO.clear();
    }

    @Test
    @DisplayName("createUser success")
    void createUserSuccess() throws DataAccessException {
        UserData user = new UserData("alice", "password123", "alice@mail.com");
        userDAO.createUser(user);

        UserData result = userDAO.getUser("alice");
        assertNotNull(result);
        assertEquals("alice", result.username());
    }

    @Test
    @DisplayName("createUser duplicate")
    void createUserDuplicate() throws DataAccessException {
        UserData user = new UserData("alice", "password123", "alice@mail.com");
        userDAO.createUser(user);

        assertThrows(DataAccessException.class, () -> userDAO.createUser(user));
    }

    @Test
    @DisplayName("getUser success")
    void getUserSuccess() throws DataAccessException {
        UserData user = new UserData("bob", "secret", "bob@mail.com");
        userDAO.createUser(user);

        UserData result = userDAO.getUser("bob");
        assertNotNull(result);
        assertEquals("bob", result.username());
        assertEquals("bob@mail.com", result.email());
        assertTrue(BCrypt.checkpw("secret", result.password()));
    }

    @Test
    @DisplayName("getUser not found")
    void getUserNotFound() throws DataAccessException {
        assertNull(userDAO.getUser("nobody"));
    }

    @Test
    @DisplayName("clear")
    void clearSuccess() throws DataAccessException {
        userDAO.createUser(new UserData("alice", "pass", "a@mail.com"));
        userDAO.clear();

        assertNull(userDAO.getUser("alice"));
    }
}
