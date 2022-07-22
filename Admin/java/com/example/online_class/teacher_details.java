package com.example.online_class;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class teacher_details extends AppCompatActivity {
    ImageView img;
    TextView sid,name,cno,mail,doj,qual;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_details);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().setTitle("Details...");
        img=(ImageView)findViewById(R.id.imageView);
        sid=(TextView)findViewById(R.id.stid);
        name=(TextView)findViewById(R.id.name);
        cno=(TextView)findViewById(R.id.cno);
        mail=(TextView)findViewById(R.id.mail);
        doj=(TextView)findViewById(R.id.doj);
        qual=(TextView)findViewById(R.id.qual);
        Glide.with(teacher_details.this).load(getIntent().getStringExtra("url")).into(img);
        sid.setText(getIntent().getStringExtra("tid"));
        name.setText(getIntent().getStringExtra("name"));
        cno.setText(getIntent().getStringExtra("cno"));
        mail.setText(getIntent().getStringExtra("mail"));
        doj.setText(getIntent().getStringExtra("doj"));
        qual.setText(getIntent().getStringExtra("qual"));
    }
}