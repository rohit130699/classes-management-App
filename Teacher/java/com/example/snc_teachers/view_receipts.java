package com.example.snc_teachers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class view_receipts extends AppCompatActivity {
    RecyclerView recv;
    receiptadapter adapter;
    EditText et1;
    ProgressDialog pdialog;
    ProgressBar pbar;
    String unam,ucno;
    boolean isOnTextChanged = false;
    ArrayList<receiptupload> itemlist;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_receipts);
        getSupportActionBar().hide();
        itemlist = new ArrayList<>();
        recv=(RecyclerView)findViewById(R.id.recv);
        et1=(EditText)findViewById(R.id.sid);
        recv.setLayoutManager(new LinearLayoutManager(this));
        pbar = (ProgressBar)findViewById(R.id.pbar);
        pdialog = new ProgressDialog(this);
        pdialog.setCanceledOnTouchOutside(false);
        pdialog.setTitle("Please Wait !!");
        pdialog.setMessage("Deleting...");
        FirebaseRecyclerOptions<receiptupload> options=
                new FirebaseRecyclerOptions.Builder<receiptupload>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("receipt"),receiptupload.class)
                        .build();
        adapter=new receiptadapter(options,this,pdialog,pbar,et1);
        recv.setAdapter(adapter);

        et1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.toString().equals("")){
                    filter("");
                }
                else {
                    filter(editable.toString());
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
                                Intent inten = new Intent(view_receipts.this, verify_user.class);
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
                        Intent inten = new Intent(view_receipts.this, verify_user.class);
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
            Intent loginintent = new Intent(view_receipts.this,verify_user.class);
            startActivity(loginintent);
            finish();
        }
    }

    private void filter(String toString) {
        FirebaseRecyclerOptions<receiptupload> options =
                new FirebaseRecyclerOptions.Builder<receiptupload>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("receipt").orderByChild("stu_id").startAt(toString).endAt(toString+"\uf8ff"), receiptupload.class)
                        .build();
        adapter = new receiptadapter(options,this,pdialog,pbar,et1);
        adapter.startListening();
        recv.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }
    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}