package net.ddns.gingerpi.chessboardnetCommon;
import java.io.Serializable;

public class ChessPacket implements Serializable
{
	public enum messageType {ack,chessMove,im,end,fullserver,refreshBoard};

	messageType header;
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

	public messageType getHeader()
	{
		return this.header;
	}

	public String getMessage()
	{
		return this.message;
	}
}
