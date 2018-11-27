package net.ddns.gingerpi.chessboardnet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;

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

    class getOpponentInfo extends Thread{
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
                response=future.get(15,TimeUnit.SECONDS);

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
        int status=0;
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

            do {
				try {
					Thread.sleep(3000);    //minimise number of requests
    	            matchCheck=new JsonObjectRequest(Request.Method.POST,url,payload,future ,errorListener);
        	        queue.add(matchCheck);
                    response=future.get(3,TimeUnit.SECONDS);
                    status=response.getInt("status");
                }

                catch (Exception e) {
                    Log.e("#HTTPAPI",e.toString());
                    break;
                }
            } while (status == 1 && goOn);

            switch(status){
                case 0: {
                    try {
                        serverHostname=response.getString("hostname");
                        serverPort=response.getInt("port");
                        opponentid=response.getString("opponentid");
                        //get opponent info
                        new getOpponentInfo().start();
                        //connect to server
                        startGame(serverHostname,serverPort);
                    }

                    catch(Exception e){
                        Log.e("#HTTPAPI", e.toString());
                    }

                    break;
                }

                case -1: {
                    break;
                }
            }

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

    class JoinLobby extends Thread{

        @Override
        public void run() {
            int status;
            String url = getResources().getString(R.string.HTTPAPIurl) + "/lobby";
            JSONObject payload = new JSONObject();
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
							Toast.makeText(getApplicationContext(), "Queueing for match", Toast.LENGTH_SHORT).show();
							break;
						}

						case 1: {
							Toast.makeText(getApplicationContext(), "already in Match", Toast.LENGTH_SHORT).show();
							break;
						}

						case -1: {
							Toast.makeText(getApplicationContext(), "Error connecting to the match making system", Toast.LENGTH_SHORT).show();
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

    public void startGame(String hostname,int port){
    	Intent startgame = new Intent(this, ChessPlayer.class);
		startgame.putExtra("loginToken", loginToken);
		startgame.putExtra("opponentid", opponentid);
		startgame.putExtra("hostname", hostname);
		startgame.putExtra("port", port);
		startActivity(startgame);

    }

    void startThreads(){
    	getUserPreferencesFromServer= new GetUserPreferencesFromServer();
		getUserPreferencesFromServer.start();

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
