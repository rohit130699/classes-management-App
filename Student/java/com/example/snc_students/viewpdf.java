package com.example.snc_students;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.net.URLEncoder;
import java.util.List;

public class viewpdf extends AppCompatActivity {
    String umail,upass,uname;
    ProgressBar pb;
    WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewpdf);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        pb = (ProgressBar)findViewById(R.id.simpleProgressBar);
        pb.setVisibility(View.VISIBLE);
        webView=(WebView)findViewById(R.id.viewpdf);
        SharedPreferences shp = getSharedPreferences("credentials",MODE_PRIVATE);
        if(shp.contains("uid")) {
            SharedPreferences sh = getSharedPreferences("credentials", getApplicationContext().MODE_PRIVATE);
            umail = sh.getString("umail", null);
            uname = sh.getString("uname", null);
            upass = sh.getString("passwd", null);
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
                                Intent inten = new Intent(viewpdf.this, home_page.class);
                                inten.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(inten);
                                finish();
                            }
                            else{
                                webView.getSettings().setJavaScriptEnabled(true);
                                //webView.setLayerType(View.LAYER_TYPE_HARDWARE,null);

                                String filename=getIntent().getStringExtra("filename");
                                String fileurl=getIntent().getStringExtra("fileurl");

                                //Toast.makeText(this,"name:"+filename,Toast.LENGTH_SHORT).show();
                                //Toast.makeText(this,"url:"+fileurl,Toast.LENGTH_SHORT).show();

                                webView.setWebViewClient(new WebViewClient()
                                {
                                    @Override
                                    public void onPageStarted(WebView view, String url, Bitmap favicon) {
                                        super.onPageStarted(view, url, favicon);
                                        pb.setVisibility(View.GONE);
                                    }

                                    @Override
                                    public void onPageFinished(WebView view, String url) {
                                        super.onPageFinished(view, url);
                                    }
                                });
                                String url="";
                                try{
                                    url= URLEncoder.encode(fileurl,"UTF-8");
                                }
                                catch(Exception ex){
                                }
                                webView.loadUrl("http://docs.google.com/gview?embedded=true&url=" + url);
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
                        Intent inten = new Intent(viewpdf.this, home_page.class);
                        inten.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(inten);
                        finish();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });
        }
        else{   //Generally this part will not happen...
            Toast.makeText(getApplicationContext(),"User not Found..Contact to Classes Owner for further Details..!!",Toast.LENGTH_LONG).show();
            Intent loginintent = new Intent(viewpdf.this,home_page.class);
            startActivity(loginintent);
            finish();
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(isTaskRoot()){
            Intent loginintent = new Intent(viewpdf.this,home_page.class);
            //loginintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(loginintent);
            finish();
        }

    }
}