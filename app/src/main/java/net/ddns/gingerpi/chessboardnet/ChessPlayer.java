package net.ddns.gingerpi.chessboardnet;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.net.*;
import java.io.*;

public class ChessPlayer extends Activity {

    ServerConnection conmanager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chess_player);

        try{
            conmanager=new ServerConnection(getResources().getString(R.string.Server),getResources().getInteger(R.integer.serverPort),(TextView) findViewById(R.id.Messages));
            conmanager.start();
        }

        catch (Exception e){
            Log.e("#GameThread",e.toString());
        }
    }

    public void sendMessage(View view){
        String s;
        TextView messageInput=findViewById(R.id.messageInput);
        s=messageInput.getText().toString();
        messageInput.setText("");
        conmanager.sendIMessage(s);
    }

    public void surrender(View view){
        conmanager.surrender();
        finish();
    }
}
