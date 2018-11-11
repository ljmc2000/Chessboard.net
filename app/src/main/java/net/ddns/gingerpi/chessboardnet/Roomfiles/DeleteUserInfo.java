package net.ddns.gingerpi.chessboardnet.Roomfiles;

import android.content.Context;

public class DeleteUserInfo extends Thread {

    Context context;

    @Override
    public void run(){
        CacheDatabase
                .getInstance(this.context)
                .getUserInfoDao()
                .clear();
    }

    public DeleteUserInfo(Context context){
        this.context=context;
    }
}
