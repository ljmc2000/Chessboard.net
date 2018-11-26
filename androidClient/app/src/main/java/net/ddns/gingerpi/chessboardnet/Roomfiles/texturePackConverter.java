package net.ddns.gingerpi.chessboardnet.Roomfiles;

import android.arch.persistence.room.TypeConverter;

import net.ddns.gingerpi.chessboardnet.ChessSet.texturePack;

public class texturePackConverter {
	@TypeConverter
	public int fromTexturePack(texturePack set){
		if(set==null)
			return -1;
		else
			return set.ordinal();
	}

	@TypeConverter
	public texturePack toTexturePack(int t){
		if(t==-1)
			return null;
		else
			return texturePack.values()[t];
	}
}
