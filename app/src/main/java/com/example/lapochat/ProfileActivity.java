package com.example.lapochat;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;

public class ProfileActivity extends AppCompatActivity {
    private TextView mProfileName,mProfileStatus,mProfileFriendsCount;
    private ImageView mProfileImage;
    private Button mProfileSendReqBtn;
    private DatabaseReference mUserDatabase;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;
    private ProgressDialog mProgressDialog;
    private String mCurrent_state;
    private FirebaseUser mCurrent_user;
    private Button mProfileDeclineBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id=getIntent().getStringExtra("user_id");

        mUserDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendReqDatabase=FirebaseDatabase.getInstance().getReference().child("Friend_Req");
        mFriendDatabase=FirebaseDatabase.getInstance().getReference().child("friends");

        mCurrent_user= FirebaseAuth.getInstance().getCurrentUser();

        mProfileImage=(ImageView)findViewById(R.id.profile_image);
        mProfileName=(TextView)findViewById(R.id.profile_displayname);
        mProfileStatus=(TextView)findViewById(R.id.profile_status);
        mProfileFriendsCount=(TextView)findViewById(R.id.profile_totalfriends);
        mProfileSendReqBtn=(Button)findViewById(R.id.profile_send_req_btn);
        mProfileDeclineBtn=(Button)findViewById(R.id.profile_decline_btn);

        mCurrent_state="not_friends";

        mProgressDialog =new ProgressDialog(this);
        mProgressDialog.setTitle("Loading User Data");
        mProgressDialog.setMessage("please wait while we load user data");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();


        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String display_name=dataSnapshot.child("name").getValue().toString();
                String status=dataSnapshot.child("status").getValue().toString();
                String image=dataSnapshot.child("image").getValue().toString();
                mProfileName.setText(display_name);
                mProfileStatus.setText(status);
                Picasso.with(ProfileActivity.this).load(image).into(mProfileImage);

                mFriendReqDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(user_id)){

                            String req_type=dataSnapshot.child(user_id).child("request_type").getValue().toString();

                            if (req_type.equals("received")){

                                mCurrent_state="req_received";
                                mProfileSendReqBtn.setText("ACCEPT FRIEND REQUEST");

                                mProfileDeclineBtn.setVisibility(View.VISIBLE);
                                mProfileDeclineBtn.setEnabled(true);

                            }else if (req_type.equals("sent")){

                                mCurrent_state="req_state";
                                mProfileSendReqBtn.setText("CANCEL FRIEND REQUEST");

                                mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                mProfileDeclineBtn.setEnabled(false);

                            }
                            mProgressDialog.dismiss();
                        }
                        else {

                            mFriendDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(user_id)) {
                                        mCurrent_state = "friends";
                                        mProfileSendReqBtn.setText("UNFRIEND THIS PERSON");

                                        mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                        mProfileDeclineBtn.setEnabled(false);
                                    }
                                    mProgressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    mProgressDialog.dismiss();

                                }
                            });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mProfileSendReqBtn.setEnabled(false);
                if (mCurrent_state.equals("not_friends")){

                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).child("request_type")
                            .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()){

                                mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).child("request_type")
                                        .setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        mProfileSendReqBtn.setEnabled(true);
                                        mCurrent_state="req_sent";
                                        mProfileSendReqBtn.setText("CANCEL FRIEND REQUEST");

                                        mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                        mProfileDeclineBtn.setEnabled(false);

                                        //Toast.makeText(ProfileActivity.this,"Request sent successfully",Toast.LENGTH_SHORT).show();

                                    }
                                });
                            }else {

                                Toast.makeText(ProfileActivity.this,"Failed Sending Request",Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                }


                if (mCurrent_state.equals("req_sent")){

                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mProfileSendReqBtn.setEnabled(true);
                                    mCurrent_state="not_friends";
                                    mProfileSendReqBtn.setText("SEND FRIEND REQUEST");

                                    mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                    mProfileDeclineBtn.setEnabled(false);
                                }
                            });
                        }
                    });

                }

                if (mCurrent_state.equals("req_received")){

                    final String currentdate= DateFormat.getDateTimeInstance().format(new Date());
                    mFriendDatabase.child(mCurrent_user.getUid()).child(user_id).setValue(currentdate)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mFriendDatabase.child(user_id).child(mCurrent_user.getUid()).setValue(currentdate)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    mProfileSendReqBtn.setEnabled(true);
                                                                    mCurrent_state="friends";
                                                                    mProfileSendReqBtn.setText("UNFRIEND THIS PERSON");

                                                                    mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                                                    mProfileDeclineBtn.setEnabled(false);
                                                                }
                                                            });
                                                        }
                                                    });

                                                }
                                            });
                                }
                            });


                }

            }
        });


    }
}
