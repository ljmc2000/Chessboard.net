package net.ddns.gingerpi.chessboardnet.Roomfiles;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class UserInfo {
    @PrimaryKey
    @NonNull
    public String id;
    @NonNull
    public String username;
    public String token;

    public UserInfo(String id,String username, String token)
    {
        this.id=id;
        this.username=username;
        this.token=token;
    }

    public String toString(){
        return id+" :: "+username;
    }
}