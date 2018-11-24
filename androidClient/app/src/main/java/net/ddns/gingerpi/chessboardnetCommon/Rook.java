package net.ddns.gingerpi.chessboardnetCommon;
import java.util.ArrayList;

public class Rook extends ChessPiece
{
	public Rook(boolean color)
	{
		super(color);
	}

	public Rook(boolean color,int moveCount, int killCount)
	{
		super(color,moveCount,killCount);
	}

	@Override
        public ArrayList<Integer> getLegalMoves(int position,ChessBoard chessBoard)
	{
		ArrayList<Integer> returnme=new ArrayList<Integer>();
		return returnme;
	}

	@Override
	public boolean checkLegal(int chessMove,boolean attacking)
	{
		int origin=chessMove/64;
		int destination=chessMove%64;

		return ((origin/8) == (destination/8)) || ((origin%8) == (destination%8));
	}
}
