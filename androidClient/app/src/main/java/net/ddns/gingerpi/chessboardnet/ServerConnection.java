package net.ddns.gingerpi.chessboardnet;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

import net.ddns.gingerpi.chessboardnetCommon.ChessBoard;
import net.ddns.gingerpi.chessboardnetCommon.ChessPacket;
import static net.ddns.gingerpi.chessboardnetCommon.ChessPacket.messageType.*;

public class ServerConnection extends Thread {
    ChessPlayer mainThread;
    Socket mycon;
    ObjectOutputStream out;
    ObjectInputStream in;
    InetAddress address;
    int port;
    String loginToken;
    ChessPlayer.OpponentInfo opponentInfo;
    TextView imout;     //object to display instant messages
    ChessBoardAdapter boardOut;      //write directly to the board
    public ChessBoard board;
    ArrayList<ChessPacket> sendQueue=new ArrayList<ChessPacket>();
    ChessPacket recievedMessage;

    public ServerConnection(ChessPlayer mainThread, String url, int port, ChessPlayer.OpponentInfo opponentInfo, String token, ChessBoardAdapter boardOut, TextView imout){
        try {
            this.mainThread=mainThread;
            this.address = InetAddress.getByName(url);
            this.port = port;
            this.opponentInfo=opponentInfo;
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
            mycon = new Socket(this.address, port);
            out = new ObjectOutputStream(mycon.getOutputStream());
            in = new ObjectInputStream(mycon.getInputStream());

            out.writeObject(loginToken);

            while (mycon.isConnected()) {
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
                                    imout.append(opponentInfo.getUsername() + ": " + recievedMessage.getMessage() + "\n");
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
                        break;
                    }

                    case refreshBoard: {
                        board=(ChessBoard) in.readObject();
                        mainThread.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                boardOut.refreshBoard(board.toString());
                                boardOut.setChessBoard(board);
                            }
                        });
                        break;
                    }
                }
            }
        }

        catch(Exception e){
            Log.e("#Network",e.toString());
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
