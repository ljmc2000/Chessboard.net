package net.ddns.gingerpi.chessboardnetCommon;
import java.util.ArrayList;

public class King extends ChessPiece
{
	public King(boolean color)
	{
		super(color);
	}

	public King(boolean color,int moveCount, int killCount)
	{
		super(color,moveCount,killCount);
	}

	@Override
        public ArrayList<Integer> getLegalMoves(int position,ChessBoard chessBoard)
	{
		ArrayList<Integer> returnme = new ArrayList<Integer>();
		int x=position%8;
		int y=position/8;

		if(y<7)
		{
			chessBoard.addMoveToList(returnme,position+010,color);
			if(x>0) chessBoard.addMoveToList(returnme,position+007,color);
			if(x<7) chessBoard.addMoveToList(returnme,position+011,color);
		}

		if(y>0)
		{
			chessBoard.addMoveToList(returnme,position-010,color);
			if(x>0) chessBoard.addMoveToList(returnme,position-011,color);
			if(x<7) chessBoard.addMoveToList(returnme,position-007,color);
		}

		if(x>0) chessBoard.addMoveToList(returnme,position-001,color);
		if(x<7) chessBoard.addMoveToList(returnme,position+001,color);

		return returnme;
	}

	@Override
	public boolean checkLegal(int chessMove,boolean attacking)
	{
		int origin=chessMove/64;
		int destination=chessMove%64;
		int difference=destination-origin;
		if(difference<0) difference*=-1;
		int x=difference%8;
		int y=difference/8;

		return x<=1 && y<=1;
	}

	public String toString()
	{
		if(color)
			return "K";
		else
			return "k";
	}
}
