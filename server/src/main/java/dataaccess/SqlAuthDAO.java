package dataaccess;

import model.AuthData;

import java.sql.SQLException;

public class SqlAuthDAO implements AuthDAO {

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        String sql = "INSERT INTO auth_tokens (auth_token, username) VALUES (?, ?)";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, auth.authToken());
            stmt.setString(2, auth.username());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error creating auth: " + e.getMessage(), e);
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        String sql = "SELECT auth_token, username FROM auth_tokens WHERE auth_token = ?";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, authToken);
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new AuthData(rs.getString("auth_token"), rs.getString("username"));
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting auth: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        String sql = "DELETE FROM auth_tokens WHERE auth_token = ?";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, authToken);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting auth: " + e.getMessage(), e);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        String sql = "TRUNCATE TABLE auth_tokens";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error clearing auth tokens: " + e.getMessage(), e);
        }
    }
}
