package server;

import chess.ChessGame;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.SqlAuthDAO;
import dataaccess.SqlGameDAO;
import io.javalin.websocket.WsConfig;
import io.javalin.websocket.WsContext;
import model.AuthData;
import model.GameData;
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
            loadGame.setGame(game.game());
            connectionManager.sendToSession(ctx, loadGame);

            // Notify all other users in the game
            ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            notification.setMessage(auth.username() + " connected to the game");
            connectionManager.broadcast(command.getGameID(), notification, ctx.getSessionId());
        } catch (Exception e) {
            sendError(ctx, "Error: " + e.getMessage());
        }
    }

    private void handleMakeMove(WsContext ctx, UserGameCommand command) {
        try {
            AuthData auth = authDAO.getAuth(command.getAuthToken());
            if (auth == null) {
                sendError(ctx, "Error: unauthorized");
                return;
            }

            GameData gameData = gameDAO.getGame(command.getGameID());
            if (gameData == null) {
                sendError(ctx, "Error: bad game ID");
                return;
            }

            ChessGame game = gameData.game();

            // Check if game is already over
            if (game.isOver()) {
                sendError(ctx, "Error: game is over");
                return;
            }

            // Determine which color this user is
            String username = auth.username();
            ChessGame.TeamColor playerColor = getPlayerColor(gameData, username);

            // Observers can't move
            if (playerColor == null) {
                sendError(ctx, "Error: you are an observer");
                return;
            }

            // Can't move opponent's pieces
            if (playerColor != game.getTeamTurn()) {
                sendError(ctx, "Error: it is not your turn");
                return;
            }

            // Attempt the move (ChessGame.makeMove validates legality)
            game.makeMove(command.getMove());

            // Persist updated game state
            gameDAO.updateGame(gameData);

            // Send LOAD_GAME to all players in the game (including sender)
            ServerMessage loadGame = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
            loadGame.setGame(game);
            connectionManager.sendToSession(ctx, loadGame);
            connectionManager.broadcast(command.getGameID(), loadGame, ctx.getSessionId());

            // Send NOTIFICATION of the move to other players
            ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            notification.setMessage(username + " made a move");
            connectionManager.broadcast(command.getGameID(), notification, ctx.getSessionId());

            // Check for checkmate or stalemate after the move
            ChessGame.TeamColor opponent = (playerColor == ChessGame.TeamColor.WHITE)
                    ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
            if (game.isInCheckmate(opponent)) {
                game.setOver(true);
                gameDAO.updateGame(gameData);
                ServerMessage checkmateNotification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
                checkmateNotification.setMessage(opponent + " is in checkmate");
                connectionManager.sendToSession(ctx, checkmateNotification);
                connectionManager.broadcast(command.getGameID(), checkmateNotification, ctx.getSessionId());
            } else if (game.isInStalemate(opponent)) {
                game.setOver(true);
                gameDAO.updateGame(gameData);
                ServerMessage stalemateNotification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
                stalemateNotification.setMessage("Stalemate");
                connectionManager.sendToSession(ctx, stalemateNotification);
                connectionManager.broadcast(command.getGameID(), stalemateNotification, ctx.getSessionId());
            } else if (game.isInCheck(opponent)) {
                ServerMessage checkNotification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
                checkNotification.setMessage(opponent + " is in check");
                connectionManager.sendToSession(ctx, checkNotification);
                connectionManager.broadcast(command.getGameID(), checkNotification, ctx.getSessionId());
            }
        } catch (InvalidMoveException e) {
            sendError(ctx, "Error: invalid move");
        } catch (Exception e) {
            sendError(ctx, "Error: " + e.getMessage());
        }
    }

    private ChessGame.TeamColor getPlayerColor(GameData gameData, String username) {
        if (username.equals(gameData.whiteUsername())) {
            return ChessGame.TeamColor.WHITE;
        } else if (username.equals(gameData.blackUsername())) {
            return ChessGame.TeamColor.BLACK;
        }
        return null;
    }

    private void handleLeave(WsContext ctx, UserGameCommand command) {
        // TODO: implement
    }

    private void handleResign(WsContext ctx, UserGameCommand command) {
        // TODO: implement
    }

    private void sendError(WsContext ctx, String errorMessage) {
        ServerMessage error = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
        error.setErrorMessage(errorMessage);
        connectionManager.sendToSession(ctx, error);
    }

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }
}
