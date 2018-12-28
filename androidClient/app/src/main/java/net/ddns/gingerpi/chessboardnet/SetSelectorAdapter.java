package net.ddns.gingerpi.chessboardnet;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.io.InputStream;

public class SetSelectorAdapter extends BaseAdapter {
	Context mContext;
	int size=ChessSet.texturePack.values().length;
	int ulocked_sets;
	ChessSet[] setsAvailable;
	ChessSet.texturePack[] allTextures=ChessSet.texturePack.values();

	public SetSelectorAdapter(Context mContext){
		this.mContext=mContext;
		setsAvailable=new ChessSet[size];

		for(int i=0; i<size; i++){
			setsAvailable[i]=new ChessSet(mContext, allTextures[i]);
		}

		loadSets();
	}

	@Override
	public Object getItem(int position){
		return setsAvailable[position].getPiece(ChessSet.piece.pawn_front);
	}

	public long getItemId(int position){
		return setsAvailable[position].getPiece(ChessSet.piece.pawn_front);
	}

	public int getCount(){
		return size;
	}

	public View getView(int position, View convertView, ViewGroup parent){
		ImageView icon = new ImageView(mContext);
		if((ulocked_sets>>position)%2==1)
			icon.setImageDrawable(mContext.getDrawable(setsAvailable[position].getPiece(ChessSet.piece.pawn_front)));
		else
			icon.setImageDrawable(mContext.getDrawable(R.drawable.ic_locked));
		icon.setMinimumHeight(96);
		return icon;
	}

	public void loadSets() {
		//load unlocked sets from file
		try {
			InputStream u_file = mContext.openFileInput("unlocked_sets");
			ulocked_sets = u_file.read();
			u_file.close();
		} catch (Exception e) {
			ulocked_sets = 3;
		}
	}
}
