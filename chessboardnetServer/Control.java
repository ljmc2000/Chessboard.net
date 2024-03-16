package ie.delilahsthings.chessboardnetServer;
import java.net.*;
import java.io.*;

import org.bson.types.ObjectId;
import java.util.HashMap;

import ie.delilahsthings.chessboardnetCommon.*;
import static ie.delilahsthings.chessboardnetCommon.ChessPacket.messageType.*;

class Control
{
	static int availableConnections=100;
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
				availableConnections=Integer.parseInt(args[2]);

			ServerSocket myServerSocket=new ServerSocket(port);
			MongoDataManager db = new MongoDataManager();

			//register with database
			serverid=db.register(hostname,port,availableConnections);
			Runtime.getRuntime().addShutdownHook(new Cleaner(serverid,db));

			//return messages to user
			System.out.println("Server listening on "+hostname+" port "+port);
			System.out.println(availableConnections+" Connections ready");

			//for the rest of runtime
			while(1==1)
			{
				try
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
					clients.put(userid,pThread);
					pThread.start();
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
}
