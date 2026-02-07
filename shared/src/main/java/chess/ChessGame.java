package chess;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessBoard board;
    private TeamColor currentTurn;
    public ChessGame() {
        board = new ChessBoard();
        board.resetBoard();
        currentTurn = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return currentTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        currentTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        Collection<ChessMove> moves = new java.util.ArrayList<>(List.of());
        ChessPiece piece = board.getPiece(startPosition);
        TeamColor teamColor = piece.getTeamColor();
        for (ChessMove move : piece.pieceMoves(board, startPosition)) {
            ChessPiece captured = board.getPiece(move.getEndPosition());
            board.addPiece(move.getEndPosition(), piece);
            board.addPiece(move.getStartPosition(), null);
            boolean inCheck = isInCheck(teamColor);
            if (!inCheck){
                moves.add(move);
            }
            board.addPiece(move.getStartPosition(), piece);
            board.addPiece(move.getEndPosition(), captured);
        }
        return moves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition start = move.getStartPosition();
        ChessPosition end  = move.getEndPosition();
        ChessPiece.PieceType promotion = move.getPromotionPiece();
        ChessPiece piece = board.getPiece(start);
        if (piece == null) throw new InvalidMoveException("no piece");
        if (piece.getTeamColor() != currentTurn) throw new InvalidMoveException("wrong turn");
        if (!validMoves(start).contains(move)) throw new InvalidMoveException("invalid move");
        if (promotion != null){
            board.addPiece(end, new ChessPiece(piece.getTeamColor(), promotion));
        } else {
            board.addPiece(end, piece);
        }
        board.addPiece(start, null);
        currentTurn = (currentTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    private ChessPosition findKing(TeamColor teamColor){
        for (int row = 1; row < 9; row++){
            for (int col = 1; col < 9; col++){
                ChessPosition position = new ChessPosition(row, col);
                if (board.getPiece(position) != null && board.getPiece(position).getPieceType() == ChessPiece.PieceType.KING && board.getPiece(position).getTeamColor() == teamColor){
                    return position;
                }
            }
        }
        return null;
    }
    private boolean isLethal(TeamColor teamColor, ChessPosition kingPosition){
        for (int row = 1; row < 9; row++) {
            for (int col = 1; col < 9; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getTeamColor() != teamColor) {
                    for (ChessMove move : piece.pieceMoves(board, position)) {
                        if (move.getEndPosition().equals(kingPosition)) {
                            return true;
                        }
                    }
                }
            }

        }
        return false;
    }
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = findKing(teamColor);
        return isLethal(teamColor, kingPosition);
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
public boolean isInCheckmate(TeamColor teamColor) {
        if (isInCheck(teamColor)){
            for (int row = 1; row < 9; row++) {
                for (int col = 1; col < 9; col++) {
                    ChessPosition position = new ChessPosition(row, col);
                    ChessPiece piece = board.getPiece(position);
                    if (piece != null && piece.getTeamColor().equals(teamColor)) {
                        for (ChessMove move : piece.pieceMoves(board, position)) {
                            ChessPiece captured = board.getPiece(move.getEndPosition());
                            board.addPiece(move.getEndPosition(), piece);
                            board.addPiece(move.getStartPosition(), null);
                            boolean stillInCheck = isInCheck(teamColor);
                            board.addPiece(move.getStartPosition(), piece);
                            board.addPiece(move.getEndPosition(), captured);
                            if (!stillInCheck){
                                return false;
                            }
                        }
                    }
                }

            }
            return true;
        }
        return false;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(ChessGame.TeamColor teamColor) {
        if(!isInCheck(teamColor)){
            for (int row = 1; row < 9; row++) {
                for (int col = 1; col < 9; col++) {
                    ChessPosition position = new ChessPosition(row, col);
                    ChessPiece piece = board.getPiece(position);
                    if (piece != null && piece.getTeamColor().equals(teamColor)) {
                        for (ChessMove move : piece.pieceMoves(board, position)) {
                            ChessPiece captured = board.getPiece(move.getEndPosition());
                            board.addPiece(move.getEndPosition(), piece);
                            board.addPiece(move.getStartPosition(), null);
                            boolean inCheck = isInCheck(teamColor);
                            board.addPiece(move.getStartPosition(), piece);
                            board.addPiece(move.getEndPosition(), captured);
                            if (!inCheck) {
                                return false;
                            }
                        }
                    }
                }

            }
            return true;
        }
        return false;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(getBoard(), chessGame.getBoard()) && currentTurn == chessGame.currentTurn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBoard(), currentTurn);
    }
}
