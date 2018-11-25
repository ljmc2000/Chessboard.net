package net.ddns.gingerpi.chessboardnet;
import static net.ddns.gingerpi.chessboardnetCommon.ChessBoard.texturePack;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

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

        //get opponent info from room
        OpponentInfo opponentInfo=new OpponentInfo(getIntent().getExtras().getString("opponentid"));
        opponentInfo.start();
        try {
            opponentInfo.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //generate chessboard
        final GridView chessBoard = (GridView) findViewById(R.id.chessboard);
        int squareSize=this.getWindowManager().getDefaultDisplay().getWidth()/16;
        Log.d("#squaresize",Integer.toString(squareSize));
        final ChessBoardAdapter chessBoardAdapter=new ChessBoardAdapter(this,squareSize,texturePack.white,texturePack.black);
        chessBoard.setAdapter(chessBoardAdapter);
        chessBoard.setOnItemClickListener(chessBoardAdapter.getOnItemClickListener);
        chessBoard.setColumnWidth(squareSize);

        //deal with instant messages
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
            conmanager=new ServerConnection(this,extras.getString("hostname"),extras.getInt("port"),opponentInfo,extras.getString("loginToken"),chessBoardAdapter,imout);
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

    public void refreshBoard(View view){
        conmanager.refreshBoard();
    }
}
