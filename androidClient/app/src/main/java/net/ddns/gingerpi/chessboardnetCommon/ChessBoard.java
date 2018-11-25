package net.ddns.gingerpi.chessboardnetCommon;
import java.util.ArrayList;
import java.io.Serializable;

public class ChessBoard implements Serializable
{
	public enum texturePack{white,black};
	texturePack player1set,player2set;
	boolean whosTurn=false;	//switches every time someone makes a move
	ChessPiece[] map={
			new Rook(true),new Knight(true),new Bishop(true),new Queen(true),new King(true),new Bishop(true),new Knight(true),new Rook(true),
			new Pawn(true),new Pawn(true),new Pawn(true),new Pawn(true),new Pawn(true),new Pawn(true),new Pawn(true),new Pawn(true),
			null,null,null,null,null,null,null,null,
			null,null,null,null,null,null,null,null,
			null,null,null,null,null,null,null,null,
			null,null,null,null,null,null,null,null,
			new Pawn(false),new Pawn(false),new Pawn(false),new Pawn(false),new Pawn(false),new Pawn(false),new Pawn(false),new Pawn(false),
			new Rook(false),new Knight(false),new Bishop(false),new Queen(false),new King(false),new Bishop(false),new Knight(false),new Rook(false)
	};

	public ChessBoard(texturePack player1set, texturePack player2set)
	{
		this.player1set=player1set;
		this.player2set=player2set;
	}

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

	public boolean movePiece(int chessMove)
	{
		int origin=chessMove/64;
		int destination=chessMove%64;

		if(origin<0 || origin>=64)
			return false;

		ChessPiece piece=map[origin];
		ChessPiece target=map[destination];
		boolean attacking=false;

		if(piece==null)
			return false;

		if(piece.getColor() != whosTurn)
			return false;

		if(target != null)
			if(piece.getColor() == target.getColor())
				return false;
			else
				attacking=true;

		if(piece.checkLegal(chessMove,attacking))
		{
			piece.addMove();
			if(attacking)
				piece.addKill();
			map[destination]=piece;
			map[origin]=null;
			whosTurn=!whosTurn;

			return true;
		}

		return false;
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

			//returnme+="\n";		//was here for human readable purpouses
		}

		return returnme;
	}
}
