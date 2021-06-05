package com.sandysmitsks.sociohub.sociohub;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllUsersActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView allUsersList;
    private DatabaseReference alldatabaseUserreference;
    private EditText SearchInputText;
    private ImageButton SearchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);
        mToolbar = (Toolbar)findViewById(R.id.all_users_app_bar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Search For Friends");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        SearchButton =(ImageButton) findViewById(R.id.search_people_button);
        SearchInputText =(EditText) findViewById(R.id.search_input_text);
        allUsersList = (RecyclerView)findViewById(R.id.all_users_list);

        alldatabaseUserreference = FirebaseDatabase.getInstance().getReference().child("Users");
        alldatabaseUserreference.keepSynced(true);

        allUsersList.setHasFixedSize(true);
        allUsersList.setLayoutManager(new LinearLayoutManager(this));

        SearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                String searchUserName= SearchInputText.getText().toString();
                if (TextUtils.isEmpty(searchUserName))
                {
                   Toast.makeText(AllUsersActivity.this,"Please enter a name to search....",Toast.LENGTH_LONG).show();
                }
                else {
                    SearchForFriends(searchUserName);
                }
            }
        });
    }

    private void SearchForFriends(String searchUserName )
    {
        Toast.makeText(this,"Searching...",Toast.LENGTH_SHORT).show();
        Query searchFriends = alldatabaseUserreference.orderByChild("user_name").startAt(searchUserName).endAt(searchUserName + "\uf8ff");
        super.onStart();
        FirebaseRecyclerAdapter<AllUsers, AllUsersViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<AllUsers, AllUsersViewHolder>(AllUsers.class, R.layout.all_user_display_layout,
                AllUsersViewHolder.class, searchFriends) {
            @Override
            protected void populateViewHolder(AllUsersViewHolder viewHolder, AllUsers model, final int position) {
                viewHolder.setUser_name(model.getUser_name());
                viewHolder.setUser_status(model.getUser_status());
                viewHolder.setUser_thumb_image(getApplicationContext(),model.getUser_thumb_image());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visit_user_id = getRef(position).getKey();

                        Intent profileIntend = new Intent(AllUsersActivity.this,ProfileActivity.class);
                        profileIntend.putExtra("visit_user_id",visit_user_id);
                        startActivity(profileIntend);

                    }
                });

            }
        };
        allUsersList.setAdapter(firebaseRecyclerAdapter);
    }
    public static class AllUsersViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        public AllUsersViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setUser_name(String user_name)
        {
            TextView name = (TextView) mView.findViewById(R.id.all_users_username);
            name.setText(user_name);
        }

        public void setUser_status(String user_status)
        {
            TextView status = (TextView) mView.findViewById(R.id.all_users_status);
            status.setText(user_status);
        }
        public void setUser_thumb_image(final Context ctx ,final String user_thumb_image)
        {
           final CircleImageView thumb_image = (CircleImageView) mView.findViewById(R.id.all_user_profile_image);

           Picasso.with(ctx).load(user_thumb_image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.download).into(thumb_image, new Callback() {
              @Override
                public void onSuccess()
               {
               }

              @Override
             public void onError()
             {
                 Picasso.with(ctx).load(user_thumb_image).placeholder(R.drawable.download).into(thumb_image);

              }
          });
        }
    }
}
