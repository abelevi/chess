package client;

import com.google.gson.Gson;
import model.AuthData;
import model.GameData;

import java.io.*;
import java.net.*;
import java.util.Map;

public class ServerFacade {

    private final String serverUrl;
    private final Gson gson = new Gson();

    public ServerFacade(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public AuthData register(String username, String password, String email) throws Exception {
        var body = Map.of("username", username, "password", password, "email", email);
        return makeRequest("POST", "/user", body, null, AuthData.class);
    }

    public AuthData login(String username, String password) throws Exception {
        var body = Map.of("username", username, "password", password);
        return makeRequest("POST", "/session", body, null, AuthData.class);
    }

    public void logout(String authToken) throws Exception {
        makeRequest("DELETE", "/session", null, authToken, null);
    }

    public GameData[] listGames(String authToken) throws Exception {
        record ListGamesResponse(GameData[] games) {}
        var response = makeRequest("GET", "/game", null, authToken, ListGamesResponse.class);
        return response.games();
    }

    public int createGame(String authToken, String gameName) throws Exception {
        var body = Map.of("gameName", gameName);
        record CreateGameResponse(int gameID) {}
        var response = makeRequest("POST", "/game", body, authToken, CreateGameResponse.class);
        return response.gameID();
    }

    public void joinGame(String authToken, String playerColor, int gameID) throws Exception {
        var body = Map.of("playerColor", playerColor, "gameID", gameID);
        makeRequest("PUT", "/game", body, authToken, null);
    }

    public void clear() throws Exception {
        makeRequest("DELETE", "/db", null, null, null);
    }

    private <T> T makeRequest(String method, String path, Object body, String authToken, Class<T> responseClass)
            throws Exception {
        URL url = new URI(serverUrl + path).toURL();
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setRequestMethod(method);
        http.setDoOutput(body != null);

        if (authToken != null) {
            http.addRequestProperty("authorization", authToken);
        }

        if (body != null) {
            http.addRequestProperty("Content-Type", "application/json");
            try (var outputStream = http.getOutputStream()) {
                outputStream.write(gson.toJson(body).getBytes());
            }
        }

        http.connect();

        var status = http.getResponseCode();
        if (status >= 400) {
            throw new Exception(readError(http));
        }

        if (responseClass != null) {
            try (var inputStream = http.getInputStream()) {
                var reader = new InputStreamReader(inputStream);
                return gson.fromJson(reader, responseClass);
            }
        }
        return null;
    }

    private String readError(HttpURLConnection http) throws IOException {
        try (var errorStream = http.getErrorStream()) {
            if (errorStream == null) {
                return "Error: " + http.getResponseCode();
            }
            var reader = new InputStreamReader(errorStream);
            var response = gson.fromJson(reader, Map.class);
            if (response != null && response.containsKey("message")) {
                return (String) response.get("message");
            }
            return "Error: " + http.getResponseCode();
        }
    }
}
