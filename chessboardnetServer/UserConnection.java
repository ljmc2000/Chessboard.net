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
	ChessPacket messageIn,messageOut;

	public UserConnection(Socket s,ObjectOutputStream out, ObjectInputStream in,ObjectId opponent,MongoDatabase db)
	{
		this.s=s;
		this.out=out;
		this.in=in;
		this.opponent=opponent;
		this.db=db;
	}

	public void putMessage(ChessPacket message)
	{
		this.messageOut=message;
	}

	public void run()
	{
		try
		{
			messageOut=new ChessPacket(ack);

			connection: while(true)
			{
				//deal with the server side stuff
				messageIn=(ChessPacket) in.readObject();

				switch(messageIn.getHeader())
				{
					case ack:
					{
						out.writeObject(messageOut);
						if(messageOut.getHeader() != ack)
							this.messageOut=new ChessPacket(ack);
						break;
					}

					case im:
					{
						UserConnection otherPlayer = Control.clients.get(opponent);
						otherPlayer.putMessage(messageIn);
						out.writeObject(new ChessPacket(ack));
						break;
					}

					case end:
					{
						System.out.println("user has disconnected; Reason: "+messageIn.getMessage());
						s.close();
						Control.releaseConnection();
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
