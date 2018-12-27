package net.ddns.gingerpi.chessboardnet.Roomfiles;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

import net.ddns.gingerpi.chessboardnet.R;

@Database(entities = {MatchStatistic.class},version=5,exportSchema = false)
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
                context.getResources().getString(R.string.DB_NAME))
				.fallbackToDestructiveMigration()
				.build();
    }

    public abstract MatchStatisticDao getMatchStatisticDao();
}
