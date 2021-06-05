package com.sandysmitsks.sociohub.sociohub;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Thread thread=new Thread()
        {
            @Override
            public void run() {
              try {
                  sleep(750);
              }
              catch (Exception e){
                  e.printStackTrace();
              }
              finally {
                  Intent intent=new Intent(WelcomeActivity.this,MainActivity.class);
                  startActivity(intent);
                  WelcomeActivity.this.finish();
                  }
            }
        };
        thread.start();
    }
}




