package net.ddns.gingerpi.chessboardnet;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import net.ddns.gingerpi.chessboardnet.Roomfiles.CacheDatabase;
import net.ddns.gingerpi.chessboardnet.Roomfiles.UserInfo;
import net.ddns.gingerpi.chessboardnet.Roomfiles.UserPreferences;

import static net.ddns.gingerpi.chessboardnet.ChessSet.texturePack;

public class ChessPlayer extends Activity {

    ServerConnection conmanager;
    TextView imout;

    class PlayerInfo extends Thread{
        String opponentid;
        UserInfo opponent;
        UserPreferences opponentPrefs;
        UserPreferences myPrefs;

        @Override
        public void run(){
            this.opponent=
                    CacheDatabase
                            .getInstance(getApplicationContext())
                            .getUserInfoDao()
                            .getOpponentInfo(opponentid);

            this.opponentPrefs=
					CacheDatabase
					.getInstance(getApplicationContext())
					.getUserPreferencesDao()
					.getOpponentPreferences(opponentid);

            this.myPrefs=
					CacheDatabase
					.getInstance(getApplicationContext())
					.getUserPreferencesDao()
					.fetchOwn();
        }

        public PlayerInfo(String opponentid){
            this.opponentid=opponentid;
        }

        public String getUsername(){
            return this.opponent.username;
        }
        public texturePack getOpponentTexturePack1(){
        	return this.opponentPrefs.favourite_set;
		}
		public texturePack getOpponentTexturePack2(){
        	return this.opponentPrefs.secondary_set;
		}

		public texturePack getMyTexturePack1(){
        	return this.myPrefs.favourite_set;
		}

		public texturePack getMyTexturePack2(){
        	return this.myPrefs.secondary_set;
		}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chess_player);

        //get opponent info from room
        PlayerInfo playerInfo=new PlayerInfo(getIntent().getExtras().getString("opponentid"));
        playerInfo.start();
        try {
            playerInfo.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //generate chessboard
        final GridView chessBoard = (GridView) findViewById(R.id.chessboard);
        int squareSize=this.getWindowManager().getDefaultDisplay().getWidth()/16;
        final ChessBoardAdapter chessBoardAdapter=new ChessBoardAdapter(this,squareSize);
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
            conmanager=new ServerConnection(this,extras.getString("hostname"),extras.getInt("port"),playerInfo,extras.getString("loginToken"),chessBoardAdapter,imout);
            chessBoardAdapter.setServer(conmanager);
            conmanager.start();
            conmanager.refreshBoard();
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
    }

    public void refreshBoard(View view){
        conmanager.refreshBoard();
    }
}
