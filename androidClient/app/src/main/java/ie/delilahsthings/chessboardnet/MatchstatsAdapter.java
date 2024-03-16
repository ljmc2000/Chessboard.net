package ie.delilahsthings.chessboardnet;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import ie.delilahsthings.chessboardnet.Roomfiles.CacheDatabase;
import ie.delilahsthings.chessboardnet.Roomfiles.MatchStatistic;

public class MatchstatsAdapter extends BaseAdapter {
	private Context mContext;
	private MatchStatistic[] matchStatistics;

	class GetData extends Thread{
		Context mContext;

		public GetData(Context mContext){
			this.mContext=mContext;
		}

		@Override
		public void run(){
			matchStatistics=CacheDatabase
				.getInstance(mContext)
				.getMatchStatisticDao()
				.getStats();
		}
	}

	public MatchstatsAdapter (Context mContext){
		this.mContext=mContext;
		new GetData(mContext).start();
	}

	@Override
	public String getItem(int position){
		return matchStatistics[position].toString();
	}

	@Override
	public long getItemId(int position){
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		TextView returnme=new TextView(mContext);
		returnme.setText(getItem(position));
		returnme.setTextSize(30);
		return returnme;
	}

	@Override
	public int getCount(){
		if (matchStatistics==null)
			return 0;
		else
			return matchStatistics.length;
	}

	public void refresh(){
		GetData g=new GetData(mContext);
		g.start();
		try {
			g.join();
		}
		catch (Exception e){
			Log.e("#refresh error",e.toString());
		}
		notifyDataSetChanged();
	}
}
