package com.example.online_class;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class view_receipts extends AppCompatActivity {
    RecyclerView recv;
    receiptadapter adapter;
    EditText et1;
    LinearLayout lout;
    ProgressDialog pdialog;
    ProgressBar pbar;
    boolean isOnTextChanged = false;
    ArrayList<receiptupload> itemlist;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_receipts);
        getSupportActionBar().hide();
        itemlist = new ArrayList<>();
        recv=(RecyclerView)findViewById(R.id.recv);
        et1=(EditText)findViewById(R.id.sid);
        lout=(LinearLayout)findViewById(R.id.ln1);
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
        adapter=new receiptadapter(options,this,pdialog,pbar,et1,lout);
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
            FirebaseRecyclerOptions<receiptupload> options =
                    new FirebaseRecyclerOptions.Builder<receiptupload>()
                            .setQuery(FirebaseDatabase.getInstance().getReference().child("receipt").orderByChild("stu_id").startAt(toString).endAt(toString+"\uf8ff"), receiptupload.class)
                            .build();
            adapter = new receiptadapter(options,this,pdialog,pbar,et1,lout);
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