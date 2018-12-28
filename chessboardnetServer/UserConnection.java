package net.ddns.gingerpi.chessboardnetServer;
import java.net.*;
import java.io.*;
import java.util.ArrayList;

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
	ObjectId gameId;
	ChessPacket messageIn,messageOut;
	ArrayList<ChessPacket> recieveQueue=new ArrayList<ChessPacket>();
	boolean color;

	public UserConnection(Socket s,ObjectOutputStream out, ObjectInputStream in,ObjectId userId,ObjectId opponent,ObjectId gameId,MongoDataManager db)
	{
		this.s=s;
		this.out=out;
		this.in=in;
		this.userID=userId;
		this.opponent=opponent;
		this.gameId=gameId;
		this.db=db;
		this.color=db.getUserColor(userId);
	}

	public void putMessage(ChessPacket message)
	{
		recieveQueue.add(message);
	}

	public void run()
	{
		try
		{
			connection: while(true)
			{
				//deal with the server side stuff
				messageIn=(ChessPacket) in.readObject();

				switch(messageIn.getHeader())
				{
					case ack:
					{
						try
						{
							messageOut = recieveQueue.get(0);
							recieveQueue.remove(0);
						}

						catch(IndexOutOfBoundsException e)
						{
							messageOut=new ChessPacket(ack);
						}

						out.writeObject(messageOut);
						break;
					}

					case chessMove:
					{
						ChessBoard chessBoard=Control.boards.get(gameId);
						int move=messageIn.getMove();

						if(color==chessBoard.getWhosTurn())
						{
							System.out.println("illegal move from "+userID.toString());
							out.writeObject(new ChessPacket(chessError,"You may only move your own pieces"));
							s.close();
						}

						else if(chessBoard.movePiece(move))
						{
							UserConnection otherPlayer = Control.clients.get(opponent);
							otherPlayer.putMessage(messageIn);
							out.writeObject(new ChessPacket(ack));
						}

						else
						{
							System.out.println("illegal move from "+userID.toString());
							out.writeObject(new ChessPacket(chessError,"Illegal move"));
							s.close();
						}

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
						switch(messageIn.getMove())
						{
							case 1:
							{
								if (Control.boards.get(gameId).inCheck(!color)>0) db.endGame(opponent,"check");
								else db.endGame(opponent,"surrender");

								//destroy the chessboard
								Control.boards.remove(gameId);

								//disconnect other player
								System.out.println("user has disconnected; Reason: "+messageIn.getMessage());
								UserConnection otherPlayer = Control.clients.get(opponent);
								otherPlayer.putMessage(messageIn);
								s.close();
								break connection;
							}

							case 2:
							{
								ChessBoard chessBoard=Control.boards.get(gameId);
								if(chessBoard.inCheck(color)!=2)
								{
									out.writeObject(new ChessPacket(chessError,"Opponent not in check"));
									s.close();
									break;
								}

								else
								{
									db.endGame(userID,"checkmate");
									UserConnection otherPlayer = Control.clients.get(opponent);
									otherPlayer.putMessage(messageIn);
									out.writeObject(new ChessPacket(ack));
									break;
								}
							}
						}
					}

					case initBoard:
					{
						ChessBoard chessBoard=Control.boards.get(gameId);

						out.writeObject(new ChessPacket(initBoard));
						out.writeObject(chessBoard);
						out.writeObject(color);
						break;
					}

					case promotion: {
						ChessBoard chessBoard = Control.boards.get(gameId);
						UserConnection otherPlayer = Control.clients.get(opponent);

						ChessBoard.Rank r = ChessBoard.Rank.valueOf(messageIn.getMessage());
						if (chessBoard.promotable(messageIn.getMove())) {
							chessBoard.promote(messageIn.getMove(), r);
							otherPlayer.putMessage(messageIn);
							out.writeObject(messageIn);
						}

						else
						{
							out.writeObject(new ChessPacket(chessError,"Failed pawn promtion"));
						}
					}
				}
			}
		}

		catch (Exception e)
		{
			System.err.println(e);
		}
	}
}
