package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.Collection;
import java.util.Collections;

import static ui.EscapeSequences.*;

public class ChessBoardRenderer {

    private static final String[] COL_LABELS = {"a", "b", "c", "d", "e", "f", "g", "h"};

    public static void drawBoard(ChessBoard board, ChessGame.TeamColor perspective) {
        drawBoard(board, perspective, null, Collections.emptySet());
    }

    public static void drawBoard(ChessBoard board, ChessGame.TeamColor perspective,
                                  ChessPosition selectedPosition, Collection<ChessPosition> highlights) {
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
                ChessPosition pos = new ChessPosition(row, col);
                boolean isLightSquare = (row + col) % 2 != 0;

                String bgColor;
                if (selectedPosition != null && selectedPosition.equals(pos)) {
                    bgColor = SET_BG_COLOR_YELLOW;
                } else if (highlights.contains(pos)) {
                    bgColor = isLightSquare ? SET_BG_COLOR_GREEN : SET_BG_COLOR_DARK_GREEN;
                } else {
                    bgColor = isLightSquare ? SET_BG_COLOR_WHITE : SET_BG_COLOR_LIGHT_GREY;
                }

                ChessPiece piece = board.getPiece(pos);
                String pieceStr = getPieceString(piece);
                String textColor = getTextColor(piece);

                System.out.print(bgColor + textColor + pieceStr);
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

    private static String getTextColor(ChessPiece piece) {
        if (piece == null) {
            return "";
        }
        return piece.getTeamColor() == ChessGame.TeamColor.WHITE
                ? SET_TEXT_COLOR_BLUE : SET_TEXT_COLOR_RED;
    }

    private static String getPieceString(ChessPiece piece) {
        if (piece == null) {
            return EMPTY;
        }
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
