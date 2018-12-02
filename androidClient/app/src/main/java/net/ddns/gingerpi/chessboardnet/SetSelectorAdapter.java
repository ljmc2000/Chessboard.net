package net.ddns.gingerpi.chessboardnet;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class SetSelectorAdapter extends BaseAdapter {
	Context mContext;
	int size=ChessSet.texturePack.values().length;
	ChessSet[] setsAvailable;
	ChessSet.texturePack[] allTextures=ChessSet.texturePack.values();

	public SetSelectorAdapter(Context mContext){
		this.mContext=mContext;
		setsAvailable=new ChessSet[size];

		for(int i=0; i<size; i++){
			setsAvailable[i]=new ChessSet(mContext, allTextures[i]);
		}
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
		icon.setImageDrawable(mContext.getDrawable(setsAvailable[position].getPiece(ChessSet.piece.pawn_front)));
		icon.setMinimumHeight(96);
		return icon;
	}

}
