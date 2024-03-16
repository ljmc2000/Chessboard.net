package ie.delilahsthings.chessboardnet;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;

import ie.delilahsthings.chessboardnet.Roomfiles.CacheDatabase;
import ie.delilahsthings.chessboardnet.Roomfiles.MatchStatistic;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Statistics extends Activity {

	String token;
	MatchstatsAdapter adapter;
	ListView matchStats;

	RequestQueue queue;
	Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e("#HTTPAPI",error.toString());
        }
    };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_statistics);

		token=getToken();
		queue=Volley.newRequestQueue(this);

		matchStats=(ListView) findViewById(R.id.pastMatchList);
		adapter=new MatchstatsAdapter(this);
		matchStats.setAdapter(adapter);
		adapter.refresh();


	}

	class Refresher extends Thread{
		String url=getResources().getString(R.string.HTTPAPIurl)+"/matchstats";

		@Override
		public void run(){
			try {
				RequestFuture<JSONArray> future = RequestFuture.newFuture();
				JSONArray payload=new JSONArray();
				JSONObject payload_contents=new JSONObject();
				JSONArray response;
				payload_contents.put("token", token);
				payload.put(payload_contents);
				JsonArrayRequest getstats=new JsonArrayRequest(Request.Method.POST,url,payload,future ,errorListener);
				queue.add(getstats);
				response=future.get(5,TimeUnit.SECONDS);
				JSONObject j;
				for(int i=0; i<response.length(); i++){
					j=response.getJSONObject(i);

					CacheDatabase
							.getInstance(getApplicationContext())
							.getMatchStatisticDao()
							.set(new MatchStatistic(
									j.getString("username"),
									j.getInt("wins"),
									j.getInt("losses"),
									j.getInt("surrenders"),
									j.getInt("total_matches")));
				}
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						adapter.refresh();
					}
				});
			}
			catch(TimeoutException e) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(getApplicationContext(), R.string.noconnhttp, Toast.LENGTH_SHORT).show();
					}
				});
			}
			catch(Exception e){
				Log.e("#JsonError",e.toString());
			}
		}
	}

	public void refresh(View view){
		Refresher r = new Refresher();
		r.start();
	}

	String getToken(){
		try {
			InputStream loginFile = openFileInput("login");
			InputStreamReader loginFileReader = new InputStreamReader(loginFile);
			BufferedReader bufferedReader = new BufferedReader(loginFileReader);
			String[] rawData = bufferedReader.readLine().split("::");
			return rawData[0];
		}
		catch(Exception e){
			Log.e("#IOError",e.toString());
			return null;
		}
	}
}
