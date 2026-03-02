package service;

import chess.ChessGame;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;

import java.util.Collection;

public class GameService {
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public Collection<GameData> listGames(String authToken) throws DataAccessException {
        AuthData auth = authenticate(authToken);
        return gameDAO.listGames();
    }

    public int createGame(String authToken, String gameName) throws DataAccessException {
        AuthData auth = authenticate(authToken);
        if (gameName == null) {
            throw new DataAccessException("Error: bad request");
        }
        GameData game = new GameData(0, null, null, gameName, new ChessGame());
        return gameDAO.createGame(game);
    }

    public void joinGame(String authToken, ChessGame.TeamColor color, int gameID) throws DataAccessException {
        AuthData auth = authenticate(authToken);
        if (color == null) {
            throw new DataAccessException("Error: bad request");
        }
        GameData game = gameDAO.getGame(gameID);
        if (game == null) {
            throw new DataAccessException("Error: bad request");
        }

        String whiteUsername = game.whiteUsername();
        String blackUsername = game.blackUsername();

        if (color == ChessGame.TeamColor.WHITE) {
            if (whiteUsername != null) {
                throw new DataAccessException("Error: already taken");
            }
            whiteUsername = auth.username();
        } else {
            if (blackUsername != null) {
                throw new DataAccessException("Error: already taken");
            }
            blackUsername = auth.username();
        }

        GameData updatedGame = new GameData(game.gameID(), whiteUsername, blackUsername, game.gameName(), game.game());
        gameDAO.updateGame(updatedGame);
    }

    private AuthData authenticate(String authToken) throws DataAccessException {
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("Error: unauthorized");
        }
        return auth;
    }
}
