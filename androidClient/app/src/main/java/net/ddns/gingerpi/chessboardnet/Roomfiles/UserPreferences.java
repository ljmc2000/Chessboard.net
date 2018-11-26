package net.ddns.gingerpi.chessboardnet.Roomfiles;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import net.ddns.gingerpi.chessboardnet.ChessSet;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity (foreignKeys = @ForeignKey(entity=UserInfo.class, parentColumns = "id", childColumns = "id", onDelete = CASCADE))
public class UserPreferences {
	@PrimaryKey
	@NonNull
	public String id;
	public ChessSet.texturePack favourite_set,secondary_set;

	public UserPreferences(String id,ChessSet.texturePack favourite_set, ChessSet.texturePack secondary_set){
		this.id=id;
		this.favourite_set=favourite_set;
		this.secondary_set=secondary_set;
	}
}
