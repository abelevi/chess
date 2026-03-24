package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

import static ui.EscapeSequences.*;

public class ChessBoardRenderer {

    private static final String[] COL_LABELS = {"a", "b", "c", "d", "e", "f", "g", "h"};

    public static void drawBoard(ChessBoard board, ChessGame.TeamColor perspective) {
        boolean whiteView = (perspective == ChessGame.TeamColor.WHITE);

        System.out.println();
        printColumnHeaders(whiteView);

        int rowStart = whiteView ? 8 : 1;
        int rowEnd = whiteView ? 1 : 8;
        int rowStep = whiteView ? -1 : 1;

        for (int row = rowStart; whiteView ? row >= rowEnd : row <= rowEnd; row += rowStep) {
            System.out.print(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_WHITE + " " + row + " ");

            int colStart = whiteView ? 1 : 8;
            int colEnd = whiteView ? 8 : 1;
            int colStep = whiteView ? 1 : -1;

            for (int col = colStart; whiteView ? col <= colEnd : col >= colEnd; col += colStep) {
                boolean isLightSquare = (row + col) % 2 != 0;
                String bgColor = isLightSquare ? SET_BG_COLOR_WHITE : SET_BG_COLOR_LIGHT_GREY;

                ChessPiece piece = board.getPiece(new ChessPosition(row, col));
                String pieceStr = getPieceString(piece);

                System.out.print(bgColor + pieceStr);
            }

            System.out.print(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_WHITE + " " + row + " ");
            System.out.println(RESET_BG_COLOR + RESET_TEXT_COLOR);
        }

        printColumnHeaders(whiteView);
        System.out.println();
    }

    private static void printColumnHeaders(boolean whiteView) {
        System.out.print(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_WHITE + "   ");
        int start = whiteView ? 0 : 7;
        int end = whiteView ? 7 : 0;
        int step = whiteView ? 1 : -1;
        for (int i = start; whiteView ? i <= end : i >= end; i += step) {
            System.out.print(" " + COL_LABELS[i] + " ");
        }
        System.out.println("   " + RESET_BG_COLOR + RESET_TEXT_COLOR);
    }

    private static String getPieceString(ChessPiece piece) {
        if (piece == null) {
            return EMPTY;
        }
        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            return switch (piece.getPieceType()) {
                case KING -> WHITE_KING;
                case QUEEN -> WHITE_QUEEN;
                case BISHOP -> WHITE_BISHOP;
                case KNIGHT -> WHITE_KNIGHT;
                case ROOK -> WHITE_ROOK;
                case PAWN -> WHITE_PAWN;
            };
        } else {
            return switch (piece.getPieceType()) {
                case KING -> BLACK_KING;
                case QUEEN -> BLACK_QUEEN;
                case BISHOP -> BLACK_BISHOP;
                case KNIGHT -> BLACK_KNIGHT;
                case ROOK -> BLACK_ROOK;
                case PAWN -> BLACK_PAWN;
            };
        }
    }
}
