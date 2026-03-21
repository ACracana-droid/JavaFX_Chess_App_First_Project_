package org.chess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.chess.ChessLoop.*;
import static org.chess.Queen.QUEEN_MOVE_VECTORS;

public class King extends Piece implements SpecialFirstMover {
    boolean firstMove = true;

    King(Team colour) {
        super(colour);
        this.id = 'K';
        this.sprite = getImageView("-king.png");
//        this.property = SpecialProperties.CHECK_AND_MATE;
    }


    @Override
    public void updateNecessaryFirstMoveInfo() {
        firstMove = false;
    }


    @Override
    public List<Move> moves(Tile[][] board) {
        List<int[]> threats = getThreats(getEnemyTeam().pieces, board);
        List<Move> ls = new ArrayList<>();

        for (int[] move : QUEEN_MOVE_VECTORS) {
            int rank = coOrds[0] + move[0];
            int file = coOrds[1] + move[1];
            if (isValidCoOrd(rank, file, team) && !isCoOrdsInThreats(new int[]{rank, file}, threats)) {
                ls.add(new Move(rank, file));
            }
        }

        /// castling check
        if (firstMove && !isCoOrdsInThreats(coOrds, threats)) {
            getAlliedRooks(team).forEach(r -> addPossibleCastleMove(r, board, threats, ls));
        }
        return ls;
    }

    private void addPossibleCastleMove(Rook r, Tile[][] board, List<int[]> threats, List<Move> ls) {
        if (r.isFirstMove()) {
            boolean possible = true;
            Move move;
            int[] place = {this.coOrds[0], this.coOrds[1]};
            /// bad recursive call. The king needs all moves of all enemies, including the king, so it calls it
            /// so it happens with new king ad infinitum.


            if (r.coOrds[1] < this.coOrds[1]) { // left-hand rook.
                move = new MultiMove(this.coOrds[0], this.coOrds[1] - 2,
                        r.coOrds, new Move(this.coOrds[0], this.coOrds[1] - 1)
                        , SpecialProperties.CASTLE);

                for (int i = 0; i < 3; i++) { // 3 spaces left of king are empty
                    place[1]--;
                    if (getTile(place, board).hasPiece()
                            || isCoOrdsInThreats(place, threats)) {
                        possible = false;
                        break;
                    }

                }
            } else { // right-hand rook.
                move = new MultiMove(this.coOrds[0], this.coOrds[1] + 2,
                        r.coOrds, new Move(this.coOrds[0], this.coOrds[1] + 1)
                        , SpecialProperties.CASTLE);

                for (int i = 0; i < 2; i++) { // 2 spaces right of king are empty
                    place[1]++;
                    if (getTile(place, board).hasPiece()
                            || isCoOrdsInThreats(place, threats)) {
                        possible = false;
                        break;
                    }
                }
            }
            if (possible) ls.add(move);
        }
    }

    private static boolean isCoOrdsInThreats(int[] coOrds, List<int[]> threats) {
        for (int[] threat : threats) {
            if (Arrays.equals(threat, coOrds)) {
                return true;
            }
        }
        return false;
    }

    private static List<Rook> getAlliedRooks(Team team) {
        List<Rook> ls = new ArrayList<>();
        for (Piece piece : team.pieces) {
            if (piece.matchesPieceId('R')) {
                ls.add((Rook) piece);
            }
        }
        return ls;
    }

    public List<Move> threatsIgnoreLegal() {
        List<Move> ls = new ArrayList<>();

        for (int[] move : QUEEN_MOVE_VECTORS) {
            int rank = coOrds[0] + move[0];
            int file = coOrds[1] + move[1];
            if (isValidCoOrd(rank, file, team)) {
                ls.add(new Move(rank, file));
            }
        }
        return ls;
    }
}
