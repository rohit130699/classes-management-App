package com.example.snc_teachers;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class receiptadapter extends FirebaseRecyclerAdapter<receiptupload, receiptadapter.viewholder> {
    ProgressDialog pdialog;
    ProgressBar pbar;
    ActionMode actm;
    ArrayList<receiptupload> displayedList;
    Bitmap bitmap;
    Activity activity;
    EditText et;
    MainViewModel mainViewModel;
    boolean isEnabled = false;
    boolean isAllSelected = false;
    int delcount,delrecipt,delimgcount,size,items,pos;
    String last_s="";
    Context c;
    ArrayList<String> selectedlist = new ArrayList<>();
    ArrayList<String> receiptlist = new ArrayList<>();
    ArrayList<String> totalleft = new ArrayList<>();
    ArrayList<Integer> poslist = new ArrayList<>();
    public receiptadapter(@NonNull FirebaseRecyclerOptions<receiptupload> options, Activity activity, ProgressDialog pdialg, ProgressBar pbar, EditText et) {
        super(options);
        this.pbar = pbar;
        this.activity = activity;
        this.pdialog = pdialg;
        this.et = et;
        StorageReference pdfimg= FirebaseStorage.getInstance().getReference().child("pdf.png");
        try{
            final File pdfimag=File.createTempFile("pdf","png");
            pdfimg.getFile(pdfimag)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            bitmap= BitmapFactory.decodeFile(pdfimag.getAbsolutePath());
                        }
                    });
        }
        catch(IOException e){
            e.printStackTrace();
        }
        FirebaseDatabase.getInstance().getReference().child("receipt").orderByChild("invoice_no")
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
    protected void onBindViewHolder(@NonNull viewholder holder, int position, @NonNull receiptupload receiptupload) {
        //Toast.makeText(holder.pdf.getContext(),""+getRef(holder.getLayoutPosition()).getKey(),Toast.LENGTH_SHORT).show();
        //StorageReference pdfimg= FirebaseStorage.getInstance().getReference().child("pdf.png");
        c=holder.name.getContext();

        if(getItemCount() > 0){
            pbar.setVisibility(View.GONE);
        }

        holder.name.setText(receiptupload.getRec_name());
        holder.size.setText(receiptupload.getSizeinKB());
        holder.date.setText(receiptupload.getTime());
        //holder.pdf.setImageBitmap(bitmap);
        Glide.with(holder.pdf.getContext()).load("https://firebasestorage.googleapis.com/v0/b/snclasses-5ed6d.appspot.com/o/pdf.png?alt=media&token=3e7af910-b7d1-4611-ab32-927ae4820500").into(holder.pdf);
        /*try{
            final File pdfimag=File.createTempFile("pdf","png");
            pdfimg.getFile(pdfimag)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Bitmap bitmap= BitmapFactory.decodeFile(pdfimag.getAbsolutePath());
                            holder.pdf.setImageBitmap(bitmap);
                        }
                    });
        }
        catch(IOException e){
            e.printStackTrace();
        }*/

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isEnabled){
                    checklist(holder);
                }
                else{
                    Intent intent=new Intent(holder.card.getContext(),viewpdf.class);
                    intent.putExtra("filename",receiptupload.getRec_name());
                    intent.putExtra("fileurl",receiptupload.getUrl());
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

        if(isEnabled){
            if(isAllSelected){
                holder.chk.setVisibility(View.VISIBLE);
                holder.itemView.setBackgroundColor(Color.parseColor("#E8E9EB"));
                holder.down.setEnabled(false);
            }
            else if(!poslist.isEmpty()){
                if(poslist.contains(position)){
                    holder.chk.setVisibility(View.VISIBLE);
                    holder.itemView.setBackgroundColor(Color.parseColor("#E8E9EB"));
                    holder.down.setEnabled(false);
                }
                else{
                    holder.chk.setVisibility(View.GONE);
                    holder.itemView.setBackgroundColor(Color.TRANSPARENT);
                    holder.down.setEnabled(true);
                }
            }
            else{
                holder.chk.setVisibility(View.GONE);
                holder.itemView.setBackgroundColor(Color.TRANSPARENT);
                holder.down.setEnabled(true);
            }}
        else {
            holder.chk.setVisibility(View.GONE);
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            holder.down.setEnabled(true);
        }

        holder.down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String geturl = receiptupload.getUrl();
                //Toast.makeText(getApplicationContext(), geturl, Toast.LENGTH_LONG).show();
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(geturl));
                request.setTitle(receiptupload.getRec_name());
                request.setDescription("Downloading Files...");
                String cookie = CookieManager.getInstance().getCookie(geturl);
                request.addRequestHeader("cookie",cookie);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,receiptupload.getRec_name());
                DownloadManager dm = (DownloadManager) holder.card.getContext().getSystemService(holder.down.getContext().DOWNLOAD_SERVICE);
                dm.enqueue(request);
                Toast.makeText(holder.card.getContext(),"Downloading Started...",Toast.LENGTH_SHORT).show();
            }
        });
        /*holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(holder.name.getContext());
                builder.setTitle("Delete Panel");
                builder.setMessage("Are you sure to delete this item..??");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String delp = getRef(holder.getLayoutPosition()).getKey();
                        Dialog dialog = ProgressDialog.show(holder.delete.getContext(),"","Deleting...");
                        FirebaseStorage.getInstance().getReference().child("receipts/"+receiptupload.getRec_name()).delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        FirebaseDatabase.getInstance().getReference().child("receipt").child(delp).removeValue();
                                        dialog.dismiss();
                                        Toast.makeText(holder.card.getContext(),"Deleted successfully...",Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        dialog.dismiss();
                                        Toast.makeText(holder.card.getContext(),"Delete Unsuccessfull!!Some Error Ocurred...",Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.show();
            }
        });*/

    }

    @NonNull
    @Override
    public viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.pdfs,parent,false);
        mainViewModel = ViewModelProviders.of((FragmentActivity) activity).get(MainViewModel.class);
        return new viewholder(view);
    }

    public void updateList(ArrayList<receiptupload> list){
        displayedList = list;
        notifyDataSetChanged();
    }

    private void checklist(viewholder holder) {
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
            holder.down.setEnabled(false);
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
            holder.down.setEnabled(true);
        }
        if(selectedlist.size() == 0){
            if(actm != null)
                actm.finish();
        }
        else{
            mainViewModel.setText(String.valueOf(selectedlist.size()));
        }
    }


    public class viewholder extends RecyclerView.ViewHolder{
        CardView card;
        CircleImageView chk,down;
        ImageView pdf;
        TextView name,size,date;
        public viewholder(@NonNull View itemView) {
            super(itemView);
            pdf=(ImageView)itemView.findViewById(R.id.pdf);
            name=(TextView)itemView.findViewById(R.id.name);
            card=(CardView)itemView.findViewById(R.id.cardv);
            size=(TextView)itemView.findViewById(R.id.size);
            date=(TextView) itemView.findViewById(R.id.u_date);
            down=(CircleImageView) itemView.findViewById(R.id.download);
            chk=(CircleImageView)itemView.findViewById(R.id.checkbtn);
        }
    }
}
