package com.example.online_class;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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
//import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class otp_test extends AppCompatActivity {
    EditText otp;
    Button b2;
    String phone_no;
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