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
    }

    public WebSocketClient(String serverUrl, ServerMessageHandler messageHandler) throws Exception {
        this.messageHandler = messageHandler;

        String wsUrl = serverUrl.replace("http", "ws") + "/ws";
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        this.session = container.connectToServer(this, new URI(wsUrl));

        this.session.addMessageHandler(new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String text) {
                ServerMessage message = gson.fromJson(text, ServerMessage.class);
                messageHandler.onMessage(message);
            }
        });
    }

    public void sendCommand(UserGameCommand command) throws Exception {
        session.getBasicRemote().sendText(gson.toJson(command));
    }

    public void close() throws Exception {
        if (session != null && session.isOpen()) {
            session.close();
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        // stored in constructor already
    }
}
