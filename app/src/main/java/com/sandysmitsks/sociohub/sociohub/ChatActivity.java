package com.sandysmitsks.sociohub.sociohub;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
//import android.widget.Toolbar;
import  android.support.v7.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String messageRecieverId;
    private String messageRecieverName;

    private Toolbar ChatToolBar;

    private TextView userNameTitle;
    private TextView userLastSeen;
    private CircleImageView userChatProfileImage;

    private DatabaseReference rootRef;

    private ImageButton SelectImageButton;
    private ImageButton SendMessageButton;
    private EditText InputMessageText;

    private FirebaseAuth mAuth;
    private String messageSenderId;

    private RecyclerView userMessagesList;

    private final List<Messages> messagesList=new ArrayList<>();

    private LinearLayoutManager linearLayoutManager;

    private MessageAdapter messageAdapter;

    private static int Gallery_Pick=1;
    private StorageReference MessageImageStorageRef;

    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        messageRecieverId=getIntent().getExtras().get("visit_user_id").toString();
        messageRecieverName=getIntent().getExtras().get("user_name").toString();

        rootRef= FirebaseDatabase.getInstance().getReference();

        mAuth=FirebaseAuth.getInstance();
        messageSenderId =mAuth.getCurrentUser().getUid();
        MessageImageStorageRef = FirebaseStorage.getInstance().getReference().child("Messages_Pictures");

        ChatToolBar =(Toolbar) findViewById(R.id.chat_bar_layout);
        setSupportActionBar(ChatToolBar);

        loadingBar=new ProgressDialog(this);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater=(LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view=layoutInflater.inflate(R.layout.chat_custom_bar,null);

        actionBar.setCustomView(action_bar_view);

        userNameTitle = (TextView) findViewById(R.id.custom_profile_name);
        userLastSeen = (TextView) findViewById(R.id.custom_user_last_seen);
        userChatProfileImage = (CircleImageView) findViewById(R.id.custom_profile_image);

        userNameTitle.setText(messageRecieverName);

        SendMessageButton = (ImageButton) findViewById(R.id.send_message_btn);
        SelectImageButton = (ImageButton) findViewById(R.id.select_image);
        InputMessageText = (EditText) findViewById(R.id.input_message);

        messageAdapter=new MessageAdapter(messagesList);

        userMessagesList = (RecyclerView) findViewById(R.id.messages_list_of_users);

        linearLayoutManager= new LinearLayoutManager(this);

        userMessagesList.setHasFixedSize(true);

        userMessagesList.setLayoutManager(linearLayoutManager);

        userMessagesList.setAdapter(messageAdapter);

        FetchMessages();

        rootRef.child("Users").child(messageRecieverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String online=dataSnapshot.child("online").getValue().toString();
                final String userThumb = dataSnapshot.child("user_thumb_image").getValue().toString();

                Picasso.with(ChatActivity.this).load(userThumb).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.download)
                        .into(userChatProfileImage, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {

                                Picasso.with(ChatActivity.this).load(userThumb).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.download).
                                        into(userChatProfileImage);

                            }
                        });
                if(online.equals("true")){
                    userLastSeen.setText("Online");
                }else {
                    long last_seen = Long.parseLong(online);
                    String lastSeenDisplayTime = LastSeenTime.getTimeAgo(last_seen, getApplicationContext()).toString();
                    userLastSeen.setText(lastSeenDisplayTime);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessage();
            }
        });
        SelectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent=new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Pick);

            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Gallery_Pick && resultCode==RESULT_OK && data!=null)
        {
            loadingBar.setTitle("Sending Chat Image");
            loadingBar.setMessage("Plz Wait...!!!");
            loadingBar.show();

            Uri ImageUri = data.getData();
            final String message_sender_ref="Messages/"+messageSenderId+"/"+messageRecieverId;
            final String message_receiver_ref="Messages/"+messageRecieverId+"/"+messageSenderId;
            DatabaseReference user_message_key=rootRef.child("Messages").child(messageSenderId).child(messageRecieverId).push();
            final String message_push_id= user_message_key.getKey();

            StorageReference filePath = MessageImageStorageRef.child(message_push_id+".jpg");

            filePath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){


                        final String downloadUrl = task.getResult().getDownloadUrl().toString();

                        Map messageTextBody=new HashMap();


                        messageTextBody.put("message",downloadUrl);
                        messageTextBody.put("seen",false);
                        messageTextBody.put("type","image");
                        messageTextBody.put("time", ServerValue.TIMESTAMP);
                        messageTextBody.put("from",messageSenderId);


                        Map messageBodyDetails=new HashMap();

                        messageBodyDetails.put(message_sender_ref+"/"+message_push_id,messageTextBody);
                        messageBodyDetails.put(message_receiver_ref+"/"+message_push_id,messageTextBody);

                        rootRef.updateChildren(messageBodyDetails, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if(databaseError!=null){
                                    Log.d("Chat Log", "onComplete:"+databaseError.getMessage().toString());
                                }
                                InputMessageText.setText("");
                                loadingBar.dismiss();
                            }
                        });

                        Toast.makeText(ChatActivity.this, "Picture sent successfully", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                    else
                    {
                        Toast.makeText(ChatActivity.this, "Picture not sent successfully", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }

                }
            });

        }
    }

    private void FetchMessages() {
        rootRef.child("Messages").child(messageSenderId).child(messageRecieverId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages messages = dataSnapshot.getValue(Messages.class);
                messagesList.add(messages);
                messageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void SendMessage() {
        String messageText=InputMessageText.getText().toString();
        if(TextUtils.isEmpty(messageText)){
            Toast.makeText(ChatActivity.this, "Plz write your message", Toast.LENGTH_SHORT).show();
        }
        else{
            String message_sender_ref="Messages/"+messageSenderId+"/"+messageRecieverId;
            String message_receiver_ref="Messages/"+messageRecieverId+"/"+messageSenderId;

            DatabaseReference user_message_key=rootRef.child("Messages").child(messageSenderId).child(messageRecieverId).push();
            String message_push_id= user_message_key.getKey();

            Map messageTextBody=new HashMap();


            messageTextBody.put("message",messageText);
            messageTextBody.put("seen",false);
            messageTextBody.put("type","text");
            messageTextBody.put("time", ServerValue.TIMESTAMP);
            messageTextBody.put("from",messageSenderId);


            Map messageBodyDetails=new HashMap();

            messageBodyDetails.put(message_sender_ref+"/"+message_push_id,messageTextBody);
            messageBodyDetails.put(message_receiver_ref+"/"+message_push_id,messageTextBody);

            rootRef.updateChildren(messageBodyDetails, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if(databaseError!=null){
                        Log.d("Chat_log",databaseError.getMessage().toString());
                    }
                    InputMessageText.setText("");
                }
            });

        }


    }


}
