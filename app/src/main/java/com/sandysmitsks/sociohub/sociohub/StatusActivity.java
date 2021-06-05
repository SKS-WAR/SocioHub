package com.sandysmitsks.sociohub.sociohub;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private Button saveChangesButton;
    private EditText statusInput;
    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;
    private DatabaseReference changestatusRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mToolbar = (Toolbar) findViewById(R.id.status_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Change Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        progressDialog = new ProgressDialog(this);

        String old_status = getIntent().getExtras().get("user_status").toString();
     //  statusInput.setText(old_status);

        mAuth = FirebaseAuth.getInstance();
        String user_id = mAuth.getCurrentUser().getUid();
        changestatusRef = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);

        saveChangesButton =(Button) findViewById(R.id.save_status_change_button);
        statusInput = (EditText) findViewById(R.id.status_input);

        saveChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String new_status=statusInput.getText().toString();

                ChangeProfileStatus(new_status);
            }
        });
    }

    private void ChangeProfileStatus(String new_status) {
        if (new_status.isEmpty())
        {
            Toast.makeText(StatusActivity.this,"Please write your status",Toast.LENGTH_LONG).show();
        }
        else {
            progressDialog.setTitle("Status Updating");
            progressDialog.show();
            changestatusRef.child("user_status").setValue(new_status).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                   if (task.isSuccessful())
                   {
                       progressDialog.dismiss();
                       Intent settingsIntent = new Intent(StatusActivity.this,SettingsActivity.class);
                       startActivity(settingsIntent);

                       Toast.makeText(StatusActivity.this,"Status Updated",Toast.LENGTH_LONG).show();
                   }
                   else {
                       Toast.makeText(StatusActivity.this,"Error Occured",Toast.LENGTH_LONG).show();
                   }
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
