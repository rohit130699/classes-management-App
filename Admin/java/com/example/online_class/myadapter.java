package com.example.online_class;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class myadapter extends FirebaseRecyclerAdapter<dataholder,myadapter.myviewholder> {
    AlertDialog dialog;
    ProgressDialog pdialog;
    ProgressBar pbar;
    ActionMode actm;
    FloatingActionButton fbtn;
    EditText et;
    LinearLayout lout;
    Activity activity;
    MainViewModel mainViewModel;
    MenuItem upd;
    String imagedel,last_s,last="",stname="",stmail="",stcno="",stpass="" ,ststd="";
    Context c;
    boolean iscount = false;
    boolean isEnabled = false;
    boolean isupd = false;
    boolean isAllSelected = false;
    int delcount,delimgcount,delrec,delrecipt,delfee,k,pos;
    ArrayList<String> selectedlist = new ArrayList<>();
    ArrayList<String> totalleft = new ArrayList<>();
    ArrayList<Integer> poslist = new ArrayList<>();
    ArrayList<String> iscontains_rec = new ArrayList<>();
    ArrayList<String> isnotcontains_rec = new ArrayList<>();
    ArrayList<String> reclist = new ArrayList<>();
    ArrayList<String> recidlist = new ArrayList<>();
    public myadapter(@NonNull FirebaseRecyclerOptions<dataholder> options,Activity activity,FloatingActionButton fbtn,ProgressDialog pdialg,ProgressBar pbar,EditText et,LinearLayout lout) {
        super(options);
        this.pbar = pbar;
        this.activity = activity;
        this.fbtn = fbtn;
        this.pdialog = pdialg;
        this.et = et;
        this.lout = lout;
        FirebaseDatabase.getInstance().getReference().child("students").orderByChild("id")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getChildrenCount() == 0){
                            pbar.setVisibility(View.GONE);
                            AlertDialog alert = new AlertDialog.Builder(activity).create();
                            alert.setTitle("No data Found..");
                            alert.setMessage("There is certainly no data existing !!");
                            alert.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    alert.dismiss();
                                }
                            });
                            alert.show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
    }

    @Override
    protected void onBindViewHolder(@NonNull myviewholder holder,final int position, @NonNull dataholder model) {
        //pbar.setVisibility(View.VISIBLE);
        //Toast.makeText(holder.img.getContext(),"OnBind Called...",Toast.LENGTH_SHORT).show();
        c = holder.img.getContext();
        if(getItemCount() > 0){
            pbar.setVisibility(View.GONE);
        }
        holder.name.setText(model.getName());
        holder.id.setText("Id:"+model.getId());
        Glide.with(holder.img.getContext()).load(model.getPimage()).into(holder.img);
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(!isEnabled){
                    ActionMode.Callback callback = new ActionMode.Callback() {
                        @Override
                        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                            delcount = 0;delimgcount = 0;delfee=0;delrec=0;delrecipt=0;k=0;pos = -1;last_s="";
                            lout.setVisibility(View.GONE);
                            MenuInflater menuInflater = actionMode.getMenuInflater();
                            menuInflater.inflate(R.menu.delmenu,menu);
                            MenuItem men = menu.findItem(R.id.menu_sel_all);
                            upd = menu.findItem(R.id.menu_upd);
                            if(!et.getText().toString().equals("")){
                                men.setEnabled(false);
                                Drawable drawable = ResourcesCompat.getDrawable(c.getResources(),R.drawable.ic_baseline_select_all_24,null);
                                drawable = DrawableCompat.wrap(drawable);
                                DrawableCompat.setTint(drawable,Color.LTGRAY);
                                men.setIcon(drawable);
                            }
                            else{
                                men.setEnabled(true);
                                Drawable drawable = ResourcesCompat.getDrawable(c.getResources(),R.drawable.ic_baseline_select_all_24,null);
                                drawable = DrawableCompat.wrap(drawable);
                                DrawableCompat.setTint(drawable,Color.WHITE);
                                men.setIcon(drawable);
                            }
                            fbtn.setVisibility(View.GONE);
                            actm = actionMode;
                            FirebaseDatabase.getInstance().getReference().child("students").orderByChild("id")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for(DataSnapshot ds : dataSnapshot.getChildren()){
                                                if(!totalleft.contains(ds.child("id").getValue().toString())){
                                                    totalleft.add(ds.child("id").getValue().toString());
                                                }}
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                                    });
                            return true;
                        }

                        @Override
                        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                            isEnabled = true;
                            checklist(holder);
                            mainViewModel.getText().observe((LifecycleOwner) holder.card.getContext(),
                                    new Observer<String>() {
                                        @Override
                                        public void onChanged(String s) {
                                            actionMode.setTitle(String.format("%s Selected" + "",s));
                                        }
                                    });
                            return true;
                        }

                        @Override
                        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                            int id = menuItem.getItemId();
                            switch(id){
                                case R.id.menu_del:
                                    if(selectedlist.size()>0) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.img.getContext());
                                        builder.setTitle("Delete Panel");
                                        builder.setMessage("Are you sure to delete " + selectedlist.size() + " items..all associated files with these items will also be deleted..??");
                                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                int size = selectedlist.size();
                                                int items = getItemCount();
                                                pdialog.show();
                                                last_s = selectedlist.get(selectedlist.size()-1);
                                                for(String s: selectedlist){
                                                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                                                    Query query = ref.child("receipt").orderByChild("stu_id").equalTo(s);
                                                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                            if(dataSnapshot.getChildrenCount() > 0){
                                                                iscontains_rec.add(s);
                                                                long count=0;long i=0;
                                                                count=dataSnapshot.getChildrenCount();
                                                                for(DataSnapshot ds:dataSnapshot.getChildren()){
                                                                    i=i+1;
                                                                    k=k+1;
                                                                    recidlist.add(ds.child("invoice_no").getValue().toString());
                                                                    reclist.add(ds.child("url").getValue().toString());
                                                                    if(s.equals(last_s) && count==i){
                                                                        //Toast.makeText(holder.img.getContext(),"Condition Satisfied",Toast.LENGTH_SHORT).show();
                                                                        iscount=true;
                                                                        executeAsyncDeleteRecords();
                                                                    }
                                                                }
                                                            }
                                                            else{
                                                                isnotcontains_rec.add(s);
                                                                if(s.equals(last_s)){
                                                                    executeAsyncDeleteRecords();
                                                                }
                                                            }
                                                            // }
                                                        }
                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                                                    });
                                                }
                                            }
                                        });
                                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                            }
                                        });
                                        builder.show();
                                    }
                                    else {
                                        Toast.makeText(holder.img.getContext(),"Nothing Selected...",Toast.LENGTH_SHORT).show();
                                    }
                                    break;
                                case R.id.menu_sel_all:
                                    if(selectedlist.size() == getItemCount()){
                                        isAllSelected = false;
                                        selectedlist.clear();
                                        poslist.clear();
                                        upd.setEnabled(true);
                                        Drawable drawable = ResourcesCompat.getDrawable(c.getResources(),R.drawable.ic_baseline_create_24,null);
                                        drawable = DrawableCompat.wrap(drawable);
                                        DrawableCompat.setTint(drawable,Color.WHITE);
                                        upd.setIcon(drawable);
                                        actionMode.finish();
                                    }
                                    else{
                                        isAllSelected = true;
                                        int i=0;
                                        selectedlist.clear();
                                        poslist.clear();
                                        for(String s:totalleft){
                                            if(!selectedlist.contains(s)){
                                                selectedlist.add(s);
                                            }}
                                        for(i=0;i<=getItemCount();i++){
                                            if(!poslist.contains(i)){
                                                poslist.add(i);
                                            }}
                                        upd.setEnabled(false);
                                        Drawable drawable = ResourcesCompat.getDrawable(c.getResources(),R.drawable.ic_baseline_create_24,null);
                                        drawable = DrawableCompat.wrap(drawable);
                                        DrawableCompat.setTint(drawable,Color.LTGRAY);
                                        upd.setIcon(drawable);
                                    }
                                    if(!(selectedlist.size() == 0)) {
                                        mainViewModel.setText(String.valueOf(selectedlist.size()));
                                    }
                                    notifyDataSetChanged();
                                    break;
                                case R.id.menu_upd:
                                    if (selectedlist.size() == 0) {
                                        Toast.makeText(holder.itemView.getContext(),"Nothing Selected..!",Toast.LENGTH_SHORT).show();
                                    }
                                    else if(selectedlist.size() > 1){
                                        Toast.makeText(holder.itemView.getContext(),"Updation can be performed only on one element at a time,more than one selected!!",Toast.LENGTH_LONG).show();
                                    }
                                    else {
                                        last = selectedlist.get(0);
                                        pos = poslist.get(0);
                                        //Toast.makeText(c,""+pos,Toast.LENGTH_SHORT).show();
                                        isupd = true;
                                        actionMode.finish();
                                    }
                                    break;
                            }
                            return true;
                        }

                        @Override
                        public void onDestroyActionMode(ActionMode actionMode) {
                            //Toast.makeText(holder.itemView.getContext(),"Destroyed...",Toast.LENGTH_SHORT).show();
                            isEnabled = false;
                            isAllSelected = false;
                            iscount = false;
                            //isupd = false;
                            selectedlist.clear();
                            reclist.clear();
                            totalleft.clear();
                            poslist.clear();
                            recidlist.clear();
                            actm = null;
                            lout.setVisibility(View.VISIBLE);
                            fbtn.setVisibility(View.VISIBLE);
                            notifyDataSetChanged();
                        }
                    };
                    ((AppCompatActivity) view.getContext()).startActionMode(callback);
                }
                else{
                    checklist(holder);
                }
                return true;
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isEnabled){
                    checklist(holder);
                }
                else{
                    Intent intent=new Intent(holder.card.getContext(),details.class);
                    intent.putExtra("stid",model.getId());
                    intent.putExtra("url",model.getPimage());
                    intent.putExtra("name",model.getName());
                    intent.putExtra("std",model.getStd());
                    intent.putExtra("cno",model.getCno());
                    intent.putExtra("mail",model.getMail());
                    intent.putExtra("doj",model.getDate());
                    intent.putExtra("fees",model.getFees());
                    intent.putExtra("pass",model.getPass());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    holder.card.getContext().startActivity(intent);
                }
            }
        });

        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if(isEnabled){
                    actm.finish();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        if(isupd){
            //Toast.makeText(c,"isupd set to true",Toast.LENGTH_SHORT).show();
            if(position == pos){
                stname = model.getName();
                stmail = model.getMail();
                stcno = model.getCno();
                stpass = model.getPass();
                ststd = model.getStd();
                /*Toast.makeText(c,""+stname,Toast.LENGTH_SHORT).show();
                Toast.makeText(c,""+stmail,Toast.LENGTH_SHORT).show();
                Toast.makeText(c,""+stcno,Toast.LENGTH_SHORT).show();
                Toast.makeText(c,""+stpass,Toast.LENGTH_SHORT).show();*/
                isupd = false;
                selectedlist.clear();
                poslist.clear();
                AlertDialog.Builder builder = new AlertDialog.Builder(holder.img.getContext());
                //builder.setTitle("Update Profile");
                LayoutInflater inflater = (LayoutInflater)holder.img.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                //View myview = getLayoutInflater().inflate(R.layout.dialoginter,null);
                View myview = inflater.inflate(R.layout.dialoginter,null);
                builder.setView(myview);
                dialog = builder.create();
                Window window = dialog.getWindow();
                window.setLayout(WindowManager.LayoutParams.FILL_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setGravity(Gravity.BOTTOM);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                ProgressBar pb = myview.findViewById(R.id.simpleProgressBar);
                EditText name = myview.findViewById(R.id.name);
                EditText mail = myview.findViewById(R.id.mail);
                EditText phone = myview.findViewById(R.id.phone);
                EditText pass = myview.findViewById(R.id.pass);
                Button btn = myview.findViewById(R.id.update);

                /*Toast.makeText(c,"stname:"+stname,Toast.LENGTH_SHORT).show();
                Toast.makeText(c,"stmail:"+stmail,Toast.LENGTH_SHORT).show();
                Toast.makeText(c,"stcno:"+stcno,Toast.LENGTH_SHORT).show();
                Toast.makeText(c,"spass:"+stpass,Toast.LENGTH_SHORT).show();*/

                name.setText(stname);
                mail.setText(stmail);
                phone.setText(stcno);
                pass.setText(stpass);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        btn.setEnabled(false);
                        if(name.getText().toString().trim().equals("") || mail.getText().toString().trim().equals("") || phone.getText().toString().trim().equals("") || pass.getText().toString().trim().equals("")){
                            Toast.makeText(c,"Some data is missing out !! Please fill out them..",Toast.LENGTH_SHORT).show();
                            btn.setEnabled(true);
                        }
                        else if(!name.getText().toString().trim().matches("^[A-Za-z ]+$")){
                            Toast.makeText(c,"Name entered might contain characters except letters..Please check!!",Toast.LENGTH_SHORT).show();
                            btn.setEnabled(true);
                        }
                        else if(!mail.getText().toString().trim().matches("^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$")){
                            Toast.makeText(c,"Mail entered is in incorrect format..Please check!!",Toast.LENGTH_SHORT).show();
                            btn.setEnabled(true);
                        }
                        else if(!phone.getText().toString().trim().matches("^[6789]\\d{9}$")){
                            Toast.makeText(c,"Contact No. entered is in incorrect format..Please check!!",Toast.LENGTH_SHORT).show();
                            btn.setEnabled(true);
                        }
                        else if(pass.getText().toString().trim().length() < 8){
                            Toast.makeText(c,"Length of password should be of mimimum 8 characters!!",Toast.LENGTH_SHORT).show();
                            btn.setEnabled(true);
                        }
                        else if(!pass.getText().toString().trim().matches("^(?=.*[0-9]).{1,}$")){
                            Toast.makeText(c,"Password must contain atleast one numeric character",Toast.LENGTH_SHORT).show();
                            btn.setEnabled(true);
                        }
                        else if(!pass.getText().toString().trim().matches("^(?=.*[!@#$%^&+=]).{1,}$")){
                            Toast.makeText(c,"Password must contain atleast one special character out of [!@#$%^&+=]",Toast.LENGTH_SHORT).show();
                            btn.setEnabled(true);
                        }
                        else{
                            if(name.getText().toString().trim().equals(stname) && mail.getText().toString().trim().equals(stmail) && phone.getText().toString().trim().equals(stcno) && pass.getText().toString().trim().equals(stpass)){
                            Toast.makeText(holder.img.getContext(),"No Updation Performed Yet!!",Toast.LENGTH_SHORT).show();
                            btn.setEnabled(true);
                            }
                            else{
                            if(!mail.getText().toString().trim().equals(stmail)){
                                FirebaseDatabase.getInstance().getReference().child("students").orderByChild("mail").equalTo(mail.getText().toString().trim())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.getChildrenCount() > 0){
                                                    Toast.makeText(holder.img.getContext(),"Already a student exists with given mail id!Please Change it !!",Toast.LENGTH_SHORT).show();
                                                    mail.setText("");
                                                    btn.setEnabled(true);
                                                }
                                                else{
                                                    pb.setVisibility(View.VISIBLE);
                                                    Map<String,Object> map = new HashMap<>();
                                                    map.put("name",name.getText().toString().trim());
                                                    map.put("mail",mail.getText().toString().trim());
                                                    map.put("cno",phone.getText().toString().trim());
                                                    map.put("pass",pass.getText().toString().trim());
                                                    map.put("search",name.getText().toString().trim().toLowerCase());

                                                    Map<String,Object> fees_map = new HashMap<>();
                                                    map.put("name",name.getText().toString().trim());
                                                    map.put("mail",mail.getText().toString().trim());
                                                    map.put("cno",phone.getText().toString().trim());
                                                    map.put("pass",pass.getText().toString().trim());
                                                    String upd = getRef(holder.getLayoutPosition()).getKey();
                                                    //Toast.makeText(holder.img.getContext(),""+upd,Toast.LENGTH_SHORT).show();
                                                    FirebaseDatabase.getInstance().getReference().child("students").child(upd).updateChildren(map)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    FirebaseDatabase.getInstance().getReference().child("fees").child(upd).updateChildren(fees_map)
                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void aVoid) {
                                                                                    FirebaseDatabase.getInstance().getReference().child("receipt").orderByChild("stu_id").equalTo(last)
                                                                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                                @Override
                                                                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                                    if(dataSnapshot.getChildrenCount()>0){
                                                                                                        for(DataSnapshot rec: dataSnapshot.getChildren()) {
                                                                                                            String inv = rec.child("invoice_no").getValue().toString();
                                                                                                            String ms = rec.child("timeinms").getValue().toString();
                                                                                                            String rec_name = inv+"_"+name.getText().toString().trim()+"_of_"+ststd+"_"+ms+".pdf";
                                                                                                            Map<String,Object> map1 = new HashMap<>();
                                                                                                            map1.put("rec_name",rec_name);
                                                                                                            rec.getRef().updateChildren(map1)
                                                                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                                        @Override
                                                                                                                        public void onSuccess(Void aVoid) {}
                                                                                                                    });
                                                                                                        }
                                                                                                        Toast.makeText(holder.img.getContext(),"Item Updated Successfully...",Toast.LENGTH_SHORT).show();
                                                                                                        pb.setVisibility(View.GONE);
                                                                                                        btn.setEnabled(true);
                                                                                                        dialog.dismiss();
                                                                                                    }
                                                                                                    else{
                                                                                                        Toast.makeText(holder.img.getContext(),"Item Updated Successfully...",Toast.LENGTH_SHORT).show();
                                                                                                        pb.setVisibility(View.GONE);
                                                                                                        btn.setEnabled(true);
                                                                                                        dialog.dismiss();
                                                                                                    }
                                                                                                }

                                                                                                @Override
                                                                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                                                                    Toast.makeText(holder.img.getContext(),"Item Updation Partially Successfull because of Network Issues...",Toast.LENGTH_SHORT).show();
                                                                                                    pb.setVisibility(View.GONE);
                                                                                                    btn.setEnabled(true);
                                                                                                    dialog.dismiss();
                                                                                                }
                                                                                            });

                                                                                }
                                                                            })
                                                                            .addOnFailureListener(new OnFailureListener() {
                                                                                @Override
                                                                                public void onFailure(@NonNull Exception e) {
                                                                                    Toast.makeText(holder.img.getContext(),"Item Updation Failed...",Toast.LENGTH_SHORT).show();
                                                                                    pb.setVisibility(View.GONE);
                                                                                    btn.setEnabled(true);
                                                                                    dialog.dismiss();
                                                                                }
                                                                            });
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Toast.makeText(holder.img.getContext(),"Item Updation Failed...",Toast.LENGTH_SHORT).show();
                                                                    pb.setVisibility(View.GONE);
                                                                    btn.setEnabled(true);
                                                                    dialog.dismiss();
                                                                }
                                                            });
                                                }
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                Toast.makeText(holder.img.getContext(), "Some Error Occurred!!Please try Again...", Toast.LENGTH_LONG).show();
                                                btn.setEnabled(true);
                                            }
                                        });
                            }
                            else{
                                pb.setVisibility(View.VISIBLE);
                                Map<String,Object> map = new HashMap<>();
                                map.put("name",name.getText().toString().trim());
                                map.put("mail",mail.getText().toString().trim());
                                map.put("cno",phone.getText().toString().trim());
                                map.put("pass",pass.getText().toString().trim());
                                map.put("search",name.getText().toString().trim().toLowerCase());

                                Map<String,Object> fees_map = new HashMap<>();
                                map.put("name",name.getText().toString().trim());
                                map.put("mail",mail.getText().toString().trim());
                                map.put("cno",phone.getText().toString().trim());
                                map.put("pass",pass.getText().toString().trim());

                                String upd = getRef(holder.getLayoutPosition()).getKey();
                                //Toast.makeText(holder.img.getContext(),""+upd,Toast.LENGTH_SHORT).show();
                                FirebaseDatabase.getInstance().getReference().child("students").child(upd).updateChildren(map)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                FirebaseDatabase.getInstance().getReference().child("fees").child(upd).updateChildren(fees_map)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                FirebaseDatabase.getInstance().getReference().child("receipt").orderByChild("stu_id").equalTo(last)
                                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                if(dataSnapshot.getChildrenCount()>0){
                                                                                    for(DataSnapshot rec: dataSnapshot.getChildren()) {
                                                                                        String inv = rec.child("invoice_no").getValue().toString();
                                                                                        String ms = rec.child("timeinms").getValue().toString();
                                                                                        Map<String,Object> map1 = new HashMap<>();
                                                                                        String rec_name = inv+"_"+name.getText().toString().trim()+"_of_"+ststd+"_"+ms+".pdf";
                                                                                        map1.put("rec_name",rec_name);
                                                                                        rec.getRef().updateChildren(map1)
                                                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onSuccess(Void aVoid) { }
                                                                                                });
                                                                                    }
                                                                                    Toast.makeText(holder.img.getContext(),"Item Updated Successfully...",Toast.LENGTH_SHORT).show();
                                                                                    pb.setVisibility(View.GONE);
                                                                                    btn.setEnabled(true);
                                                                                    dialog.dismiss();
                                                                                }
                                                                                else{
                                                                                    Toast.makeText(holder.img.getContext(),"Item Updated Successfully...",Toast.LENGTH_SHORT).show();
                                                                                    pb.setVisibility(View.GONE);
                                                                                    btn.setEnabled(true);
                                                                                    dialog.dismiss();
                                                                                }
                                                                            }

                                                                            @Override
                                                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                                                Toast.makeText(holder.img.getContext(),"Item Updation Partially Successfull because of Network Issues...",Toast.LENGTH_SHORT).show();
                                                                                pb.setVisibility(View.GONE);
                                                                                btn.setEnabled(true);
                                                                                dialog.dismiss();
                                                                            }
                                                                        });

                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Toast.makeText(holder.img.getContext(),"Item Updation Failed...",Toast.LENGTH_SHORT).show();
                                                                btn.setEnabled(true);
                                                                pb.setVisibility(View.GONE);
                                                                dialog.dismiss();
                                                            }
                                                        });
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(holder.img.getContext(),"Item Updation Failed...",Toast.LENGTH_SHORT).show();
                                                btn.setEnabled(true);
                                                pb.setVisibility(View.GONE);
                                                dialog.dismiss();
                                            }
                                        });
                            }}
                    }}
                });
                dialog.show();
            }
        }


        if(isEnabled){
            if(isAllSelected){
                holder.chk.setVisibility(View.VISIBLE);
                holder.itemView.setBackgroundColor(Color.parseColor("#E8E9EB"));
            }
            else if(!poslist.isEmpty()){
                if(poslist.contains(position)){
                    holder.chk.setVisibility(View.VISIBLE);
                    holder.itemView.setBackgroundColor(Color.parseColor("#E8E9EB"));
                }
                else{
                    holder.chk.setVisibility(View.GONE);
                    holder.itemView.setBackgroundColor(Color.TRANSPARENT);
                }
            }
            else{
                holder.chk.setVisibility(View.GONE);
                holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            }}
        else {
            holder.chk.setVisibility(View.GONE);
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    @NonNull
    @Override
    public myviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.singlero,parent,false);
        mainViewModel = ViewModelProviders.of((FragmentActivity) activity).get(MainViewModel.class);
        return new myviewholder(view);
    }

    private void checklist(myviewholder holder) {
        int position = holder.getLayoutPosition();

        if(isAllSelected){
            isAllSelected=false;
        }
        if(holder.chk.getVisibility() == View.GONE){
            if(!poslist.contains(position)){
                poslist.add(position);
            }
            if(!selectedlist.contains(getRef(holder.getLayoutPosition()).getKey())){
                selectedlist.add(getRef(holder.getLayoutPosition()).getKey());
            }
            holder.chk.setVisibility(View.VISIBLE);
            holder.itemView.setBackgroundColor(Color.parseColor("#E8E9EB"));
        }
        else{
            if(poslist.contains(position)){
                poslist.remove(new Integer(position));
            }
            if(selectedlist.contains(getRef(holder.getLayoutPosition()).getKey())){
                selectedlist.remove(getRef(holder.getLayoutPosition()).getKey());
            }
            holder.chk.setVisibility(View.GONE);
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }
        if(selectedlist.size() == 0){
            if(actm != null)
                actm.finish();
        }
        else{
            mainViewModel.setText(String.valueOf(selectedlist.size()));
        }

        //Disabling update menu when more than one item selected
        if(selectedlist.size() != 1){
            upd.setEnabled(false);
            Drawable drawable = ResourcesCompat.getDrawable(c.getResources(),R.drawable.ic_baseline_create_24,null);
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTint(drawable,Color.LTGRAY);
            upd.setIcon(drawable);
        }
        else{
            upd.setEnabled(true);
            Drawable drawable = ResourcesCompat.getDrawable(c.getResources(),R.drawable.ic_baseline_create_24,null);
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTint(drawable,Color.WHITE);
            upd.setIcon(drawable);
        }
    }

    class myviewholder extends RecyclerView.ViewHolder{
        CardView card;
        CircleImageView img,chk;
        //ImageView upd,chk;
        TextView name,id;
        public myviewholder(@NonNull View itemView) {
            super(itemView);
            img=(CircleImageView) itemView.findViewById(R.id.img1);
            //upd=(ImageView) itemView.findViewById(R.id.updbtn);
            chk=(CircleImageView) itemView.findViewById(R.id.checkbtn);
            id=(TextView)itemView.findViewById(R.id.stid);
            name=(TextView)itemView.findViewById(R.id.name);
            card=(CardView)itemView.findViewById(R.id.cardv);
        }
    }

    public void executeAsyncDeleteRecords()
    {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

            /**
             * @param voids
             * @deprecated
             */
            @Override
            protected Void doInBackground(Void... voids) {
                //Deleting students selected
                for (String s : selectedlist) {
                    FirebaseDatabase.getInstance().getReference().child("students").child(s).removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    delcount = delcount+1;
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(s).removeValue()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    delfee = delfee+1;
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    delfee = -1;
                                                    pdialog.dismiss();
                                                    //handler.removeCallbacks(runn);
                                                    //Toast.makeText(holder.img.getContext(),"Some Error Occurred!!..Deletion failed..!!",Toast.LENGTH_SHORT).show();
                                                    actm.finish();
                                                }
                                            });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    delcount = -1;
                                    pdialog.dismiss();
                                    //handler.removeCallbacks(runn);
                                    //Toast.makeText(holder.img.getContext(),"Some Error Occurred!!..Deletion failed..!!",Toast.LENGTH_SHORT).show();
                                    actm.finish();
                                }
                            });
                }
                return null;
            }

            /**
             * @param aVoid
             * @deprecated
             */
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                //Toast.makeText(c,"Deleted records.",Toast.LENGTH_SHORT).show();
                executeAsyncDeletePhotos();
            }
        };

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB/*HONEYCOMB = 11*/) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            task.execute();
        }
    }

    public void executeAsyncDeletePhotos()
    {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            /**
             * @param voids
             * @deprecated
             */
            @Override
            protected Void doInBackground(Void... voids) {
                for(String s: selectedlist){
                    imagedel = "Image"+ s;
                    FirebaseStorage.getInstance().getReference().child("profile/" + imagedel).delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    delimgcount = delimgcount+1;
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    delimgcount = -1;
                                    //handler.removeCallbacks(runn);
                                    pdialog.dismiss();
                                    actm.finish();
                                }
                            });
                }
                return null;
            }

            /**
             * @param aVoid
             * @deprecated
             */
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                //Toast.makeText(c,"Completed deleting photos.",Toast.LENGTH_SHORT).show();
                //Toast.makeText(c,"reclist size:."+reclist.size(),Toast.LENGTH_SHORT).show();
                //Toast.makeText(c,"recidlist:."+recidlist.size(),Toast.LENGTH_SHORT).show();
                executeAsyncDeleterec();
            }

        };

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB/*HONEYCOMB = 11*/) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            task.execute();
        }
    }

    public void executeAsyncDeleterec()
    {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            /**
             * @param voids
             * @deprecated
             */
            @Override
            protected Void doInBackground(Void... voids) {
                if (recidlist.size() > 0) {
                    for (String s : recidlist) {
                        FirebaseDatabase.getInstance().getReference().child("receipt").child(s).removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        delrec = delrec + 1;
                                    }
                                });
                    }
                    for (String s : reclist) {
                        FirebaseStorage.getInstance().getReferenceFromUrl(s).delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        delrecipt = delrecipt + 1;
                                        //Toast.makeText(holder.img.getContext(),"delrec: "+delrec,Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }
                return null;
            }

            /**
             * @param aVoid
             * @deprecated
             */
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                pdialog.dismiss();
                actm.finish();
                Toast.makeText(c,"Deleted Successfully....",Toast.LENGTH_SHORT).show();
            }
        };

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB/*HONEYCOMB = 11*/) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            task.execute();
        }
    }
}
