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
import org.json.JSONObject;

import net.ddns.gingerpi.chessboardnet.Roomfiles.SaveToken;


public class Login extends Activity {

    String token;
    String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


    }

    public void login(View view){
        EditText username=(EditText) findViewById(R.id.loginUsername);
        EditText password=(EditText) findViewById(R.id.loginPassword);
        String url=getResources().getString(R.string.HTTPAPIurl)+"/signin";
        Log.d("#url",url);
        final String uname=username.getText().toString();
        JSONObject payload=new JSONObject();
        try{
            payload.put("username",username.getText().toString() );
            payload.put("password",password.getText().toString() );
        }

        catch(Exception e){
            Log.e("#JsonError",e.toString());
        }

        Response.Listener responseListener = new Response.Listener<JSONObject>() {
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
                    Toast.makeText(getApplicationContext(), "Failed to connect to the Authentication server", Toast.LENGTH_SHORT).show();
                    Log.e("#HTTPAPI", e.toString());
                }
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("#HTTPAPI",error.toString());
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest login=new JsonObjectRequest(Request.Method.POST,url,payload,responseListener ,errorListener);
        queue.add(login);
    }
}
