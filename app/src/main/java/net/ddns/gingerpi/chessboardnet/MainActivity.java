package net.ddns.gingerpi.chessboardnet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import net.ddns.gingerpi.chessboardnet.Roomfiles.CacheDatabase;

public class MainActivity extends Activity {

    class GetUsername extends Thread {

        Context context;
        String result;

        @Override
        public void run() {
            this.result=
            CacheDatabase
                    .getInstance(this.context)
                    .getUserInfoDao()
                    .getUsername();
        }

        public GetUsername(Context context){
            this.context=context;
        }

        public String getStringResult() {
            return this.result;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GetUsername request= new GetUsername(this);
        if(request.getStringResult() == null) {
            Intent login = new Intent(this,Login.class);
            startActivity(login);
        }
    }
}
