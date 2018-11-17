package net.ddns.gingerpi.chessboardnet.Roomfiles;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import java.util.List;

@Dao
public interface UserInfoDao {
    @Query("Select * from userinfo where token not null")
    public UserInfo fetch();

    @Insert
    public void insert(UserInfo userinfo);

    @Query("delete from userinfo")
    public void clear();
}
