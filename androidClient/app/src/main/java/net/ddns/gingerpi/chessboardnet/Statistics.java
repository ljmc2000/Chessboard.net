package net.ddns.gingerpi.chessboardnet;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;

import net.ddns.gingerpi.chessboardnet.Roomfiles.CacheDatabase;
import net.ddns.gingerpi.chessboardnet.Roomfiles.MatchStatistic;
import net.ddns.gingerpi.chessboardnet.Roomfiles.UserInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Statistics extends Activity {

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

		queue=Volley.newRequestQueue(this);
	}

	class Refresher extends Thread{
		String url=getResources().getString(R.string.HTTPAPIurl)+"/matchstats";

		@Override
		public void run(){
			UserInfo self=CacheDatabase
							.getInstance(getApplicationContext())
							.getUserInfoDao()
							.fetch();

			try {
				RequestFuture<JSONArray> future = RequestFuture.newFuture();
				JSONArray payload=new JSONArray();
				JSONObject payload_contents=new JSONObject();
				JSONArray response;
				payload_contents.put("token", self.token);
				payload.put(payload_contents);
				JsonArrayRequest getstats=new JsonArrayRequest(Request.Method.POST,url,payload,future ,errorListener);
				queue.add(getstats);
				response=future.get(5,TimeUnit.SECONDS);
				JSONObject j;
				for(int i=0; i<response.length(); i++){
					j=response.getJSONObject(i);

					CacheDatabase
							.getInstance(getApplicationContext())
							.getUserInfoDao()
							.insert(new UserInfo(
									j.getString("user_id"),
									j.getString("username"),
									null)
							);

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
		new Refresher().start();
	}
}
