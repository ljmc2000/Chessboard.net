package net.ddns.gingerpi.chessboardnet.Roomfiles;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.Request;

import net.ddns.gingerpi.chessboardnet.R;

import org.json.JSONException;
import org.json.JSONObject;

@Entity
public class UserInfo {
    @PrimaryKey
    @NonNull
    public final String id;
    @NonNull
    public final String username;
    public final String token;

    public UserInfo(@NonNull String id, @NonNull String username, String token)
    {
        this.id=id;
        this.username=username;
        this.token=token;
    }
}