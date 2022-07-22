package com.example.snc_students;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;

public class video_frag extends Fragment {
    Activity actv;
    RecyclerView recv;
    ProgressBar pbar;
    EditText et1;
    String loguserstd;
    videoadapter adapter;
    public video_frag() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView=inflater.inflate(R.layout.fragment_video_frag, container, false);
        actv = this.getActivity();
        pbar = (ProgressBar)rootView.findViewById(R.id.pbar);
        et1=(EditText)rootView.findViewById(R.id.sid);
        recv=(RecyclerView)rootView.findViewById(R.id.recv);

        recv.setLayoutManager(new LinearLayoutManager(getContext()));

        SharedPreferences sh = getActivity().getSharedPreferences("credentials",getContext().MODE_PRIVATE);
        loguserstd = sh.getString("ustd",null);
        //Toast.makeText(getContext(),"ddd "+loguserstd+" ddd", Toast.LENGTH_LONG).show();
        FirebaseRecyclerOptions<videoupload> options=
                new FirebaseRecyclerOptions.Builder<videoupload>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("videos").orderByChild("std").equalTo(loguserstd),videoupload.class)
                        .build();
        adapter=new videoadapter(options,pbar,getActivity());
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
                    //Toast.makeText(getContext(),"ddd "+editable.toString()+"_"+loguser+" ddd",Toast.LENGTH_LONG).show();
                    filter(editable.toString());
                }
            }
        });
        return rootView;
    }
    private void filter(String toString) {
        FirebaseRecyclerOptions<videoupload> options=
                new FirebaseRecyclerOptions.Builder<videoupload>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("videos").orderByChild("stu_filter").startAt(loguserstd+"_"+toString.toLowerCase()).endAt(loguserstd+"_"+toString.toLowerCase()+"\uf8ff"),videoupload.class)
                        .build();
        adapter = new videoadapter(options,pbar,getActivity());
        adapter.startListening();
        recv.setAdapter(adapter);
        adapter.notifyDataSetChanged();
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