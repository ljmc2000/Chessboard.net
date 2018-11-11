package net.ddns.gingerpi.chessboardnetServer;

class ChessPacket
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
}
