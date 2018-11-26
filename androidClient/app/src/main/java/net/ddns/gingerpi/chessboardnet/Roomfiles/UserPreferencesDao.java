package net.ddns.gingerpi.chessboardnet.Roomfiles;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Insert;

@Dao
public interface UserPreferencesDao {
	@Query("select * from UserPreferences where id in(select id from userinfo where (token not null))")
	public UserPreferences fetchOwn();

	@Query("select * from UserPreferences where (id=:id)")
	public UserPreferences getOpponentPreferences(String id);

	@Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insert(UserPreferences userPreferences);
}
