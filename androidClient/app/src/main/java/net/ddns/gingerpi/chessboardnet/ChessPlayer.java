package net.ddns.gingerpi.chessboardnet;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import net.ddns.gingerpi.chessboardnet.Roomfiles.CacheDatabase;
import net.ddns.gingerpi.chessboardnet.Roomfiles.UserInfo;

public class ChessPlayer extends Activity {

    ServerConnection conmanager;
    TextView imout;

    class OpponentInfo extends Thread{
        String opponentid;
        UserInfo opponent;

        @Override
        public void run(){
            this.opponent=
                    CacheDatabase
                            .getInstance(getApplicationContext())
                            .getUserInfoDao()
                            .getOpponentInfo(opponentid);
        }

        public OpponentInfo(String opponentid){
            this.opponentid=opponentid;
        }

        public String getUsername(){
            return this.opponent.username;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chess_player);
        OpponentInfo opponentInfo=new OpponentInfo(getIntent().getExtras().getString("opponentid"));
        opponentInfo.start();
        try {
            opponentInfo.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        imout=(TextView) findViewById(R.id.Messages);
        EditText edittext = (EditText) findViewById(R.id.messageInput);
        edittext.setOnKeyListener(new View.OnKeyListener() {
        public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    sendMessage(imout);
                }
                return false;
            }
        });

        try{
            imout.setMovementMethod(new ScrollingMovementMethod());
            Bundle extras=getIntent().getExtras();
            conmanager=new ServerConnection(extras.getString("hostname"),extras.getInt("port"),opponentInfo,extras.getString("loginToken"),imout);
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
        imout.append("You: "+s+"\n");
        conmanager.sendIMessage(s);
    }

    public void surrender(View view){
        conmanager.surrender();
        finish();
    }
}
