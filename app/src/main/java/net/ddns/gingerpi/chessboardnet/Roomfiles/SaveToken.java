package net.ddns.gingerpi.chessboardnet.Roomfiles;

import android.content.Context;

public class SaveToken extends Thread {
    Context context;
    String id;
    String username;
    String token;

    @Override
    public void run() {
        CacheDatabase
                .getInstance(this.context)
                .getUserInfoDao()
                .insert(new UserInfo(id,username,token));
    }

    public SaveToken(Context context,String id,String username, String token){
        this.context=context;
        this.id=id;
        this.username=username;
        this.token=token;
    }
}