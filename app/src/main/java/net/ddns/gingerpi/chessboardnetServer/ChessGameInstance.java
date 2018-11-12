package net.ddns.gingerpi.chessboardnetServer;
import java.net.*;
import java.io.*;

class ChessGameInstance extends Thread
{
	ObjectOutputStream out;
	ObjectInputStream in;
	ChessPacket message;

	public ChessGameInstance(ObjectOutputStream out, ObjectInputStream in)
	{
		this.out=out;
		this.in=in;
	}

	public void run()
	{
		while(true)
		{
			try
			{
				message=(ChessPacket) in.readObject();
				//deal with the server side stuff
				out.writeObject(message);
			}

			catch (Exception e)
			{
				System.err.println(e);
			}
		}
	}
}
