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
        String sql = "SELECT game_id, white_username, black_username, game_name, game_data FROM games WHERE game_id = ?";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, gameID);
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    ChessGame chessGame = gson.fromJson(rs.getString("game_data"), ChessGame.class);
                    return new GameData(
                            rs.getInt("game_id"),
                            rs.getString("white_username"),
                            rs.getString("black_username"),
                            rs.getString("game_name"),
                            chessGame
                    );
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting game: " + e.getMessage(), e);
        }
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        String sql = "SELECT game_id, white_username, black_username, game_name, game_data FROM games";
        var games = new ArrayList<GameData>();
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql);
             var rs = stmt.executeQuery()) {
            while (rs.next()) {
                ChessGame chessGame = gson.fromJson(rs.getString("game_data"), ChessGame.class);
                games.add(new GameData(
                        rs.getInt("game_id"),
                        rs.getString("white_username"),
                        rs.getString("black_username"),
                        rs.getString("game_name"),
                        chessGame
                ));
            }
            return games;
        } catch (SQLException e) {
            throw new DataAccessException("Error listing games: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        String sql = "UPDATE games SET white_username=?, black_username=?, game_data=? WHERE game_id=?";
        String gameJson = gson.toJson(game.game());
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, game.whiteUsername());
            stmt.setString(2, game.blackUsername());
            stmt.setString(3, gameJson);
            stmt.setInt(4, game.gameID());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error updating game: " + e.getMessage(), e);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        String sql = "TRUNCATE TABLE games";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error clearing games: " + e.getMessage(), e);
        }
    }
}
