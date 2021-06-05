package com.sandysmitsks.sociohub.sociohub;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlogActivity extends AppCompatActivity {

   private Toolbar mToolbar;
    private RecyclerView postList;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private  DatabaseReference PostsRef,LikesRef;
    Boolean LikeChecker = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog);

       mAuth = FirebaseAuth.getInstance();
       currentUserID = mAuth.getCurrentUser().getUid();

        mToolbar=(Toolbar) findViewById(R.id.blog_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Blog");
//        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef =FirebaseDatabase.getInstance().getReference().child("Posts");
        LikesRef= FirebaseDatabase.getInstance().getReference().child("Likes");
        postList = (RecyclerView) findViewById(R.id.all_user_post_list);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager =new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);


        DisplayAllusersPost();

    }

    private void DisplayAllusersPost()
    {
        Query SortPostsInDesendingOrder = PostsRef.orderByChild("counter");
        FirebaseRecyclerAdapter<Blog,PostsViewHolder>firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Blog, PostsViewHolder>
                (
                        Blog.class,R.layout.all_posts_layout,PostsViewHolder.class,SortPostsInDesendingOrder
                ) {
            @Override
            protected void populateViewHolder(PostsViewHolder viewHolder, Blog model, int position)
            {
               final String PostKey = getRef(position).getKey();

                viewHolder.setFullname(model.getFullname());
                viewHolder.setTime(model.getTime());
                viewHolder.setDate(model.getDate());
                viewHolder.setDescription(model.getDescription());
                viewHolder.setProfileimage(getApplicationContext(),model.getProfileimage());
                viewHolder.setPostimage(getApplicationContext(),model.getPostimage());

                viewHolder.setLikeButtonStatus(PostKey);

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent clickPostIntent = new Intent(BlogActivity.this,ClickPostActivity.class);
                        clickPostIntent.putExtra("PostKey",PostKey);
                        startActivity(clickPostIntent);
                    }
                });
                viewHolder.CommentPostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent commentPostIntent = new Intent(BlogActivity.this,CommentsActivity.class);
                        commentPostIntent.putExtra("PostKey",PostKey);
                        startActivity(commentPostIntent);
                    }
                });

                viewHolder.LikePostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        LikeChecker=true;

                        LikesRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (LikeChecker.equals(true))
                                {
                                    if (dataSnapshot.child(PostKey).hasChild(currentUserID))
                                    {
                                        LikesRef.child(PostKey).child(currentUserID).removeValue();
                                        LikeChecker=false;
                                    }
                                    else
                                    {
                                        LikesRef.child(PostKey).child(currentUserID).setValue(true);
                                        LikeChecker=false;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });

            }
        };
        postList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class PostsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        ImageButton LikePostButton, CommentPostButton;
        TextView DisplayNoOfLikes;
        int countLikes;
        String currentUserID;
        DatabaseReference LikesRef;
        public PostsViewHolder(View itemView) {
            super(itemView);
            mView=itemView;

            LikePostButton = (ImageButton)mView.findViewById(R.id.like_button);
            CommentPostButton = (ImageButton)mView.findViewById(R.id.comment_button);
            DisplayNoOfLikes = (TextView)mView.findViewById(R.id.display_no_of_likes);

            LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        public void setLikeButtonStatus(final String PostKey)
        {
            LikesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                   if (dataSnapshot.child(PostKey).hasChild(currentUserID))
                   {
                       countLikes=(int) dataSnapshot.child(PostKey).getChildrenCount();
                       LikePostButton.setImageResource(R.drawable.like);
                       DisplayNoOfLikes.setText((Integer.toString(countLikes)+(" Likes")));
                   }
                   else
                       {
                           countLikes=(int) dataSnapshot.child(PostKey).getChildrenCount();
                           LikePostButton.setImageResource(R.drawable.dislike);
                           DisplayNoOfLikes.setText((Integer.toString(countLikes)+(" Likes")));
                       }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }


        public void setFullname(String fullname)
        {
            TextView username = (TextView)mView.findViewById(R.id.post_user_name);
            username.setText(fullname);
        }
        public void setProfileimage(Context ctx ,String profileimage)
        {
            CircleImageView image = (CircleImageView)mView.findViewById(R.id.post_profile_image);
            Picasso.with(ctx).load(profileimage).into(image);
        }
        public void setTime(String time)
        {
            TextView postTime = (TextView)mView.findViewById(R.id.post_time);
            postTime.setText("  "+time);
        }
        public void setDate(String date)
        {
            TextView postDate = (TextView)mView.findViewById(R.id.post_date);
            postDate.setText("  "+date);
        }
        public void setDescription(String description)
        {
            TextView postDescription = (TextView)mView.findViewById(R.id.post_description);
            postDescription.setText(description);
        }
        public void setPostimage(Context ctx1 ,String postimage)
        {
            ImageView postImage = (ImageView)mView.findViewById(R.id.post_image);
            Picasso.with(ctx1).load(postimage).into(postImage);
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.blog_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()== R.id.action_add)
        {
            startActivity(new Intent(BlogActivity.this,PostsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}
