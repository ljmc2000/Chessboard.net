package net.ddns.gingerpi.chessboardnet;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import net.ddns.gingerpi.chessboardnet.Roomfiles.CacheDatabase;
import net.ddns.gingerpi.chessboardnet.Roomfiles.UserInfo;
import net.ddns.gingerpi.chessboardnet.Roomfiles.UserPreferences;
import net.ddns.gingerpi.chessboardnetCommon.ChessBoard;

import static net.ddns.gingerpi.chessboardnet.ChessSet.texturePack;

public class ChessPlayer extends Activity {

    ServerConnection conmanager;
    TextView imout;
    PlayerInfo playerInfo;
	ChessBoardAdapter chessBoardAdapter;
	GridView chessBoard;
	Bundle extras;

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

        //get info passed by main
        extras=getIntent().getExtras();

        //enable the promotion menu
		registerForContextMenu(findViewById(R.id.promenu_anchor));

        //get opponent info from room
		playerInfo=new PlayerInfo(getIntent().getExtras().getString("opponentid"));
        playerInfo.start();
        try {
            playerInfo.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //generate chessboard
        chessBoard = (GridView) findViewById(R.id.chessboard);
        int squareSize=this.getWindowManager().getDefaultDisplay().getWidth()/16;
        chessBoardAdapter=new ChessBoardAdapter(this,squareSize,(ImageView) findViewById(R.id.whosTurn));
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
            conmanager=new ServerConnection(this,extras.getString("hostname"),extras.getInt("port"),playerInfo,extras.getString("loginToken"),chessBoardAdapter,imout);
            chessBoardAdapter.setServer(conmanager);
            conmanager.start();
            conmanager.initBoard();
        }

        catch (Exception e){
            Log.e("#GameThread",e.toString());
        }
    }

    @Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
	    super.onCreateContextMenu(menu, v, menuInfo);
    	MenuInflater inflater = getMenuInflater();
   		inflater.inflate(R.menu.promenu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
    	AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
    	switch(item.getItemId()){
			case R.id.promotequeen:{
				conmanager.promote(ChessBoard.Rank.queen);
				return true;
			}

			case R.id.promoteknight:{
				conmanager.promote(ChessBoard.Rank.knight);
				return true;
			}

			case R.id.promotebishop:{
				conmanager.promote(ChessBoard.Rank.bishop);
				return true;
			}

			case R.id.promoterook:{
				conmanager.promote(ChessBoard.Rank.rook);
				return true;
			}

    		default: return false;
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

    public void reconnect(View view){
    	conmanager.disconnect();
		conmanager=new ServerConnection(this,extras.getString("hostname"),extras.getInt("port"),playerInfo,extras.getString("loginToken"),chessBoardAdapter,imout);
		chessBoardAdapter.setServer(conmanager);
    	conmanager.start();
    	conmanager.initBoard();
	}
}
