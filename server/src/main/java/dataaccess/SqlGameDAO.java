package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

public class SqlGameDAO implements GameDAO {
    private final Gson gson = new Gson();

    @Override
    public int createGame(GameData game) throws DataAccessException {
        String sql = "INSERT INTO games (white_username, black_username, game_name, game_data) VALUES (?, ?, ?, ?)";
        String gameJson = gson.toJson(game.game());
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, game.whiteUsername());
            stmt.setString(2, game.blackUsername());
            stmt.setString(3, game.gameName());
            stmt.setString(4, gameJson);
            stmt.executeUpdate();

            try (var keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
                throw new DataAccessException("No game ID generated");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error creating game: " + e.getMessage(), e);
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        // TODO: SELECT from games WHERE game_id = ?
        // - Deserialize game_data JSON back to ChessGame with gson.fromJson()
        // - Return new GameData(gameID, whiteUsername, blackUsername, gameName, chessGame)
        // - Return null if not found
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        // TODO: SELECT all rows from games table
        // - Deserialize each game_data column
        // - Return a Collection<GameData>
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        // TODO: UPDATE games SET white_username=?, black_username=?, game_data=? WHERE game_id=?
        // - Re-serialize the ChessGame to JSON
    }

    @Override
    public void clear() throws DataAccessException {
        // TODO: TRUNCATE the games table
    }
}
