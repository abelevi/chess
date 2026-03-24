package client;

import org.junit.jupiter.api.*;
import server.Server;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade("http://localhost:" + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void clear() throws Exception {
        facade.clear();
    }

    @Test
    public void registerPositive() throws Exception {
        var auth = facade.register("testuser", "password", "test@email.com");
        Assertions.assertNotNull(auth.authToken());
        Assertions.assertEquals("testuser", auth.username());
    }

    @Test
    public void registerDuplicate() {
        Assertions.assertThrows(Exception.class, () -> {
            facade.register("testuser", "password", "test@email.com");
            facade.register("testuser", "password", "test@email.com");
        });
    }

    @Test
    public void loginPositive() throws Exception {
        facade.register("testuser", "password", "test@email.com");
        var auth = facade.login("testuser", "password");
        Assertions.assertNotNull(auth.authToken());
        Assertions.assertEquals("testuser", auth.username());
    }

    @Test
    public void loginWrongPassword() {
        Assertions.assertThrows(Exception.class, () -> {
            facade.register("testuser", "password", "test@email.com");
            facade.login("testuser", "wrongpassword");
        });
    }

    @Test
    public void logoutPositive() throws Exception {
        var auth = facade.register("testuser", "password", "test@email.com");
        Assertions.assertDoesNotThrow(() -> facade.logout(auth.authToken()));
    }

    @Test
    public void logoutInvalidToken() {
        Assertions.assertThrows(Exception.class, () -> {
            facade.logout("not-a-real-token");
        });
    }

    @Test
    public void listGamesPositive() throws Exception {
        var auth = facade.register("testuser", "password", "test@email.com");
        facade.createGame(auth.authToken(), "game1");
        facade.createGame(auth.authToken(), "game2");
        var games = facade.listGames(auth.authToken());
        Assertions.assertEquals(2, games.length);
    }

    @Test
    public void listGamesInvalidToken() {
        Assertions.assertThrows(Exception.class, () -> {
            facade.listGames("not-a-real-token");
        });
    }

}
