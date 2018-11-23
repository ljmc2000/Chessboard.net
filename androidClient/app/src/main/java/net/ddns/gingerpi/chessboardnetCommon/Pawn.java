package net.ddns.gingerpi.chessboardnetCommon;
import java.util.ArrayList;

//this class uses base 8 mathamatics as it makes dealing with chess co-ordinates easier
public class Pawn extends ChessPiece
{
	public Pawn(boolean color)
	{
		super(color);
	}

	public Pawn(boolean color,int moveCount,int killCount)
	{
		super(color);
		this.moveCount=moveCount;
		this.killCount=killCount;
	}

	@Override
	public ArrayList<Integer> getLegalMoves(int position)//,ChessBoard chessBoard)
	{
		ArrayList<Integer> returnme=new ArrayList<Integer>();

		if(color)
			returnme.add(position+010);
		else
			returnme.add(position-010);

		return returnme;
	}

	@Override
	public boolean checkLegal(int chessMove,boolean attacking)
	{
		if(color)
			chessMove=invertMove(chessMove);

		int origin=chessMove/64;
		int destination=chessMove%64;
		int difference=destination-origin;

		switch(difference)
		{
			case 020:
				return moveCount==0 && !attacking;
			case 010:
				return !attacking;

			case 007:	//010-1
			case 011:	//010+1
				return divineInterventionCheck(chessMove) && attacking;

			default:
				return false;
		}
	}
}
