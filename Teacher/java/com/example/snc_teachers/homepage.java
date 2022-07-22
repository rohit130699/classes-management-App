package com.example.snc_teachers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class homepage extends AppCompatActivity {
    Button b1,b2,b3,b4;
    String sid,no,loguser_no,user,orguser,sname,scno,user_name,unam,ucno,loguser;
    AlertDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        SharedPreferences sh = getSharedPreferences("credentials",getApplicationContext().MODE_PRIVATE);
        loguser = sh.getString("uname",null);
        Toast.makeText(getApplicationContext(),"User :"+loguser,Toast.LENGTH_LONG).show();

        b1=(Button)findViewById(R.id.buttonfees);
        b2=(Button)findViewById(R.id.buttongoal);
        b3=(Button)findViewById(R.id.studymaterial);
        b4=(Button)findViewById(R.id.feesrecords);

        /*no = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        loguser_no = no.substring(3);
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
                        Toast.makeText(getApplicationContext(),"User :"+user,Toast.LENGTH_LONG).show();
                    }
                    SharedPreferences shp = getSharedPreferences("credentials",MODE_PRIVATE);
                    SharedPreferences.Editor myedit = shp.edit();
                    myedit.putString("uname",user);
                    myedit.putString("cno",loguser_no);
                    myedit.putString("username",orguser);
                    //myedit.putString("uname","Rohit Pani");
                    myedit.commit();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });*/

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(homepage.this,stu_data.class));
            }
        });
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(homepage.this,view_receipts.class));
            }
        });
        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(homepage.this,tablayout.class));
            }
        });
        b4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(homepage.this, view_records.class));
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.exam_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                SharedPreferences sh = getSharedPreferences("credentials",0);
                sh.edit().remove("uname").apply();
                sh.edit().remove("cno").apply();
                sh.edit().remove("username").apply();
                Toast.makeText(getApplicationContext(),"Logged Out!!",Toast.LENGTH_SHORT).show();
                startActivity(new Intent(homepage.this, verify_user.class));
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences shp = getSharedPreferences("credentials",MODE_PRIVATE);
        if(shp.contains("uname")) {
            SharedPreferences sh = getSharedPreferences("credentials",getApplicationContext().MODE_PRIVATE);
            unam = sh.getString("username",null);
            ucno = sh.getString("cno",null);
            Query query = FirebaseDatabase.getInstance().getReference().child("teachers").orderByChild("cno").equalTo(ucno);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getChildrenCount() > 0){
                        for(DataSnapshot rec: dataSnapshot.getChildren()) {
                            if(!rec.child("name").getValue().toString().equals(unam)){
                                @SuppressLint("WrongConstant")
                                SharedPreferences sh = getSharedPreferences("credentials",0);
                                sh.edit().remove("uname").apply();
                                sh.edit().remove("username").apply();
                                sh.edit().remove("cno").apply();
                                Toast.makeText(getApplicationContext(),"Some details changed..Please login again!!",Toast.LENGTH_LONG).show();
                                Intent inten = new Intent(homepage.this, verify_user.class);
                                inten.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(inten);
                                finish();
                            }
                        }
                    }
                    else{
                        @SuppressLint("WrongConstant")
                        SharedPreferences sh = getSharedPreferences("credentials",0);
                        sh.edit().remove("uname").apply();
                        sh.edit().remove("username").apply();
                        sh.edit().remove("cno").apply();
                        Toast.makeText(getApplicationContext(),"User not Found..Contact to Classes Owner for further Details..!!",Toast.LENGTH_LONG).show();
                        Intent inten = new Intent(homepage.this, verify_user.class);
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
            Intent loginintent = new Intent(homepage.this,verify_user.class);
            startActivity(loginintent);
            finish();
        }
    }
}