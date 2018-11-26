package net.ddns.gingerpi.chessboardnet.Roomfiles;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import net.ddns.gingerpi.chessboardnet.R;

@Database(entities = {UserInfo.class},version=1,exportSchema = false)
public abstract class CacheDatabase extends RoomDatabase{
    private static volatile CacheDatabase instance;

    public static synchronized CacheDatabase getInstance(Context context) {
        if (instance == null) {
            instance = create(context);
        }
        return instance;
    }

    private static CacheDatabase create(final Context context) {
        return Room.databaseBuilder(
                context,
                CacheDatabase.class,
                context.getResources().getString(R.string.DB_NAME)).build();
    }

    public abstract UserInfoDao getUserInfoDao();
}
