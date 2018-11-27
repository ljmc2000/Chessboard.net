package net.ddns.gingerpi.chessboardnetServer;
import java.net.*;
import java.io.*;

import org.bson.types.ObjectId;

import net.ddns.gingerpi.chessboardnetCommon.*;
import static net.ddns.gingerpi.chessboardnetCommon.ChessPacket.messageType.*;

class UserConnection extends Thread
{
	Socket s;
	ObjectId userID;
	ObjectId opponent;
	ObjectOutputStream out;
	ObjectInputStream in;
	MongoDataManager db;
	ChessBoard chessBoard;
	ChessPacket messageIn,messageOut;
	boolean color;

	public UserConnection(Socket s,ObjectOutputStream out, ObjectInputStream in,ObjectId userId,ObjectId opponent,ChessBoard chessBoard,MongoDataManager db)
	{
		this.s=s;
		this.out=out;
		this.in=in;
		this.userID=userId;
		this.opponent=opponent;
		this.chessBoard=chessBoard;
		this.db=db;
		this.color=db.getUserColor(userId);
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

					case chessMove:
					{
						int move=messageIn.getMove();
						chessBoard.movePiece(move);
						//inform opponent
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
						//end the game and record the result
						db.endGame(opponent,messageIn.getMessage());

						//destroy the chessboard
						chessBoard=null;

						//disconnect other player
						System.out.println("user has disconnected; Reason: "+messageIn.getMessage());
						UserConnection otherPlayer = Control.clients.get(opponent);
						otherPlayer.putMessage(messageIn);
						s.close();
						Control.releaseConnection();
						break connection;
					}

					case refreshBoard:
					{
						out.writeObject(new ChessPacket(refreshBoard));
						out.writeObject(chessBoard);
						out.writeObject(color);
						break;
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
