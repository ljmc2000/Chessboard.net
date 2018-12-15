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

		//implement Rooking
		if(moveCount==0)
		{
			int kingRow=position/010;
			int space;
			ChessPiece piece;

			space=position+1;
			do
			{
				piece=chessBoard.getItem(space);
				if(piece!=null)
					break;
				space++;
			}while(space/010 == kingRow);
			if(space%010==7 && piece!=null)
				if(piece.getMoveCount()==0)
					returnme.add(space);
			space=position-1;
			do
			{
				piece=chessBoard.getItem(space);
				if(piece!=null)
					break;
				space--;
			}while(space/010 == kingRow && space>=0);
			if(space%010==0 || space==0)
				if(piece.getMoveCount()==0)
					returnme.add(space);
		}//end implementing rook

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
		boolean canrook;

		int origin=chessMove/64;
		int destination=chessMove%64;
		int difference=destination-origin;

		if(color) canrook=(destination == 007 || destination == 000)&&moveCount==0;
		else canrook=(destination == 077 || destination == 070)&&moveCount==0;

		if(difference<0) difference*=-1;
		int x=difference%8;
		int y=difference/8;

		return (x<=1 && y<=1) || canrook;
	}

	@Override
	public char toChar()
	{
		return 'K';
	}

	public String toString()
	{
		if(color)
			return "K";
		else
			return "k";
	}
}
