package net.ddns.gingerpi.chessboardnetCommon;
import java.util.ArrayList;

public class Bishop extends ChessPiece
{
	public Bishop(boolean color)
	{
		super(color);
	}

	public Bishop(boolean color,int moveCount, int killCount)
	{
		super(color,moveCount,killCount);
	}

	public Bishop(ChessPiece piece)
	{
		super(piece);
	}

	@Override
        public ArrayList<Integer> getLegalMoves(int position,ChessBoard chessBoard)
	{
		int i;
		int stopAt;
		int sPosition=position*0100;	//position to combine with a square to make a move
		final ArrayList<Integer> returnme=new ArrayList<Integer>();

		//down right
		i=position+011;	//+1+1
		while(i<64)
		{
			if(!divineInterventionCheck(sPosition+i))
				break;
			if(!chessBoard.addMoveToList(returnme,i,color))
				break;
			i+=011;
		}

		//down left
		i=position+007;	//+1-1
		while(i<64)
		{
			if(!divineInterventionCheck(sPosition+i))
				break;
			if(!chessBoard.addMoveToList(returnme,i,color))
				break;
			i+=007;
		}

		//up right
		i=position-007;	//-1+1
		while(i>0)
		{
			if(!divineInterventionCheck(sPosition+i))
				break;
			if(!chessBoard.addMoveToList(returnme,i,color))
				break;
			i-=007;
		}

		//up left
		i=position-011;	//-1-1
		while(i>0)
		{
			if(!divineInterventionCheck(sPosition+i))
				break;
			if(!chessBoard.addMoveToList(returnme,i,color))
				break;
			i-=011;
		}

		return returnme;
	}

	@Override
	public boolean checkLegal(int chessMove,boolean attacking)
	{
		int origin=chessMove/64;
		int destination=chessMove%64;

		return divineInterventionCheck(chessMove) && ((origin%011) == (destination%011) || (origin%007) == (destination%007));
	}

	@Override
	public char toChar()
	{
		return 'B';
	}

	public String toString()
	{
		if(color)
			return "B";
		else
			return "b";
	}
}
