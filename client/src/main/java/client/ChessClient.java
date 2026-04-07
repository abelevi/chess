package client;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import model.AuthData;
import model.GameData;
import ui.ChessBoardRenderer;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.util.Scanner;

public class ChessClient implements WebSocketClient.ServerMessageHandler {

    private final ServerFacade server;
    private final String serverUrl;
    private final Scanner scanner = new Scanner(System.in);
    private String authToken = null;
    private GameData[] lastGameList = null;

    private enum State { LOGGED_OUT, LOGGED_IN, IN_GAME }
    private State state = State.LOGGED_OUT;

    private WebSocketClient ws;
    private int currentGameID;
    private ChessGame.TeamColor playerColor; // null if observing
    private ChessGame currentGame;

    public ChessClient(String serverUrl) {
        this.serverUrl = serverUrl;
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
        } else if (state == State.IN_GAME) {
            return switch (command) {
                case "help" -> helpGameplay();
                case "redraw" -> redraw();
                case "move" -> makeMove(tokens);
                case "resign" -> resign();
                case "leave" -> leaveGame();
                case "highlight" -> highlightMoves(tokens);
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
        playerColor = color.equals("BLACK") ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
        currentGameID = game.gameID();
        connectWebSocket();
        state = State.IN_GAME;
        return "Joined game " + game.gameName() + " as " + color + ".";
    }

    private String observeGame(String[] tokens) throws Exception {
        if (tokens.length < 2) {
            return "Usage: observe <ID>";
        }
        int index = parseGameIndex(tokens[1]);
        var game = lastGameList[index];
        playerColor = null;
        currentGameID = game.gameID();
        connectWebSocket();
        state = State.IN_GAME;
        return "Observing game " + game.gameName() + ".";
    }

    // ── Gameplay commands ─────────────────────────

    private String helpGameplay() {
        return """
                redraw - redraw the chess board
                move <FROM> <TO> [PROMOTION] - make a move (e.g. move e2 e4)
                resign - forfeit the game
                leave - leave the game
                highlight <POSITION> - show legal moves (e.g. highlight e2)
                help - with possible commands""";
    }

    private String redraw() {
        if (currentGame != null) {
            var perspective = (playerColor != null) ? playerColor : ChessGame.TeamColor.WHITE;
            ChessBoardRenderer.drawBoard(currentGame.getBoard(), perspective);
        }
        return "";
    }

    private String makeMove(String[] tokens) throws Exception {
        if (playerColor == null) {
            return "Observers cannot make moves.";
        }
        if (tokens.length < 3) {
            return "Usage: move <FROM> <TO> [PROMOTION]";
        }
        ChessPosition from = parsePosition(tokens[1]);
        ChessPosition to = parsePosition(tokens[2]);
        chess.ChessPiece.PieceType promotion = null;
        if (tokens.length >= 4) {
            promotion = parsePromotion(tokens[3]);
        }
        ChessMove move = new ChessMove(from, to, promotion);
        var command = new UserGameCommand(authToken, currentGameID, move);
        ws.sendCommand(command);
        return "";
    }

    private String resign() throws Exception {
        if (playerColor == null) {
            return "Observers cannot resign.";
        }
        System.out.print("Are you sure you want to resign? (yes/no): ");
        var confirm = scanner.nextLine().trim().toLowerCase();
        if (!confirm.equals("yes")) {
            return "Resignation cancelled.";
        }
        var command = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, currentGameID);
        ws.sendCommand(command);
        return "";
    }

    private String leaveGame() throws Exception {
        var command = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, currentGameID);
        ws.sendCommand(command);
        ws.close();
        ws = null;
        currentGame = null;
        state = State.LOGGED_IN;
        return "Left the game.";
    }

    private String highlightMoves(String[] tokens) {
        if (tokens.length < 2) {
            return "Usage: highlight <POSITION> (e.g. highlight e2)";
        }
        if (currentGame == null) {
            return "No game loaded.";
        }
        ChessPosition position = parsePosition(tokens[1]);
        var piece = currentGame.getBoard().getPiece(position);
        if (piece == null) {
            return "No piece at that position.";
        }
        var validMoves = currentGame.validMoves(position);
        var destinations = validMoves.stream()
                .map(ChessMove::getEndPosition)
                .collect(java.util.stream.Collectors.toSet());
        var perspective = (playerColor != null) ? playerColor : ChessGame.TeamColor.WHITE;
        ChessBoardRenderer.drawBoard(currentGame.getBoard(), perspective, position, destinations);
        if (validMoves.isEmpty()) {
            return "No legal moves for that piece.";
        }
        return validMoves.size() + " legal move(s) highlighted.";
    }

    // ── WebSocket ─────────────────────────────────

    private void connectWebSocket() throws Exception {
        ws = new WebSocketClient(serverUrl, this);
        var command = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, currentGameID);
        ws.sendCommand(command);
    }

    @Override
    public void onMessage(ServerMessage message) {
        switch (message.getServerMessageType()) {
            case LOAD_GAME -> {
                currentGame = message.getGame();
                var perspective = (playerColor != null) ? playerColor : ChessGame.TeamColor.WHITE;
                System.out.println();
                ChessBoardRenderer.drawBoard(currentGame.getBoard(), perspective);
                printPrompt();
            }
            case NOTIFICATION -> {
                System.out.println();
                System.out.println("NOTIFICATION: " + message.getMessage());
                printPrompt();
            }
            case ERROR -> {
                System.out.println();
                System.out.println("ERROR: " + message.getErrorMessage());
                printPrompt();
            }
        }
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

    private ChessPosition parsePosition(String input) {
        if (input.length() != 2) {
            throw new IllegalArgumentException("Invalid position: " + input);
        }
        int col = input.charAt(0) - 'a' + 1;
        int row = input.charAt(1) - '0';
        return new ChessPosition(row, col);
    }

    private String positionToString(ChessPosition pos) {
        char col = (char) ('a' + pos.getColumn() - 1);
        return "" + col + pos.getRow();
    }

    private chess.ChessPiece.PieceType parsePromotion(String input) {
        return switch (input.toLowerCase()) {
            case "queen", "q" -> chess.ChessPiece.PieceType.QUEEN;
            case "rook", "r" -> chess.ChessPiece.PieceType.ROOK;
            case "bishop", "b" -> chess.ChessPiece.PieceType.BISHOP;
            case "knight", "n" -> chess.ChessPiece.PieceType.KNIGHT;
            default -> throw new IllegalArgumentException("Invalid promotion piece: " + input);
        };
    }

    private void printPrompt() {
        var label = switch (state) {
            case LOGGED_OUT -> "[LOGGED_OUT]";
            case LOGGED_IN -> "[LOGGED_IN]";
            case IN_GAME -> "[IN_GAME]";
        };
        System.out.print(label + " >>> ");
    }
}
