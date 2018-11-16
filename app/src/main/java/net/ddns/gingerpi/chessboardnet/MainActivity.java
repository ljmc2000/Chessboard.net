package net.ddns.gingerpi.chessboardnet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import net.ddns.gingerpi.chessboardnet.Roomfiles.CacheDatabase;
import net.ddns.gingerpi.chessboardnet.Roomfiles.UserInfo;

public class MainActivity extends Activity {

    String loginToken;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginToken=checkLogin();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loginToken=checkLogin();
    }

    public void logout(View view) {
        DeleteUserInfo request=new DeleteUserInfo(getApplicationContext());
        request.start();
        Intent login = new Intent(this,Login.class);
        startActivity(login);
    }

    public void startGame(View view){
        Intent startgame=new Intent(this,ChessPlayer.class);
        startgame.putExtra("loginToken", loginToken);
        startActivity(startgame);
    }

    String checkLogin() {
        //if not logged in
        GetUserInfo request= new GetUserInfo(this);
        try {
            request.start();
            request.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(request.getUsername() == null) {
            Intent login = new Intent(this,Login.class);
            startActivity(login);
        }

        else {
            //once logged in
            TextView usernameBox = (TextView) findViewById(R.id.usernameBox);
            usernameBox.setText("logged in as " + request.getUsername());
        }

        return request.getToken();
    }
}
