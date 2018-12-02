package net.ddns.gingerpi.chessboardnet.Roomfiles;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(foreignKeys = @ForeignKey(entity=UserInfo.class, parentColumns = "username", childColumns = "username", onDelete = CASCADE))
public class MatchStatistic {
	@PrimaryKey
	@NonNull
	public String username;
	public int wins;
	public int losses;
	public int surrenders;
	public int total_matches;

	public MatchStatistic(String username, int wins, int losses, int surrenders, int total_matches){
		this.username=username;
		this.wins=wins;
		this.losses=losses;
		this.surrenders=surrenders;
		this.total_matches=total_matches;
	}
}
