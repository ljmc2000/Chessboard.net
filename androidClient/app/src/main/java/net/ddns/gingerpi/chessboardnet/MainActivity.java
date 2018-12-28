package net.ddns.gingerpi.chessboardnet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;

import net.ddns.gingerpi.chessboardnetCommon.VersionInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;


public class MainActivity extends Activity {

    RequestQueue queue;
    String loginToken="";
    String username=null;

    //threads
	MatchChecker matchChecker;
	GetMatchInfo getMatchInfo;

    Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e("#HTTPAPI",error.toString());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkLogin();
        queue=Volley.newRequestQueue(this);

		startThreads();
    }

    @Override
    protected void onPause(){
    	super.onPause();
    	stopThreads();
    	queue.stop();
	}

    @Override
    protected void onResume() {
        super.onResume();
        checkLogin();
        queue=Volley.newRequestQueue(this);

		startThreads();
    }

    class GetMatchInfo extends Thread{
        String url=getResources().getString(R.string.HTTPAPIurl)+"/userinfo";
        String opponentid;

        String own_username;
        String own_favourite_set;
        String own_secondary_set;
		String opp_username;
        String opp_favourite_set;
        String opp_secondary_set;

        RequestFuture<JSONObject> future1 = RequestFuture.newFuture();
        RequestFuture<JSONObject> future2 = RequestFuture.newFuture();
        JSONObject payload1=new JSONObject();
        JSONObject payload2=new JSONObject();
        JSONObject response;

        public GetMatchInfo(String opponentid){
        	this.opponentid=opponentid;
		}

        @Override
        public void run(){
            try{
                payload1.put("token",loginToken);
                payload1.put("userid",opponentid);
                payload2.put("token",loginToken);

                JsonObjectRequest opponentInfo=new JsonObjectRequest(Request.Method.POST,url,payload1,future1 ,errorListener);
                JsonObjectRequest ownInfo=new JsonObjectRequest(Request.Method.POST, url, payload2, future2, errorListener);

                queue.add(opponentInfo);
                queue.add(ownInfo);

                response=future1.get(5,TimeUnit.SECONDS);
                opp_username=response.getString("username");
                opp_favourite_set=response.getString("favourite_set");
                opp_secondary_set=response.getString("secondary_set");

                response=future2.get(5,TimeUnit.SECONDS);
                own_username=response.getString("username");
                own_favourite_set=response.getString("favourite_set");
                own_secondary_set=response.getString("secondary_set");
            }
            catch (JSONException e) {
                Log.e("#JsonError",e.toString());
            }
            catch (Exception e){
                Log.e("#HTTPAPI",e.toString());
            }
        }

        public String getOwnUsername(){
        	return own_username;
		}
		public String getOwnFavourite_set(){
        	return own_favourite_set;
		}
		public String getOwnSecondary_set(){
        	return own_secondary_set;
		}

		public String getOppUsername(){
			return opp_username;
		}
		public String getOppFavourite_set(){
			return opp_favourite_set;
		}
		public String getOppSecondary_set(){
			return opp_secondary_set;
		}
    }


    class MatchChecker extends Thread{
        String url=getResources().getString(R.string.HTTPAPIurl)+"/getmatch";
		boolean goOn=true;

        JSONObject payload=new JSONObject();
        JSONObject response;
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest matchCheck;

        public MatchChecker(){
            try {
                payload.put("token",loginToken);
            } catch (JSONException e) {
                Log.e("#JsonError",e.toString());
            }
        }

        @Override
        public void run(){
        	int status=0;

            do {
				try {
					Thread.sleep(5000);    //minimise number of requests
    	            matchCheck=new JsonObjectRequest(Request.Method.POST,url,payload,future ,errorListener);
        	        queue.add(matchCheck);
                    response=future.get(3,TimeUnit.SECONDS);
                    status=response.getInt("status");
                }

                catch (Exception e) {
					matchCheck=new JsonObjectRequest(Request.Method.POST,url,payload,future ,errorListener);
        	        queue.add(matchCheck);
        	        status=-1;
                    Log.e("#HTTPAPI",e.toString());
                }

				switch(status){
					case 0: {
						try {
							String serverHostname=response.getString("hostname");
							int serverPort=response.getInt("port");
							String opponentid=response.getString("opponentid");
							//get opponent info
							getMatchInfo=new GetMatchInfo(opponentid);
							getMatchInfo.start();
							getMatchInfo.join();
							//connect to server
							startGame(serverHostname,serverPort,getMatchInfo);
							stopSearch();
						}

						catch(Exception e){
							Log.e("#HTTPAPI", e.toString());
						}

						break;
					}

					case 1: {	// normal/nomatch
						break;
					}

					case 2:{	//a challenge
						break;
					}

					case 5: {	//login token is invalid
						stopSearch();
						if(new File(getFilesDir()+"/login").lastModified()+10000<System.currentTimeMillis())
							logout(null);
						break;
					}

					case -1: {
						break;
					}
				}

            } while (goOn);
        }

		public void stopSearch() {
			this.goOn=false;
			try{
				this.finalize();
			}
			catch(java.lang.Throwable e){
				Log.e("#matchChecker",e.toString());
			}
		}
    }

    class CheckVersion extends Thread {
		String url=getResources().getString(R.string.HTTPAPIurl)+"/version";
		JsonObjectRequest checkVersion;
		JSONObject response;
		RequestFuture<JSONObject> future = RequestFuture.newFuture();
		Context mContext;

		public CheckVersion(Context mContext){
			this.mContext=mContext;
		}

		@Override
		public void run(){
			try {
				checkVersion=new JsonObjectRequest(Request.Method.GET,url,null,future ,errorListener);
				queue.add(checkVersion);
				response = future.get(3, TimeUnit.SECONDS);
				if(VersionInfo.version!=(int)response.get("version")){
					runOnUiThread(new Runnable() {
									  @Override
									  public void run() {
									  	AlertDialog.Builder newVersionAvail = new AlertDialog.Builder(mContext);
									  	newVersionAvail.setTitle(R.string.newVersion);
									  	newVersionAvail.setPositiveButton(R.string.confirmChallenge, new DialogInterface.OnClickListener() {
									  		@Override
											public void onClick(DialogInterface dialog, int which){
									  			Intent download=new Intent(Intent.ACTION_VIEW,Uri.parse(getString(R.string.HTTPAPIurl)+"/download"));
									  			startActivity(download);
									  			finishAffinity();
											}
									  	});
									  	newVersionAvail.show();
									  }
								  }
					);
				}
			}

			catch(Exception e){
				Log.d("#JSONError",e.toString());
			}
		}
	}

    class JoinLobby extends Thread{

    	JSONObject payload = new JSONObject();

    	JoinLobby(){
		}

		JoinLobby(String opponent){
    		try {
				payload.put("opponent", opponent);
			}
			catch(Exception e){
    			Log.e("#JSONError",e.toString());
			}
		}

        @Override
        public void run() {
            int status;
            String url = getResources().getString(R.string.HTTPAPIurl) + "/lobby";
            JSONObject response;
            RequestFuture<JSONObject> future = RequestFuture.newFuture();


            try {
                payload.put("token", loginToken);
                JsonObjectRequest lobby = new JsonObjectRequest(Request.Method.POST, url, payload, future, errorListener);
                queue.add(lobby);
                response = future.get(5,TimeUnit.SECONDS);
                status = response.getInt("status");
            }

            catch (Exception e) {
                status=-1;
                Log.e("#LobbyError", e.toString());
            }

            final int s=status;
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					switch (s) {
						case 0: {
							Toast.makeText(getApplicationContext(), R.string.queueingToast, Toast.LENGTH_SHORT).show();
							break;
						}

						case 1: {
							Toast.makeText(getApplicationContext(), R.string.inMatchToast, Toast.LENGTH_SHORT).show();
							break;
						}

						case 2: {
							Toast.makeText(getApplicationContext(),R.string.noserver , Toast.LENGTH_SHORT).show();
							break;
						}

						case 4:{
							Toast.makeText(getApplicationContext(),R.string.noSuchOpponentToast,Toast.LENGTH_SHORT).show();
							break;
						}

						case 5:{
							Toast.makeText(getApplicationContext(),R.string.opponentInMatchToast,Toast.LENGTH_SHORT).show();
							break;
						}

						case -1: {
							Toast.makeText(getApplicationContext(), R.string.matchmakingError, Toast.LENGTH_SHORT).show();
							break;
						}
					}
				}
			});
        }
    }

    public void logout(View view) {
        String url=getResources().getString(R.string.HTTPAPIurl)+"/signout";
        JSONObject payload=new JSONObject();
        try{
            payload.put("token",loginToken);
        }
        catch (Exception ex){
            Log.e("#jsonerror",ex.toString());
        }
        JsonObjectRequest logout=new JsonObjectRequest(Request.Method.POST,url,payload,null ,errorListener);
        queue.add(logout);

        deleteFile("login");

        Intent login = new Intent(this,Login.class);
        startActivity(login);
    }

    public void joinLobby(View view){
        new JoinLobby().start();
    }

    public void declareChallenge(View view) {
    	final AlertDialog.Builder usernamebox = new AlertDialog.Builder(this);
		usernamebox.setTitle(R.string.setUsername);
		final EditText userInput = new EditText(this);
		usernamebox.setView(userInput);
		usernamebox.setPositiveButton(R.string.confirmChallenge, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String roomname = userInput.getText().toString();
				new JoinLobby(roomname).start();
			}
		});
		usernamebox.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
    		}
		});
		usernamebox.show();
	}

    public void showStatistics(View view){
    	Intent showStatistics=new Intent(this,Statistics.class);
    	startActivity(showStatistics);
	}

	public void gotoSettings(View view){
    	Intent settings=new Intent(this,Settings.class);
    	settings.putExtra("loginToken", loginToken);
    	startActivity(settings);
	}

    public void startGame(String hostname,int port,GetMatchInfo matchInfo){
    	Intent startgame = new Intent(this, ChessPlayer.class);
    	try {
			matchInfo.join();
			startgame.putExtra("loginToken", loginToken);
			startgame.putExtra("opponentUsername", matchInfo.getOppUsername());
			startgame.putExtra("opp_favourite_set", matchInfo.getOppFavourite_set());
			startgame.putExtra("opp_secondary_set", matchInfo.getOppSecondary_set());
			startgame.putExtra("own_favourite_set", matchInfo.getOwnFavourite_set());
			startgame.putExtra("own_secondary_set", matchInfo.getOwnSecondary_set());
			startgame.putExtra("hostname", hostname);
			startgame.putExtra("port", port);
			startActivity(startgame);
		}
		catch (Exception e){
    		Log.d("MatchInfoError","error fecthing preferences from server");
		}

		startActivity(startgame);

    }

    void startThreads(){
    	CheckVersion checkVersion=new CheckVersion(this);
		checkVersion.start();

    	matchChecker=new MatchChecker();
		matchChecker.start();
	}

	void stopThreads(){
    	matchChecker.stopSearch();
	}

    void checkLogin() {
    	username=null;
    	loginToken=null;
		try{
			InputStream loginFile=openFileInput("login");
			InputStreamReader loginFileReader=new InputStreamReader(loginFile);
			BufferedReader bufferedReader=new BufferedReader(loginFileReader);
			String[] rawData=bufferedReader.readLine().split("::");
			username=rawData[1];
			loginToken=rawData[0];
		}
		catch (Exception e){
			Log.e("#IOError",e.toString());
		}

    	//if not logged in
		if(username == null) {
            Intent login = new Intent(this,Login.class);
            startActivity(login);
        }

        else {
            //once logged in
            TextView usernameBox = findViewById(R.id.usernameBox);
            usernameBox.setText("logged in as " + username);
        }
    }
}
