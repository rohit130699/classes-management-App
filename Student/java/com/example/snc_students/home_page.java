package com.example.snc_students;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
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
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class home_page extends AppCompatActivity {
    String umail,upass,unam,pimglink,uname;
    CircleImageView cimg;
    TextView name,mail;
    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    NavigationView nav_view;
    View headerview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        SharedPreferences sh = getSharedPreferences("credentials",getApplicationContext().MODE_PRIVATE);
        uname = sh.getString("uname",null);
        pimglink = sh.getString("pimg",null);
        //Toast.makeText(getApplicationContext(),"Home Page called..",Toast.LENGTH_SHORT).show();
        // drawer layout instance to toggle the menu icon to open
        // drawer and back button to close drawer
        drawerLayout = findViewById(R.id.my_drawer_layout);
        nav_view = findViewById(R.id.nav_v);
        headerview = nav_view.getHeaderView(0);
        cimg = (CircleImageView)headerview.findViewById(R.id.pimg);
        name = (TextView)headerview.findViewById(R.id.name);
        mail = (TextView)headerview.findViewById(R.id.mail);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);

        // pass the Open and Close toggle for the drawer layout listener
        // to toggle the button
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        // to make the Navigation drawer icon always appear on the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Glide.with(getApplicationContext()).load(pimglink).into(cimg);
        name.setText(uname+"(Std:"+sh.getString("ustd",null)+")");
        mail.setText(sh.getString("umail",null));

        getSupportFragmentManager().beginTransaction().replace(R.id.container,new home_frag()).commit();
        nav_view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            Fragment temp;
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.home:
                        temp = new home_frag();
                        //Toast.makeText(getApplicationContext(),"Home",Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.pdfs:
                        temp = new pdf_frag();
                        //Toast.makeText(getApplicationContext(),"Pdfs",Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.videos:
                        temp = new video_frag();
                        //Toast.makeText(getApplicationContext(),"Videos",Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.receipts:
                        temp = new receipt_frag();
                        //Toast.makeText(getApplicationContext(),"Receipts",Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.logout:
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
                        NotificationManager mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                        mNotificationManager.cancelAll();
                        Toast.makeText(getApplicationContext(),"Logged Out!!",Toast.LENGTH_SHORT).show();
                        Intent inten = new Intent(home_page.this, login_activity.class);
                        startActivity(inten);
                        finish();
                        break;
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.container,temp).commit();
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //NotificationManager mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        //mNotificationManager.cancelAll();
        SharedPreferences shp = getSharedPreferences("credentials",MODE_PRIVATE);
        if(shp.contains("uid")) {
        SharedPreferences sh = getSharedPreferences("credentials",getApplicationContext().MODE_PRIVATE);
        umail = sh.getString("umail",null);
        unam = sh.getString("uname",null);
        upass = sh.getString("passwd",null);
        Query query = FirebaseDatabase.getInstance().getReference().child("students").orderByChild("mail").equalTo(umail);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount() > 0){
                    for(DataSnapshot rec: dataSnapshot.getChildren()) {
                        if(!rec.child("pass").getValue().toString().equals(upass) || !rec.child("name").getValue().toString().equals(unam)){
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
                            Intent inten = new Intent(home_page.this, login_activity.class);
                            inten.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
                    Intent inten = new Intent(home_page.this, login_activity.class);
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
            Intent loginintent = new Intent(home_page.this,login_activity.class);
            startActivity(loginintent);
            finish();
        }
    }
}