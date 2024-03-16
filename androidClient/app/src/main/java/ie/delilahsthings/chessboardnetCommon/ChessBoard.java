package ie.delilahsthings.chessboardnetCommon;

import java.io.Serializable;
import java.util.ArrayList;

public class ChessBoard implements Serializable
{
	int king1=004;	//square where king1 sits
	int king2=074;	//square where king2 sits

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

	public ChessBoard(){};

	public ChessBoard(ChessBoard template)
	{
		this.king1=template.getKing(true);
		this.king2=template.getKing(false);
		this.whosTurn=template.getWhosTurn();

		int i;
		String templateMap=template.toCompString();
		for(i=0; i<64; i++)
			this.map[i]=ChessPiece.fromChar(templateMap.charAt(i));
	}

	public ChessPiece getItem(int position)
	{
		if(0<=position && position<64)
			return map[position];
		else
			return null;
	}

	public int getKing(boolean color)
	{
		if(color) return king1;
		else return king2;
	}

	public boolean getWhosTurn()
	{
		return whosTurn;
	}

	public boolean addMoveToList(ArrayList<Integer> returnme,int position,boolean color)
	//adds a move to list if the target square is empty or contains an enemy
	{
		if(position>63 || position<0)
		{
			return false;
		}

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
		boolean rooking=false;
		if(piece==null)
			return false;

		if(piece.getColor() != whosTurn)
			return false;

		if(target != null)
		{
			rooking = (piece.toString().equals("K") && target.toString().equals("R"))
				|| (piece.toString().equals("k") && target.toString().equals("r"));
			if (rooking);    //nothing happens here, just stop the next one executing
			else if (piece.getColor() == target.getColor())
				return false;
			else
				attacking = true;
		}

		if(piece.checkLegal(chessMove,attacking))//simple check
		if(piece.getLegalMoves(origin,this).contains(destination))//more robust
		{
			piece.addMove();
			if(attacking)
				piece.addKill();
			if(rooking)
			{
				map[origin]=piece;
				int row=origin/010;
				row*=010;
				if(origin-destination<0)
				{
					map[origin+2]=piece;
					map[origin+1]=target;
					//update king location
					if(piece.toString().equals("K")) king1=origin+2;
					else if(piece.toString().equals("k")) king2=origin+2;
				}
				else
				{
					map[origin-2]=piece;
					map[origin-1]=target;
					//update king location
					if(piece.toString().equals("K")) king1=origin-2;
					else if(piece.toString().equals("k")) king2=origin-2;
				}
				map[origin]=null;
				map[destination]=null;	//destination works a little different here
			}

			else
			{
				map[destination]=piece;
				map[origin]=null;
				//update king location
				if(piece.toString().equals("K")) king1=destination;
				else if(piece.toString().equals("k")) king2=destination;
			}

			whosTurn=!whosTurn;
			return true;
		}

		return false;
	}//end move piece

	public ArrayList<Integer> inDanger(int square,boolean color)
	{
		//return list of pieces threathening piece at square
		ArrayList<Integer> returnme=new ArrayList<Integer>();
		int i;
		for(i=0;i<64;i++)
		{
			if(map[i]!=null)
			if(map[i].getColor()!=color)
			if(map[i].getLegalMoves(i,this).contains(square))
			if(!(map[i].toChar() == 'P' && i%010==square%010))
				returnme.add(i);
		}

		return returnme;
	}

	public int inCheck(boolean color)
	{
		int square;
		if(color) square=king1;
		else square=king2;

		ChessPiece king=map[square];
		if(king.toChar() != 'K')
			return 2;	//if they somehow murder the king
		ArrayList<Integer> potAssasains=inDanger(square,color);

		switch(potAssasains.size())
		{
			case 0: return 0;	//not in check
			case 1:			//in check from one piece
			{
				ArrayList<Integer> moves=king.getLegalMoves(square,this);
				for(int i=0; i<moves.size(); i++)
					if(inDanger(moves.get(i).intValue(),color).size()==0)
						return 1;	//just check

				int assasian=potAssasains.get(0).intValue();
				if(inDanger(assasian,!color).size() > 0)
					return 1;
				else
					return 2;	//if assasain cannot be taken, checkmate
			}

			default:	//2 pieces or more
			{
				ArrayList<Integer> moves=king.getLegalMoves(square,this);
				for(int i=0; i<moves.size(); i++)
					if(inDanger(moves.get(i).intValue(),color).size()==0)
						return 1;	//just check

				//else
				return 2;
			}
		}
	}

	public boolean promotable(int position)
	{
		if (map[position]==null) return false;
		else return ((position<010 || position>067) && map[position].toChar()=='P');
	}

	public enum Rank{queen,knight,rook,bishop};
	public void promote(int position,Rank rank)
	{
		if(!promotable(position))
			return;

		else switch(rank)
		{
			case queen:
			{
				map[position]=new Queen(map[position]);
				break;
			}

			case knight:
			{
				map[position]=new Knight(map[position]);
				break;
			}

			case rook:
			{
				map[position]=new Rook(map[position]);
				break;
			}

			case bishop:
			{
				map[position]=new Bishop(map[position]);
				break;
			}
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

	public String toCompString()
	{
		String returnme="";
		int i,j;

		for(i=0; i<0100; i+=010)
		for(j=0; j<8; j++)
		{
			if (map[i+j]!=null)
				returnme+=map[i+j].toString();
			else
				returnme+=" ";
		}

		return returnme;
	}

	public void reverse()
	{
		int i;
		ChessPiece tmp;
		whosTurn=!whosTurn;

		for(i=0; i<map.length/2; i++)
		{
			tmp=map[i];
			map[i]=map[077-i];
			map[077-i]=tmp;

			if(map[i] != null)
			{
				map[i].invertColor();
				if(map[i].toString().equals("K")) king1=i;
				else if(map[i].toString().equals("k")) king2=i;
			}

			if(map[077-i] != null)
			{
				map[077-i].invertColor();
				if(map[077-i].toString().equals("K")) king1=077-i;
				else if(map[077-i].toString().equals("k")) king2=077-i;
			}
		}
	}
}
