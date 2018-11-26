package net.ddns.gingerpi.chessboardnet.Roomfiles;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverter;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;

import net.ddns.gingerpi.chessboardnet.ChessSet.texturePack;

@Entity
public class UserInfo {
    @PrimaryKey
    @NonNull
    public String id;

    @NonNull
    public String username;

    public texturePack favourite_set,secondary_set;

    public String token;

    public UserInfo(String id,String username,texturePack favourite_set,texturePack secondary_set, String token)
    {
        this.id=id;
        this.username=username;
        this.favourite_set=favourite_set;
        this.secondary_set=secondary_set;
        this.token=token;
    }

    public String toString(){
        return id+" :: "+username;
    }
}