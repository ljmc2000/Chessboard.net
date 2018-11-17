package net.ddns.gingerpi.chessboardnetServer;
import java.net.*;
import java.io.*;

import org.bson.*;
import com.mongodb.client.*;
import com.mongodb.MongoClient;
import com.mongodb.BasicDBObject;

import net.ddns.gingerpi.chessboardnetCommon.*;
import static net.ddns.gingerpi.chessboardnetCommon.ChessPacket.messageType.*;

class ChessGameInstance extends Thread
{
	Socket s;
	String userID;
	String opponentUsername;
	ObjectOutputStream out;
	ObjectInputStream in;
	MongoDatabase db;
	ChessPacket message;

	public ChessGameInstance(Socket s,ObjectOutputStream out, ObjectInputStream in,MongoDatabase db)
	{
		this.s=s;
		this.out=out;
		this.in=in;
		this.db=db;
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

					case signin:
					{
						MongoCollection<Document> user_tokens = db.getCollection("user_tokens");
						BasicDBObject request=new BasicDBObject();
						request.put("_id",message.getMessage());
						Document user=user_tokens.find(request).first();
						out.writeObject(new ChessPacket(opponent,user.get("user_id").toString()));
						break;
					}

					case im:
					{
						System.out.println(message.getMessage());
						out.writeObject(message);
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
