package net.ddns.gingerpi.chessboardnetServer;
import java.net.*;
import java.io.*;

import org.bson.types.ObjectId;
import java.util.HashMap;

import net.ddns.gingerpi.chessboardnetCommon.*;
import static net.ddns.gingerpi.chessboardnetCommon.ChessPacket.messageType.*;

class Control
{
	static int remainingConnections=100;
	static int port=7000;
	static String hostname="localhost";
	static ObjectId serverid;
	public static HashMap<ObjectId,UserConnection> clients = new HashMap<ObjectId,UserConnection>();
	public static HashMap<ObjectId,ChessBoard> boards = new HashMap<ObjectId,ChessBoard>();

	public static void main(String[] args)
	{
		try
		{
			//get port and connection count from command line if available
			if(args.length > 0)
				hostname=args[0];
			if(args.length > 1)
				port=Integer.parseInt(args[1]);
			if(args.length > 2)
				remainingConnections=Integer.parseInt(args[2]);

			ServerSocket myServerSocket=new ServerSocket(port);
			MongoDataManager db = new MongoDataManager();

			//register with database
			serverid=db.register(hostname,port,remainingConnections);
			Runtime.getRuntime().addShutdownHook(new Cleaner(serverid,db));

			//return messages to user
			System.out.println("Server listening on "+hostname+" port "+port);
			System.out.println(remainingConnections+" Connections remain");

			//for the rest of runtime
			while(1==1)
			{
				try
				{
					if(remainingConnections>0)
					{
						//connect
						Socket connection = myServerSocket.accept();
						connection.setSoTimeout(5000);
						System.out.println("Connection from "+connection.getRemoteSocketAddress());
						ObjectOutputStream out=new ObjectOutputStream(connection.getOutputStream());
						ObjectInputStream in=new ObjectInputStream(connection.getInputStream());

						//login
						String token=(String) in.readObject();
						ObjectId userid=db.getUserId(token);

						//get opponent
						ObjectId opponentid=db.getOpponentId(userid);

						//setup chessboard
						ObjectId gameId=db.getGameId(userid);
						ChessBoard chessBoard=boards.get(gameId);
						if(chessBoard==null)
							chessBoard=new ChessBoard();
						boards.put(gameId,chessBoard);

						//start the network handler thread for user
						UserConnection pThread = new UserConnection(connection,out,in,userid,opponentid,gameId,db);
						if(clients.get(userid)!=null)
						{
							remainingConnections++;
							clients.get(userid).stop();
						}
						clients.put(userid,pThread);
						pThread.start();
						remainingConnections--;
						System.out.println(remainingConnections+" Connections remain");
					}

					else
					{
						Socket connection = myServerSocket.accept();
						ObjectOutputStream out=new ObjectOutputStream(connection.getOutputStream());
						out.writeObject(new ChessPacket(fullserver,"Server has no free connections"));
					}
				}

				catch(Exception e)
				{
					System.out.println("main: "+e);
				}
			}

		}

		catch (Exception e)
		{
			System.exit(1);
		}
	}

	public static void releaseConnection()
	{
		remainingConnections++;
		System.out.println("Connection freed: "+remainingConnections+" Connections remain");
	}
}
