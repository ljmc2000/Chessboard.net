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
			new Pawn(false),new Pawn(false),new Pawn(false),new Pawn(false),new Pawn(false),new Pawn(false),new Pawn(false),new Pawn(false),
			new Rook(false),null,null,null,null,null,null,new Rook(false)
	};

	public ChessPiece getItem(int position)
	{
		return map[position];
	}

	public boolean addMoveToList(ArrayList<Integer> returnme,int position,boolean color)
	//adds a move to list if the target square is empty or contains an enemy
	{
		if(map[position]==null)
		{
			returnme.add(position);
			return true;
		}

		else if(map[position].getColor() != color)
		{
			returnme.add(position);
			return false;
		}

		else
		{
			return false;
		}
	}

	public String toString()
	{
		String returnme="";
		int i,j;

		for(i=0; i<0100; i+=010)
		{
			for(j=0; j<8; j++)
			{
				if (map[i+j]!=null)
					returnme+=map[i+j].toString();
				else
					returnme+=" ";
			}

			returnme+="\n";
		}

		return returnme;
	}
}
