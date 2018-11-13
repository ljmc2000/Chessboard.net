package net.ddns.gingerpi.chessboardnet.Roomfiles;

import android.content.Context;

public class GetUserInfo extends Thread {
    Context context;
    UserInfo result;

    @Override
    public void run() {
        this.result=
                CacheDatabase
                        .getInstance(this.context)
                        .getUserInfoDao()
                        .fetch();
    }

    public GetUserInfo(Context context){
        this.context=context;
    }

    public String getUsername() {
        try {
            return this.result.username;
        }

        catch(Exception e){
            return null;
        }
    }

    public String getID() {
        return this.result.id;
    }

    public String getToken() {
        return this.result.token;
    }
}
