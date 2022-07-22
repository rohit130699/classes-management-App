package com.example.snc_teachers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public class otp_test extends AppCompatActivity {
    EditText otp;
    Button b2;
    String phone_no,loguser_no,sid,user,orguser;
    String otpid;
    FirebaseAuth mauth;
    ProgressBar pb,pb2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_test);
        pb = (ProgressBar)findViewById(R.id.simpleProgressBar);
        pb2 = (ProgressBar)findViewById(R.id.simpleProgressBar2);
        otp = (EditText)findViewById(R.id.otp);
        b2 = (Button) findViewById(R.id.button2);
        pb.setVisibility(View.VISIBLE);
        phone_no = getIntent().getStringExtra("mobile").toString();
        mauth = FirebaseAuth.getInstance();

        //no = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        loguser_no = phone_no.substring(3);
        Query query = FirebaseDatabase.getInstance().getReference().child("teachers").orderByChild("cno").equalTo(loguser_no);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Toast.makeText(getApplicationContext(),"Inside onDatachange()",Toast.LENGTH_LONG).show();
                if(dataSnapshot.getChildrenCount() > 0){
                    //loggedin_user = dataSnapshot.child("name").getValue().toString();
                    for(DataSnapshot rec: dataSnapshot.getChildren()) {
                        sid = rec.child("id").getValue().toString();
                        orguser = rec.child("name").getValue().toString();
                        user = orguser +"(Teacher Id:"+ rec.child("id").getValue().toString()+")";
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        initiateotp();
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(otp.getText().toString().isEmpty()){
                    Toast.makeText(otp_test.this,"Blank Field cannot be processed", Toast.LENGTH_SHORT).show();
                }
                else if(otp.getText().toString().length() != 6) {
                    Toast.makeText(otp_test.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
                }
                else{
                    pb2.setVisibility(View.VISIBLE);
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(otpid,otp.getText().toString());
                    signInWithCredentials1(credential);
                }
            }
        });
    }
    private void initiateotp() {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phone_no,
                30,
                TimeUnit.SECONDS,
                this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        otpid = s;
                        pb.setVisibility(View.GONE);
                    }
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                        signInWithCredentials(phoneAuthCredential);
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        Toast.makeText(otp_test.this,e.getMessage(), Toast.LENGTH_SHORT).show();
                        pb.setVisibility(View.GONE);
                    }
                }
        );
    }

    private void signInWithCredentials(PhoneAuthCredential credential) {
        mauth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            SharedPreferences shp = getSharedPreferences("credentials",MODE_PRIVATE);
                            SharedPreferences.Editor myedit = shp.edit();
                            myedit.putString("uname",user);
                            myedit.putString("cno",loguser_no);
                            myedit.putString("username",orguser);
                            //myedit.putString("uname","Rohit Pani");
                            myedit.commit();
                            pb.setVisibility(View.GONE);
                            startActivity(new Intent(otp_test.this,homepage.class));
                            finish();
                        }
                        else{
                            pb.setVisibility(View.GONE);
                            Toast.makeText(otp_test.this,"Login Unsuccessful", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void signInWithCredentials1(PhoneAuthCredential credential) {
        mauth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            SharedPreferences shp = getSharedPreferences("credentials",MODE_PRIVATE);
                            SharedPreferences.Editor myedit = shp.edit();
                            myedit.putString("uname",user);
                            myedit.putString("cno",loguser_no);
                            myedit.putString("username",orguser);
                            //myedit.putString("uname","Rohit Pani");
                            myedit.commit();
                            pb2.setVisibility(View.GONE);
                            startActivity(new Intent(otp_test.this,homepage.class));
                            finish();
                        }
                        else{
                            pb2.setVisibility(View.GONE);
                            Toast.makeText(otp_test.this,"Login Unsuccessful", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}