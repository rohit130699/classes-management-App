package com.example.snc_teachers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
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

import java.net.URLEncoder;

public class viewpdf extends AppCompatActivity {
    ProgressBar pb;
    WebView webView;
    String unam,ucno;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewpdf);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        pb = (ProgressBar)findViewById(R.id.simpleProgressBar);
        pb.setVisibility(View.VISIBLE);

        webView=(WebView)findViewById(R.id.viewpdf);
        webView.getSettings().setJavaScriptEnabled(true);
        //webView.setLayerType(View.LAYER_TYPE_HARDWARE,null);

        String filename=getIntent().getStringExtra("filename");
        String fileurl=getIntent().getStringExtra("fileurl");

        //Toast.makeText(this,fileurl,Toast.LENGTH_SHORT).show();

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
                                Intent inten = new Intent(viewpdf.this, verify_user.class);
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
                        Intent inten = new Intent(viewpdf.this, verify_user.class);
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
            Intent loginintent = new Intent(viewpdf.this,verify_user.class);
            startActivity(loginintent);
            finish();
        }
    }
}