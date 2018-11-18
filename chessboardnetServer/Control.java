package net.ddns.gingerpi.chessboardnetServer;
import java.net.*;
import java.io.*;

import org.bson.types.ObjectId;
import java.util.HashMap;

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
			MongoDataManager db = new MongoDataManager();

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
					ObjectId userid=db.getUserId(token);

					//get opponent
					ObjectId opponentid=db.getOpponentId(userid);

					//start the network handler thread for user
					UserConnection pThread = new UserConnection(connection,out,in,userid,opponentid,db);
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
