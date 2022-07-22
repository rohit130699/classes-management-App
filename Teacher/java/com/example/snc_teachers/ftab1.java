package com.example.snc_teachers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import static android.content.Context.MODE_APPEND;

public class ftab1 extends Fragment {
    Activity actv;
    FloatingActionButton fbtn;
    RecyclerView recv;
    ProgressDialog pdialog;
    ProgressBar pbar;
    EditText et1;
    String loguser;
    ViewPager vpg;
    TabLayout tbl;
    pdfadapter adapter;
    LinearLayout lout;

    public ftab1(ViewPager viewpg, TabLayout tblout) {
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
        // Inflate the layout for this fragment
        View rootView=inflater.inflate(R.layout.fragment_ftab1, container, false);
        actv = this.getActivity();
        pbar = (ProgressBar)rootView.findViewById(R.id.pbar);
        fbtn=(FloatingActionButton)rootView.findViewById(R.id.addpdf);
        et1=(EditText)rootView.findViewById(R.id.sid);
        lout=(LinearLayout)rootView.findViewById(R.id.ln1);
        recv=(RecyclerView)rootView.findViewById(R.id.recv);
        pdialog = new ProgressDialog(this.getContext());
        pdialog.setCanceledOnTouchOutside(false);
        pdialog.setTitle("Please Wait !!");
        pdialog.setMessage("Deleting...");
        fbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(),browse_pdf.class));
            }
        });
        recv.setLayoutManager(new LinearLayoutManager(getContext()));

        SharedPreferences sh = getActivity().getSharedPreferences("credentials",getContext().MODE_PRIVATE);
        loguser = sh.getString("uname",null);
        //Toast.makeText(getContext(),"ddd "+loguser+" ddd",Toast.LENGTH_LONG).show();
        FirebaseRecyclerOptions<pdfupload> options=
                new FirebaseRecyclerOptions.Builder<pdfupload>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("documents").orderByChild("uploader").equalTo(loguser),pdfupload.class)
                        .build();
        adapter=new pdfadapter(options,actv,fbtn,pdialog,vpg,tbl,pbar,et1);
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
        FirebaseRecyclerOptions<pdfupload> options=
                new FirebaseRecyclerOptions.Builder<pdfupload>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("documents").orderByChild("filter_search").startAt(loguser+"_"+toString.toLowerCase()).endAt(loguser+"_"+toString.toLowerCase()+"\uf8ff"),pdfupload.class)
                        .build();
        adapter = new pdfadapter(options,this.getActivity(),fbtn,pdialog,vpg,tbl,pbar,et1);
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