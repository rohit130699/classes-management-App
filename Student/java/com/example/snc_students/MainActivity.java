package com.example.snc_students;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {
    ImageView img1;
    TextView t1;
    Animation top,bottom;
    String umail,uname,upass;
    private static int splash = 2500;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        img1=(ImageView)findViewById(R.id.imgview_snc);
        t1=(TextView)findViewById(R.id.snc);

        top= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.mainlogoanima);
        bottom= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.textanima);

        img1.setAnimation(top);
        t1.setAnimation(bottom);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SharedPreferences shp = getSharedPreferences("credentials",MODE_PRIVATE);
                if(shp.contains("uid")){
                    //Toast.makeText(getApplicationContext(),"exists",Toast.LENGTH_SHORT).show();
                    SharedPreferences sh = getSharedPreferences("credentials",getApplicationContext().MODE_PRIVATE);
                    umail = sh.getString("umail",null);
                    upass = sh.getString("passwd",null);
                    uname = sh.getString("uname",null);
                    //Toast.makeText(getApplicationContext(),"mail"+umail,Toast.LENGTH_SHORT).show();
                    //Toast.makeText(getApplicationContext(),"pass"+upass,Toast.LENGTH_SHORT).show();
                    Query query = FirebaseDatabase.getInstance().getReference().child("students").orderByChild("mail").equalTo(umail);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.getChildrenCount() > 0){
                                for(DataSnapshot rec: dataSnapshot.getChildren()) {
                                    if(!rec.child("pass").getValue().toString().equals(upass) || !rec.child("name").getValue().toString().equals(uname)){
                                        @SuppressLint("WrongConstant")
                                        SharedPreferences sha = getSharedPreferences("credentials",MODE_APPEND);
                                        FirebaseMessaging.getInstance().unsubscribeFromTopic(sha.getString("ustd",""));
                                        SharedPreferences sh = getSharedPreferences("credentials",0);
                                        sh.edit().remove("uid").apply();
                                        sh.edit().remove("uname").apply();
                                        sh.edit().remove("umail").apply();
                                        sh.edit().remove("passwd").apply();
                                        sh.edit().remove("ustd").apply();
                                        sh.edit().remove("pimg").apply();
                                        Toast.makeText(getApplicationContext(),"Some details changed..Please login again!!",Toast.LENGTH_LONG).show();
                                        Intent inten = new Intent(MainActivity.this, login_activity.class);
                                        inten.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(inten);
                                        finish();
                                    }
                                    else{
                                        Intent inten = new Intent(MainActivity.this, home_page.class);
                                        startActivity(inten);
                                        finish();
                                    }
                                }
                            }
                            else{
                                @SuppressLint("WrongConstant")
                                SharedPreferences sha = getSharedPreferences("credentials",MODE_APPEND);
                                FirebaseMessaging.getInstance().unsubscribeFromTopic(sha.getString("ustd",""));
                                SharedPreferences sh = getSharedPreferences("credentials",0);
                                sh.edit().remove("uid").apply();
                                sh.edit().remove("uname").apply();
                                sh.edit().remove("umail").apply();
                                sh.edit().remove("passwd").apply();
                                sh.edit().remove("ustd").apply();
                                sh.edit().remove("pimg").apply();
                                Toast.makeText(getApplicationContext(),"User not Found..Contact to Classes Owner for further Details..!!",Toast.LENGTH_LONG).show();
                                Intent inten = new Intent(MainActivity.this, login_activity.class);
                                inten.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(inten);
                                finish();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                    });
                }
                else{
                    Intent loginintent = new Intent(MainActivity.this,login_activity.class);
                    startActivity(loginintent);
                    finish();
                }
                }
        },splash);
    }
}