package com.example.online_class;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class fee_accept extends AppCompatActivity {
    String sid,feesm,month,sstd,sname,cno,smail,pimage;
    AutoCompleteTextView mode;
    String datepatternformat;
    SimpleDateFormat datePatternformat1;
    DateFormat formatter;
    FirebaseStorage storage;
    FirebaseDatabase database;
    DatabaseReference ref2,ref3;
    int mon=0;
    int check = 0;
    int count=0;
    int recpt_id =0;
    int paid_upto = 0,calc_month = 0,tobe_paid = 0;
    int TIMEOUT = 20;
    boolean downloaded = false;
    boolean isfirst = false;
    boolean isvalid = false;
    double n;
    Button upd;
    Uri filepath;
    EditText amt,date;
    ProgressDialog dialog;
    String paidm,rec_name,sel_date,filesize,user,loggedin_user,tmp_month;
    long filesizeInBytes;
    double filesizeinKB,filesizeinMB;
    Date dop;
    File fold,myimg;
    String[] pmode={"By Cash","By Cheque"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fee_accept);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        SharedPreferences sh = getSharedPreferences("credentials",getApplicationContext().MODE_PRIVATE);
        user = sh.getString("uname",null);
        loggedin_user = "By "+user;
        //Toast.makeText(getApplicationContext(),"onCreate called...",Toast.LENGTH_LONG).show();

        sid = getIntent().getStringExtra("sid");
        feesm = getIntent().getStringExtra("feesm");
        month = getIntent().getStringExtra("month");

        //datePatternformat = new SimpleDateFormat("dd - MM - yyyy hh:mm a");
        formatter = new SimpleDateFormat("dd/MM/yyyy");
        datePatternformat1 = new SimpleDateFormat("dd MMM yyyy");

        storage = FirebaseStorage.getInstance();
        amt = findViewById(R.id.amt);
        date = findViewById(R.id.date);
        mode = findViewById(R.id.mode);

        dialog = new ProgressDialog(this);

        Calendar cal = Calendar.getInstance();
        final int year = cal.get(Calendar.YEAR);
        final int month = cal.get(Calendar.MONTH);
        final int day = cal.get(Calendar.DAY_OF_MONTH);

        fold = new File(this.getExternalFilesDir("/"), "Download");
        if (fold.exists()) {
            //Toast.makeText(getApplicationContext(),"Exists",Toast.LENGTH_SHORT).show();
            String[] entries = fold.list();
            for (String s : entries) {
                File currentfile = new File(fold.getPath(), s);
                currentfile.delete();
            }
            fold.delete();
        }

        myimg = new File(this.getExternalFilesDir("/"), "Download/ProfileImage.jpg");

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        fee_accept.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        month = month+1;
                        mon = month;
                        String exact_date = day+"/"+month+"/"+year;
                        sel_date = exact_date;
                        date.setText(exact_date);
                        //Toast.makeText(getApplicationContext(),"Month inside"+month,Toast.LENGTH_SHORT).show();
                        //Toast.makeText(getApplicationContext(),"mon inside."+mon,Toast.LENGTH_SHORT).show();
                    }
                },year,month,day);
                datePickerDialog.show();
            }
        });

        database=FirebaseDatabase.getInstance();
        ref2=database.getReference("fees");
        ref3=database.getReference("receipt");

        upd=(Button)findViewById(R.id.update);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.select_dialog_item,pmode);
        mode.setThreshold(1);
        mode.setAdapter(adapter);

        upd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //new imageDownload().execute();
                isvalid = validate();
                if(isvalid){
                    dialog.show();
                    dialog.setContentView(R.layout.progress_dialog);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    executeAsyncTask();
                }
            }
        });

        Query query = FirebaseDatabase.getInstance().getReference().child("students").orderByChild("id").equalTo(sid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount() > 0){
                    for(DataSnapshot rec: dataSnapshot.getChildren()) {
                        sname = rec.child("name").getValue().toString();
                        sstd = rec.child("std").getValue().toString();
                        smail = rec.child("mail").getValue().toString();
                        cno = rec.child("cno").getValue().toString();
                        pimage = rec.child("pimage").getValue().toString();
                    }}
                else{
                    Toast.makeText(getApplicationContext(),"No such record found",Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(),"Some Error has Occurred...",Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean validate() {
            upd.setEnabled(false);
            if(amt.getText().toString().trim().equals("") || date.getText().toString().trim().equals("") || mode.getText().toString().trim().equals("")){
                Toast.makeText(getApplicationContext(),"Some data is missing out !! Please fill out them..",Toast.LENGTH_SHORT).show();
                upd.setEnabled(true);
                return false;
            }
            if(!amt.getText().toString().trim().matches("\\d+")) {
                Toast.makeText(getApplicationContext(),"Fees entered might contain characters except numbers..Please check!!",Toast.LENGTH_SHORT).show();
                upd.setEnabled(true);
                return false;
            }
            if(!date.getText().toString().trim().matches("^\\d{1,2}\\/\\d{1,2}\\/\\d{4}$")){
                Toast.makeText(getApplicationContext(),"Date entered is in incorrect format..Please check!!",Toast.LENGTH_SHORT).show();
                upd.setEnabled(true);
                //date.setText("");
                return false;
            }
            if(!mode.getText().toString().trim().equals("By Cash") && !mode.getText().toString().trim().equals("By Cheque")){
                Toast.makeText(getApplicationContext(),"Mode should be either 'By Cash' or 'By Cheque'!!",Toast.LENGTH_SHORT).show();
                upd.setEnabled(true);
                return false;
            }
            return true;
    }

    private void value_upd() {
        if(mon == 6){
            calc_month = 1;
        }
        else if(mon == 7){
            calc_month = 2;
        }
        else if(mon == 8){
            calc_month = 3;
        }
        else if(mon == 9){
            calc_month = 4;
        }
        else if(mon == 10){
            calc_month = 5;
        }
        else if(mon == 11){
            calc_month = 6;
        }
        else if(mon == 12){
            calc_month = 7;
        }
        else if(mon == 1){
            calc_month = 8;
        }
        else if(mon == 2){
            calc_month = 9;
        }
        else if(mon == 3){
            calc_month = 10;
        }
        else if(mon == 4){
            calc_month = 11;
        }
        else if(mon == 5){
            calc_month = 12;
        }

        if(month.equals("January")){
            paid_upto = 8;
        }
        else if(month.equals("February")){
            paid_upto = 9;
        }
        else if(month.equals("March")){
            paid_upto = 10;
        }
        else if(month.equals("April")){
            paid_upto = 11;
        }
        else if(month.equals("May")){
            paid_upto = 12;
        }
        else if(month.equals("June")){
            paid_upto = 1;
        }
        else if(month.equals("July")){
            paid_upto = 2;
        }
        else if(month.equals("August")){
            paid_upto = 3;
        }
        else if(month.equals("September")){
            paid_upto = 4;
        }
        else if(month.equals("October")){
            paid_upto = 5;
        }
        else if(month.equals("November")){
            paid_upto = 6;
        }
        else if(month.equals("December")){
            paid_upto = 7;
        }
        else{
            if(!month.equals("June") && !month.equals("July") && !month.equals("August") && !month.equals("September") && !month.equals("October") && !month.equals("November") && !month.equals("December") && !month.equals("January") && !month.equals("February") && !month.equals("March") && !month.equals("April") && !month.equals("May")){
                tmp_month = month;
                month = tmp_month.substring(24);
                //Toast.makeText(getApplicationContext(),"Month with substring: "+month,Toast.LENGTH_SHORT).show();
            }
            isfirst = true;
        }
    }
    private boolean typcheck(double d) {
        double tol = 1E-5;
        return Math.abs(Math.floor(d)-d) < tol;
    }
    private void upload(){
        //Toast.makeText(getApplicationContext(),"Month"+month,Toast.LENGTH_SHORT).show();
        //Toast.makeText(getApplicationContext(),"mon"+mon,Toast.LENGTH_SHORT).show();
        double am = Double.parseDouble(amt.getText().toString().trim());
        double feesmon = Double.parseDouble(feesm);
        if (!isfirst) {
            if (calc_month == paid_upto + 1) {
                n = am / feesmon;
                if (typcheck(n) == true) {
                    if (n <= 12) {
                        //Toast.makeText(getApplicationContext(), "Inside upload()", Toast.LENGTH_SHORT).show();
                        if (downloaded) {
                            if (n == 1) {
                                if (mon == 6) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("jun", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jun";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 7) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("july", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "July";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 8) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Aug";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 9) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Sep";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 10) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Oct";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 11) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Nov";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 12) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Dec";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 1) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jan";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 2) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Feb";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 3) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Mar";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 4) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("apr", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Apr";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 5) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("may", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "May";
                                                    generate_rid();
                                                }
                                            });
                                }
                            } else if (n == 2) {
                                if (mon == 6) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("jun", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("july", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jun,Jul";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 7) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("july", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jul, Aug";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 8) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Aug, Sep";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 9) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Sep, Oct";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 10) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Oct, Nov";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 11) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Nov, Dec";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 12) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Dec, Jan";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 1) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jan, Feb";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 2) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Feb, Mar";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 3) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("apr", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Mar, Apr";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 4) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("apr", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("may", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Apr,May";
                                                    generate_rid();
                                                }
                                            });
                                } else {
                                    dialog.dismiss();
                                    amt.setText("");
                                    upd.setEnabled(true);
                                    AlertDialog alert = new AlertDialog.Builder(fee_accept.this).create();
                                    alert.setTitle("ATTENTION!!");
                                    alert.setMessage("Amount entered is exceeding than the total Payment of remaining months..!!");
                                    alert.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            alert.dismiss();
                                        }
                                    });
                                    alert.show();
                                }
                            } else if (n == 3) {
                                if (mon == 6) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("jun", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("july", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jun,Jul,Aug";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 7) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("july", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jul, Aug, Sep";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 8) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Aug, Sep, Oct";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 9) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Sep, Oct, Nov";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 10) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Oct, Nov, Dec";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 11) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Nov, Dec, Jan";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 12) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Dec, Jan, Feb";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 1) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jan, Feb, Mar";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 2) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("apr", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Feb, Mar, Apr";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 3) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("apr", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("may", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Mar, Apr, May";
                                                    generate_rid();
                                                }
                                            });
                                } else {
                                    dialog.dismiss();
                                    amt.setText("");
                                    upd.setEnabled(true);
                                    AlertDialog alert = new AlertDialog.Builder(fee_accept.this).create();
                                    alert.setTitle("ATTENTION!!");
                                    alert.setMessage("Amount entered is exceeding than the total Payment of remaining months..!!");
                                    alert.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            alert.dismiss();
                                        }
                                    });
                                    alert.show();
                                }
                            } else if (n == 4) {
                                if (mon == 6) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("jun", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("july", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jun, Jul, Aug, Sep";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 7) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("july", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jul, Aug, Sep, Oct";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 8) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Aug, Sep, Oct, Nov";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 9) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Sep, Oct, Nov, Dec";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 10) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Oct, Nov, Dec, Jan";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 11) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Nov, Dec, Jan, Feb";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 12) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Dec, Jan, Feb, Mar";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 1) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("apr", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jan, Feb, Mar, Apr";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 2) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("apr", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("may", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Feb, Mar, Apr, May";
                                                    generate_rid();
                                                }
                                            });
                                } else {
                                    dialog.dismiss();
                                    amt.setText("");
                                    upd.setEnabled(true);
                                    AlertDialog alert = new AlertDialog.Builder(fee_accept.this).create();
                                    alert.setTitle("ATTENTION!!");
                                    alert.setMessage("Amount entered is exceeding than the total Payment of remaining months..!!");
                                    alert.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            alert.dismiss();
                                        }
                                    });
                                    alert.show();
                                }
                            } else if (n == 5) {
                                if (mon == 6) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("jun", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("july", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jun, Jul, Aug, Sep, Oct";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 7) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("july", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jul, Aug, Sep, Oct, Nov";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 8) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Aug, Sep, Oct, Nov, Dec";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 9) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Sep, Oct, Nov, Dec, Jan";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 10) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Oct, Nov, Dec, Jan, Feb";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 11) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Nov, Dec, Jan, Feb, Mar";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 12) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("apr", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Dec, Jan, Feb, Mar, Apr";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 1) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("apr", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("may", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jan, Feb, Mar, Apr, May";
                                                    generate_rid();
                                                }
                                            });
                                } else {
                                    dialog.dismiss();
                                    amt.setText("");
                                    upd.setEnabled(true);
                                    AlertDialog alert = new AlertDialog.Builder(fee_accept.this).create();
                                    alert.setTitle("ATTENTION!!");
                                    alert.setMessage("Amount entered is exceeding than the total Payment of remaining months..!!");
                                    alert.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            alert.dismiss();
                                        }
                                    });
                                    alert.show();
                                }
                            } else if (n == 6) {
                                if (mon == 6) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("jun", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("july", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jun, Jul, Aug, Sep, Oct, Nov";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 7) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("july", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jul, Aug, Sep, Oct, Nov, Dec";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 8) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);

                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Aug, Sep, Oct, Nov, Dec, Jan";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 9) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Sep, Oct, Nov, Dec, Jan, Feb";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 10) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Oct, Nov, Dec, Jan, Feb, Mar";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 11) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("apr", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Nov, Dec, Jan, Feb, Mar, Apr";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 12) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("apr", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("may", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Dec, Jan, Feb, Mar, Apr, May";
                                                    generate_rid();
                                                }
                                            });
                                } else {
                                    dialog.dismiss();
                                    amt.setText("");
                                    upd.setEnabled(true);
                                    AlertDialog alert = new AlertDialog.Builder(fee_accept.this).create();
                                    alert.setTitle("ATTENTION!!");
                                    alert.setMessage("Amount entered is exceeding than the total Payment of remaining months..!!");
                                    alert.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            alert.dismiss();
                                        }
                                    });
                                    alert.show();
                                }
                            } else if (n == 7) {
                                if (mon == 6) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("jun", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("july", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jun, Jul, Aug, Sep, Oct, Nov, Dec";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 7) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("july", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jul, Aug, Sep, Oct, Nov, Dec, Jan";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 8) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Aug, Sep, Oct, Nov, Dec, Jan, Feb";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 9) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Sep, Oct, Nov, Dec, Jan, Feb, Mar";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 10) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("apr", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Oct, Nov, Dec, Jan, Feb, Mar, Apr";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 11) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("apr", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("may", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Nov, Dec, Jan, Feb, Mar, Apr, May";
                                                    generate_rid();
                                                }
                                            });
                                } else {
                                    dialog.dismiss();
                                    amt.setText("");
                                    upd.setEnabled(true);
                                    AlertDialog alert = new AlertDialog.Builder(fee_accept.this).create();
                                    alert.setTitle("ATTENTION!!");
                                    alert.setMessage("Amount entered is exceeding than the total Payment of remaining months..!!");
                                    alert.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            alert.dismiss();
                                        }
                                    });
                                    alert.show();
                                }
                            } else if (n == 8) {
                                if (mon == 6) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("jun", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("july", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jun, Jul, Aug, Sep, Oct, Nov, Dec, Jan";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 7) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("july", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jul, Aug, Sep, Oct, Nov, Dec, Jan, Feb";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 8) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Aug, Sep, Oct, Nov, Dec, Jan, Feb, Mar";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 9) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("apr", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Sep, Oct, Nov, Dec, Jan, Feb, Mar, Apr";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 10) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("apr", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("may", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Oct, Nov, Dec, Jan, Feb, Mar, Apr, May";
                                                    generate_rid();
                                                }
                                            });
                                } else {
                                    dialog.dismiss();
                                    amt.setText("");
                                    upd.setEnabled(true);
                                    AlertDialog alert = new AlertDialog.Builder(fee_accept.this).create();
                                    alert.setTitle("ATTENTION!!");
                                    alert.setMessage("Amount entered is exceeding than the total Payment of remaining months..!!");
                                    alert.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            alert.dismiss();
                                        }
                                    });
                                    alert.show();
                                }
                            } else if (n == 9) {
                                if (mon == 6) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("jun", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("july", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jun, Jul, Aug, Sep, Oct, Nov, Dec, Jan, Feb";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 7) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("july", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jul, Aug, Sep, Oct, Nov, Dec, Jan, Feb, Mar";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 8) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("apr", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Aug, Sep, Oct, Nov, Dec, Jan, Feb, Mar, Apr";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 9) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("apr", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("may", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Sep, Oct, Nov, Dec, Jan, Feb, Mar, Apr, May";
                                                    generate_rid();
                                                }
                                            });
                                } else {
                                    dialog.dismiss();
                                    amt.setText("");
                                    upd.setEnabled(true);
                                    AlertDialog alert = new AlertDialog.Builder(fee_accept.this).create();
                                    alert.setTitle("ATTENTION!!");
                                    alert.setMessage("Amount entered is exceeding than the total Payment of remaining months..!!");
                                    alert.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            alert.dismiss();
                                        }
                                    });
                                    alert.show();
                                }
                            } else if (n == 10) {
                                if (mon == 6) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("jun", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("july", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jun, Jul, Aug, Sep, Oct, Nov, Dec, Jan, Feb, Mar";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 7) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("july", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("apr", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jul, Aug, Sep, Oct, Nov, Dec, Jan, Feb, Mar, Apr";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 8) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("apr", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("may", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Aug, Sep, Oct, Nov, Dec, Jan, Feb, Mar, Apr, May";
                                                    generate_rid();
                                                }
                                            });
                                } else {
                                    dialog.dismiss();
                                    amt.setText("");
                                    upd.setEnabled(true);
                                    AlertDialog alert = new AlertDialog.Builder(fee_accept.this).create();
                                    alert.setTitle("ATTENTION!!");
                                    alert.setMessage("Amount entered is exceeding than the total Payment of remaining months..!!");
                                    alert.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            alert.dismiss();
                                        }
                                    });
                                    alert.show();
                                }
                            } else if (n == 11) {
                                if (mon == 6) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("jun", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("july", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("apr", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jun, Jul, Aug, Sep, Oct, Nov, Dec, Jan, Feb, Mar, Apr";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 7) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("july", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("apr", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("may", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jul, Aug, Sep, Oct, Nov, Dec, Jan, Feb, Mar, Apr,May";
                                                    generate_rid();
                                                }
                                            });
                                } else {
                                    dialog.dismiss();
                                    amt.setText("");
                                    AlertDialog alert = new AlertDialog.Builder(fee_accept.this).create();
                                    alert.setTitle("ATTENTION!!");
                                    alert.setMessage("Amount entered is exceeding than the total Payment of remaining months..!!");
                                    alert.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            alert.dismiss();
                                        }
                                    });
                                    alert.show();
                                }
                            } else if (n == 12) {
                                if (mon == 6) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("jun", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("july", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("apr", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("may", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jun, Jul, Aug, Sep, Oct, Nov, Dec, Jan, Feb, Mar, Apr, May";
                                                    generate_rid();
                                                }
                                            });
                                } else {
                                    dialog.dismiss();
                                    amt.setText("");
                                    upd.setEnabled(true);
                                    AlertDialog alert = new AlertDialog.Builder(fee_accept.this).create();
                                    alert.setTitle("ATTENTION!!");
                                    alert.setMessage("Amount entered is exceeding than the total Payment of remaining months..!!");
                                    alert.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            alert.dismiss();
                                        }
                                    });
                                    alert.show();
                                }
                            }
                        } else {
                            dialog.dismiss();
                            upd.setEnabled(true);
                            AlertDialog alert = new AlertDialog.Builder(fee_accept.this).create();
                            alert.setTitle("Error occurred..");
                            alert.setMessage("Updation Failed !! There might be Network issues..Please check your connection and try again later...");
                            alert.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    alert.dismiss();
                                }
                            });
                            alert.show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Amount exceeding than total fees", Toast.LENGTH_LONG).show();
                        amt.setText("");
                        dialog.dismiss();
                        upd.setEnabled(true);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please enter proper amount...", Toast.LENGTH_LONG).show();
                    amt.setText("");
                    dialog.dismiss();
                    upd.setEnabled(true);
                }
            } else {
                if (calc_month <= paid_upto) {
                    Toast.makeText(getApplicationContext(), "Please check...you might have paid fees for the month selected in your date", Toast.LENGTH_LONG).show();
                    date.setText("");
                    dialog.dismiss();
                    upd.setEnabled(true);
                }
                if (calc_month > (paid_upto + 1)) {
                    Toast.makeText(getApplicationContext(), "Please check...you might have not paid fees for few month/s prior to your selected date", Toast.LENGTH_LONG).show();
                    date.setText("");
                    dialog.dismiss();
                    upd.setEnabled(true);
                }
            }
        }
        else{
            if(month.equals("January")){
                tobe_paid = 8;
            }
            else if(month.equals("February")){
                tobe_paid = 9;
            }
            else if(month.equals("March")){
                tobe_paid = 10;
            }
            else if(month.equals("April")){
                tobe_paid = 11;
            }
            else if(month.equals("May")){
                tobe_paid = 12;
            }
            else if(month.equals("June")){
                tobe_paid = 1;
            }
            else if(month.equals("July")){
                tobe_paid = 2;
            }
            else if(month.equals("August")){
                tobe_paid = 3;
            }
            else if(month.equals("September")){
                tobe_paid = 4;
            }
            else if(month.equals("October")){
                tobe_paid = 5;
            }
            else if(month.equals("November")){
                tobe_paid = 6;
            }
            else if(month.equals("December")){
                tobe_paid = 7;
            }
            if (calc_month == tobe_paid) {
                n = am / feesmon;
                if (typcheck(n) == true) {
                    if (n <= 12) {
                        //Toast.makeText(getApplicationContext(), "Inside upload()", Toast.LENGTH_SHORT).show();
                        if (downloaded) {
                            if (n == 1) {
                                if (mon == 6) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("jun", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jun";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 7) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("july", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "July";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 8) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Aug";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 9) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Sep";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 10) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Oct";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 11) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Nov";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 12) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Dec";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 1) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jan";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 2) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Feb";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 3) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Mar";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 4) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("apr", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Apr";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 5) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("may", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "May";
                                                    generate_rid();
                                                }
                                            });
                                }
                            } else if (n == 2) {
                                if (mon == 6) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("jun", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("july", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jun,Jul";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 7) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("july", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jul, Aug";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 8) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Aug, Sep";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 9) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Sep, Oct";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 10) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Oct, Nov";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 11) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Nov, Dec";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 12) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Dec, Jan";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 1) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jan, Feb";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 2) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Feb, Mar";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 3) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("apr", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Mar, Apr";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 4) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("apr", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("may", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Apr,May";
                                                    generate_rid();
                                                }
                                            });
                                } else {
                                    dialog.dismiss();
                                    amt.setText("");
                                    upd.setEnabled(true);
                                    AlertDialog alert = new AlertDialog.Builder(fee_accept.this).create();
                                    alert.setTitle("ATTENTION!!");
                                    alert.setMessage("Amount entered is exceeding than the total Payment of remaining months..!!");
                                    alert.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            alert.dismiss();
                                        }
                                    });
                                    alert.show();
                                }
                            } else if (n == 3) {
                                if (mon == 6) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("jun", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("july", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jun,Jul,Aug";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 7) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("july", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jul, Aug, Sep";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 8) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Aug, Sep, Oct";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 9) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Sep, Oct, Nov";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 10) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Oct, Nov, Dec";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 11) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Nov, Dec, Jan";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 12) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Dec, Jan, Feb";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 1) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jan, Feb, Mar";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 2) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("apr", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Feb, Mar, Apr";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 3) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("apr", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("may", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Mar, Apr, May";
                                                    generate_rid();
                                                }
                                            });
                                } else {
                                    dialog.dismiss();
                                    amt.setText("");
                                    upd.setEnabled(true);
                                    AlertDialog alert = new AlertDialog.Builder(fee_accept.this).create();
                                    alert.setTitle("ATTENTION!!");
                                    alert.setMessage("Amount entered is exceeding than the total Payment of remaining months..!!");
                                    alert.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            alert.dismiss();
                                        }
                                    });
                                    alert.show();
                                }
                            } else if (n == 4) {
                                if (mon == 6) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("jun", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("july", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jun, Jul, Aug, Sep";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 7) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("july", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jul, Aug, Sep, Oct";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 8) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Aug, Sep, Oct, Nov";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 9) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Sep, Oct, Nov, Dec";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 10) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Oct, Nov, Dec, Jan";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 11) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Nov, Dec, Jan, Feb";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 12) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Dec, Jan, Feb, Mar";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 1) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("apr", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jan, Feb, Mar, Apr";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 2) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("apr", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("may", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Feb, Mar, Apr, May";
                                                    generate_rid();
                                                }
                                            });
                                } else {
                                    dialog.dismiss();
                                    amt.setText("");
                                    upd.setEnabled(true);
                                    AlertDialog alert = new AlertDialog.Builder(fee_accept.this).create();
                                    alert.setTitle("ATTENTION!!");
                                    alert.setMessage("Amount entered is exceeding than the total Payment of remaining months..!!");
                                    alert.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            alert.dismiss();
                                        }
                                    });
                                    alert.show();
                                }
                            } else if (n == 5) {
                                if (mon == 6) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("jun", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("july", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jun, Jul, Aug, Sep, Oct";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 7) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("july", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jul, Aug, Sep, Oct, Nov";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 8) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Aug, Sep, Oct, Nov, Dec";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 9) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Sep, Oct, Nov, Dec, Jan";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 10) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Oct, Nov, Dec, Jan, Feb";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 11) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Nov, Dec, Jan, Feb, Mar";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 12) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("apr", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Dec, Jan, Feb, Mar, Apr";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 1) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("apr", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("may", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jan, Feb, Mar, Apr, May";
                                                    generate_rid();
                                                }
                                            });
                                } else {
                                    dialog.dismiss();
                                    amt.setText("");
                                    upd.setEnabled(true);
                                    AlertDialog alert = new AlertDialog.Builder(fee_accept.this).create();
                                    alert.setTitle("ATTENTION!!");
                                    alert.setMessage("Amount entered is exceeding than the total Payment of remaining months..!!");
                                    alert.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            alert.dismiss();
                                        }
                                    });
                                    alert.show();
                                }
                            } else if (n == 6) {
                                if (mon == 6) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("jun", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("july", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jun, Jul, Aug, Sep, Oct, Nov";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 7) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("july", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jul, Aug, Sep, Oct, Nov, Dec";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 8) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);

                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Aug, Sep, Oct, Nov, Dec, Jan";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 9) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Sep, Oct, Nov, Dec, Jan, Feb";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 10) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Oct, Nov, Dec, Jan, Feb, Mar";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 11) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("apr", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Nov, Dec, Jan, Feb, Mar, Apr";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 12) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("apr", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("may", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Dec, Jan, Feb, Mar, Apr, May";
                                                    generate_rid();
                                                }
                                            });
                                } else {
                                    dialog.dismiss();
                                    amt.setText("");
                                    upd.setEnabled(true);
                                    AlertDialog alert = new AlertDialog.Builder(fee_accept.this).create();
                                    alert.setTitle("ATTENTION!!");
                                    alert.setMessage("Amount entered is exceeding than the total Payment of remaining months..!!");
                                    alert.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            alert.dismiss();
                                        }
                                    });
                                    alert.show();
                                }
                            } else if (n == 7) {
                                if (mon == 6) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("jun", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("july", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jun, Jul, Aug, Sep, Oct, Nov, Dec";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 7) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("july", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jul, Aug, Sep, Oct, Nov, Dec, Jan";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 8) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Aug, Sep, Oct, Nov, Dec, Jan, Feb";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 9) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Sep, Oct, Nov, Dec, Jan, Feb, Mar";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 10) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("apr", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Oct, Nov, Dec, Jan, Feb, Mar, Apr";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 11) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("apr", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("may", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Nov, Dec, Jan, Feb, Mar, Apr, May";
                                                    generate_rid();
                                                }
                                            });
                                } else {
                                    dialog.dismiss();
                                    amt.setText("");
                                    upd.setEnabled(true);
                                    AlertDialog alert = new AlertDialog.Builder(fee_accept.this).create();
                                    alert.setTitle("ATTENTION!!");
                                    alert.setMessage("Amount entered is exceeding than the total Payment of remaining months..!!");
                                    alert.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            alert.dismiss();
                                        }
                                    });
                                    alert.show();
                                }
                            } else if (n == 8) {
                                if (mon == 6) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("jun", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("july", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jun, Jul, Aug, Sep, Oct, Nov, Dec, Jan";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 7) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("july", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jul, Aug, Sep, Oct, Nov, Dec, Jan, Feb";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 8) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Aug, Sep, Oct, Nov, Dec, Jan, Feb, Mar";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 9) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("apr", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Sep, Oct, Nov, Dec, Jan, Feb, Mar, Apr";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 10) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("apr", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("may", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Oct, Nov, Dec, Jan, Feb, Mar, Apr, May";
                                                    generate_rid();
                                                }
                                            });
                                } else {
                                    dialog.dismiss();
                                    amt.setText("");
                                    upd.setEnabled(true);
                                    AlertDialog alert = new AlertDialog.Builder(fee_accept.this).create();
                                    alert.setTitle("ATTENTION!!");
                                    alert.setMessage("Amount entered is exceeding than the total Payment of remaining months..!!");
                                    alert.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            alert.dismiss();
                                        }
                                    });
                                    alert.show();
                                }
                            } else if (n == 9) {
                                if (mon == 6) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("jun", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("july", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jun, Jul, Aug, Sep, Oct, Nov, Dec, Jan, Feb";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 7) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("july", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jul, Aug, Sep, Oct, Nov, Dec, Jan, Feb, Mar";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 8) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("apr", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Aug, Sep, Oct, Nov, Dec, Jan, Feb, Mar, Apr";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 9) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("apr", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("may", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Sep, Oct, Nov, Dec, Jan, Feb, Mar, Apr, May";
                                                    generate_rid();
                                                }
                                            });
                                } else {
                                    dialog.dismiss();
                                    amt.setText("");
                                    upd.setEnabled(true);
                                    AlertDialog alert = new AlertDialog.Builder(fee_accept.this).create();
                                    alert.setTitle("ATTENTION!!");
                                    alert.setMessage("Amount entered is exceeding than the total Payment of remaining months..!!");
                                    alert.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            alert.dismiss();
                                        }
                                    });
                                    alert.show();
                                }
                            } else if (n == 10) {
                                if (mon == 6) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("jun", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("july", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jun, Jul, Aug, Sep, Oct, Nov, Dec, Jan, Feb, Mar";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 7) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("july", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("apr", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jul, Aug, Sep, Oct, Nov, Dec, Jan, Feb, Mar, Apr";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 8) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("apr", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("may", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Aug, Sep, Oct, Nov, Dec, Jan, Feb, Mar, Apr, May";
                                                    generate_rid();
                                                }
                                            });
                                } else {
                                    dialog.dismiss();
                                    amt.setText("");
                                    upd.setEnabled(true);
                                    AlertDialog alert = new AlertDialog.Builder(fee_accept.this).create();
                                    alert.setTitle("ATTENTION!!");
                                    alert.setMessage("Amount entered is exceeding than the total Payment of remaining months..!!");
                                    alert.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            alert.dismiss();
                                        }
                                    });
                                    alert.show();
                                }
                            } else if (n == 11) {
                                if (mon == 6) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("jun", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("july", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("apr", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jun, Jul, Aug, Sep, Oct, Nov, Dec, Jan, Feb, Mar, Apr";
                                                    generate_rid();
                                                }
                                            });
                                } else if (mon == 7) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("july", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("apr", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("may", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jul, Aug, Sep, Oct, Nov, Dec, Jan, Feb, Mar, Apr,May";
                                                    generate_rid();
                                                }
                                            });
                                } else {
                                    dialog.dismiss();
                                    amt.setText("");
                                    upd.setEnabled(true);
                                    AlertDialog alert = new AlertDialog.Builder(fee_accept.this).create();
                                    alert.setTitle("ATTENTION!!");
                                    alert.setMessage("Amount entered is exceeding than the total Payment of remaining months..!!");
                                    alert.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            alert.dismiss();
                                        }
                                    });
                                    alert.show();
                                }
                            } else if (n == 12) {
                                if (mon == 6) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("jun", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("july", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("aug", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("sept", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("oct", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("nov", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("dec", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("jan", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("feb", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("mar", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("apr", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    map.put("may", "PAID ," + sel_date + " ," + mode.getText().toString() + " ," + loggedin_user);
                                    FirebaseDatabase.getInstance().getReference().child("fees").child(sid).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    paidm = "Jun, Jul, Aug, Sep, Oct, Nov, Dec, Jan, Feb, Mar, Apr, May";
                                                    generate_rid();
                                                }
                                            });
                                } else {
                                    dialog.dismiss();
                                    amt.setText("");
                                    upd.setEnabled(true);
                                    AlertDialog alert = new AlertDialog.Builder(fee_accept.this).create();
                                    alert.setTitle("ATTENTION!!");
                                    alert.setMessage("Amount entered is exceeding than the total Payment of remaining months..!!");
                                    alert.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            alert.dismiss();
                                        }
                                    });
                                    alert.show();
                                }
                            }
                        } else {
                            dialog.dismiss();
                            upd.setEnabled(true);
                            AlertDialog alert = new AlertDialog.Builder(fee_accept.this).create();
                            alert.setTitle("Error occurred..");
                            alert.setMessage("Updation Failed !! There might be Network issues..Please check your connection and try again later...");
                            alert.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    alert.dismiss();
                                }
                            });
                            alert.show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Amount exceeding than total fees", Toast.LENGTH_LONG).show();
                        amt.setText("");
                        dialog.dismiss();
                        upd.setEnabled(true);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please enter proper amount...", Toast.LENGTH_LONG).show();
                    amt.setText("");
                    dialog.dismiss();
                    upd.setEnabled(true);
                }
            } else {
                if (calc_month < tobe_paid) {
                    Toast.makeText(getApplicationContext(), "Please check...Student might have not joined for the selected month", Toast.LENGTH_LONG).show();
                    date.setText("");
                    dialog.dismiss();
                    upd.setEnabled(true);
                }
                if (calc_month > tobe_paid) {
                    Toast.makeText(getApplicationContext(), "Please check...you might have not paid fees for few month/s prior to your selected date", Toast.LENGTH_LONG).show();
                    date.setText("");
                    dialog.dismiss();
                    upd.setEnabled(true);
                }
            }
        }
        }

    public void generate_rid(){
        RandomGenerator2 rndgen = new RandomGenerator2();
        recpt_id = rndgen.nextInt();
        verify_unique_rid(recpt_id);
        //Toast.makeText(this,""+stud_id,Toast.LENGTH_SHORT).show();
    }
    public void verify_unique_rid(int uniqueid){
        Query query = database.getReference().child("receipt").orderByChild("invoice_no").equalTo(Integer.toString(uniqueid));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount() >= 1){
                    generate_rid();
                }
                else{
                    createPDF();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                dialog.dismiss();
                upd.setEnabled(true);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void createPDF() {
        //Toast.makeText(getApplicationContext(),"Inside createpdf",Toast.LENGTH_LONG).show();

        try{
            String datesel = (date.getText().toString().trim());
            dop = formatter.parse(datesel);
            datepatternformat = new SimpleDateFormat("dd - MM - yyyy").format(dop);
            //Toast.makeText(getApplicationContext(), datepatternformat, Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){
            //
            dialog.dismiss();
            upd.setEnabled(true);
        }

        PdfDocument document=new PdfDocument();
        PdfDocument.PageInfo mypageInfo=new PdfDocument.PageInfo.Builder(365,600,1).create();
        PdfDocument.Page mypage=document.startPage(mypageInfo);
        Canvas canvas=mypage.getCanvas();
        Paint paint=new Paint();
        Paint forlinepaint=new Paint();

        paint.setTextSize(15.5f);
        paint.setColor(Color.rgb(204,51,139));
        paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));

        Bitmap bmp= BitmapFactory.decodeResource(getResources(),R.drawable.snclasses);
        Bitmap scaledBitmap=Bitmap.createScaledBitmap(bmp,60,49,true);
        canvas.drawBitmap(scaledBitmap,75,15,paint);

        canvas.drawText("S.N.CLASSES",142,27,paint);
        paint.setTextSize(8.5f);

        paint.setColor(Color.rgb(0,191,255));
        canvas.drawText("Room no.2,Near Nav Jeevan School,",142,40,paint);
        canvas.drawText("Ostwal Nagari,Nallasopara(E)",142,51,paint);
        canvas.drawText("Cont.No : 9665128322/9011660075",142,63,paint);

        forlinepaint.setStyle(Paint.Style.STROKE);
        forlinepaint.setPathEffect(new DashPathEffect(new float[]{5,5},0));
        forlinepaint.setColor(Color.rgb(0,191,255));
        forlinepaint.setStrokeWidth(2);
        canvas.drawLine(20,75,345,75,forlinepaint);

        paint.setTextSize(13.5f);
        paint.setColor(Color.rgb(144,238,144));
        canvas.drawText("STUDENT INFORMATION",112,90,paint);
        try {
            BitmapFactory.Options opt=new BitmapFactory.Options();
            opt.inScaled = false;
            File myimg = new File(this.getExternalFilesDir("/"), "Download/ProfileImage.jpg");
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(myimg),null,opt);
            Bitmap scaledBitmap1=BITMAP_RESIZER(b,90,110);
            canvas.drawBitmap(scaledBitmap1,20,103,paint);
        }
        catch(Exception e){
            dialog.dismiss();
            upd.setEnabled(true);
            Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT).show();
        }

        paint.setTextSize(12.3f);
        paint.setColor(Color.rgb(204,51,139));
        canvas.drawText("ID : ",115,108,paint);

        paint.setTextSize(11.7f);
        paint.setColor(Color.rgb(0,0,0));
        paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.NORMAL));
        canvas.drawText(sid,138,108,paint);

        paint.setTextSize(12.3f);
        paint.setColor(Color.rgb(204,51,139));
        paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
        canvas.drawText("NAME : ",115,130,paint);

        paint.setTextSize(11.7f);
        paint.setColor(Color.rgb(0,0,0));
        paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.NORMAL));
        canvas.drawText(sname,159,130,paint);

        paint.setTextSize(12.3f);
        paint.setColor(Color.rgb(204,51,139));
        paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
        canvas.drawText("STD : ",115,152,paint);

        paint.setTextSize(11.7f);
        paint.setColor(Color.rgb(0,0,0));
        paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.NORMAL));
        canvas.drawText(sstd,147,152,paint);

        paint.setTextSize(12.3f);
        paint.setColor(Color.rgb(204,51,139));
        paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
        canvas.drawText("CONT. NO : ",115,176,paint);

        paint.setTextSize(11.7f);
        paint.setColor(Color.rgb(0,0,0));
        paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.NORMAL));
        canvas.drawText(cno,176,176,paint);

        paint.setTextSize(12.3f);
        paint.setColor(Color.rgb(204,51,139));
        paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
        canvas.drawText("MAIL ID : ",115,198,paint);

        paint.setTextSize(11.7f);
        paint.setColor(Color.rgb(0,0,0));
        paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.NORMAL));
        canvas.drawText(smail,170,198,paint);

        paint.setTextSize(12.3f);
        paint.setColor(Color.rgb(204,51,139));
        paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
        canvas.drawText("FEES/MON : ",115,220,paint);

        paint.setTextSize(11.7f);
        paint.setColor(Color.rgb(0,0,0));
        paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.NORMAL));
        canvas.drawText("₹"+feesm,184,220,paint);

        canvas.drawLine(20,233,345,233,forlinepaint);

        paint.setTextSize(13.5f);
        paint.setColor(Color.rgb(144,238,144));
        paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
        canvas.drawText("FEES DETAILS",141,249,paint);

        paint.setTextSize(12.3f);
        paint.setColor(Color.rgb(204,51,139));
        paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
        canvas.drawText("RECPT. NO : ",20,267,paint);

        paint.setTextSize(11.7f);
        paint.setColor(Color.rgb(0,0,0));
        paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.NORMAL));
        canvas.drawText(Integer.toString(recpt_id),89,267,paint);

        paint.setTextSize(12.3f);
        paint.setColor(Color.rgb(204,51,139));
        paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
        canvas.drawText("DATE : ",20,289,paint);

        paint.setTextSize(11.7f);
        paint.setColor(Color.rgb(0,0,0));
        paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.NORMAL));
        canvas.drawText(""+datepatternformat,60,289,paint);

        paint.setTextSize(12.3f);
        paint.setColor(Color.rgb(204,51,139));
        paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
        canvas.drawText("AMOUNT PAID : ",20,311,paint);

        paint.setTextSize(11.7f);
        paint.setColor(Color.rgb(0,0,0));
        paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.NORMAL));
        canvas.drawText("₹"+amt.getText(),111,311,paint);

        paint.setTextSize(12.3f);
        paint.setColor(Color.rgb(204,51,139));
        paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
        canvas.drawText("PAID FOR : ",20,333,paint);

        paint.setTextSize(11.7f);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.NORMAL));
        paint.setColor(Color.rgb(0,0,0));
        canvas.drawText(""+paidm,86,333,paint);

        paint.setTextSize(12.3f);
        paint.setColor(Color.rgb(204,51,139));
        paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
        canvas.drawText("MODE : ",20,355,paint);

        paint.setTextSize(11.7f);
        paint.setColor(Color.rgb(0,0,0));
        paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.NORMAL));
        canvas.drawText(""+mode.getText(),65,355,paint);

        paint.setTextSize(12.3f);
        paint.setColor(Color.rgb(204,51,139));
        paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
        canvas.drawText("ACCEPTED BY : ",20,377,paint);


        paint.setTextSize(11.7f);
        paint.setColor(Color.rgb(0,0,0));
        paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.NORMAL));
        canvas.drawText(""+user,110,377,paint);

        rec_name = Integer.toString(recpt_id)+"_"+sname+"_of_"+sstd+"_"+System.currentTimeMillis()+".pdf";

        document.finishPage(mypage);
        File myfile=new File(this.getExternalFilesDir("/"),rec_name);
        try{
            document.writeTo(new FileOutputStream(myfile));
            filepath=Uri.fromFile(myfile);
            filesizeInBytes = myfile.length();
            DecimalFormat df = new DecimalFormat("####0.00");
            filesizeinKB = filesizeInBytes / 1024;
            filesizeinMB = filesizeinKB / 1024;
            if(filesizeInBytes < 1024){
                filesize = filesizeInBytes+" Bytes";
            }
            else if((filesizeInBytes >= 1024) && (filesizeInBytes < 1048576)){
                if(filesizeinKB % 1 != 0) {
                    filesize = df.format(filesizeinKB) + " KB";
                }
                else{
                    filesize = (long)filesizeinKB + " KB";
                }
            }
            else{
                if(filesizeinMB % 1 != 0) {
                    filesize = df.format(filesizeinMB) + " MB";
                }
                else{
                    filesize = (long)filesizeinMB + " MB";
                }
            }
            pdfstore();
        }
        catch(Exception ex){
            Toast.makeText(this,"Error Occurred.....",Toast.LENGTH_SHORT).show();
            ex.printStackTrace();
            disp(ex.getMessage());
            dialog.dismiss();
            upd.setEnabled(true);
        }
        document.close();
    }

    private void pdfstore() {
        //Toast.makeText(getApplicationContext(),"Inside pdfstore()...",Toast.LENGTH_LONG).show();
        final StorageReference reference=storage.getReference("receipts/"+rec_name);
        reference.putFile(filepath)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                receiptupload r_up = new receiptupload(Integer.toString(recpt_id), rec_name, uri.toString(), sid, sname, filesize,datePatternformat1.format(new Date().getTime()),""+System.currentTimeMillis());
                                ref3.child(String.valueOf(recpt_id)).setValue(r_up);
                                endofall(uri.toString());
                                dialog.dismiss();
                                upd.setEnabled(true);
                                //Toast.makeText(getApplicationContext(),"Uri "+uri.toString(),Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(fee_accept.this, pdfviewer.class);
                                intent.putExtra("stu_cno",cno);
                                intent.putExtra("stu_mail", smail);
                                intent.putExtra("filename", rec_name);
                                intent.putExtra("down_url", uri.toString());
                                intent.putExtra("context", "fee_accept");
                                startActivity(intent);
                                finish();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                        upd.setEnabled(true);
                    }
                });
    }

    public Bitmap BITMAP_RESIZER(Bitmap bitmap,int newWidth,int newHeight) {
        Bitmap scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);
        float ratioX = newWidth / (float) bitmap.getWidth();
        float ratioY = newHeight / (float) bitmap.getHeight();
        float middleX = newWidth / 2.0f;
        float middleY = newHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap, middleX - bitmap.getWidth() / 2, middleY - bitmap.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

        return scaledBitmap;
    }

    public void  disp(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }

    private void downloadFile(Context context, String filename, String fileextension, String destDirectory, String url) {
        //Toast.makeText(getApplicationContext(),"downloadFile called..",Toast.LENGTH_SHORT).show();
        DownloadManager dm=(DownloadManager)context.getSystemService(context.DOWNLOAD_SERVICE);
        Uri uri=Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
        request.setDestinationInExternalFilesDir(context,destDirectory,filename+fileextension);
        dm.enqueue(request);
    }

    private void endofall(String s) {
        if(check == 0){
            receipt_check(s);
        }}

    public void receipt_check(String s) {
        File fold = new File(this.getExternalFilesDir("/"), "Download");
        Query query = FirebaseDatabase.getInstance().getReference().child("receipt").orderByChild("url").equalTo(s);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount() > 0){
                    check=1;
                    if (fold.exists()) {
                        String[] entries = fold.list();
                        for (String s : entries) {
                            File currentfile = new File(fold.getPath(), s);
                            currentfile.delete();
                        }
                        fold.delete();
                    }
                    amt.setText("");
                    date.setText("");
                    mode.setText("");
                    mode.setCursorVisible(false);
                    dialog.dismiss();
                    upd.setEnabled(true);
                    Toast.makeText(getApplicationContext(), "Uploaded", Toast.LENGTH_LONG).show();
                }
                else{
                    //Toast.makeText(getApplicationContext(),"Inside re load",Toast.LENGTH_LONG).show();
                    final StorageReference reference=storage.getReference("receipts/"+rec_name);
                    reference.putFile(filepath)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    receiptupload r_up = new receiptupload(Integer.toString(recpt_id), rec_name, s, sid, sname,filesize,datePatternformat1.format(new Date().getTime()),""+System.currentTimeMillis());
                                    ref3.child(String.valueOf(recpt_id)).setValue(r_up);
                                    endofall(s);
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

   /* private class imageDownload extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            downloadFile(getApplicationContext(), "ProfileImage", ".jpg", DIRECTORY_DOWNLOADS, pimage);
                while (count <= TIMEOUT) {
                    try {
                        Thread.sleep(1000);
                        count++;
                        if (myimg.exists()) {
                            downloaded = true;
                            break;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            count=0;
            Toast.makeText(getApplication(),"downloaded = "+downloaded,Toast.LENGTH_SHORT).show();
            value_upd();
            upload();
        }
    }*/
    public void executeAsyncTask()
    {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                downloadFile(getApplicationContext(), "ProfileImage", ".jpg", DIRECTORY_DOWNLOADS, pimage);
                while (count <= TIMEOUT) {
                    try {
                        Thread.sleep(1000);
                        count++;
                        if (myimg.exists()) {
                            downloaded = true;
                            break;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
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
                count=0;
                //Toast.makeText(getApplication(),"downloaded = "+downloaded,Toast.LENGTH_SHORT).show();
                value_upd();
                upload();
            }
        };

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB/*HONEYCOMB = 11*/) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            task.execute();
        }
    }
}