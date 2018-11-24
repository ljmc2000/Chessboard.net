package net.ddns.gingerpi.chessboardnetCommon;
import java.util.ArrayList;

public class ChessBoard
{
	ChessPiece[] map={
			new Rook(true),null,null,null,null,null,null,new Rook(true),
			new Pawn(true),new Pawn(true),new Pawn(true),new Pawn(true),new Pawn(true),new Pawn(true),new Pawn(true),new Pawn(true),
			null,null,null,null,null,null,null,null,
			null,null,null,null,null,null,null,null,
			null,null,null,null,null,null,null,null,
			null,null,null,null,null,null,null,null,
			null,null,null,null,null,null,null,null,
			null,null,null,null,null,null,null,null,
			new Pawn(false),new Pawn(false),new Pawn(false),new Pawn(false),new Pawn(false),new Pawn(false),new Pawn(false),new Pawn(false),
			new Rook(false),null,null,null,null,null,null,new Rook(false)
	};

	public ChessPiece getItem(int position)
	{
		return map[position];
	}
}
