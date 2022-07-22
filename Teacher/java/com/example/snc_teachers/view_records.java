package com.example.snc_teachers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class view_records extends AppCompatActivity {
    EditText sid;
    FloatingActionButton btn;
    ImageView img;
    TextView tv1, tv2, tv3, tv4, id, name, std, fees, rec, jan, feb, mar, apr, may, jun, jul, aug, sep, oct, nov, dec, Jan, Feb, Mar, Apr, May, Jun, July, Aug, Sep, Oct, Nov, Dec;
    String stuid,unam,ucno;//ststd,sfees,spimg,stuid;
    ProgressBar pb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_records);
        getSupportActionBar().setTitle("Records...");
        img = (ImageView) findViewById(R.id.imageView);
        pb = (ProgressBar) findViewById(R.id.simpleProgressBar);
        sid = (EditText) findViewById(R.id.sid);
        tv1 = (TextView) findViewById(R.id.tv1);
        tv2 = (TextView) findViewById(R.id.tv2);
        tv3 = (TextView) findViewById(R.id.tv3);
        tv4 = (TextView) findViewById(R.id.tv4);
        id = (TextView) findViewById(R.id.stuid);
        name = (TextView) findViewById(R.id.name);
        std = (TextView) findViewById(R.id.std);
        fees = (TextView) findViewById(R.id.fees);

        rec = (TextView) findViewById(R.id.rec);

        jan = (TextView) findViewById(R.id.jan);
        feb = (TextView) findViewById(R.id.feb);
        mar = (TextView) findViewById(R.id.mar);
        apr = (TextView) findViewById(R.id.apr);
        may = (TextView) findViewById(R.id.may);
        jun = (TextView) findViewById(R.id.jun);
        jul = (TextView) findViewById(R.id.july);
        aug = (TextView) findViewById(R.id.aug);
        sep = (TextView) findViewById(R.id.sep);
        oct = (TextView) findViewById(R.id.oct);
        nov = (TextView) findViewById(R.id.nov);
        dec = (TextView) findViewById(R.id.dec);

        Jan = (TextView) findViewById(R.id.Jan);
        Feb = (TextView) findViewById(R.id.Feb);
        Mar = (TextView) findViewById(R.id.Mar);
        Apr = (TextView) findViewById(R.id.Apr);
        May = (TextView) findViewById(R.id.May);
        Jun = (TextView) findViewById(R.id.Jun);
        July = (TextView) findViewById(R.id.July);
        Aug = (TextView) findViewById(R.id.Aug);
        Sep = (TextView) findViewById(R.id.Sep);
        Oct = (TextView) findViewById(R.id.Oct);
        Nov = (TextView) findViewById(R.id.Nov);
        Dec = (TextView) findViewById(R.id.Dec);

        btn = (FloatingActionButton) findViewById(R.id.search);

        img.setImageDrawable(null);
        tv1.setVisibility(View.GONE);
        tv2.setVisibility(View.GONE);
        tv3.setVisibility(View.GONE);
        tv4.setVisibility(View.GONE);
        id.setVisibility(View.GONE);
        name.setVisibility(View.GONE);
        std.setVisibility(View.GONE);
        fees.setVisibility(View.GONE);

        rec.setVisibility(View.GONE);

        jan.setVisibility(View.GONE);
        feb.setVisibility(View.GONE);
        mar.setVisibility(View.GONE);
        apr.setVisibility(View.GONE);
        may.setVisibility(View.GONE);
        jun.setVisibility(View.GONE);
        jul.setVisibility(View.GONE);
        aug.setVisibility(View.GONE);
        sep.setVisibility(View.GONE);
        oct.setVisibility(View.GONE);
        nov.setVisibility(View.GONE);
        dec.setVisibility(View.GONE);

        Jan.setVisibility(View.GONE);
        Feb.setVisibility(View.GONE);
        Mar.setVisibility(View.GONE);
        Apr.setVisibility(View.GONE);
        May.setVisibility(View.GONE);
        Jun.setVisibility(View.GONE);
        July.setVisibility(View.GONE);
        Aug.setVisibility(View.GONE);
        Sep.setVisibility(View.GONE);
        Oct.setVisibility(View.GONE);
        Nov.setVisibility(View.GONE);
        Dec.setVisibility(View.GONE);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pb.setVisibility(View.VISIBLE);
                if(!"".equals(sid.getText().toString().trim())) {
                    stuid = sid.getText().toString().trim();
                    Query query = FirebaseDatabase.getInstance().getReference().child("students").orderByChild("id").equalTo(sid.getText().toString().trim());
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.getChildrenCount() > 0) {
                                Query query1 = FirebaseDatabase.getInstance().getReference().child("fees").orderByChild("id").equalTo(sid.getText().toString().trim());
                                query1.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                                        if(dataSnapshot1.getChildrenCount() > 0){
                                            pb.setVisibility(View.GONE);
                                            for (DataSnapshot rec : dataSnapshot.getChildren()) {
                                                tv1.setVisibility(View.VISIBLE);
                                                tv2.setVisibility(View.VISIBLE);
                                                tv3.setVisibility(View.VISIBLE);
                                                tv4.setVisibility(View.VISIBLE);
                                                id.setVisibility(View.VISIBLE);
                                                name.setVisibility(View.VISIBLE);
                                                std.setVisibility(View.VISIBLE);
                                                fees.setVisibility(View.VISIBLE);
                                                Glide.with(getApplicationContext()).load(rec.child("pimage").getValue().toString()).into(img);
                                                id.setText(sid.getText().toString().trim());
                                                name.setText(rec.child("name").getValue().toString());
                                                std.setText(rec.child("std").getValue().toString());
                                                fees.setText("₹ " + rec.child("fees").getValue().toString());
                                                sid.setText("");
                                            }
                                            for (DataSnapshot recp : dataSnapshot1.getChildren()) {
                                                rec.setVisibility(View.VISIBLE);
                                                jan.setVisibility(View.VISIBLE);
                                                feb.setVisibility(View.VISIBLE);
                                                mar.setVisibility(View.VISIBLE);
                                                apr.setVisibility(View.VISIBLE);
                                                may.setVisibility(View.VISIBLE);
                                                jun.setVisibility(View.VISIBLE);
                                                jul.setVisibility(View.VISIBLE);
                                                aug.setVisibility(View.VISIBLE);
                                                sep.setVisibility(View.VISIBLE);
                                                oct.setVisibility(View.VISIBLE);
                                                nov.setVisibility(View.VISIBLE);
                                                dec.setVisibility(View.VISIBLE);
                                                Jan.setVisibility(View.VISIBLE);
                                                Feb.setVisibility(View.VISIBLE);
                                                Mar.setVisibility(View.VISIBLE);
                                                Apr.setVisibility(View.VISIBLE);
                                                May.setVisibility(View.VISIBLE);
                                                Jun.setVisibility(View.VISIBLE);
                                                July.setVisibility(View.VISIBLE);
                                                Aug.setVisibility(View.VISIBLE);
                                                Sep.setVisibility(View.VISIBLE);
                                                Oct.setVisibility(View.VISIBLE);
                                                Nov.setVisibility(View.VISIBLE);
                                                Dec.setVisibility(View.VISIBLE);
                                                Jan.setText(recp.child("jan").getValue().toString());
                                                Feb.setText(recp.child("feb").getValue().toString());
                                                Mar.setText(recp.child("mar").getValue().toString());
                                                Apr.setText(recp.child("apr").getValue().toString());
                                                May.setText(recp.child("may").getValue().toString());
                                                Jun.setText(recp.child("jun").getValue().toString());
                                                July.setText(recp.child("july").getValue().toString());
                                                Aug.setText(recp.child("aug").getValue().toString());
                                                Sep.setText(recp.child("sept").getValue().toString());
                                                Oct.setText(recp.child("oct").getValue().toString());
                                                Nov.setText(recp.child("nov").getValue().toString());
                                                Dec.setText(recp.child("dec").getValue().toString());
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Toast.makeText(getApplicationContext(),"Search Unsuccessfull !!",Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            else{
                                Toast.makeText(getApplicationContext(),"No such records found !!",Toast.LENGTH_SHORT).show();
                                pb.setVisibility(View.GONE);
                                img.setImageDrawable(null);
                                tv1.setVisibility(View.GONE);
                                tv2.setVisibility(View.GONE);
                                tv3.setVisibility(View.GONE);
                                tv4.setVisibility(View.GONE);
                                id.setVisibility(View.GONE);
                                name.setVisibility(View.GONE);
                                std.setVisibility(View.GONE);
                                fees.setVisibility(View.GONE);

                                rec.setVisibility(View.GONE);

                                jan.setVisibility(View.GONE);
                                feb.setVisibility(View.GONE);
                                mar.setVisibility(View.GONE);
                                apr.setVisibility(View.GONE);
                                may.setVisibility(View.GONE);
                                jun.setVisibility(View.GONE);
                                jul.setVisibility(View.GONE);
                                aug.setVisibility(View.GONE);
                                sep.setVisibility(View.GONE);
                                oct.setVisibility(View.GONE);
                                nov.setVisibility(View.GONE);
                                dec.setVisibility(View.GONE);

                                Jan.setVisibility(View.GONE);
                                Feb.setVisibility(View.GONE);
                                Mar.setVisibility(View.GONE);
                                Apr.setVisibility(View.GONE);
                                May.setVisibility(View.GONE);
                                Jun.setVisibility(View.GONE);
                                July.setVisibility(View.GONE);
                                Aug.setVisibility(View.GONE);
                                Sep.setVisibility(View.GONE);
                                Oct.setVisibility(View.GONE);
                                Nov.setVisibility(View.GONE);
                                Dec.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(getApplicationContext(),"Search Unsuccessfull !!",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else{
                    Toast.makeText(getApplicationContext(),"No id entered...",Toast.LENGTH_LONG).show();
                    pb.setVisibility(View.GONE);
                }
            }
        });
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
                                Intent inten = new Intent(view_records.this, verify_user.class);
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
                        Intent inten = new Intent(view_records.this, verify_user.class);
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
            Intent loginintent = new Intent(view_records.this,verify_user.class);
            startActivity(loginintent);
            finish();
        }
    }
}