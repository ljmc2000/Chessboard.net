package net.ddns.gingerpi.chessboardnet;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

public class Settings extends Activity {

	Bundle extras;
	String token;
	String url;
	RequestQueue queue;
	RequestFuture<JSONObject> future=RequestFuture.newFuture();
	Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e("#HTTPAPI",error.toString());
        }
    };

	class Watcher extends Thread{

		@Override
		public void run(){
			while(1==1){
				try {
					JSONObject r = future.get(5, TimeUnit.SECONDS);
					final String message;
					switch (r.getInt("status")){
						case 0:{
							message=getResources().getString(R.string.success);
							break;
						}

						case 1:{
							message=r.getString("reason");
							break;
						}

						default:{
							message="invalid status code";
							break;
						}
					}

					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
						}
					});
				}
				catch(Exception e){

				}
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		queue=Volley.newRequestQueue(this);
		extras=getIntent().getExtras();
		token=extras.getString("loginToken");
		url=getResources().getString(R.string.HTTPAPIurl)+"/setprefs";

		GridView set1_selector =findViewById(R.id.set1_selector);
		SetSelectorAdapter s=new SetSelectorAdapter(this);
		AdapterView.OnItemClickListener set1_listener=new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				try {
					JSONObject payload=new JSONObject();
					payload.put("token",token);
					payload.put("favourite_set",ChessSet.texturePack.values()[position].toString());
					JsonObjectRequest change_set1 = new JsonObjectRequest(url, payload, future, errorListener);
					queue.add(change_set1);
				}
				catch (Exception e){
					Log.e("#JSONError",e.toString());
				}
			}
		};
		set1_selector.setAdapter(s);
		set1_selector.setOnItemClickListener(set1_listener);

		GridView set2_selector =findViewById(R.id.set2_selector);
		set2_selector.setAdapter(s);
	}
}
