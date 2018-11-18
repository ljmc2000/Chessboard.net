package net.ddns.gingerpi.chessboardnetServer;
import java.net.*;
import java.io.*;

import org.bson.*;
import org.bson.types.ObjectId;
import com.mongodb.client.*;
import com.mongodb.MongoClient;
import com.mongodb.BasicDBObject;

import net.ddns.gingerpi.chessboardnetCommon.*;
import static net.ddns.gingerpi.chessboardnetCommon.ChessPacket.messageType.*;

class UserConnection extends Thread
{
	Socket s;
	String userID;
	ObjectId opponent;
	ObjectOutputStream out;
	ObjectInputStream in;
	MongoDatabase db;
	ChessPacket message;

	public UserConnection(Socket s,ObjectOutputStream out, ObjectInputStream in,ObjectId opponent,MongoDatabase db)
	{
		this.s=s;
		this.out=out;
		this.in=in;
		this.opponent=opponent;
		this.db=db;
	}

	public boolean putMessage(ChessPacket message)
	{
		try
		{
			out.writeObject(message);
			return true;
		}

		catch (Exception e)
		{
			return false;
		}
	}

	public void run()
	{
		try
		{
			connection: while(true)
			{
				message=(ChessPacket) in.readObject();
				//deal with the server side stuff
				switch(message.getHeader())
				{
					case ack:
					{
						out.writeObject(message);
						break;
					}

					case im:
					{
						UserConnection otherPlayer = Control.clients.get(opponent);
						otherPlayer.putMessage(message);
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
			}
		}

		catch (Exception e)
		{
			System.err.println(e);
			Control.releaseConnection();
		}
	}
}
