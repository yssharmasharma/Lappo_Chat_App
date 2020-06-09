package com.example.lapochat;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {
    private RecyclerView mUserslist;
    private Toolbar mToolbar;
    private DatabaseReference mUsersDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        mToolbar=(Toolbar)findViewById(R.id.users_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("Users");
        mUsersDatabase= FirebaseDatabase.getInstance().getReference().child("Users");

        mUserslist=(RecyclerView)findViewById(R.id.mUserslist);
        mUserslist.setHasFixedSize(true);
        mUserslist.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Users> options =
                new FirebaseRecyclerOptions.Builder<Users>()
                        .setQuery(mUsersDatabase, Users.class)
                        .build();

        FirebaseRecyclerAdapter<Users, UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(options)
        {

            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_single_layout, parent, false);
                return new UsersViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull UsersViewHolder holder, int position, @NonNull Users model) {
                holder.setName(model.getName());
               holder.setRole(model.getStatus());
                holder.setImage(model.getImage());

                final String user_id=getRef(position).getKey();


                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent profileintent=new Intent(UsersActivity.this,ProfileActivity.class);
                        profileintent.putExtra("user_id",user_id);
                        startActivity(profileintent);
                    }
                });
            }
//            @Override
//            public int getItemCount() {
//                return mUserslist.size();
//            }

            /*@Override
            protected void  populateViewHolder(UsersViewHolder usersViewHolder,Users users,int i){

                usersViewHolder.setDisplayName(users.getName());

            }*/
        };
        mUserslist.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }
    @Override

    protected void onStop() {
        super.onStop();
    }

            public class UsersViewHolder extends RecyclerView.ViewHolder {
                View mView;

                public UsersViewHolder(View itemView){

                    super(itemView);
                    mView=itemView;
                }
                public void setName(String name){
                    TextView userNameView=(TextView)mView.findViewById(R.id.users_single_name);
                    userNameView.setText(name);
                }
                public void setRole(String role){
                    TextView userRole = mView.findViewById(R.id.users_single_status);
                    userRole.setText(role);
                }

                public void setImage(String image){
                    CircleImageView userImage = mView.findViewById(R.id.users_single_image);
                    Picasso.with(UsersActivity.this).load(image).into(userImage);
                }
            }


    }


