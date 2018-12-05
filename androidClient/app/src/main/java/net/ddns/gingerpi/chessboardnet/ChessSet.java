package net.ddns.gingerpi.chessboardnet;

import android.content.Context;

public class ChessSet {
    public enum texturePack{white,black,goblins,teatime};
    public enum piece{pawn_front,pawn_back,rook_front,rook_back,knight_front,knight_back,bishop_front,bishop_back,queen_front,queen_back,king_front,king_back};
    int[] chessSet = new int[piece.values().length];
    Context mContext;

    public ChessSet(Context mContext, texturePack set)
    {
        this.mContext=mContext;

        switch(set){
            case black: {
                this.chessSet[piece.pawn_front.ordinal()]=R.drawable.doodle_black_pawn;
                this.chessSet[piece.pawn_back.ordinal()]=R.drawable.doodle_black_pawn_back;
                this.chessSet[piece.rook_front.ordinal()]=R.drawable.doodle_black_rook;
                this.chessSet[piece.rook_back.ordinal()]=R.drawable.doodle_black_rook;
                this.chessSet[piece.knight_front.ordinal()]=R.drawable.doodle_black_knight;
                this.chessSet[piece.knight_back.ordinal()]=R.drawable.doodle_black_knight_back;
                this.chessSet[piece.bishop_front.ordinal()]=R.drawable.doodle_black_bishop;
                this.chessSet[piece.bishop_back.ordinal()]=R.drawable.doodle_black_bishop_back;
                this.chessSet[piece.queen_front.ordinal()]=R.drawable.doodle_black_queen;
                this.chessSet[piece.queen_back.ordinal()]=R.drawable.doodle_black_queen_back;
                this.chessSet[piece.king_front.ordinal()]=R.drawable.doodle_black_king;
                this.chessSet[piece.king_back.ordinal()]=R.drawable.doodle_black_king_back;
                break;
            }

            case white: {
                this.chessSet[piece.pawn_front.ordinal()]=R.drawable.doodle_white_pawn;
                this.chessSet[piece.pawn_back.ordinal()]=R.drawable.doodle_white_pawn_back;
                this.chessSet[piece.rook_front.ordinal()]=R.drawable.doodle_white_rook;
                this.chessSet[piece.rook_back.ordinal()]=R.drawable.doodle_white_rook;
                this.chessSet[piece.knight_front.ordinal()]=R.drawable.doodle_white_knight;
                this.chessSet[piece.knight_back.ordinal()]=R.drawable.doodle_white_knight_back;
                this.chessSet[piece.bishop_front.ordinal()]=R.drawable.doodle_white_bishop;
                this.chessSet[piece.bishop_back.ordinal()]=R.drawable.doodle_white_bishop_back;
                this.chessSet[piece.queen_front.ordinal()]=R.drawable.doodle_white_queen;
                this.chessSet[piece.queen_back.ordinal()]=R.drawable.doodle_white_queen_back;
                this.chessSet[piece.king_front.ordinal()]=R.drawable.doodle_white_king;
                this.chessSet[piece.king_back.ordinal()]=R.drawable.doodle_white_king_back;
                break;
            }

			case goblins: {
				this.chessSet[piece.pawn_front.ordinal()]=R.drawable.goblin_pawn;
				this.chessSet[piece.pawn_back.ordinal()]=R.drawable.goblin_pawn_back;
				this.chessSet[piece.rook_front.ordinal()]=R.drawable.goblin_rook;
				this.chessSet[piece.rook_back.ordinal()]=R.drawable.goblin_rook_back;
				this.chessSet[piece.knight_front.ordinal()]=R.drawable.goblin_knight;
				this.chessSet[piece.knight_back.ordinal()]=R.drawable.goblin_knight_back;
				this.chessSet[piece.bishop_front.ordinal()]=R.drawable.goblin_bishop;
				this.chessSet[piece.bishop_back.ordinal()]=R.drawable.goblin_bishop_back;
				this.chessSet[piece.queen_front.ordinal()]=R.drawable.goblin_queen;
				this.chessSet[piece.queen_back.ordinal()]=R.drawable.goblin_queen_back;
				this.chessSet[piece.king_front.ordinal()]=R.drawable.goblin_king;
				this.chessSet[piece.king_back.ordinal()]=R.drawable.goblin_king_back;
				break;
			}

			case teatime: {
				this.chessSet[piece.pawn_front.ordinal()]=R.drawable.teatime_pawn;
				this.chessSet[piece.pawn_back.ordinal()]=R.drawable.teatime_pawn_back;
				this.chessSet[piece.rook_front.ordinal()]=R.drawable.teatime_rook;
				this.chessSet[piece.rook_back.ordinal()]=R.drawable.teatime_rook;
				this.chessSet[piece.knight_front.ordinal()]=R.drawable.teatime_knight;
				this.chessSet[piece.knight_back.ordinal()]=R.drawable.teatime_knight_back;
				this.chessSet[piece.bishop_front.ordinal()]=R.drawable.teatime_bishop;
				this.chessSet[piece.bishop_back.ordinal()]=R.drawable.teatime_bishop_back;
				this.chessSet[piece.queen_front.ordinal()]=R.drawable.teatime_queen;
				this.chessSet[piece.queen_back.ordinal()]=R.drawable.teatime_queen_back;
				this.chessSet[piece.king_front.ordinal()]=R.drawable.teatime_king;
				this.chessSet[piece.king_back.ordinal()]=R.drawable.teatime_king_back;
				break;
			}
        }
    }

    public int getPiece(piece p){
        return this.chessSet[p.ordinal()];
    }
}
