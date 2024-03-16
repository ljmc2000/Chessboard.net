package ie.delilahsthings.chessboardnet.Roomfiles;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

@Dao
public interface MatchStatisticDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	public void set(MatchStatistic matchStatistic);

	@Query("select * from MatchStatistic")
	public MatchStatistic[] getStats();
}
