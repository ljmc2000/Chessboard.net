package net.ddns.gingerpi.chessboardnet.Roomfiles;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

@Dao
public interface UserInfoDao {
    @Query("Select * from userinfo where token not null")
    public UserInfo fetch();

    @Query("select * from userinfo where (id = :id)")
    public UserInfo getOpponentInfo(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insert(UserInfo userinfo);

    @Query("delete from userinfo")
    public void clear();
}
