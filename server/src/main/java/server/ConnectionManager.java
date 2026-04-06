package server;

import com.google.gson.Gson;
import io.javalin.websocket.WsContext;
import websocket.messages.ServerMessage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {

    // gameID -> (sessionID -> Connection)
    private final ConcurrentHashMap<Integer, ConcurrentHashMap<String, Connection>> gameConnections = new ConcurrentHashMap<>();
    private final Gson gson = new Gson();

    public void addConnection(int gameID, String sessionID, Connection connection) {
        gameConnections.computeIfAbsent(gameID, k -> new ConcurrentHashMap<>()).put(sessionID, connection);
    }

    public void removeConnection(String sessionID) {
        for (var connections : gameConnections.values()) {
            connections.remove(sessionID);
        }
    }

    public void removeConnection(int gameID, String sessionID) {
        var connections = gameConnections.get(gameID);
        if (connections != null) {
            connections.remove(sessionID);
        }
    }

    public void broadcast(int gameID, ServerMessage message, String excludeSessionID) {
        var connections = gameConnections.get(gameID);
        if (connections == null) {
            return;
        }
        String json = gson.toJson(message);
        for (Map.Entry<String, Connection> entry : connections.entrySet()) {
            if (!entry.getKey().equals(excludeSessionID)) {
                sendMessage(entry.getValue().session, json);
            }
        }
    }

    public void sendToSession(WsContext session, ServerMessage message) {
        sendMessage(session, gson.toJson(message));
    }

    private void sendMessage(WsContext session, String json) {
        try {
            session.send(json);
        } catch (Exception e) {
            // connection is dead, will be cleaned up on close
        }
    }
}
