package chess;

import jdk.jshell.spi.ExecutionControl;

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

        return moves;
    }

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        this.board = board;
        this.myPosition = myPosition;
//        List<Pair> kingMoves = List.of(
//                new Pair(-1,-1), new Pair(-1, 0), new Pair(-1, 1),
//                new Pair( 0,-1),                  new Pair( 0, 1),
//                new Pair( 1,-1), new Pair( 1, 0), new Pair( 1, 1)
//        );
        //Start with Queen (hardest)
        if (this.type == PieceType.QUEEN) {
            int[][] directionsQueen = {
                    {-1, -1}, {0, -1}, {1, -1},
                    {-1, 0},           {1, 0},
                    {-1, 1},  {0, 1},  {1, 1}
            };

//            for (int[] direction : directionsQueen) {
//                int rowDelta = direction[0];
//                int colDelta = direction[1];
//
//                int newRow = myPosition.getRow() + rowDelta;
//                int newCol = myPosition.getColumn() + colDelta;
//
//                // Keep moving in this direction until she hit the edge or a piece
//                while (newRow >= 1 && newRow <= 8 && newCol >= 1 && newCol <= 8) {
//                    ChessPosition newPosition = new ChessPosition(newRow, newCol);
//                    ChessPiece pieceAtNewPosition = board.getPiece(newPosition);
//
//                    if (pieceAtNewPosition == null) {
//                        // Empty square
//                        queenMoves.add(new ChessMove(myPosition, newPosition, null));
//                    } else if (pieceAtNewPosition.getTeamColor() != this.pieceColor) {
//                        // Enemy piece
//                        queenMoves.add(new ChessMove(myPosition, newPosition, null));
//                        break;
//                    } else {
//                        // Own piece
//                        break;
//                    }
//
//                    // Continue in direction
//                    newRow += rowDelta;
//                    newCol += colDelta;
//                }
//            }
//
//            return queenMoves;
            return pieceMovesHelper(directionsQueen);
        }
        if (this.type == PieceType.BISHOP) {

            int[][] directionsBishop = {
                    {-1,-1},{-1,1},{1,-1},{1,1}
            };
            return pieceMovesHelper(directionsBishop);
        }

        if (this.type == PieceType.ROOK) {
            int[][] directionsRook = {
                    {1,0},{0,1},{-1,0},{0,-1}
            };
            return  pieceMovesHelper(directionsRook);
        }

        if (this.type == PieceType.PAWN){
            Collection<ChessMove> moves = new ArrayList<>();
            //check if forward one is empty, if it is, it is available
            //check if diagonal forward one either direction then add those
            int directionColor = (this.getTeamColor() == ChessGame.TeamColor.WHITE) ? 1 : -1;
            int startRow = (this.getTeamColor() == ChessGame.TeamColor.WHITE) ? 2 : 7;
            int[][] directionsPawn = {
                    {directionColor, 0}, {directionColor, -1}, {directionColor,1}, {2*directionColor, 0}
            };

            for (int[] direction : directionsPawn) {
                int rowDelta = direction[0];
                int colDelta = direction[1];

                int newRow = myPosition.getRow() + rowDelta;
                int newCol = myPosition.getColumn() + colDelta;
                ChessPosition newPosition = new ChessPosition(newRow, newCol);
                ChessPiece pieceAtNewPosition = board.getPiece(newPosition);

                if (pieceAtNewPosition == null && colDelta == 0) {
                    // Empty square
                    ChessPosition frontPos= new ChessPosition(directionColor, 0);
                    if (((rowDelta == 2 || rowDelta == -2) && !(myPosition.getRow() == startRow) && !(board.getPiece(frontPos)==null))) {
                        break;
                    }
                    moves.add(new ChessMove(myPosition, newPosition, null));
                } else if (pieceAtNewPosition != null && pieceAtNewPosition.getTeamColor() != this.pieceColor && colDelta != 0) {
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

            return moves;


        }

        if (this.type == PieceType.KNIGHT){
            //directions to be single instance {2,1} and variants thereof
            int[][] directionsKnight = {
                    {2, 1}, {2, -1}, {-2, 1}, {-2, -1},
                    {1, 2}, {1, -2}, {-1, 2}, {-1, -2}
            };
            return  pieceMovesHelperSingle(directionsKnight);
        }
        if (this.type == PieceType.KING) {
            //directions like queen but not continuous
            int[][] directionsKing = {
                    {-1, -1}, {0, -1}, {1, -1},
                    {-1, 0},           {1, 0},
                    {-1, 1},  {0, 1},  {1, 1}
            };
            return pieceMovesHelperSingle(directionsKing);
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type && Objects.equals(myPosition, that.myPosition) && Objects.equals(board, that.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type, myPosition, board);
    }
}
