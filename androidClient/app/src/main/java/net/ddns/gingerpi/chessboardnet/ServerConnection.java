package net.ddns.gingerpi.chessboardnet;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import net.ddns.gingerpi.chessboardnetCommon.ChessBoard;
import net.ddns.gingerpi.chessboardnetCommon.ChessPacket;
import net.ddns.gingerpi.chessboardnetCommon.ChessPiece;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

import static net.ddns.gingerpi.chessboardnetCommon.ChessPacket.messageType.ack;
import static net.ddns.gingerpi.chessboardnetCommon.ChessPacket.messageType.chessError;
import static net.ddns.gingerpi.chessboardnetCommon.ChessPacket.messageType.end;
import static net.ddns.gingerpi.chessboardnetCommon.ChessPacket.messageType.im;
import static net.ddns.gingerpi.chessboardnetCommon.ChessPacket.messageType.initBoard;

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
	ChessBoard board;
    ChessBoardAdapter boardOut;      //write directly to the board
	ImageView whosTurn;

	boolean color;
    ArrayList<ChessPacket> sendQueue=new ArrayList<ChessPacket>();

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

            Loop: while (true) {
                try {
                    sendMessage = sendQueue.get(0);
                    sendQueue.remove(0);
                }
                catch(IndexOutOfBoundsException e){
                    sendMessage=new ChessPacket(ack);
                }
                out.writeObject(sendMessage);


                final ChessPacket recievedMessage = (ChessPacket) in.readObject();
                switch (recievedMessage.getHeader()) {
                    case ack: {
                        break;
                    }

                    case chessMove: {
                    	int move=recievedMessage.getMove();
                    	if(!color) move=07777-move;
                    	board.movePiece(move);
                    	mainThread.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								boardOut.refreshBoard();
								//if(board.inCheck(!color))
							}
						});
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
                        mainThread.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imout.append("Opponent has surrendered"+"\n");
                            }
                        });
                        break Loop;
                    }

                    case initBoard: {
                    	board=(ChessBoard) in.readObject();
                    	color=(boolean) in.readObject();
						if(!color) board.reverse();
						boardOut.setChessBoard(board);


                        //set textures and settle disputes
                        ChessSet.texturePack mine=playerInfo.getMyTexturePack1();
                        ChessSet.texturePack opptp=playerInfo.getOpponentTexturePack1();
                        if(mine==opptp) {
							if (color)
								opptp = playerInfo.getOpponentTexturePack2();
							else
								mine = playerInfo.getMyTexturePack2();

							if(color)
								boardOut.setTextures(mine, opptp);
							else
								boardOut.setTextures(opptp, mine);
						}
						boardOut.setTextures(mine, opptp);

                        //refresh the view
                        mainThread.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								boardOut.refreshBoard();
							}
						});
                        break;
                    }

					case chessError: {
						mainThread.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imout.append("Kicked from Server: "+recievedMessage.getMessage()+"\n");
                            }
                        });

						break Loop;
					}
                }
            }
        }

        catch(Exception e) {
			Log.e("#Network", e.toString());
			mainThread.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					imout.append("lost connection to server\n");

				}
			});
		}
    }

    public void sendIMessage(String s){
        this.sendQueue.add(new ChessPacket(im,s));
    }

    public void surrender(){
    	mainThread.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				imout.append("Surrendering and disconnecting from server\n");
			}
		});
        this.sendQueue.add(new ChessPacket(end,"surrender"));
    }

    public void initBoard(){
        this.sendQueue.add(new ChessPacket(initBoard));
    }

    public boolean movePiece(int move){
    	ChessBoard tmpBoard=new ChessBoard(board);

    	if(tmpBoard.movePiece(move))	//move legality
    	if(tmpBoard.inCheck(!color)==0){	//move causes yourself to go into check
    		board.movePiece(move);

			if(!color) move=07777-move;
    		this.sendQueue.add(new ChessPacket(move));

    		return true;
		}

		mainThread.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(mainThread.getApplicationContext(), R.string.selfCheck,Toast.LENGTH_SHORT ).show();
			}
		});
		return false;
	}

	public void disconnect(){
    	try {
			mycon.close();
		}
		catch (Exception e){
    		Log.e("#disconnectFail",e.toString());
		}
	}
}
