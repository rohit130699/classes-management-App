package com.example.snc_students;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class videoadapter extends FirebaseRecyclerAdapter<videoupload, videoadapter.viewholder> {
    ProgressBar pbar;
    Activity activity;
    Context c;
    String ustd;
    public videoadapter(@NonNull FirebaseRecyclerOptions<videoupload> options,ProgressBar pbar,Activity activity) {
        super(options);
        this.pbar = pbar;
        this.activity = activity;

        SharedPreferences sh = activity.getSharedPreferences("credentials",activity.MODE_PRIVATE);
        ustd = sh.getString("ustd",null);

        if(!isNetworkAvailable()){
            AlertDialog alert = new AlertDialog.Builder(activity).create();
            alert.setTitle("No Network Connection..");
            alert.setMessage("There is probably low or no network Connection !!");
            alert.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    alert.dismiss();
                }
            });
            alert.show();
        }
        else{
            FirebaseDatabase.getInstance().getReference().child("videos").orderByChild("std").equalTo(ustd)
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
    }

    @Override
    protected void onBindViewHolder(@NonNull videoadapter.viewholder holder, int position, @NonNull videoupload videoupload) {
        c = holder.pdf.getContext();

        if(getItemCount() > 0){
            pbar.setVisibility(View.GONE);
        }
        //StorageReference vdoimg= FirebaseStorage.getInstance().getReference().child("video.png");
        holder.name.setText(videoupload.getVideotitle());
        holder.size.setText(videoupload.getVideosize());
        holder.date.setText(videoupload.getTime());
        /*try{
            final File vdoimag=File.createTempFile("video","png");
            vdoimg.getFile(vdoimag)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Bitmap bitmap= BitmapFactory.decodeFile(vdoimag.getAbsolutePath());
                            holder.pdf.setImageBitmap(bitmap);
                        }
                    });
        }
        catch(IOException e){
            e.printStackTrace();
        }*/
        Glide.with(holder.pdf.getContext()).load("https://firebasestorage.googleapis.com/v0/b/snclasses-5ed6d.appspot.com/o/video.png?alt=media&token=e8ff99b5-3916-4b44-a516-46ddcbd36bdd").into(holder.pdf);

        holder.down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String geturl = videoupload.getVideoUri();
                //Toast.makeText(getApplicationContext(), geturl, Toast.LENGTH_LONG).show();
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(geturl));
                request.setTitle(videoupload.getVideoName());
                request.setDescription("Downloading Files...");
                String cookie = CookieManager.getInstance().getCookie(geturl);
                request.addRequestHeader("cookie",cookie);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,videoupload.getVideoName());
                DownloadManager dm = (DownloadManager) holder.card.getContext().getSystemService(holder.down.getContext().DOWNLOAD_SERVICE);
                dm.enqueue(request);
                Toast.makeText(holder.card.getContext(),"Downloading Started...",Toast.LENGTH_SHORT).show();
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    Intent intent=new Intent(holder.card.getContext(),viewvideo.class);
                    intent.putExtra("App_context", String.valueOf(holder.card.getContext()));
                    intent.putExtra("filename",videoupload.getVideotitle());
                    intent.putExtra("fileurl",videoupload.getVideoUri());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    c.startActivity(intent);

            }
        });
    }

    @NonNull
    @Override
    public viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.pdfs,parent,false);
        return new viewholder(view);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    public class viewholder extends RecyclerView.ViewHolder{
        CardView card;
        CircleImageView down;
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
        }
    }
}
