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
        java.util.List<String> dead = new java.util.ArrayList<>();
        for (Map.Entry<String, Connection> entry : connections.entrySet()) {
            if (entry.getKey().equals(excludeSessionID)) {
                continue;
            }
            if (!sendMessage(entry.getValue().session, json)) {
                dead.add(entry.getKey());
            }
        }
        // Evict any sessions whose send failed so subsequent broadcasts
        // don't keep hitting a dead socket.
        for (String sessionId : dead) {
            connections.remove(sessionId);
        }
    }

    public void sendToSession(WsContext session, ServerMessage message) {
        sendMessage(session, gson.toJson(message));
    }

    private boolean sendMessage(WsContext session, String json) {
        try {
            session.send(json);
            return true;
        } catch (Exception e) {
            System.err.println("WebSocket send failed for session "
                    + session.sessionId() + ": " + e.getMessage());
            e.printStackTrace(System.err);
            return false;
        }
    }
}
