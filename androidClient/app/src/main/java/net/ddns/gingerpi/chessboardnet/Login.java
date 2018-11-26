package net.ddns.gingerpi.chessboardnet;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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


public class Login extends Activity {

    String token;
    String id;

    public class SaveToken extends Thread {
        Context context;
        String id;
        String username;
        String token;

        @Override
        public void run() {
            CacheDatabase
                    .getInstance(this.context)
                    .getUserInfoDao()
                    .insert(new UserInfo(id,username,token));
        }

        public SaveToken(Context context,String id,String username, String token){
            this.context=context;
            this.id=id;
            this.username=username;
            this.token=token;
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
        setContentView(R.layout.activity_login);


    }

    public void login(View view){
        EditText username=(EditText) findViewById(R.id.loginUsername);
        EditText password=(EditText) findViewById(R.id.loginPassword);
        final String uname=username.getText().toString();
        String url=getResources().getString(R.string.HTTPAPIurl)+"/signin";
        JSONObject payload=new JSONObject();
        try{
            payload.put("username",username.getText().toString() );
            payload.put("password",password.getText().toString() );
        }

        catch(Exception e){
            Log.e("#JsonError",e.toString());
        }

        Response.Listener loginResponseListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    int status = response.getInt("status");

                    switch (status) {
                        case 0: { //success
                            id = response.getString("id");
                            token = response.getString("token");
                            SaveToken t = new SaveToken(getApplicationContext(),id,uname,token);
                            t.start();
                            t.join();
                            finish();
                            break;
                        }

                        case 1: { //failure
                            Toast.makeText(getApplicationContext(), "Password incorrect", Toast.LENGTH_SHORT).show();
                            break;
                        }

                        case -1: { //exception
                            Toast.makeText(getApplicationContext(), "User does not exist", Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Failed to Authenticate", Toast.LENGTH_SHORT).show();
                    Log.e("#HTTPAPI", e.toString());
                }
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest login=new JsonObjectRequest(Request.Method.POST,url,payload,loginResponseListener ,errorListener);
        queue.add(login);
    }

    public void register(View view){
        EditText username=(EditText) findViewById(R.id.registerUsername);
        EditText password1=(EditText) findViewById(R.id.registerPassword1);
        EditText password2=(EditText) findViewById(R.id.registerPassword2);
        final String uname=username.getText().toString();
        String url=getResources().getString(R.string.HTTPAPIurl)+"/signup";
        String p1=password1.getText().toString();
        String p2=password2.getText().toString();

        if(!p1.contentEquals(p2)) {
            Toast.makeText(getApplicationContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject payload=new JSONObject();
        try{
            payload.put("username",uname);
            payload.put("password",p1);
        }

        catch (Exception e){
            Log.e("#JsonError",e.toString());
        }

        Response.Listener registerResponseListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    int status = response.getInt("status");

                    switch (status) {
                        case 0: {   //success
                            id = response.getString("id");
                            token = response.getString("token");
                            SaveToken t = new SaveToken(getApplicationContext(), id, uname, token);
                            t.start();
                            t.join();
                            finish();
                            break;
                        }

                        case -1: {   //sever side exception
                            Toast.makeText(getApplicationContext(), "Existing username found: try another", Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }

                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Failed to connect to the Authentication server", Toast.LENGTH_SHORT).show();
                    Log.e("#HTTPAPI", e.toString());
                }
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest login=new JsonObjectRequest(Request.Method.POST,url,payload,registerResponseListener ,errorListener);
        queue.add(login);
    }
}
