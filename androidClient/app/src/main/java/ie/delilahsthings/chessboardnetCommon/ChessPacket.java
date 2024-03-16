package ie.delilahsthings.chessboardnetCommon;
import java.io.Serializable;

public class ChessPacket implements Serializable
{
	public enum messageType {ack,chessMove,im,end,fullserver,initBoard,chessError,promotion};

	messageType header;
	int move=-1;	//chess moves are represented as an integer
	String message;

	public ChessPacket(messageType header,String message)
	{
		this.header=header;
		this.message=message;
	}

	public ChessPacket(messageType header)
	{
		this.header=header;
	}

	public ChessPacket(int move)
	{
		this.header=messageType.chessMove;
		this.move=move;
	}

	public ChessPacket(messageType header,int move,String message)
	{
		this.header=header;
		this.move=move;
		this.message=message;
	}

	public messageType getHeader()
	{
		return this.header;
	}

	public int getMove()
	{
		return this.move;
	}

	public String getMessage()
	{
		return this.message;
	}
}
