package net.ddns.gingerpi.chessboardnet;
import android.util.Log;
import android.widget.TextView;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import net.ddns.gingerpi.chessboardnetServer.ChessPacket;
import static net.ddns.gingerpi.chessboardnetServer.ChessPacket.messageType.*;

public class ServerConnection extends Thread {
    ObjectOutputStream out;
    ObjectInputStream in;
    InetAddress address;
    int port;
    TextView imout;
    ChessPacket sendMessage;
    ChessPacket recievedMessage;

    public ServerConnection(String url,int port,TextView imout){
        try {
            this.imout = imout;
            this.address = InetAddress.getByName(url);
            this.port = port;
        }

        catch(Exception e){
            Log.e("#failed to get address",e.toString());
        }
    }

    public void run() {
        try {
            Socket mycon = new Socket(this.address, port);
            ObjectOutputStream out = new ObjectOutputStream(mycon.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(mycon.getInputStream());

            while (true) {
                if (sendMessage != null) {
                    out.writeObject(sendMessage);
                    sendMessage=null;

                } else {
                    out.writeObject(new ChessPacket());
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
                        imout.setText(recievedMessage.getMessage());
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
        this.sendMessage=new ChessPacket(im,s);
    }
}
