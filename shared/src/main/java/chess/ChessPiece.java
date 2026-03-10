package chess;

import java.util.*;

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

    private Collection<ChessMove> pieceMovesHelper(int[][] directions){
        Collection<ChessMove> moves = new ArrayList<>();
        for (int[] direction : directions) {
            int rowDelta = direction[0];
            int colDelta = direction[1];

            int newRow = myPosition.getRow() + rowDelta;
            int newCol = myPosition.getColumn() + colDelta;

            // Keep moving in this direction until she hit the edge or a piece
            while (newRow >= 1 && newRow <= 8 && newCol >= 1 && newCol <= 8) {
                ChessPosition newPosition = new ChessPosition(newRow, newCol);
                ChessPiece pieceAtNewPosition = board.getPiece(newPosition);

                if (pieceAtNewPosition == null) {
                    // Empty square
                    moves.add(new ChessMove(myPosition, newPosition, null));
                } else if (pieceAtNewPosition.getTeamColor() != this.pieceColor) {
                    // Enemy piece
                    moves.add(new ChessMove(myPosition, newPosition, null));
                    break;
                } else {
                    // Own piece
                    break;
                }

                // Continue in direction
                newRow += rowDelta;
                newCol += colDelta;
            }
        }

        return moves;
    }

    private Collection<ChessMove> pieceMovesHelperSingle(int[][] directions){
        Collection<ChessMove> moves = new ArrayList<>();
        for (int[] direction : directions) {
            int rowDelta = direction[0];
            int colDelta = direction[1];

            int newRow = myPosition.getRow() + rowDelta;
            int newCol = myPosition.getColumn() + colDelta;

            if (newRow < 1 || newRow > 8 || newCol < 1 || newCol > 8) {
                continue;
            }

            ChessPosition newPosition = new ChessPosition(newRow, newCol);
            ChessPiece pieceAtNewPosition = board.getPiece(newPosition);

            if (pieceAtNewPosition == null) {
                    // Empty square
                    moves.add(new ChessMove(myPosition, newPosition, null));
            } else if (pieceAtNewPosition.getTeamColor() != this.pieceColor) {
                    // Enemy piece
                    moves.add(new ChessMove(myPosition, newPosition, null));
            }
            // Own piece - do nothing, check next direction

                // Continue in direction
            newRow += rowDelta;
            newCol += colDelta;
            }

        return moves;
    }

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        this.board = board;
        this.myPosition = myPosition;

        return switch (this.type) {
            case QUEEN -> pieceMovesHelper(new int[][]{
                    {-1, -1}, {0, -1}, {1, -1},
                    {-1, 0},           {1, 0},
                    {-1, 1},  {0, 1},  {1, 1}
            });
            case BISHOP -> pieceMovesHelper(new int[][]{{-1, -1}, {-1, 1}, {1, -1}, {1, 1}});
            case ROOK -> pieceMovesHelper(new int[][]{{1, 0}, {0, 1}, {-1, 0}, {0, -1}});
            case KNIGHT -> pieceMovesHelperSingle(new int[][]{
                    {2, 1}, {2, -1}, {-2, 1}, {-2, -1},
                    {1, 2}, {1, -2}, {-1, 2}, {-1, -2}
            });
            case KING -> pieceMovesHelperSingle(new int[][]{
                    {-1, -1}, {0, -1}, {1, -1},
                    {-1, 0},           {1, 0},
                    {-1, 1},  {0, 1},  {1, 1}
            });
            case PAWN -> pawnMoves();
        };
    }

    private Collection<ChessMove> pawnMoves() {
        Collection<ChessMove> moves = new ArrayList<>();
        int directionColor = (this.getTeamColor() == ChessGame.TeamColor.WHITE) ? 1 : -1;
        int startRow = (this.getTeamColor() == ChessGame.TeamColor.WHITE) ? 2 : 7;
        int promotionRow = (this.getTeamColor() == ChessGame.TeamColor.WHITE) ? 8 : 1;
        int[][] directionsPawn = {
                {directionColor, 0}, {directionColor, -1}, {directionColor, 1}, {2 * directionColor, 0}
        };

        for (int[] direction : directionsPawn) {
            int rowDelta = direction[0];
            int colDelta = direction[1];
            int newRow = myPosition.getRow() + rowDelta;
            int newCol = myPosition.getColumn() + colDelta;
            if (newRow < 1 || newRow > 8 || newCol < 1 || newCol > 8) {
                continue;
            }
            ChessPosition newPosition = new ChessPosition(newRow, newCol);
            ChessPiece pieceAtNewPosition = board.getPiece(newPosition);

            boolean canMoveForward = (pieceAtNewPosition == null && colDelta == 0);
            boolean canCapture = (pieceAtNewPosition != null
                    && pieceAtNewPosition.getTeamColor() != this.pieceColor && colDelta != 0);

            if (canMoveForward) {
                ChessPosition frontPos = new ChessPosition(myPosition.getRow() + directionColor, myPosition.getColumn());
                boolean isDoubleMove = (rowDelta == 2 || rowDelta == -2);
                if (isDoubleMove && (myPosition.getRow() != startRow || board.getPiece(frontPos) != null)) {
                    continue;
                }
            }

            if (canMoveForward || canCapture) {
                if (newRow == promotionRow) {
                    moves.add(new ChessMove(myPosition, newPosition, PieceType.QUEEN));
                    moves.add(new ChessMove(myPosition, newPosition, PieceType.ROOK));
                    moves.add(new ChessMove(myPosition, newPosition, PieceType.BISHOP));
                    moves.add(new ChessMove(myPosition, newPosition, PieceType.KNIGHT));
                } else {
                    moves.add(new ChessMove(myPosition, newPosition, null));
                }
            }
        }
        return moves;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }
}
