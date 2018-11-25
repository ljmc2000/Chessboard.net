package net.ddns.gingerpi.chessboardnet;
import android.util.Log;
import android.widget.TextView;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

import net.ddns.gingerpi.chessboardnetCommon.ChessPacket;
import static net.ddns.gingerpi.chessboardnetCommon.ChessPacket.messageType.*;

public class ServerConnection extends Thread {
    Socket mycon;
    ObjectOutputStream out;
    ObjectInputStream in;
    InetAddress address;
    int port;
    String loginToken;
    ChessPlayer.OpponentInfo opponentInfo;
    TextView imout;     //object to display instant messages
    ArrayList<ChessPacket> sendQueue=new ArrayList<ChessPacket>();
    ChessPacket recievedMessage;

    public ServerConnection(String url, int port, ChessPlayer.OpponentInfo opponentInfo, String token, TextView imout){
        try {
            this.imout = imout;
            this.address = InetAddress.getByName(url);
            this.loginToken=token;
            this.opponentInfo=opponentInfo;
            this.port = port;
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
                    sendMessage=null;
                }

                if (sendMessage != null) {
                    out.writeObject(sendMessage);
                    sendMessage=null;

                } else {
                    out.writeObject(new ChessPacket(ack));
                }

                recievedMessage = (ChessPacket) in.readObject();

                switch (recievedMessage.getHeader()) {
                    case ack: {
                        break;
                    }

                    case chessMove: {
                        break;
                    }

                    case im: {
                        imout.post(new Runnable() {
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
                        imout.post(new Runnable() {
                            @Override
                            public void run() {
                                imout.append("Opponent has surrendered"+"\n");
                            }
                        });
                        mycon.close();
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
}
