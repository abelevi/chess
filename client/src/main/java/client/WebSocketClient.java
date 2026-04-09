package client;

import com.google.gson.Gson;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import jakarta.websocket.*;
import java.net.URI;

public class WebSocketClient extends Endpoint {

    private Session session;
    private final Gson gson = new Gson();
    private final ServerMessageHandler messageHandler;

    public interface ServerMessageHandler {
        void onMessage(ServerMessage message);
        void onDisconnect(String reason);
    }

    public WebSocketClient(String serverUrl, ServerMessageHandler messageHandler) throws Exception {
        this.messageHandler = messageHandler;

        String wsUrl = serverUrl.replace("http", "ws") + "/ws";
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        this.session = container.connectToServer(this, new URI(wsUrl));
    }

    public void sendCommand(UserGameCommand command) throws Exception {
        session.getBasicRemote().sendText(gson.toJson(command));
    }

    public boolean isOpen() {
        return session != null && session.isOpen();
    }

    public void close() throws Exception {
        if (session != null && session.isOpen()) {
            session.close();
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        this.session = session;
        // Register the message handler HERE so it is ready before
        // connectToServer() returns — no race with early server frames.
        session.addMessageHandler(new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String text) {
                try {
                    ServerMessage message = gson.fromJson(text, ServerMessage.class);
                    messageHandler.onMessage(message);
                } catch (Throwable t) {
                    // Never let an exception escape into the Jakarta container —
                    // it would tear down the session.
                    System.err.println("WebSocket message handling failed: " + t.getMessage());
                }
            }
        });
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        messageHandler.onDisconnect("connection closed: " + closeReason.getReasonPhrase());
    }

    @Override
    public void onError(Session session, Throwable thr) {
        messageHandler.onDisconnect("connection error: " + thr.getMessage());
    }
}
