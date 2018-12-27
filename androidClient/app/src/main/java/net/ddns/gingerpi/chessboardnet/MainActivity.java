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

import net.ddns.gingerpi.chessboardnet.Roomfiles.CacheDatabase;
import net.ddns.gingerpi.chessboardnet.Roomfiles.UserInfo;
import net.ddns.gingerpi.chessboardnet.Roomfiles.UserPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;


public class MainActivity extends Activity {

    RequestQueue queue;
    String loginToken="";
    String userId;
    String serverHostname="";
    int serverPort;
    String opponentid="";

    //threads
	GetUserPreferencesFromServer getUserPreferencesFromServer;
	MatchChecker matchChecker;
	GetOpponentInfo getOpponentInfo;

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

        loginToken=checkLogin();
        opponentid=null;
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
        loginToken=checkLogin();
        opponentid=null;
        queue=Volley.newRequestQueue(this);

		startThreads();
    }

	public class GetUserInfo extends Thread {
		Context context;
		UserInfo result;
		UserPreferences pref;

		@Override
		public void run() {
			this.result=
					CacheDatabase
							.getInstance(this.context)
							.getUserInfoDao()
							.fetch();

			this.pref=
					CacheDatabase
							.getInstance(this.context)
							.getUserPreferencesDao()
							.fetchOwn();

		}

		public GetUserInfo(Context context){
			this.context=context;
		}

		public String getUsername() {
			try {
				return this.result.username;
			}

			catch(Exception e){
				return null;
			}
		}

		public String getID() {
			return this.result.id;
		}

		public String getToken() {
			try{
				return this.result.token;
			}

			catch (NullPointerException e){
				return null;
			}
		}
	}
	public class DeleteUserInfo extends Thread {

		Context context;

		@Override
		public void run(){
			CacheDatabase
					.getInstance(this.context)
					.getUserInfoDao()
					.clear();
		}

		public DeleteUserInfo(Context context){
			this.context=context;
		}
	}

    class GetOpponentInfo extends Thread{
        String url=getResources().getString(R.string.HTTPAPIurl)+"/userinfo";
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JSONObject payload=new JSONObject();
        JSONObject response;
        UserInfo opponent;
        UserPreferences oppref;

        @Override
        public void run(){
            try{
                payload.put("token",loginToken);
                payload.put("userid",opponentid);

                JsonObjectRequest opponentInfo=new JsonObjectRequest(Request.Method.POST,url,payload,future ,errorListener);
                queue.add(opponentInfo);
                response=future.get(5,TimeUnit.SECONDS);

                //put into database
                opponent=new UserInfo(
                        response.getString("userid"),
                        response.getString("username"),
                        null);

                oppref=new UserPreferences(
                		response.getString("userid"),
						ChessSet.texturePack.valueOf(response.getString("favourite_set")),
						ChessSet.texturePack.valueOf(response.getString("secondary_set"))
				);

                Log.d("#inserting_user",opponent.toString());

                CacheDatabase
                    .getInstance(getApplicationContext())
                    .getUserInfoDao()
                    .insert(opponent);

                CacheDatabase
						.getInstance(getApplicationContext())
						.getUserPreferencesDao()
						.insert(oppref);
            }
            catch (JSONException e) {
                Log.e("#JsonError",e.toString());
            }
            catch (Exception e){
                Log.e("#HTTPAPI",e.toString());
            }
        }
    }

    class GetUserPreferencesFromServer extends Thread{
		String url=getResources().getString(R.string.HTTPAPIurl)+"/userinfo";

		UserPreferences prefs;
		JSONObject payload=new JSONObject();
        JSONObject response;
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest userPreferences;

        public GetUserPreferencesFromServer(){
        	try {
        		payload.put("token",loginToken);
        		payload.put("userid",userId);

			}
			catch(JSONException e){
                Log.e("#JsonError",e.toString());
			}
		}

		@Override
		public void run(){
        	try {
				userPreferences = new JsonObjectRequest(Request.Method.POST, url, payload, future, errorListener);
				queue.add(userPreferences);
				response = future.get(15, TimeUnit.SECONDS);

				prefs = new UserPreferences(
						response.getString("userid"),
						ChessSet.texturePack.valueOf(response.getString("favourite_set")),
						ChessSet.texturePack.valueOf(response.getString("secondary_set"))
				);

				CacheDatabase
						.getInstance(getApplicationContext())
						.getUserPreferencesDao()
						.insert(prefs);
			}

			catch(Exception e){
        		Log.d("#prefs",e.toString());
			}
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
					Thread.sleep(3000);    //minimise number of requests
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
							serverHostname=response.getString("hostname");
							serverPort=response.getInt("port");
							opponentid=response.getString("opponentid");
							//get opponent info
							getOpponentInfo=new GetOpponentInfo();
							getOpponentInfo.start();
							//connect to server
							startGame(serverHostname,serverPort);
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
        DeleteUserInfo request=new DeleteUserInfo(getApplicationContext());
        request.start();

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

    public void startGame(String hostname,int port){
    	Intent startgame = new Intent(this, ChessPlayer.class);
		startgame.putExtra("loginToken", loginToken);
		startgame.putExtra("opponentid", opponentid);
		startgame.putExtra("hostname", hostname);
		startgame.putExtra("port", port);

		try {
			getUserPreferencesFromServer.join();
			getOpponentInfo.join();
		}

		catch (Exception e) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(getApplicationContext(), R.string.usercheckupfail, Toast.LENGTH_SHORT);
				}
			});
		}
		startActivity(startgame);

    }

    void startThreads(){
		getUserPreferencesFromServer= new GetUserPreferencesFromServer();
		getUserPreferencesFromServer.start();

		CheckVersion checkVersion=new CheckVersion(this);
		checkVersion.start();

    	matchChecker=new MatchChecker();
		matchChecker.start();
	}

	void stopThreads(){
    	matchChecker.stopSearch();
	}

    String checkLogin() {
        //if not logged in
        GetUserInfo request= new GetUserInfo(this);
        try {
            request.start();
            request.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(request.getUsername() == null) {
            Intent login = new Intent(this,Login.class);
            startActivity(login);
        }

        else {
            //once logged in
            TextView usernameBox = findViewById(R.id.usernameBox);
            usernameBox.setText("logged in as " + request.getUsername());
            this.userId=request.getID();
        }

        return request.getToken();
    }
}
