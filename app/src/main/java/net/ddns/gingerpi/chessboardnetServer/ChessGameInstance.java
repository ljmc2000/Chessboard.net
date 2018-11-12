package net.ddns.gingerpi.chessboardnetServer;
import java.net.*;
import java.io.*;
import static net.ddns.gingerpi.chessboardnetServer.ChessPacket.messageType.*;

class ChessGameInstance extends Thread
{
	Socket s;
	ObjectOutputStream out;
	ObjectInputStream in;
	ChessPacket message;

	public ChessGameInstance(Socket s,ObjectOutputStream out, ObjectInputStream in)
	{
		this.s=s;
		this.out=out;
		this.in=in;
	}

	public void run()
	{
		connection: while(true)
		{
			try
			{
				message=(ChessPacket) in.readObject();
				//deal with the server side stuff
				switch(message.getHeader())
				{
					case ack:
					{
						break;
					}

					case im:
					{
						System.out.println(message.getMessage());
						break;
					}

					case end:
					{
						System.out.println("user has disconnected; Reason: "+message.getMessage());
						out.writeObject(message);
						s.close();
						break connection;
					}
				}

				out.writeObject(message);
			}

			catch (Exception e)
			{
				System.err.println(e);
				Control.releaseConnection();
				break;
			}
		}
	}
}
