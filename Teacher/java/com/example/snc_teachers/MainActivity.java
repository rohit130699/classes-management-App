package com.example.snc_teachers;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    ImageView img1;
    TextView t1;
    Animation top,bottom;
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
                Intent loginintent = new Intent(MainActivity.this,verify_user.class);
                startActivity(loginintent);
                finish();
            }
        },splash);
    }
}