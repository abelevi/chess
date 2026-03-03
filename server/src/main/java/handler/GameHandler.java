package handler;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.http.Context;
import model.AuthData;
import model.GameData;
import model.UserData;
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
        }
        catch (DataAccessException e) {
            if (e.getMessage().contains("unauthorized")) {
                ctx.status(401);
            } else {
                ctx.status(500);
            }
            ctx.json(Map.of("message", e.getMessage()));
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
            if (e.getMessage().contains("bad request")) {
                ctx.status(400);
            } else if (e.getMessage().contains("unauthorized")) {
                ctx.status(401);
            } else {
                ctx.status(500);
            }
            ctx.json(Map.of("message", e.getMessage()));
        }
    }

    public void joinGame(Context ctx) {
        // TODO: get auth token from header
        // TODO: parse body to get "playerColor" (String) and "gameID" (int)
        // TODO: convert playerColor string to ChessGame.TeamColor enum
        // TODO: call gameService.joinGame(authToken, color, gameID)
        // TODO: return 200 with empty JSON
        // TODO: catch errors — "unauthorized" → 401, "bad request" → 400, "already taken" → 403, else → 500
    }
}
