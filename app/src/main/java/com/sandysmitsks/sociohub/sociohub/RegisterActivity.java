package com.sandysmitsks.sociohub.sociohub;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import org.w3c.dom.Text;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference storeUserDefaultDataReference;

    private ProgressDialog progressDialog;

    private Toolbar mToolbar;
    private EditText RegisterUserName;
    private EditText RegisterUserEmail;
    private EditText RegisterUserPassword;
    private Button CreateAccountButton,SignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mToolbar=(Toolbar) findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("SignUp");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth =FirebaseAuth.getInstance();

        RegisterUserName = (EditText) findViewById(R.id.register_name);
        RegisterUserEmail = (EditText) findViewById(R.id.register_email);
        RegisterUserPassword = (EditText) findViewById(R.id.register_password);
        SignIn=(Button)findViewById(R.id.signin) ;
        CreateAccountButton=(Button) findViewById(R.id.create_account_button);
        progressDialog = new ProgressDialog(this);


        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name=RegisterUserName.getText().toString();
                String email=RegisterUserEmail.getText().toString();
                String password=RegisterUserPassword.getText().toString();

                RegisterAccount(name,email,password);



            }
        });
        SignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().getCurrentUser().reload()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                             if(FirebaseAuth.getInstance().getCurrentUser().isEmailVerified())
                             {
                                 String Device_Token = FirebaseInstanceId.getInstance().getToken();

                                 String current_user_id = mAuth.getCurrentUser().getUid();
                                 storeUserDefaultDataReference = FirebaseDatabase.getInstance().getReference().child("Users").child(current_user_id);

                                 final String name=RegisterUserName.getText().toString();

                                 storeUserDefaultDataReference.child("user_name").setValue(name);
                                 storeUserDefaultDataReference.child("user_status").setValue("Hey there,I am a member of SocioHub");
                                 storeUserDefaultDataReference.child("user_image").setValue("download");
                                 storeUserDefaultDataReference.child("device_token").setValue(Device_Token);
                                 storeUserDefaultDataReference.child("user_thumb_image").setValue("download").addOnCompleteListener(new OnCompleteListener<Void>() {
                                     @Override
                                     public void onComplete(@NonNull Task<Void> task) {
                                         if(task.isSuccessful())
                                         {
                                             Intent mainIntend = new Intent(RegisterActivity.this,MainActivity.class);
                                             mainIntend.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                             startActivity(mainIntend);
                                             finish();
                                         }
                                     }
                                 });
                             }
                            }
                        });
            }
        });
    }

    private void RegisterAccount(final String name, String email, String password) {
        if(TextUtils.isEmpty(name)){
            RegisterUserName.setError("Please enter a valid User Name");
            RegisterUserName.requestFocus();
        }else
//        if(TextUtils.isEmpty(email) || !email.matches("[a-zA-z0-9._-]+@[a-z]+.[a-z]+")){
        if(TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            RegisterUserEmail.setError("Please enter a valid Email");
            RegisterUserEmail.requestFocus();
        }else if(TextUtils.isEmpty(password)){
            RegisterUserPassword.setError("Please enter a valid password");
            RegisterUserPassword.requestFocus();
        }
        else {
            progressDialog.setTitle("Creating New Account");
            progressDialog.setMessage("Please wait,While we are creating your Account");
            progressDialog.show();

            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                  if (task.isSuccessful())
                  {
                      Toast.makeText(RegisterActivity.this, "Account created successfully", Toast.LENGTH_SHORT).show();
                      FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification();
                  }
                  else {
                      task.addOnFailureListener(new OnFailureListener() {
                          @Override
                          public void onFailure(@NonNull Exception e) {
                              Toast.makeText(RegisterActivity.this, e.getMessage(),Toast.LENGTH_LONG).show();
                          }
                      });
                  }
                  progressDialog.dismiss();

                }
            });
            Toast.makeText(RegisterActivity.this, "Verification link has been sent to your email", Toast.LENGTH_SHORT).show();
        }

        }
    }

