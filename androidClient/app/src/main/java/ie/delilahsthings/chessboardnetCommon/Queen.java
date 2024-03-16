package ie.delilahsthings.chessboardnetCommon;
import java.util.ArrayList;

public class Queen extends ChessPiece
{
	public Queen(boolean color)
	{
		super(color);
	}

	public Queen(boolean color,int moveCount, int killCount)
	{
		super(color,moveCount,killCount);
	}

	public Queen(ChessPiece piece)
	{
		super(piece);
	}

	@Override
	public ArrayList<Integer> getLegalMoves(int position,ChessBoard chessBoard)
	{
		final ArrayList<Integer> returnme=new ArrayList<Integer>();
		Rook rook=new Rook(color);
		Bishop bishop=new Bishop(color);

		returnme.addAll(rook.getLegalMoves(position,chessBoard));
		returnme.addAll(bishop.getLegalMoves(position,chessBoard));

		return returnme;
	}

	@Override
	public boolean checkLegal(int chessMove,boolean attacking)
	{
		Rook rook=new Rook(color);
		Bishop bishop=new Bishop(color);

		return rook.checkLegal(chessMove,attacking) || bishop.checkLegal(chessMove,attacking);
	}

	@Override
	public char toChar()
	{
		return 'Q';
	}

	public String toString()
	{
		if(color)
			return "Q";
		else
			return "q";
	}
}
