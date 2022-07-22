package com.example.online_class;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaderFactory;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class stu_data extends AppCompatActivity {
    EditText sid;
    FloatingActionButton btn;
    Button proceed;
    ImageView img;
    int count = 0;
    int TIMEOUT = 10;
    boolean calculated = false,notfound = false;
    TextView tv1,tv2,tv3,tv4,tv5,id,name,std,fees,mon;
    String sname,ststd,sfees,spimg,stuid,doj,jan,feb,mar,apr,may,jun,jul,aug,sep,oct,nov,dec;
    String paidm = "";
    ProgressBar pb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stu_data);
        getSupportActionBar().setTitle("Fees...");
        img=(ImageView)findViewById(R.id.imageView);
        pb = (ProgressBar)findViewById(R.id.simpleProgressBar);
        sid = (EditText) findViewById(R.id.sid);
        tv1=(TextView)findViewById(R.id.tv1);
        tv2=(TextView)findViewById(R.id.tv2);
        tv3=(TextView)findViewById(R.id.tv3);
        tv4=(TextView)findViewById(R.id.tv4);
        tv5=(TextView)findViewById(R.id.tv5);
        id=(TextView)findViewById(R.id.stuid);
        name=(TextView)findViewById(R.id.name);
        std=(TextView)findViewById(R.id.std);
        fees=(TextView)findViewById(R.id.fees);
        mon=(TextView)findViewById(R.id.mon);
        btn = (FloatingActionButton) findViewById(R.id.search);
        proceed = (Button) findViewById(R.id.proceed);

        tv1.setVisibility(View.INVISIBLE);
        tv2.setVisibility(View.INVISIBLE);
        tv3.setVisibility(View.INVISIBLE);
        tv4.setVisibility(View.INVISIBLE);
        tv5.setVisibility(View.INVISIBLE);

        img.setImageDrawable(null);

        id.setVisibility(View.INVISIBLE);
        name.setVisibility(View.INVISIBLE);
        std.setVisibility(View.INVISIBLE);
        fees.setVisibility(View.INVISIBLE);
        mon.setVisibility(View.INVISIBLE);
        proceed.setVisibility(View.INVISIBLE);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pb.setVisibility(View.VISIBLE);
               if(!"".equals(sid.getText().toString().trim())) {
                    stuid = sid.getText().toString().trim();
                    executeAsyncTask();
                    //new calc_last().execute();
                    //calc_lastpaid();
                }
                else{
                    Toast.makeText(getApplicationContext(),"No id entered...",Toast.LENGTH_LONG).show();
                    pb.setVisibility(View.GONE);
                }
            }
        });

        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(paidm.equals("May")){
                    AlertDialog alert = new AlertDialog.Builder(stu_data.this).create();
                    alert.setTitle("ATTENTION!!");
                    alert.setMessage("All Fees paid of "+sname+" i.e. upto May.Hence can't Proceed!!");
                    alert.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            alert.dismiss();
                        }
                    });
                    alert.show();
                }
                else{
                Intent intent=new Intent(stu_data.this,fee_accept.class);
                intent.putExtra("sid",stuid);
                intent.putExtra("month",paidm);
                intent.putExtra("feesm",sfees);
                startActivity(intent);
            }}
        });
    }

    private void calculate() {
        if(calculated) {
            //Toast.makeText(getApplicationContext(),"Calculate",Toast.LENGTH_LONG).show();
            Query query = FirebaseDatabase.getInstance().getReference().child("students").orderByChild("id").equalTo(sid.getText().toString().trim());
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getChildrenCount() > 0) {
                        pb.setVisibility(View.GONE);
                        for (DataSnapshot rec : dataSnapshot.getChildren()) {
                            sname = rec.child("name").getValue().toString();
                            sfees = rec.child("fees").getValue().toString();
                            ststd = rec.child("std").getValue().toString();
                            spimg = rec.child("pimage").getValue().toString();
                            tv1.setVisibility(View.VISIBLE);
                            tv2.setVisibility(View.VISIBLE);
                            tv3.setVisibility(View.VISIBLE);
                            tv4.setVisibility(View.VISIBLE);
                            tv5.setVisibility(View.VISIBLE);

                            id.setVisibility(View.VISIBLE);
                            name.setVisibility(View.VISIBLE);
                            std.setVisibility(View.VISIBLE);
                            fees.setVisibility(View.VISIBLE);
                            mon.setVisibility(View.VISIBLE);
                            proceed.setVisibility(View.VISIBLE);

                            Glide.with(getApplicationContext()).load(spimg).into(img);

                            id.setText(sid.getText().toString().trim());
                            name.setText(sname);
                            std.setText(ststd);
                            fees.setText("₹ " + sfees);
                            mon.setText(paidm);

                            sid.setText("");
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "No such record found", Toast.LENGTH_LONG).show();
                        pb.setVisibility(View.GONE);
                        sid.setText("");
                        tv1.setVisibility(View.INVISIBLE);
                        tv2.setVisibility(View.INVISIBLE);
                        tv3.setVisibility(View.INVISIBLE);
                        tv4.setVisibility(View.INVISIBLE);
                        tv5.setVisibility(View.INVISIBLE);

                        img.setImageDrawable(null);

                        id.setVisibility(View.INVISIBLE);
                        name.setVisibility(View.INVISIBLE);
                        std.setVisibility(View.INVISIBLE);
                        fees.setVisibility(View.INVISIBLE);
                        mon.setVisibility(View.INVISIBLE);
                        proceed.setVisibility(View.INVISIBLE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getApplicationContext(), "Some Error has Occurred...", Toast.LENGTH_LONG).show();
                    sid.setText("");
                }
            });
        }
        else{
            pb.setVisibility(View.GONE);
            AlertDialog alert = new AlertDialog.Builder(stu_data.this).create();
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
    }

    public void last_paid(){
        Query query1 = FirebaseDatabase.getInstance().getReference().child("fees").orderByChild("id").equalTo(sid.getText().toString().trim());
        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                if(dataSnapshot1.getChildrenCount() > 0){
                    for(DataSnapshot rec: dataSnapshot1.getChildren()) {
                        doj = rec.child("date").getValue().toString();
                        jan = rec.child("jan").getValue().toString();
                        feb = rec.child("feb").getValue().toString();
                        mar = rec.child("mar").getValue().toString();
                        apr = rec.child("apr").getValue().toString();
                        may = rec.child("may").getValue().toString();
                        jun = rec.child("jun").getValue().toString();
                        jul = rec.child("july").getValue().toString();
                        aug = rec.child("aug").getValue().toString();
                        sep = rec.child("sept").getValue().toString();
                        oct = rec.child("oct").getValue().toString();
                        nov = rec.child("nov").getValue().toString();
                        dec = rec.child("dec").getValue().toString();

                        if((!"Not Joined".equals(jun)) && (!"NOT PAID".equals(jun))){
                            paidm="June";
                        }
                        //Toast.makeText(getApplicationContext(),"paidm "+paidm,Toast.LENGTH_LONG).show();
                        if((!"Not Joined".equals(jul)) && (!"NOT PAID".equals(jul))){
                            paidm="July";
                        }
                        //Toast.makeText(getApplicationContext(),"paidm "+paidm,Toast.LENGTH_LONG).show();
                        if((!"Not Joined".equals(aug)) && (!"NOT PAID".equals(aug))){
                            paidm="August";
                        }
                        //Toast.makeText(getApplicationContext(),"paidm "+paidm,Toast.LENGTH_LONG).show();
                        if((!"Not Joined".equals(sep)) && (!"NOT PAID".equals(sep))){
                            paidm="September";
                        }
                        //Toast.makeText(getApplicationContext(),"paidm "+paidm,Toast.LENGTH_LONG).show();
                        if((!"Not Joined".equals(oct)) && (!"NOT PAID".equals(oct))){
                            paidm="October";
                        }
                        //Toast.makeText(getApplicationContext(),"paidm "+paidm,Toast.LENGTH_LONG).show();
                        if((!"Not Joined".equals(nov)) && (!"NOT PAID".equals(nov))){
                            paidm="November";
                        }
                        //Toast.makeText(getApplicationContext(),"paidm "+paidm,Toast.LENGTH_LONG).show();
                        if((!"Not Joined".equals(dec)) && (!"NOT PAID".equals(dec))){
                            paidm="December";
                        }
                        //Toast.makeText(getApplicationContext(),"paidm "+paidm,Toast.LENGTH_LONG).show();
                        if((!"Not Joined".equals(jan)) && (!"NOT PAID".equals(jan))){
                            paidm="January";
                        }
                        //Toast.makeText(getApplicationContext(),"paidm "+paidm,Toast.LENGTH_LONG).show();
                        if((!"Not Joined".equals(feb)) && (!"NOT PAID".equals(feb))){
                            paidm="February";
                        }
                        //Toast.makeText(getApplicationContext(),"paidm "+paidm,Toast.LENGTH_LONG).show();
                        if((!"Not Joined".equals(mar)) && (!"NOT PAID".equals(mar))){
                            paidm="March";
                        }
                        //Toast.makeText(getApplicationContext(),"paidm "+paidm,Toast.LENGTH_LONG).show();
                        if((!"Not Joined".equals(apr)) && (!"NOT PAID".equals(apr))){
                            paidm="April";
                        }
                        //Toast.makeText(getApplicationContext(),"paidm "+paidm,Toast.LENGTH_LONG).show();
                        if((!"Not Joined".equals(may)) && (!"NOT PAID".equals(may))){
                            paidm="May";
                        }
                        if(paidm.equals("")){
                            try {
                                Date date = new SimpleDateFormat("dd/MM/yyyy").parse(doj);
                                int m = date.getMonth();
                                //Toast.makeText(getApplicationContext(),"Month:"+m,Toast.LENGTH_LONG).show();
                                if(m == 0){
                                    paidm = "NOTHING PAID, Joined in January";
                                }
                                if(m == 1){
                                    paidm = "NOTHING PAID, Joined in February";
                                }
                                if(m == 2){
                                    paidm = "NOTHING PAID, Joined in March";
                                }
                                if(m == 3){
                                    paidm = "NOTHING PAID, Joined in April";
                                }
                                if(m == 4){
                                    paidm = "NOTHING PAID, Joined in May";
                                }
                                if(m == 5){
                                    paidm = "NOTHING PAID, Joined in June";
                                }
                                if(m == 6){
                                    paidm = "NOTHING PAID, Joined in July";
                                }
                                if(m == 7){
                                    paidm = "NOTHING PAID, Joined in August";
                                }
                                if(m == 8){
                                    paidm = "NOTHING PAID, Joined in September";
                                }
                                if(m == 9){
                                    paidm = "NOTHING PAID, Joined in October";
                                }
                                if(m == 10){
                                    paidm = "NOTHING PAID, Joined in November";
                                }
                                if(m == 11){
                                    paidm = "NOTHING PAID, Joined in December";
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                        //Toast.makeText(getApplicationContext(),"Calculating paidm",Toast.LENGTH_LONG).show();
                        //Toast.makeText(getApplicationContext(),"paidm "+paidm,Toast.LENGTH_LONG).show();

                    }}
                else{
                    if(isNetworkAvailable()){
                        notfound = true;
                    }
                }

                        /*Toast.makeText(getApplicationContext(),"jan "+jan,Toast.LENGTH_LONG).show();
                        Toast.makeText(getApplicationContext(),"feb "+feb,Toast.LENGTH_LONG).show();
                        Toast.makeText(getApplicationContext(),"mar "+mar,Toast.LENGTH_LONG).show();
                        Toast.makeText(getApplicationContext(),"apr "+apr,Toast.LENGTH_LONG).show();
                        Toast.makeText(getApplicationContext(),"may "+may,Toast.LENGTH_LONG).show();
                        Toast.makeText(getApplicationContext(),"jun "+jun,Toast.LENGTH_LONG).show();
                        Toast.makeText(getApplicationContext(),"jul "+jul,Toast.LENGTH_LONG).show();
                        Toast.makeText(getApplicationContext(),"aug "+aug,Toast.LENGTH_LONG).show();
                        Toast.makeText(getApplicationContext(),"sep "+sep,Toast.LENGTH_LONG).show();
                        Toast.makeText(getApplicationContext(),"oct "+oct,Toast.LENGTH_LONG).show();
                        Toast.makeText(getApplicationContext(),"nov "+nov,Toast.LENGTH_LONG).show();
                        Toast.makeText(getApplicationContext(),"dec "+dec,Toast.LENGTH_LONG).show();*/
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }
    public void executeAsyncTask()
    {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

            /**
             * @deprecated
             */
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                calculated = false;
                notfound = false;
                paidm = "";
            }

            @Override
            protected Void doInBackground(Void... params) {
                calculated = false;
                last_paid();
                while (count < TIMEOUT)
                {
                    try {
                        Thread.sleep(1000);
                        count++;
                        if(notfound){
                            break;
                        }
                        else if(!paidm.equals("")){
                            calculated = true;
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
                if(notfound){
                    Toast.makeText(getApplication(),"No student exists with entered ID!!",Toast.LENGTH_SHORT).show();
                    pb.setVisibility(View.GONE);
                    sid.setText("");
                    tv1.setVisibility(View.INVISIBLE);
                    tv2.setVisibility(View.INVISIBLE);
                    tv3.setVisibility(View.INVISIBLE);
                    tv4.setVisibility(View.INVISIBLE);
                    tv5.setVisibility(View.INVISIBLE);

                    img.setImageDrawable(null);

                    id.setVisibility(View.INVISIBLE);
                    name.setVisibility(View.INVISIBLE);
                    std.setVisibility(View.INVISIBLE);
                    fees.setVisibility(View.INVISIBLE);
                    mon.setVisibility(View.INVISIBLE);
                    proceed.setVisibility(View.INVISIBLE);
                }
                else{
                    //Toast.makeText(getApplication(),"calculated = "+calculated,Toast.LENGTH_SHORT).show();
                    //Toast.makeText(getApplication(),"paidm:"+paidm,Toast.LENGTH_SHORT).show();
                    calculate();
                }
            }
        };

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB/*HONEYCOMB = 11*/) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            task.execute();
        }
    }
}