package com.example.snc_teachers;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ftab3 extends Fragment {
    FloatingActionButton fbtn;
    RecyclerView recv;
    videoadapter vadapt;
    ViewPager vpg;
    ProgressDialog pdialog;
    EditText et1;
    String loguser;
    ProgressBar pbar;
    TabLayout tbl;
    LinearLayout lout;
    public ftab3(ViewPager viewpg, TabLayout tblout) {
        // Required empty public constructor
        vpg=viewpg;
        tbl=tblout;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView=inflater.inflate(R.layout.fragment_ftab3, container, false);
        pbar = (ProgressBar)rootView.findViewById(R.id.pbar);
        et1=(EditText)rootView.findViewById(R.id.sid);
        lout=(LinearLayout)rootView.findViewById(R.id.ln1);
        pdialog = new ProgressDialog(this.getContext());
        pdialog.setCanceledOnTouchOutside(false);
        pdialog.setTitle("Please Wait !!");
        pdialog.setMessage("Deleting...");
        fbtn=(FloatingActionButton)rootView.findViewById(R.id.add);
        fbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(),browse_video.class));
            }
        });
        recv=(RecyclerView)rootView.findViewById(R.id.recv);
        recv.setLayoutManager(new LinearLayoutManager(getContext()));


        SharedPreferences sh = getActivity().getSharedPreferences("credentials",getContext().MODE_PRIVATE);
        loguser = sh.getString("uname",null);
        FirebaseRecyclerOptions<videoupload> options=
                new FirebaseRecyclerOptions.Builder<videoupload>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("videos").orderByChild("uploader").equalTo(loguser),videoupload.class)
                        .build();
        vadapt=new videoadapter(options,this.getActivity(),fbtn,pdialog,vpg,tbl,pbar,et1);
        recv.setAdapter(vadapt);

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
        return rootView;
    }

    private void filter(String toString) {
        FirebaseRecyclerOptions<videoupload> options=
                new FirebaseRecyclerOptions.Builder<videoupload>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("videos").orderByChild("filter_search").startAt(loguser+"_"+toString.toLowerCase()).endAt(loguser+"_"+toString.toLowerCase()+"\uf8ff"),videoupload.class)
                        .build();
        vadapt = new videoadapter(options,this.getActivity(),fbtn,pdialog,vpg,tbl,pbar,et1);
        vadapt.startListening();
        recv.setAdapter(vadapt);
        vadapt.notifyDataSetChanged();
    }

    @Override
    public void onStart() {
        super.onStart();
        vadapt.startListening();
    }
    @Override
    public void onStop() {
        super.onStop();
        vadapt.stopListening();
    }
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.search:
                if(lout.getVisibility() == View.GONE){
                    lout.setVisibility(View.VISIBLE);
                }
                else{
                    lout.setVisibility(View.GONE);
                }
        }
        return false;
    }
}