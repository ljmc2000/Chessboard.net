package net.ddns.gingerpi.chessboardnetCommon;
import java.util.ArrayList;

public class Pawn extends ChessPiece
{
	public Pawn(boolean color)
	{
		super(color);
	}

	public Pawn(boolean color,int moveCount, int killCount)
	{
		super(color,moveCount,killCount);
	}

	@Override
	public ArrayList<Integer> getLegalMoves(int position,ChessBoard chessBoard)
	{
		ArrayList<Integer> returnme=new ArrayList<Integer>();

		int direction;
		if(color)
			direction=1;
		else
			direction=-1;


		returnme.add(position+(010*direction));
		if(moveCount==0)
			returnme.add(position+(020*direction));

		ChessPiece target=chessBoard.getItem(position+011);	//010+1
		if(target!=null)
			if(divineInterventionCheck((position*0100)+(position+011)))
				if(target.getColor()!=color)
					 returnme.add(position+011);
		target=chessBoard.getItem(position+007);	//010-1
		if(target!=null)
			if(divineInterventionCheck((position*0100)+(position+007)))
				if(target.getColor()!=color)
					returnme.add(position+011);

		return returnme;
	}

	@Override
	public boolean checkLegal(int chessMove,boolean attacking)
	{
		if(!color)
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

	public String toString()
	{
		if(color)
			return "P";
		else
			return "p";
	}
}
