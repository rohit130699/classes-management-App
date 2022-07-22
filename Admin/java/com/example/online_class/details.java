package com.example.online_class;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

public class details extends AppCompatActivity {
    ImageView img;
    TextView sid,name,std,cno,mail,doj,fees,pass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().setTitle("Details...");
        img=(ImageView)findViewById(R.id.imageView);
        sid=(TextView)findViewById(R.id.stid);
        name=(TextView)findViewById(R.id.name);
        std=(TextView)findViewById(R.id.std);
        cno=(TextView)findViewById(R.id.cno);
        mail=(TextView)findViewById(R.id.mail);
        doj=(TextView)findViewById(R.id.doj);
        fees=(TextView)findViewById(R.id.fees);
        pass=(TextView)findViewById(R.id.pass);
        Glide.with(details.this).load(getIntent().getStringExtra("url")).into(img);
        sid.setText(getIntent().getStringExtra("stid"));
        name.setText(getIntent().getStringExtra("name"));
        std.setText(getIntent().getStringExtra("std"));
        cno.setText(getIntent().getStringExtra("cno"));
        mail.setText(getIntent().getStringExtra("mail"));
        doj.setText(getIntent().getStringExtra("doj"));
        fees.setText("₹"+getIntent().getStringExtra("fees"));
        pass.setText(getIntent().getStringExtra("pass"));
    }
}