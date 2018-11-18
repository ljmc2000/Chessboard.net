package net.ddns.gingerpi.chessboardnetServer;
import java.net.*;
import java.io.*;


import org.bson.*;
import org.bson.types.ObjectId;
import com.mongodb.client.*;
import com.mongodb.MongoClient;
import com.mongodb.BasicDBObject;
import static com.mongodb.client.model.Filters.*;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import  net.ddns.gingerpi.chessboardnetCommon.*;
import static net.ddns.gingerpi.chessboardnetCommon.ChessPacket.messageType.*;

class Control
{
	static int remainingConnections=100;
	public static HashMap<ObjectId,UserConnection> clients = new HashMap<ObjectId,UserConnection>();

	public static void main(String[] args)
	{
		try
		{
			int port=7000;
			ServerSocket myServerSocket=new ServerSocket(port);

			MongoClient mongoClient = new MongoClient();
			MongoDatabase db = mongoClient.getDatabase("ChessboardNet");
			MongoCollection<Document> userTokens = db.getCollection("user_tokens");
			MongoCollection<Document> ongoingMatches=db.getCollection("ongoing_matches");


			System.out.println("Server listening on port "+port);
			//for the rest of runtime
			while(1==1)
			{
				if(remainingConnections>0)
				{
					//connect
					Socket connection = myServerSocket.accept();
					System.out.println("Connection from "+connection.getRemoteSocketAddress());
					ObjectOutputStream out=new ObjectOutputStream(connection.getOutputStream());
					ObjectInputStream in=new ObjectInputStream(connection.getInputStream());

					//login
					String token=(String) in.readObject();
					BasicDBObject fields = new BasicDBObject();
					fields.put("_id",token);
					Document user=userTokens.find(fields).first();
					ObjectId userid=(ObjectId) user.get("user_id");

					//get opponent
					List<ObjectId> innerfieldsArray= new ArrayList<>();
					innerfieldsArray.add(userid);
					Document match=ongoingMatches.find(in("players",innerfieldsArray)).first();
					ArrayList<ObjectId> players=(ArrayList<ObjectId>) match.get("players");
					ObjectId opponentid=players.get(0);
					if (opponentid.toString().equals(userid.toString()))
						opponentid=players.get(1);


					UserConnection pThread = new UserConnection(connection,out,in,opponentid,db);
					clients.put(userid,pThread);
					pThread.start();
					remainingConnections--;
				}

				else
				{
					Socket connection = myServerSocket.accept();
					ObjectOutputStream out=new ObjectOutputStream(connection.getOutputStream());
					out.writeObject(new ChessPacket(fullserver,"Server has no free connections"));
				}
			}
		}

		catch(Exception e)
		{
			System.out.println("main: "+e);
		}
	}

	public static void releaseConnection()
	{
		remainingConnections++;
		System.out.println("Connection freed");
	}
}
