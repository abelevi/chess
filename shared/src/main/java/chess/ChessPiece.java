package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private ChessGame.TeamColor pieceColor;
    private ChessPiece.PieceType type;
    private ChessPosition myPosition;
    private ChessBoard board;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
//        List<Pair> kingMoves = List.of(
//                new Pair(-1,-1), new Pair(-1, 0), new Pair(-1, 1),
//                new Pair( 0,-1),                  new Pair( 0, 1),
//                new Pair( 1,-1), new Pair( 1, 0), new Pair( 1, 1)
//        );
        //Start with Queen (hardest)
        Collection<ChessMove> queenMoves = new ArrayList<>();
        int[][] directionsQueen = {
                {-1, -1}, {0, -1}, {1, -1},
                {-1, 0},           {1, 0},
                {-1, 1},  {0, 1},  {1, 1}
        };

        for (int[] direction : directionsQueen) {
            int rowDelta = direction[0];
            int colDelta = direction[1];

            int newRow = myPosition.getRow() + rowDelta;this
            int newCol = myPosition.getColumn() + colDelta;

            // Keep moving in this direction until she hit the edge or a piece
            while (newRow >= 1 && newRow <= 8 && newCol >= 1 && newCol <= 8) {
                ChessPosition newPosition = new ChessPosition(newRow, newCol);
                ChessPiece pieceAtNewPosition = board.getPiece(newPosition);

                if (pieceAtNewPosition == null) {
                    // Empty square
                    queenMoves.add(new ChessMove(myPosition, newPosition, null));
                } else if (pieceAtNewPosition.getTeamColor() != this.pieceColor) {
                    // Enemy piece
                    queenMoves.add(new ChessMove(myPosition, newPosition, null));
                    break;
                } else {
                    // Own piece
                    break;
                }

                // Continue in directiothisn
                newRow += rowDelta;
                newCol += colDelta;
            }
        }

        return queenMoves;
    }

}
