//an abstract class to represent a chesspiece
package net.ddns.gingerpi.chessboardnetCommon;
import java.io.Serializable;
import java.util.ArrayList;

public abstract class ChessPiece implements Serializable
{
	int moveCount;
	int killCount;
	boolean color;	//true for one player, false for the other

	public abstract ArrayList<Integer> getLegalMoves(int position,ChessBoard map);	//list all possible moves the piece could take from it's current position
	public abstract boolean checkLegal(int chessMove,boolean attacking);		//check a move is chess legal
	public abstract char toChar();	//return K for king, Q for queen, N for knight etc...

	public ChessPiece(boolean color)
	{
		this.moveCount=0;
		this.killCount=0;
		this.color=color;
	}

	public ChessPiece(ChessPiece piece)
	{
		this.color=piece.getColor();
		this.killCount=piece.getKillCount();
		this.moveCount=piece.getMoveCount();
	}

	public ChessPiece(boolean color,int moveCount, int killCount)
	{
		this.moveCount=moveCount;
		this.killCount=killCount;
		this.color=color;
	}

	public static ChessPiece fromChar(char c)
	{
		switch(c)
		{
			case 'K':	return new King(true);
			case 'k':	return new King(false);
			case 'Q':	return new Queen(true);
			case 'q':	return new Queen(false);
			case 'B':	return new Bishop(true);
			case 'b':	return new Bishop(false);
			case 'N':	return new Knight(true);
			case 'n':	return new Knight(false);
			case 'R':	return new Rook(true);
			case 'r':	return new Rook(false);
			case 'P':	return new Pawn(true);
			case 'p':	return new Pawn(false);

			default:	return null;
		}
	}

	public void addKill()
	{
		killCount++;
	}

	public int getKillCount()
	{
		return this.killCount;
	}

	public int getMoveCount()
	{
		return this.moveCount;
	}

	public void addMove()
	{
		moveCount++;
	}

	public boolean getColor()
	{
		return color;
	}

	public void invertColor()
	{
		color=!color;
	}

	public int invertMove(int chessMove)	//return the same move if made from the other side of the board
	{
		/*this is a lot harder to explain than code but here goes
		07777 is 4095 which is is 64 times 64 minus 1
		a chessmove is comprised of two values between 0 and 63
		this can be represented in base 8 maths as 0mmtt
		where mm is the co-ordinates of the original square
		and tt is the square you are moving to

		by subtracting a square from 077 (63) you get it's opposite
		for more information see the project report*/
		return 07777-chessMove;
	}

	public boolean divineInterventionCheck(int chessMove)
	{
		/*
		Check a piece hasn't started on a black square
		and ended on a white square.
		*/
		int origin=chessMove/64;
		int destination=chessMove%64;

		origin=(origin/8)+(origin%8);
		destination=(destination/8)+(destination%8);

		return origin%2 == destination%2;
	}
}
