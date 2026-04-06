package server;

import com.google.gson.Gson;
import io.javalin.websocket.WsConfig;
import io.javalin.websocket.WsContext;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class WebSocketHandler implements Consumer<WsConfig> {

    private final ConcurrentHashMap<String, WsContext> connections = new
            ConcurrentHashMap<>();
    private final Gson gson = new Gson();

    @Override
    public void accept(WsConfig wsConfig) {
        wsConfig.onConnect(ctx -> {
            connections.put(ctx.getSessionId(), ctx);
        });

        wsConfig.onMessage(ctx -> {
            String message = ctx.message();
            UserGameCommand command = gson.fromJson(message,
                    UserGameCommand.class);
            // TODO: handle commands (CONNECT, MAKE_MOVE, LEAVE, RESIGN)
        });

        wsConfig.onClose(ctx -> {
            connections.remove(ctx.getSessionId());
        });

        wsConfig.onError(ctx -> {
            connections.remove(ctx.getSessionId());
        });
    }
}
