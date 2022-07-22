package com.example.snc_students;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;

public class receipt_frag extends Fragment {
    Activity actv;
    RecyclerView recv;
    ProgressBar pbar;
    String loguserid;
    receiptadapter adapter;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootview = inflater.inflate(R.layout.fragment_receipt_frag, container, false);
        actv = this.getActivity();
        pbar = (ProgressBar)rootview.findViewById(R.id.pbar);
        recv=(RecyclerView)rootview.findViewById(R.id.recv);

        recv.setLayoutManager(new LinearLayoutManager(getContext()));

        SharedPreferences sh = getActivity().getSharedPreferences("credentials",getContext().MODE_PRIVATE);
        loguserid = sh.getString("uid",null);
        //Toast.makeText(getContext(),"ddd "+loguserstd+" ddd", Toast.LENGTH_LONG).show();
        FirebaseRecyclerOptions<receiptupload> options=
                new FirebaseRecyclerOptions.Builder<receiptupload>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("receipt").orderByChild("stu_id").equalTo(loguserid),receiptupload.class)
                        .build();
        adapter=new receiptadapter(options,pbar,getActivity());
        recv.setAdapter(adapter);
        return  rootview;
    }
    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }
    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}