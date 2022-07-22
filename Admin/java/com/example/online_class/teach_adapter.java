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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class teach_adapter  extends FirebaseRecyclerAdapter<teachupload,teach_adapter.myviewholder> {
    AlertDialog dialog;
    ProgressBar pbar;
    ProgressDialog pdialog;
    ActionMode actm;
    FloatingActionButton fbtn;
    EditText et;
    LinearLayout lout;
    Activity activity;
    MainViewModel mainViewModel;
    MenuItem upd;
    String imagedel,last="",tname="",tmail="",tcno="",tqual="";
    boolean isEnabled = false;
    boolean isAllSelected = false;
    boolean isupd = false;
    int delcount,delimgcount,size,items,pos;
    Context c;
    ArrayList<String> selectedlist = new ArrayList<>();
    ArrayList<String> totalleft = new ArrayList<>();
    ArrayList<Integer> poslist = new ArrayList<>();

    public teach_adapter(@NonNull FirebaseRecyclerOptions<teachupload> options,Activity activity,FloatingActionButton fbtn,ProgressDialog pdialg,ProgressBar pbar,EditText et,LinearLayout lout) {
        super(options);
        this.pbar = pbar;
        this.activity = activity;
        this.fbtn = fbtn;
        this.pdialog = pdialg;
        this.et = et;
        this.lout = lout;

        FirebaseDatabase.getInstance().getReference().child("teachers").orderByChild("id")
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

    @NonNull
    @Override
    public myviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.teach_rows,parent,false);
        mainViewModel = ViewModelProviders.of((FragmentActivity) activity).get(MainViewModel.class);
        return new myviewholder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull myviewholder holder, int position, @NonNull teachupload model) {
        c=holder.img.getContext();
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
                            delcount = 0;delimgcount = 0;size = 0;items = 0;pos = -1;
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
                            FirebaseDatabase.getInstance().getReference().child("teachers").orderByChild("id")
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
                                                    size = selectedlist.size();
                                                    items = getItemCount();
                                                    pdialog.show();
                                                    /*final Handler handler = new Handler();
                                                    final Runnable runn = new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            if(delcount == selectedlist.size()){
                                                                if(delimgcount == selectedlist.size()){
                                                                    if(getItemCount() == (items-size)){
                                                                        handler.removeCallbacks(this);
                                                                        pdialog.dismiss();
                                                                        Toast.makeText(holder.img.getContext(),"Deleted Successfully..",Toast.LENGTH_SHORT).show();
                                                                        actionMode.finish();
                                                                    }
                                                                }
                                                                else{
                                                                    handler.postDelayed(this,1000);
                                                                    for(String s: selectedlist){
                                                                        imagedel = "Image"+ s;
                                                                        FirebaseStorage.getInstance().getReference().child("teacherprofile/" + imagedel).delete()
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
                                                                                        actionMode.finish();
                                                                                    }
                                                                                });
                                                                    }
                                                                }
                                                            }
                                                            else{
                                                                handler.postDelayed(this,1000);
                                                            }
                                                        }
                                                    };
                                                    handler.removeCallbacks(runn);
                                                    handler.postDelayed(runn,0);*/
                                                    for (String s : selectedlist) {
                                                        FirebaseDatabase.getInstance().getReference().child("teachers").child(s).removeValue()
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        delcount = delcount+1;
                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        delcount = -1;
                                                                        pdialog.dismiss();
                                                                        //handler.removeCallbacks(runn);
                                                                        Toast.makeText(holder.img.getContext(),"Some Error Occurred!!..Deletion failed..!!",Toast.LENGTH_SHORT).show();
                                                                        actionMode.finish();
                                                                    }
                                                                });
                                                    }
                                                    executeAsyncDeletePhotos();
                                                    //new delete_photos().execute();
                                                    //Toast.makeText(holder.img.getContext(),"Deleted Successfully..",Toast.LENGTH_SHORT).show();
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
                                    else{
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
                            selectedlist.clear();
                            totalleft.clear();
                            poslist.clear();
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
                    Intent intent=new Intent(holder.card.getContext(),teacher_details.class);
                    intent.putExtra("tid",model.getId());
                    intent.putExtra("url",model.getPimage());
                    intent.putExtra("name",model.getName());
                    intent.putExtra("cno",model.getCno());
                    intent.putExtra("mail",model.getMail());
                    intent.putExtra("doj",model.getDate());
                    intent.putExtra("qual",model.getQual());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    holder.card.getContext().startActivity(intent);
                }
            }
        });

        if(isupd){
            //Toast.makeText(c,"isupd set to true",Toast.LENGTH_SHORT).show();
            if(position == pos) {
                tname = model.getName();
                tmail = model.getMail();
                tcno = model.getCno();
                tqual = model.getQual();
                /*Toast.makeText(c,""+tname,Toast.LENGTH_SHORT).show();
                Toast.makeText(c,""+tmail,Toast.LENGTH_SHORT).show();
                Toast.makeText(c,""+tcno,Toast.LENGTH_SHORT).show();
                Toast.makeText(c,""+tqual,Toast.LENGTH_SHORT).show();*/
                isupd = false;
                selectedlist.clear();
                poslist.clear();
                AlertDialog.Builder builder = new AlertDialog.Builder(holder.img.getContext());
                LayoutInflater inflater = (LayoutInflater)holder.img.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View myview = inflater.inflate(R.layout.upd_teachers,null);
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
                EditText qual = myview.findViewById(R.id.qual);
                Button btn = myview.findViewById(R.id.update);

                name.setText(tname);
                mail.setText(tmail);
                phone.setText(tcno);
                qual.setText(tqual);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        btn.setEnabled(false);
                        if(name.getText().toString().trim().equals("") || mail.getText().toString().trim().equals("") || phone.getText().toString().trim().equals("") || qual.getText().toString().trim().equals("")){
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
                        else if(!qual.getText().toString().trim().matches("^[A-Za-z ]+$")){
                            Toast.makeText(c,"Qualification entered might contain characters except letters..Please check!!",Toast.LENGTH_SHORT).show();
                            btn.setEnabled(true);
                        }
                        else{
                        if(name.getText().toString().trim().equals(tname) && mail.getText().toString().trim().equals(tmail) && phone.getText().toString().trim().equals(tcno) && qual.getText().toString().trim().equals(tqual)){
                            Toast.makeText(holder.img.getContext(),"No Updation Performed Yet!!",Toast.LENGTH_SHORT).show();
                            btn.setEnabled(true);
                        }
                        else{
                            if(!phone.getText().toString().trim().equals(tcno)){
                                FirebaseDatabase.getInstance().getReference().child("teachers").orderByChild("cno").equalTo(phone.getText().toString().trim())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                                                if(dataSnapshot1.getChildrenCount() > 0){
                                                    Toast.makeText(holder.img.getContext(),"Already a teacher exists with given phone no.!Please Change it !!",Toast.LENGTH_SHORT).show();
                                                    phone.setText("");
                                                    btn.setEnabled(true);
                                                }
                                                else{
                                                    FirebaseDatabase.getInstance().getReference().child("admin").orderByChild("cno").equalTo(phone.getText().toString().trim())
                                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                    if(dataSnapshot.getChildrenCount() > 0){
                                                                        Toast.makeText(holder.img.getContext(),"Already an admin exists with given Contact No!Please Change it !!",Toast.LENGTH_SHORT).show();
                                                                        phone.setText("");
                                                                        btn.setEnabled(true);
                                                                    }
                                                                    else{
                                                                            pb.setVisibility(View.VISIBLE);
                                                                            Map<String,Object> map = new HashMap<>();
                                                                            map.put("name",name.getText().toString().trim());
                                                                            map.put("mail",mail.getText().toString().trim());
                                                                            map.put("cno",phone.getText().toString().trim());
                                                                            map.put("qual",qual.getText().toString().trim());
                                                                            map.put("search",name.getText().toString().toLowerCase().trim());
                                                                            String upd = getRef(holder.getLayoutPosition()).getKey();
                                                                            //Toast.makeText(holder.upd.getContext(),""+upd,Toast.LENGTH_SHORT).show();
                                                                            FirebaseDatabase.getInstance().getReference().child("teachers").child(upd).updateChildren(map)
                                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                        @Override
                                                                                        public void onSuccess(Void aVoid) {
                                                                                            Toast.makeText(holder.chk.getContext(),"Item Updated Successfully...",Toast.LENGTH_SHORT).show();
                                                                                            pb.setVisibility(View.GONE);
                                                                                            btn.setEnabled(true);
                                                                                            dialog.dismiss();
                                                                                        }
                                                                                    })
                                                                                    .addOnFailureListener(new OnFailureListener() {
                                                                                        @Override
                                                                                        public void onFailure(@NonNull Exception e) {
                                                                                            Toast.makeText(holder.chk.getContext(),"Item Updation Failed...",Toast.LENGTH_SHORT).show();
                                                                                            pb.setVisibility(View.GONE);
                                                                                            btn.setEnabled(true);
                                                                                            dialog.dismiss();
                                                                                        }
                                                                                    });
                                                                    }
                                                                }
                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError databaseError) {

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
                                map.put("qual",qual.getText().toString().trim());
                                map.put("search",name.getText().toString().toLowerCase().trim());
                                String upd = getRef(holder.getLayoutPosition()).getKey();
                                //Toast.makeText(holder.upd.getContext(),""+upd,Toast.LENGTH_SHORT).show();
                                FirebaseDatabase.getInstance().getReference().child("teachers").child(upd).updateChildren(map)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(holder.chk.getContext(),"Item Updated Successfully...",Toast.LENGTH_SHORT).show();
                                                pb.setVisibility(View.GONE);
                                                btn.setEnabled(true);
                                                dialog.dismiss();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(holder.chk.getContext(),"Item Updation Failed...",Toast.LENGTH_SHORT).show();
                                                pb.setVisibility(View.GONE);
                                                btn.setEnabled(true);
                                                dialog.dismiss();
                                            }
                                        });
                            }}
                    }}
                });
                dialog.show();
            }}

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
                    FirebaseStorage.getInstance().getReference().child("teacherprofile/" + imagedel).delete()
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
                //handler.removeCallbacks(this);
                pdialog.dismiss();
                Toast.makeText(c,"Deleted Successfully..",Toast.LENGTH_SHORT).show();
                actm.finish();
            }
        };

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB/*HONEYCOMB = 11*/) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            task.execute();
        }
    }
}
