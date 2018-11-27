package net.ddns.gingerpi.chessboardnet;

import android.util.Log;
import android.widget.TextView;

import net.ddns.gingerpi.chessboardnetCommon.ChessBoard;
import net.ddns.gingerpi.chessboardnetCommon.ChessPacket;
import net.ddns.gingerpi.chessboardnetCommon.ChessPiece;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

import static net.ddns.gingerpi.chessboardnetCommon.ChessPacket.messageType.ack;
import static net.ddns.gingerpi.chessboardnetCommon.ChessPacket.messageType.end;
import static net.ddns.gingerpi.chessboardnetCommon.ChessPacket.messageType.im;
import static net.ddns.gingerpi.chessboardnetCommon.ChessPacket.messageType.refreshBoard;

public class ServerConnection extends Thread {
    ChessPlayer mainThread;
    Socket mycon;
    ObjectOutputStream out;
    ObjectInputStream in;
    InetAddress address;
    int port;
    String loginToken;
    ChessPlayer.PlayerInfo playerInfo;
    TextView imout;     //object to display instant messages
    ChessBoardAdapter boardOut;      //write directly to the board
    public ChessBoard board;
    boolean color;
    ArrayList<ChessPacket> sendQueue=new ArrayList<ChessPacket>();
    ChessPacket recievedMessage;

    public ServerConnection(ChessPlayer mainThread, String url, int port, ChessPlayer.PlayerInfo playerInfo, String token, ChessBoardAdapter boardOut, TextView imout){
        try {
            this.mainThread=mainThread;
            this.address = InetAddress.getByName(url);
            this.port = port;
            this.playerInfo=playerInfo;
            this.loginToken=token;
            this.boardOut=boardOut;
            this.imout = imout;
        }

        catch(Exception e){
            Log.e("#failed to get address",e.toString());
        }
    }

    public void run() {
        try {
			ChessPacket sendMessage;

        	mainThread.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					imout.append("connecting to server\n");
				}
			});
        	Thread.sleep(1000);

            mycon = new Socket(this.address, port);
            out = new ObjectOutputStream(mycon.getOutputStream());
            in = new ObjectInputStream(mycon.getInputStream());

            out.writeObject(loginToken);

            mainThread.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					imout.append("connected to server\n");
				}
			});

            Loop: while (mycon.isConnected()) {
                try {
                    sendMessage = sendQueue.get(0);
                    sendQueue.remove(0);
                }
                catch(IndexOutOfBoundsException e){
                    sendMessage=new ChessPacket(ack);
                }
                out.writeObject(sendMessage);


                recievedMessage = (ChessPacket) in.readObject();
                switch (recievedMessage.getHeader()) {
                    case ack: {
                        break;
                    }

                    case chessMove: {
                        break;
                    }

                    case im: {
                        mainThread.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    imout.append(playerInfo.getUsername() + ": " + recievedMessage.getMessage() + "\n");
                                }

                                catch(Exception e){
                                    imout.append("Unknown user: " + recievedMessage.getMessage() + "\n");
                                }
                            }
                        });
                        break;
                    }

                    case end: {
                        out.writeObject(new ChessPacket(end,"AcKSurrender"));
                        mainThread.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imout.append("Opponent has surrendered"+"\n");
                            }
                        });
                        mycon.close();
                        break Loop;
                    }

                    case refreshBoard: {
                        board=(ChessBoard) in.readObject();
                        color=(boolean) in.readObject();
                        ChessSet.texturePack mine=playerInfo.getMyTexturePack1();
                        ChessSet.texturePack opptp=playerInfo.getOpponentTexturePack1();
                        if(mine==opptp) {
							if (color)
								opptp = playerInfo.getOpponentTexturePack2();
							else
								mine=playerInfo.getMyTexturePack2();
						}
						if(color)
							boardOut.setTextures(mine, opptp);
						else
							boardOut.setTextures(opptp, mine);


                        mainThread.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
								boardOut.setChessBoard(board);
								boardOut.setColor(color);

                            	if (color)
	                                boardOut.refreshBoard(board.toString());
                            	else
                            		boardOut.refreshBoard(board.toStringReversed());
                            }
                        });
                        break;
                    }
                }
            }
        }

        catch(Exception e){
            Log.e("#Network",e.toString());
            run();
        }
    }

    public void sendIMessage(String s){
        this.sendQueue.add(new ChessPacket(im,s));
    }

    public void surrender(){
        this.sendQueue.add(new ChessPacket(end,"surrender"));
    }

    public void refreshBoard(){
        this.sendQueue.add(new ChessPacket(refreshBoard));
    }
}
