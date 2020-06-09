package com.example.lapochat;
import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    private TextInputLayout reg_display;
    private TextInputLayout reg_email;
    private TextInputLayout reg_password;
    private Button reg_create_btn;
    private FirebaseAuth mAuth;
    private Toolbar mtoolbar;
    private ProgressDialog mRegProgress;
    private DatabaseReference mDatabase;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        reg_display=(TextInputLayout)findViewById(R.id.reg_display);
        reg_email=(TextInputLayout)findViewById(R.id.reg_email);
        reg_password=(TextInputLayout)findViewById(R.id.reg_pass);
        reg_create_btn=(Button) findViewById(R.id.reg_create_btn);

        mtoolbar=(Toolbar)findViewById(R.id.register_toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mRegProgress=new ProgressDialog(this);


        mAuth = FirebaseAuth.getInstance();


        reg_create_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String display_name= reg_display.getEditText().getText().toString();
                String email= reg_email.getEditText().getText().toString();
                String password= reg_password.getEditText().getText().toString();

                if(!TextUtils.isEmpty(display_name)||!TextUtils.isEmpty(email)||!TextUtils.isEmpty(password)){

                    mRegProgress.setTitle("Registering User");
                    mRegProgress.setMessage("please wait while your account is created");
                    mRegProgress.setCanceledOnTouchOutside(false);
                    mRegProgress.show();

                    register_user(display_name,email,password);
                }




            }
        });

    }

    private void register_user(final String display_name, String email, String password) {

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){

                    FirebaseUser current_user=FirebaseAuth.getInstance().getCurrentUser();
                    String uid=current_user.getUid();

                    mDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                    HashMap<String,String> user_map=new HashMap<>();
                    user_map.put("name", display_name);
                    user_map.put("status","hi iam using lappo chat");
                    user_map.put("image","default");
                    user_map.put("thumb_image","default");

                    mDatabase.setValue(user_map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){

                    mRegProgress.dismiss();
                    Intent mainIntent=new Intent(RegisterActivity.this,MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mainIntent);
                    finish();

                            }
                        }
                    });




                }
                else {
                    mRegProgress.hide();
                    Toast.makeText(RegisterActivity.this,"you got some error",Toast.LENGTH_LONG).show();

                }
            }
                });

    }
}
