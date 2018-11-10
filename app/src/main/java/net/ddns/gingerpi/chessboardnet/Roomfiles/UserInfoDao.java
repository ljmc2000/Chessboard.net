package net.ddns.gingerpi.chessboardnet.Roomfiles;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import java.util.List;

@Dao
public interface UserInfoDao {
    @Query("Select username from userinfo")
    String getUserName();

    @Query("Select token from userinfo")
    String getToken();

    @Query("Select username from userinfo")
    String getUserID();

    @Insert
    void insert(UserInfo userinfo);

    @Delete
    void delete(UserInfo userinfo);
}
