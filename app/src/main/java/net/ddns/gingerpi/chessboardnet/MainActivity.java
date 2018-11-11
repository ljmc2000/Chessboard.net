package net.ddns.gingerpi.chessboardnet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import net.ddns.gingerpi.chessboardnet.Roomfiles.CacheDatabase;
import net.ddns.gingerpi.chessboardnet.Roomfiles.UserInfo;

import java.util.List;

public class MainActivity extends Activity {

    class GetUsername extends Thread {

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

        public GetUsername(Context context){
            this.context=context;
        }

        public String getUsername() {
            return this.result.username;
        }

        public String getID() {
            return this.result.id;
        }

        public String token() {
            return this.result.token;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //if not logged in
        GetUsername request= new GetUsername(this);
        try {
            request.start();
            request.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d("#username",request.getUsername());
        if(request.getUsername() == null) {
            Intent login = new Intent(this,Login.class);
            startActivity(login);
        }

        //once logged in
        TextView usernameBox=(TextView) findViewById(R.id.UsernameBox);
        usernameBox.setText("logged in as "+request.getUsername());
    }
}
