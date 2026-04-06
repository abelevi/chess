package server;

import io.javalin.websocket.WsContext;

public class Connection {
    public final String username;
    public final WsContext session;

    public Connection(String username, WsContext session) {
        this.username = username;
        this.session = session;
    }
}
