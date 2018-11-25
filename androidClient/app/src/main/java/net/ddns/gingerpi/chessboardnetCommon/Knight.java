package net.ddns.gingerpi.chessboardnetCommon;
import java.util.ArrayList;

public class Knight extends ChessPiece
{
	public Knight(boolean color)
	{
		super(color);
	}

	public Knight(boolean color,int moveCount, int killCount)
	{
		super(color,moveCount,killCount);
	}

	@Override
	public ArrayList<Integer> getLegalMoves(int position,ChessBoard chessBoard)
	{
		final ArrayList<Integer> returnme=new ArrayList<Integer>();
		int x=position%8;
		int y=position/8;

		if(y<7)
		{
			if(x>1 && position+006<64) returnme.add(position+006);
			if(x<6 && position+012<64) returnme.add(position+012);
		}

		if(y<6)
		{
			if(x>0 && position+017<64) returnme.add(position+017);
			if(x<7 && position+021<64) returnme.add(position+021);
		}

		if(y>1)
		{
			if(x<6 && position-006>0) returnme.add(position-006);
			if(x>1 && position-006>0) returnme.add(position-0012);
		}
		if(y>0)
		{
			if(x<7 && position-006>0) returnme.add(position-017);
			if(x>0 && position-006>0) returnme.add(position-021);
		}

		return returnme;
	}

	public boolean checkLegal(int chessMove,boolean attacking)
	{
		int origin=chessMove/64;
		int destination=chessMove%64;
		int difference=destination-origin;
		int x=origin%8;
		int y=origin/8;

		switch(difference)
		{
			case  006:	//+1-2
				return x>1 && y<7;
			case  012:	//+1+2
				return x<6 && y<7;
			case  017:	//+2-1
				return x>0 && y<6;
			case  021:	//+2+1
				return x<7 && y<6;
			case -006:	//-1+2
				return x<6 && y>1;
			case -012:	//-1-2
				return x>1 && y>1;
			case -017:	//-2+1
				return x<7 && y>0;
			case -021:	//-2-1
				return x>0 && y>0;
		}

		return false;
	}

	public String toString()
	{
		if(color)
			return "K";
		else
			return "k";
	}
}
