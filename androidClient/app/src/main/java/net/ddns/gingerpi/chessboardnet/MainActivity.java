package net.ddns.gingerpi.chessboardnet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import net.ddns.gingerpi.chessboardnet.Roomfiles.CacheDatabase;
import net.ddns.gingerpi.chessboardnet.Roomfiles.UserInfo;

import org.json.JSONObject;

public class MainActivity extends Activity {

    RequestQueue queue;
    String loginToken="";
    String server="";
    String opponent="";

    public class GetUserInfo extends Thread {
        Context context;
        UserInfo result;

        @Override
        public void run() {
            this.result=
                    CacheDatabase
                            .getInstance(this.context)
                            .getUserInfoDao()
                            .fetch();
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
        queue=Volley.newRequestQueue(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loginToken=checkLogin();
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

    public void getGame(View view){
        String url=getResources().getString(R.string.HTTPAPIurl)+"/lobby";
        JSONObject payload=new JSONObject();
        try{
            payload.put("token",loginToken);
        }

        catch (Exception e){
            Log.e("#JsonError",e.toString());
        }

        Response.Listener lobbyResponseListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    int status = response.getInt("status");

                    switch (status) {
                        case 0: {
                            Toast.makeText(getApplicationContext(), "Queueing for match", Toast.LENGTH_SHORT).show();
                            break;
                        }

                        case 1: {
                            Toast.makeText(getApplicationContext(), "already in Match", Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }
                } catch (Exception e) {
                    Log.e("#HTTPAPI", e.toString());
                }
            }
        };

        JsonObjectRequest lobby=new JsonObjectRequest(Request.Method.POST,url,payload,lobbyResponseListener ,errorListener);
        queue.add(lobby);
    }

    public void startGame(View view){
        Intent startgame=new Intent(this,ChessPlayer.class);
        startgame.putExtra("loginToken", loginToken);
        startActivity(startgame);
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
            TextView usernameBox = (TextView) findViewById(R.id.usernameBox);
            usernameBox.setText("logged in as " + request.getUsername());
        }

        return request.getToken();
    }
}
