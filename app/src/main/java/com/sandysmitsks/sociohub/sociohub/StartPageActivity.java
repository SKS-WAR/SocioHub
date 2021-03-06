package com.sandysmitsks.sociohub.sociohub;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class StartPageActivity extends AppCompatActivity {

    private Button NeedNewAccountButton,AlreadyHaveAccountButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_page);
        NeedNewAccountButton=(Button) findViewById(R.id.need_account_button);
        AlreadyHaveAccountButton=(Button)findViewById(R.id.already_have_account_button);

            NeedNewAccountButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent register_intent = new Intent(StartPageActivity.this,RegisterActivity.class);
                    startActivity(register_intent);
                }
            });


        AlreadyHaveAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent login_intent = new Intent(StartPageActivity.this,LoginActivity.class);
                startActivity(login_intent);
            }
        });

    }
}
