package ie.delilahsthings.chessboardnet;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.pm.ConfigurationInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import ie.delilahsthings.chessboardnet.Roomfiles.CacheDatabase;
import ie.delilahsthings.chessboardnetCommon.ChessBoard;

import static ie.delilahsthings.chessboardnet.ChessSet.texturePack;

public class ChessPlayer extends Activity {

    ServerConnection conmanager;
    TextView imout;
	ChessBoardAdapter chessBoardAdapter;
	GridView chessBoard;
	Bundle extras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        setContentView(R.layout.activity_chess_player);

        //get info passed by main
        extras=getIntent().getExtras();

        //create the server thread
        conmanager = new ServerConnection(this, extras);
		conmanager.start();

        //enable the promotion menu
		registerForContextMenu(findViewById(R.id.promenu_anchor));

		//generate chessboard
		int squareSize = this.getWindowManager().getDefaultDisplay().getWidth() / 16;
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
			squareSize *= 2;
		chessBoardAdapter = new ChessBoardAdapter(this, extras, squareSize, (ImageView) findViewById(R.id.whosTurn));
		chessBoardAdapter.setServer(conmanager);
		chessBoard = (GridView) findViewById(R.id.chessboard);
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
		imout.setMovementMethod(new ScrollingMovementMethod());

        try {
			conmanager.initBoard();
		}

		catch (Exception e) {
			Log.e("#GameThread", e.toString());
			Log.e("#GameThread", conmanager.toString());
		}

    }

    @Override
    protected void onPause(){
    	super.onPause();
    	conmanager.disconnect();
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
        imout.append(getResources().getString(R.string.you)+s+"\n");
        conmanager.sendIMessage(s);
    }

    public void surrender(View view){
        conmanager.surrender();
    }

    public void reconnect(View view){
    	conmanager.disconnect();
		conmanager=new ServerConnection(this,extras);
		chessBoardAdapter.setServer(conmanager);
    	conmanager.start();
    	conmanager.initBoard();
	}
}
