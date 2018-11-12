package net.ddns.gingerpi.chessboardnetServer;
import java.io.Serializable;

public class ChessPacket implements Serializable
{
	public enum messageType {ack,chessMove,im};

	messageType header;
	//Chessmove move
	String message;

	public ChessPacket(messageType header,String message)
	{
		this.header=header;
		this.message=message;
	}

	public ChessPacket()
	{
		this.header=messageType.ack;
	}

	public messageType getHeader()
	{
		return this.header;
	}

	public String getMessage()
	{
		return this.message;
	}
}
