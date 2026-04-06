package server;

import com.google.gson.Gson;
import dataaccess.SqlAuthDAO;
import dataaccess.SqlGameDAO;
import io.javalin.websocket.WsConfig;
import io.javalin.websocket.WsContext;
import model.AuthData;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.util.function.Consumer;

public class WebSocketHandler implements Consumer<WsConfig> {

    private final ConnectionManager connectionManager = new ConnectionManager();
    private final SqlAuthDAO authDAO;
    private final SqlGameDAO gameDAO;
    private final Gson gson = new Gson();

    public WebSocketHandler(SqlAuthDAO authDAO, SqlGameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    @Override
    public void accept(WsConfig wsConfig) {
        wsConfig.onConnect(ctx -> {
            // connection tracked when CONNECT command is received, not on raw ws connect
        });

        wsConfig.onMessage(ctx -> {
            String message = ctx.message();
            UserGameCommand command = gson.fromJson(message, UserGameCommand.class);

            switch (command.getCommandType()) {
                case CONNECT -> handleConnect(ctx, command);
                case MAKE_MOVE -> handleMakeMove(ctx, command);
                case LEAVE -> handleLeave(ctx, command);
                case RESIGN -> handleResign(ctx, command);
            }
        });

        wsConfig.onClose(ctx -> {
            connectionManager.removeConnection(ctx.getSessionId());
        });

        wsConfig.onError(ctx -> {
            connectionManager.removeConnection(ctx.getSessionId());
        });
    }

    private void handleConnect(WsContext ctx, UserGameCommand command) {
        try {
            AuthData auth = authDAO.getAuth(command.getAuthToken());
            if (auth == null) {
                sendError(ctx, "Error: unauthorized");
                return;
            }

            var game = gameDAO.getGame(command.getGameID());
            if (game == null) {
                sendError(ctx, "Error: bad game ID");
                return;
            }

            Connection connection = new Connection(auth.username(), ctx);
            connectionManager.addConnection(command.getGameID(), ctx.getSessionId(), connection);

            // Send LOAD_GAME to the connecting user
            ServerMessage loadGame = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
            connectionManager.sendToSession(ctx, loadGame);

            // Notify all other users in the game
            ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            connectionManager.broadcast(command.getGameID(), notification, ctx.getSessionId());
        } catch (Exception e) {
            sendError(ctx, "Error: " + e.getMessage());
        }
    }

    private void handleMakeMove(WsContext ctx, UserGameCommand command) {
        // TODO: implement
    }

    private void handleLeave(WsContext ctx, UserGameCommand command) {
        // TODO: implement
    }

    private void handleResign(WsContext ctx, UserGameCommand command) {
        // TODO: implement
    }

    private void sendError(WsContext ctx, String errorMessage) {
        ServerMessage error = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
        connectionManager.sendToSession(ctx, error);
    }

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }
}
