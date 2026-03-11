package handler;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.http.Context;
import model.GameData;
import service.GameService;

import java.util.Collection;
import java.util.Map;

public class GameHandler {
    private final GameService gameService;
    private final Gson gson = new Gson();

    public GameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    public void listGames(Context ctx) {
        String authToken = ctx.header("authorization");
        try {
            Collection<GameData> games = gameService.listGames(authToken);
            ctx.status(200);
            ctx.json(Map.of("games", games));
        } catch (DataAccessException e) {
            ExceptionHandler.handleDataAccessException(ctx, e);
        }
    }

    public void createGame(Context ctx) {
        String authToken = ctx.header("authorization");
        try {
            Map body = gson.fromJson(ctx.body(), Map.class);
            String gameName = (String) body.get("gameName");
            int gameID = gameService.createGame(authToken, gameName);
            ctx.status(200);
            ctx.json(Map.of("gameID", gameID));
        } catch (DataAccessException e) {
            ExceptionHandler.handleDataAccessException(ctx, e);
        }
    }

    public void joinGame(Context ctx) {
        String authToken = ctx.header("authorization");
        try {
            Map body = gson.fromJson(ctx.body(), Map.class);
            String playerColor = (String) body.get("playerColor");
            ChessGame.TeamColor color = ChessGame.TeamColor.valueOf(playerColor);
            int gameID = ((Double) body.get("gameID")).intValue();
            gameService.joinGame(authToken, color, gameID);
            ctx.status(200);
            ctx.json(new Object());
        } catch (IllegalArgumentException | NullPointerException e) {
            ctx.status(400);
            ctx.json(Map.of("message", "Error: bad request"));
        } catch (DataAccessException e) {
            ExceptionHandler.handleDataAccessException(ctx, e);
        }
    }
}
