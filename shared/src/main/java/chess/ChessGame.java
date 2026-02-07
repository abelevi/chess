package chess;

import java.util.Collection;

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
        team = currentTurn;
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
        ChessPiece piece = board.getPiece(startPosition);
        for (ChessMove move : piece.pieceMoves(board, startPosition){
            
        }
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
        if (promotion != null){
            board.addPiece(end, new ChessPiece(piece.getTeamColor(), promotion));
        } else {
            board.addPiece(end, piece);
        }
        board.addPiece(start, null);
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
                if (board.getPiece(position).getPieceType() == ChessPiece.PieceType.KING && board.getPiece(position).getTeamColor() == teamColor){
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
                if (piece.getTeamColor() != teamColor) {
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
            ChessPosition kingPosition = findKing(teamColor);
            assert kingPosition != null;
            Collection<ChessMove> kingMoves = board.getPiece(kingPosition).pieceMoves(board,kingPosition);
            for (ChessMove move : kingMoves){
                if (!isLethal(teamColor, move.getEndPosition())){ //if the king has a move that doesn't result in a lethal play, checkmate is false
                    return false;
                }
            }
            for (int row = 1; row < 9; row++) {
                for (int col = 1; col < 9; col++) {
                    ChessPosition position = new ChessPosition(row, col);
                    ChessPiece piece = board.getPiece(position);
                    if (piece.getTeamColor().equals(teamColor)) {
                        for (ChessMove move : piece.pieceMoves(board, position)) {
                            ChessPiece captured = board.getPiece(move.getEndPosition());
                            board.addPiece(move.getEndPosition(), piece);
                            board.addPiece(move.getStartPosition(), null);
                            boolean stillInCheck = isInCheck(teamColor);
                            if (!stillInCheck){
                                return false;
                            }
                            board.addPiece(move.getStartPosition(), piece);
                            board.addPiece(move.getEndPosition(), captured);
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
                    if (piece.getTeamColor().equals(teamColor)) {
                        for (ChessMove move : piece.pieceMoves(board, position)) {
                            ChessPiece captured = board.getPiece(move.getEndPosition());
                            board.addPiece(move.getEndPosition(), piece);
                            board.addPiece(move.getStartPosition(), null);
                            boolean inCheck = isInCheck(teamColor);
                            if (!inCheck){
                                return true;
                            }
                            board.addPiece(move.getStartPosition(), piece);
                            board.addPiece(move.getEndPosition(), captured);
                        }
                    }
                }

            }
        }
        return false;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        board = new ChessBoard();
        board.resetBoard();
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }
}
