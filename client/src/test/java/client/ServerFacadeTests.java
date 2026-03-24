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

}
