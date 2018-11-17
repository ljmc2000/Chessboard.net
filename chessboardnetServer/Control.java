package net.ddns.gingerpi.chessboardnetServer;
import java.net.*;
import java.io.*;
import org.bson.*;
import com.mongodb.client.*;
import com.mongodb.MongoClient;

import  net.ddns.gingerpi.chessboardnetCommon.*;
import static net.ddns.gingerpi.chessboardnetCommon.ChessPacket.messageType.*;

class Control
{
	static int remainingConnections=100;

	public static void main(String[] args)
	{
		try
		{
			final String serverAddress="192.168.0.1";
			int port=7000;
			ServerSocket myServerSocket=new ServerSocket(port);

			MongoClient mongoClient = new MongoClient();
			MongoDatabase db = mongoClient.getDatabase("ChessboardNet");
			/*
			MongoCollection<Document> users = db.getCollection("users");
			Document user=users.find().first();
			System.out.println(user.toJson());
			*/


			System.out.println("Server listening on port "+port);
			//for the rest of runtime
			while(1==1)
			{
				if(remainingConnections>0)
				{
					Socket connection1 = myServerSocket.accept();
					System.out.println("Connection from "+connection1.getRemoteSocketAddress());
					Socket connection2 = myServerSocket.accept();
					System.out.println("Connection from "+connection2.getRemoteSocketAddress());
					System.out.println("Beginning Game: "+(remainingConnections-1)+" Connections remain");
					ObjectOutputStream out1=new ObjectOutputStream(connection1.getOutputStream());
					ObjectInputStream in1=new ObjectInputStream(connection1.getInputStream());
					ObjectOutputStream out2=new ObjectOutputStream(connection2.getOutputStream());
					ObjectInputStream in2=new ObjectInputStream(connection2.getInputStream());

					ChessGameInstance p1Thread = new ChessGameInstance(connection1,out2,in1,db);
					ChessGameInstance p2Thread = new ChessGameInstance(connection2,out1,in2,db);
					p1Thread.start();
					p2Thread.start();
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
