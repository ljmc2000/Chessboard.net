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
}