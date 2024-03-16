package ie.delilahsthings.chessboardnet;

import android.app.Activity;
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

import java.io.OutputStream;


public class Login extends Activity {

    String token;

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
                            token = response.getString("token");
                            saveToken(uname, token);
                            finish();
                            break;
                        }

                        case 1: { //failure
                            Toast.makeText(getApplicationContext(), R.string.badpwd, Toast.LENGTH_SHORT).show();
                            break;
                        }

                        case -1: { //exception
                            Toast.makeText(getApplicationContext(), R.string.nouser, Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), R.string.authfail, Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getApplicationContext(), R.string.nomatchpwd, Toast.LENGTH_SHORT).show();
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
                            token = response.getString("token");
                            saveToken(uname, token);
                            finish();
                            break;
                        }

                        case -1: {   //sever side exception
                            Toast.makeText(getApplicationContext(), R.string.duplicateUser, Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }

                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), R.string.connfail, Toast.LENGTH_SHORT).show();
                    Log.e("#HTTPAPI", e.toString());
                }
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest login=new JsonObjectRequest(Request.Method.POST,url,payload,registerResponseListener ,errorListener);
        queue.add(login);
    }

    void saveToken(String username,String token){
    	 try{
    	 	deleteFile("login");
    	 	OutputStream login=openFileOutput("login", MODE_APPEND);
    	 	login.write((token+"::"+username).getBytes());
    	 }
    	 catch(Exception e){
    	 	Log.e("#IOError",e.toString());
    	 }
	}
}
