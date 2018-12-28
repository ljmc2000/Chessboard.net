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

import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

public class Settings extends Activity {

	Bundle extras;
	String token;
	String url;
	SetSelectorAdapter setSelectorAdapter;
	RequestQueue queue;
	Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e("#HTTPAPI",error.toString());
        }
    };

	class Watcher extends Thread{
		RequestFuture<JSONObject> future;
		String message;


		public Watcher(RequestFuture future){
			this.future=future;
			this.message=null;
		}

		@Override
		public void run(){
			try {
				JSONObject r = future.get(1, TimeUnit.SECONDS);
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
						Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
					}
				});
			}
			catch(Exception e) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(getApplicationContext(), R.string.noconnhttp, Toast.LENGTH_SHORT).show();
					}
				});
			}
		}

	}

	class SyncSets extends Thread{
		String url=getResources().getString(R.string.HTTPAPIurl)+"/getunlocked";
		RequestFuture<JSONObject> future=RequestFuture.newFuture();
		SetSelectorAdapter setSelectorAdapter;
		JSONObject payload;
		JSONObject response;

		public SyncSets(String token,SetSelectorAdapter setSelectorAdapter){
			payload=new JSONObject();
			try{
				payload.put("token",token);
			}
			catch(Exception e){
				Log.e("#JSONError",e.toString());
			}
			this.setSelectorAdapter=setSelectorAdapter;
		}

		@Override
		public void run() {
			JsonObjectRequest getunlocked=new JsonObjectRequest(url, payload, future, errorListener);
			queue.add(getunlocked);
			try{
				response=future.get(3, TimeUnit.SECONDS);
				switch(response.getInt("status")){
					case 0:{
						deleteFile("unlocked_sets");
						OutputStream avsets=openFileOutput("unlocked_sets", MODE_APPEND);
						avsets.write(response.getInt("unlocked"));
						avsets.close();
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								setSelectorAdapter.loadSets();
								setSelectorAdapter.notifyDataSetChanged();
							}
						});
						break;
					}
					case 1:{
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(getApplicationContext(), R.string.noconnhttp, Toast.LENGTH_SHORT);
							}
						});
						break;
					}
				}
			}
			catch (Exception e){
				Log.e("#JSONError",e.toString());
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

		//first and second menuitems
		setSelectorAdapter=new SetSelectorAdapter(this);

		//first menuitem
		GridView set1_selector =findViewById(R.id.set1_selector);
		AdapterView.OnItemClickListener set1_listener=new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				try {
					RequestFuture<JSONObject> future=RequestFuture.newFuture();
					JSONObject payload=new JSONObject();
					payload.put("token",token);
					payload.put("favourite_set",ChessSet.texturePack.values()[position].toString());
					JsonObjectRequest change_set1 = new JsonObjectRequest(url, payload, future, errorListener);
					queue.add(change_set1);
					new Watcher(future).start();
				}
				catch (Exception e){
					Log.e("#JSONError",e.toString());
				}
			}
		};
		set1_selector.setAdapter(setSelectorAdapter);
		set1_selector.setOnItemClickListener(set1_listener);


		//second menuitem
		GridView set2_selector =findViewById(R.id.set2_selector);
		AdapterView.OnItemClickListener set2_listener=new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				try {
					RequestFuture<JSONObject> future=RequestFuture.newFuture();
					JSONObject payload=new JSONObject();
					payload.put("token",token);
					payload.put("secondary_set",ChessSet.texturePack.values()[position].toString());
					JsonObjectRequest change_set2 = new JsonObjectRequest(url, payload, future, errorListener);
					queue.add(change_set2);
					new Watcher(future).start();
				}
				catch (Exception e){
					Log.e("#JSONError",e.toString());
				}
			}
		};
		set2_selector.setAdapter(setSelectorAdapter);
		set2_selector.setOnItemClickListener(set2_listener);
	}

	public void refresh(View view){
		SyncSets syncSets=new SyncSets(token,setSelectorAdapter);
		syncSets.start();
	}
}
