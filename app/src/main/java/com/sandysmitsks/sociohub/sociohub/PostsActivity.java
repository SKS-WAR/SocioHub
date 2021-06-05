package com.sandysmitsks.sociohub.sociohub;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class PostsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton SelectPostImage;
    private Button UpdatePostButton;
    private EditText PostDescription;
    private static final int Gallery_Pick=1;
    private Uri ImageUri;
    private String Description;
    private  StorageReference PostsImagesReference;
    private  String saveCurrentDate,saveCurrentTime,postRandomName,downloadUrl,current_user_id;
    private DatabaseReference UsersRef,PostRef;
    private ProgressDialog loadingBar;
    private FirebaseAuth mAuth;
    private long countPost= 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts);

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        mToolbar = (Toolbar) findViewById(R.id.blog_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle("Update Blog");
        SelectPostImage =(ImageButton)findViewById(R.id.select_post_image);
        UpdatePostButton=(Button)findViewById(R.id.update_post_button);
        PostDescription =(EditText)findViewById(R.id.post_description);
        mAuth=FirebaseAuth.getInstance();
        current_user_id=mAuth.getUid();
        PostsImagesReference = FirebaseStorage.getInstance().getReference();
        loadingBar = new ProgressDialog(this);


        SelectPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenGallery();
            }
        });
        UpdatePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidatePostInfo();
            }
        });

    }

    private void ValidatePostInfo()
    {
        Description = PostDescription.getText().toString();
        if (ImageUri == null)
        {
            Toast.makeText(this,"Please Select an Image",Toast.LENGTH_LONG).show();
        }

        else if (TextUtils.isEmpty(Description))
        {
            Toast.makeText(this,"Please give a description about the post",Toast.LENGTH_LONG).show();
        }
        else
        {
            loadingBar.setTitle("Adding new Post");
            loadingBar.setMessage("Please Wait,while we are updating your post");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
            StoringImageToFirebaseStorage();
        }
    }

    private void StoringImageToFirebaseStorage()
    {
        Calendar callForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
        saveCurrentDate = currentDate.format(callForDate.getTime());

        Calendar callForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(callForDate.getTime());

        postRandomName = saveCurrentDate + saveCurrentTime;
        StorageReference filepath = PostsImagesReference.child("Post Images").child(ImageUri.getLastPathSegment() + postRandomName + ".jpg");
        filepath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
               if (task.isSuccessful())
               {
                   downloadUrl =task.getResult().getDownloadUrl().toString();

                   Toast.makeText(PostsActivity.this,"Post Uploaded Successfully",Toast.LENGTH_LONG).show();

                   SavingPostInformationToDatabase();
               }
               else
                   {
                       String msg=task.getException().getMessage();
                       Toast.makeText(PostsActivity.this,"Error Occured"+ msg,Toast.LENGTH_LONG).show();
                   }
            }
        });
    }

    private void SavingPostInformationToDatabase()
    {
        PostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    countPost=dataSnapshot.getChildrenCount();
                }
                else
                {
                    countPost= 0;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        UsersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    String userFullName = dataSnapshot.child("user_name").getValue().toString();
                    String userProfileImage = dataSnapshot.child("user_image").getValue().toString();

                    HashMap postsMap =new HashMap();
                    postsMap.put("uid",current_user_id);
                    postsMap.put("date",saveCurrentDate);
                    postsMap.put("time",saveCurrentTime);
                    postsMap.put("description",Description);
                    postsMap.put("postimage",downloadUrl);
                    postsMap.put("profileimage",userProfileImage);
                    postsMap.put("fullname",userFullName);
                    postsMap.put("counter",countPost);
                    PostRef.child(current_user_id + postRandomName).updateChildren(postsMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                           if (task.isSuccessful())
                           {
                               loadingBar.dismiss();
                               Intent BlogIntent = new Intent(PostsActivity.this,BlogActivity.class);
                               BlogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                               startActivity(BlogIntent);
                               finish();
                               Toast.makeText(PostsActivity.this,"New Post is Updated Successfully",Toast.LENGTH_LONG).show();

                           }
                           else
                           {

                               Toast.makeText(PostsActivity.this,"Error Occured While Updating",Toast.LENGTH_LONG).show();
                               loadingBar.dismiss();
                           }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void OpenGallery()
    {
        Intent galleryIntend = new Intent();
        galleryIntend.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntend.setType("image/*");
        startActivityForResult(galleryIntend,Gallery_Pick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==Gallery_Pick && resultCode== RESULT_OK && data!=null)
        {
            ImageUri = data.getData();
            SelectPostImage.setImageURI(ImageUri);
        }
    }
}
