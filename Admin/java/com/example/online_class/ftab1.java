package com.example.online_class;

import android.app.ProgressDialog;
import android.content.Intent;
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

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.FirebaseDatabase;

public class ftab1 extends Fragment {
    FloatingActionButton fbtn;
    RecyclerView recv;
    ProgressDialog pdialog;
    ProgressBar pbar;
    EditText et1;
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

        FirebaseRecyclerOptions<pdfupload> options=
                new FirebaseRecyclerOptions.Builder<pdfupload>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("documents"),pdfupload.class)
                        .build();
        adapter=new pdfadapter(options,this.getActivity(),fbtn,pdialog,vpg,tbl,pbar,et1);
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
        return rootView;
    }
    private void filter(String toString) {
        FirebaseRecyclerOptions<pdfupload> options=
                new FirebaseRecyclerOptions.Builder<pdfupload>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("documents").orderByChild("search").startAt(toString.toLowerCase()).endAt(toString.toLowerCase()+"\uf8ff"),pdfupload.class)
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