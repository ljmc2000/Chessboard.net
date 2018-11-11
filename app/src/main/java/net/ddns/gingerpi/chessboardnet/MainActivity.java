package net.ddns.gingerpi.chessboardnet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import net.ddns.gingerpi.chessboardnet.Roomfiles.DeleteUserInfo;
import net.ddns.gingerpi.chessboardnet.Roomfiles.GetUserInfo;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkLogin();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkLogin();
    }

    public void logout(View view) {
        DeleteUserInfo request=new DeleteUserInfo(getApplicationContext());
        request.start();
        Intent login = new Intent(this,Login.class);
        startActivity(login);
    }

    void checkLogin() {
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
            TextView usernameBox = (TextView) findViewById(R.id.UsernameBox);
            usernameBox.setText("logged in as " + request.getUsername());
        }
    }
}
