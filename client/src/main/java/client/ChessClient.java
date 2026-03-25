package client;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import ui.ChessBoardRenderer;
import java.util.Scanner;

public class ChessClient {

    private final ServerFacade server;
    private final Scanner scanner = new Scanner(System.in);
    private String authToken = null;
    private GameData[] lastGameList = null;

    private enum State { LOGGED_OUT, LOGGED_IN }
    private State state = State.LOGGED_OUT;

    public ChessClient(String serverUrl) {
        this.server = new ServerFacade(serverUrl);
    }

    public void run() {
        System.out.println("♕ Welcome to 240 Chess. Type help to get started.");
        while (true) {
            printPrompt();
            var input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                continue;
            }
            try {
                var result = processCommand(input);
                System.out.println(result);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private String processCommand(String input) throws Exception {
        var tokens = input.split("\\s+");
        var command = tokens[0].toLowerCase();

        if (state == State.LOGGED_OUT) {
            return switch (command) {
                case "help" -> helpPrelogin();
                case "quit" -> quit();
                case "login" -> login(tokens);
                case "register" -> register(tokens);
                default -> "Unknown command. Type help for available commands.";
            };
        } else {
            return switch (command) {
                case "help" -> helpPostlogin();
                case "logout" -> logout();
                case "create" -> createGame(tokens);
                case "list" -> listGames();
                case "play" -> playGame(tokens);
                case "observe" -> observeGame(tokens);
                default -> "Unknown command. Type help for available commands.";
            };
        }
    }

    // ── Prelogin commands ──────────────────────────

    private String helpPrelogin() {
        return """
                register <USERNAME> <PASSWORD> <EMAIL> - to create an account
                login <USERNAME> <PASSWORD> - to play chess
                quit - playing chess
                help - with possible commands""";
    }

    private String quit() {
        System.out.println("Goodbye!");
        System.exit(0);
        return "";
    }

    private String login(String[] tokens) throws Exception {
        if (tokens.length < 3) {
            return "Usage: login <USERNAME> <PASSWORD>";
        }
        AuthData auth = server.login(tokens[1], tokens[2]);
        authToken = auth.authToken();
        state = State.LOGGED_IN;
        return "Logged in as " + auth.username() + ".";
    }

    private String register(String[] tokens) throws Exception {
        if (tokens.length < 4) {
            return "Usage: register <USERNAME> <PASSWORD> <EMAIL>";
        }
        AuthData auth = server.register(tokens[1], tokens[2], tokens[3]);
        authToken = auth.authToken();
        state = State.LOGGED_IN;
        return "Registered and logged in as " + auth.username() + ".";
    }

    // ── Postlogin commands ─────────────────────────

    private String helpPostlogin() {
        return """
                create <NAME> - a game
                list - games
                play <ID> [WHITE|BLACK] - a game
                observe <ID> - a game
                logout - when you are done
                help - with possible commands""";
    }

    private String logout() throws Exception {
        server.logout(authToken);
        authToken = null;
        state = State.LOGGED_OUT;
        return "Logged out.";
    }

    private String createGame(String[] tokens) throws Exception {
        if (tokens.length < 2) {
            return "Usage: create <NAME>";
        }
        var gameID = server.createGame(authToken, tokens[1]);
        return "Created game " + gameID + ".";
    }

    private String listGames() throws Exception {
        lastGameList = server.listGames(authToken);
        if (lastGameList.length == 0) {
            return "No games available.";
        }
        var sb = new StringBuilder();
        for (int i = 0; i < lastGameList.length; i++) {
            var game = lastGameList[i];
            sb.append(String.format("  %d. %s  |  White: %s  |  Black: %s%n",
                    i + 1,
                    game.gameName(),
                    orEmpty(game.whiteUsername()),
                    orEmpty(game.blackUsername())));
        }
        return sb.toString().stripTrailing();
    }

    private String playGame(String[] tokens) throws Exception {
        if (tokens.length < 3) {
            return "Usage: play <ID> [WHITE|BLACK]";
        }
        int index = parseGameIndex(tokens[1]);
        var color = tokens[2].toUpperCase();
        var game = lastGameList[index];
        server.joinGame(authToken, color, game.gameID());
        var perspective = color.equals("BLACK") ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
        ChessBoardRenderer.drawBoard(new ChessGame().getBoard(), perspective);
        return "Joined game " + game.gameName() + " as " + color + ".";
    }

    private String observeGame(String[] tokens) throws Exception {
        if (tokens.length < 2) {
            return "Usage: observe <ID>";
        }
        int index = parseGameIndex(tokens[1]);
        var game = lastGameList[index];
        ChessBoardRenderer.drawBoard(new ChessGame().getBoard(), ChessGame.TeamColor.WHITE);
        return "Observing game " + game.gameName() + ".";
    }

    // ── Helpers ────────────────────────────────────

    private int parseGameIndex(String token) throws Exception {
        int num;
        try {
            num = Integer.parseInt(token);
        } catch (NumberFormatException e) {
            throw new Exception("Please enter a valid game number.");
        }
        if (lastGameList == null || num < 1 || num > lastGameList.length) {
            throw new Exception("Invalid game number. Run list first.");
        }
        return num - 1;
    }

    private String orEmpty(String value) {
        return value == null ? "—" : value;
    }

    private void printPrompt() {
        var label = (state == State.LOGGED_OUT) ? "[LOGGED_OUT]" : "[LOGGED_IN]";
        System.out.print(label + " >>> ");
    }
}
