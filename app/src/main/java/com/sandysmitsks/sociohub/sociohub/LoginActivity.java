package com.sandysmitsks.sociohub.sociohub;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private FirebaseAuth mAuth;

    private Button LoginButton;
    private EditText LoginEmail;
    private EditText LoginPassword;
    private ProgressDialog progressDialog;
    private TextView ForgetpasswordLink;

    private DatabaseReference usersReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth =FirebaseAuth.getInstance();
        usersReference= FirebaseDatabase.getInstance().getReference().child("Users");

        mToolbar=(Toolbar) findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("SignIn");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LoginButton = (Button) findViewById(R.id.login_button);
        LoginEmail =(EditText) findViewById(R.id.login_email);
        LoginPassword =(EditText) findViewById(R.id.login_password);
        ForgetpasswordLink = (TextView)findViewById(R.id.forget_password_link);
        progressDialog=new ProgressDialog(this);

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=LoginEmail.getText().toString();
                String password=LoginPassword.getText().toString();

                LoginUserAccount(email,password);
            }
        });

        ForgetpasswordLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,ResetPasswordActivity.class));
            }
        });
    }

    private void LoginUserAccount(String email, String password) {
        if (TextUtils.isEmpty(email)|| !email.matches("[a-zA-z0-9._-]+@[a-z]+.[a-z]+")){
            LoginEmail.setError("Please enter a valid Email");
            LoginEmail.requestFocus();
        }
        if (TextUtils.isEmpty(password)){
            LoginPassword.setError("Please enter a valid Email");
            LoginPassword.requestFocus();
        }
        else
        {
            progressDialog.setTitle("Logging In");
            progressDialog.setMessage("Please Wait,While we are verifying your Email and password");
            progressDialog.show();

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful())
                    {
                        if(mAuth.getCurrentUser().isEmailVerified()) {

                            String online_user_id = mAuth.getCurrentUser().getUid();
                            String Device_Token = FirebaseInstanceId.getInstance().getToken();

                            usersReference.child(online_user_id).child("device_token").setValue(Device_Token).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        }
                        else
                        {
                            Toast.makeText(LoginActivity.this, "A verification link has been send to your email " , Toast.LENGTH_SHORT).show();
                            mAuth.getCurrentUser().sendEmailVerification();
                        }


                    }
                    else
                        {
                            Toast.makeText(LoginActivity.this,"Invalid Email or Password",Toast.LENGTH_LONG).show();
                        }
                        progressDialog.dismiss();
                }
            });
        }
    }
}