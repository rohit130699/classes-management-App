package com.example.online_class;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;

public class
show_data extends AppCompatActivity {
    RecyclerView recv;
    FloatingActionButton fadd;
    ProgressBar pbar;
    ProgressDialog pdialog;
    EditText et1;
    LinearLayout lout;
    myadapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_data);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //getSupportActionBar().setTitle("Students");
        getSupportActionBar().hide();
        pbar = (ProgressBar)findViewById(R.id.pbar);
        et1=(EditText)findViewById(R.id.sid);
        lout=(LinearLayout)findViewById(R.id.ln1);
        pdialog = new ProgressDialog(this);
        pdialog.setCanceledOnTouchOutside(false);
        pdialog.setTitle("Please Wait !!");
        pdialog.setMessage("Deleting...");
        File fold = new File(this.getExternalFilesDir("/"), "files");
        if (fold.exists()) {
            String[] entries = fold.list();
            for (String s : entries) {
                File currentfile = new File(fold.getPath(), s);
                currentfile.delete();
            }
            fold.delete();
        }
        fadd=(FloatingActionButton)findViewById(R.id.add);
        fadd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(show_data.this,student_registration.class));
            }
        });
        recv=(RecyclerView)findViewById(R.id.recv);
        recv.setLayoutManager(new LinearLayoutManager(this));

        FirebaseRecyclerOptions<dataholder> options=
                new FirebaseRecyclerOptions.Builder<dataholder>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("students"),dataholder.class)
                        .build();
        adapter=new myadapter(options,this,fadd,pdialog,pbar,et1,lout);
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

    private void filter(String toString) {
        FirebaseRecyclerOptions<dataholder> options=
                new FirebaseRecyclerOptions.Builder<dataholder>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("students").orderByChild("search").startAt(toString.toLowerCase()).endAt(toString.toLowerCase()+"\uf8ff"),dataholder.class)
                        .build();
        adapter = new myadapter(options,this,fadd,pdialog,pbar,et1,lout);
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

   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.searchmenu,menu);
        MenuItem item=menu.findItem(R.id.search);
        SearchView searchview=(SearchView)item.getActionView();
        searchview.setMaxWidth(Integer.MAX_VALUE);
        searchview.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                processsearch(s);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                processsearch(s);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }
    private void processsearch(String s){
        FirebaseRecyclerOptions<dataholder> options=
                new FirebaseRecyclerOptions.Builder<dataholder>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("students").orderByChild("search").startAt(s.toLowerCase()).endAt(s.toLowerCase()+"\uf8ff"),dataholder.class)
                        .build();
        adapter=new myadapter(options,this,fadd,pdialog,pbar);
        adapter.startListening();
        recv.setAdapter(adapter);

    }*/
}