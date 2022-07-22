package com.example.online_class;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.exoplayer2.C;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import java.util.HashMap;
import java.util.Map;

public class homepage extends AppCompatActivity {
Button b1,b2,b3,b4,b5,b6;
String sid,no,loguser_no,user,orguser,sname,scno,user_name;
AlertDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        b1=(Button)findViewById(R.id.buttonprofile);
        b2=(Button)findViewById(R.id.buttoneducation);
        b3=(Button)findViewById(R.id.buttonfees);
        b5=(Button)findViewById(R.id.studymaterial);
        b4=(Button)findViewById(R.id.buttongoal);
        b6=(Button)findViewById(R.id.feesrecords);
        no = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        loguser_no = no.substring(3);
        Query query = FirebaseDatabase.getInstance().getReference().child("admin").orderByChild("cno").equalTo(loguser_no);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Toast.makeText(getApplicationContext(),"Inside onDatachange()",Toast.LENGTH_LONG).show();
                if(dataSnapshot.getChildrenCount() > 0){
                    //loggedin_user = dataSnapshot.child("name").getValue().toString();
                    for(DataSnapshot rec: dataSnapshot.getChildren()) {
                        sid = rec.child("id").getValue().toString();
                        orguser = rec.child("name").getValue().toString();
                        user = orguser +"(Admin Id:"+ rec.child("id").getValue().toString()+")";
                        Toast.makeText(getApplicationContext(),"User :"+user,Toast.LENGTH_LONG).show();
                    }
                    SharedPreferences shp = getSharedPreferences("credentials",MODE_PRIVATE);
                    SharedPreferences.Editor myedit = shp.edit();
                    myedit.putString("uname",user);
                    myedit.putString("username",orguser);
                    //myedit.putString("uname","Rohit Pani");
                    myedit.commit();
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(homepage.this,show_data.class));
            }
        });
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(homepage.this,show_teachers.class));
            }
        });
        b5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(homepage.this,tablayout.class));
            }
        });
        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(homepage.this,stu_data.class));
            }
        });
        b4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(homepage.this, view_receipts.class));
            }
        });
        b6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(homepage.this, view_records.class));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.exam_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.upd_prof:
                /*final DialogPlus dialogPlus = DialogPlus.newDialog(homepage.this)
                        .setContentHolder(new ViewHolder(R.layout.update_dialog))
                        .setExpanded(true,865)
                        .setGravity(Gravity.CENTER)
                        .setCancelable(false)
                        .create();
                View myview = dialogPlus.getHolderView();*/

               AlertDialog.Builder builder = new AlertDialog.Builder(homepage.this);
                //builder.setTitle("Update Profile");
                View myview = getLayoutInflater().inflate(R.layout.update_dialog,null);
                EditText name = myview.findViewById(R.id.name);
                EditText cno = myview.findViewById(R.id.cno);
                Button ubtn = myview.findViewById(R.id.update);
                Button cbtn = myview.findViewById(R.id.cancel);
                builder.setView(myview);
                dialog = builder.create();
                dialog.setCancelable(false);
                Window window = dialog.getWindow();
                window.setLayout(WindowManager.LayoutParams.FILL_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                FirebaseDatabase.getInstance().getReference().child("admin").orderByChild("id").equalTo(sid)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot ds : dataSnapshot.getChildren()){
                                    sname = ds.child("name").getValue().toString();
                                    scno = ds.child("cno").getValue().toString();
                                }
                                name.setText(sname);
                                cno.setText(scno);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) { }
                        });
                ubtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ubtn.setEnabled(false);
                        if(name.getText().toString().equals(sname) && cno.getText().toString().equals(scno)){
                            Toast.makeText(getApplicationContext(),"No Updation Performed Yet",Toast.LENGTH_SHORT).show();
                            ubtn.setEnabled(true);
                        }
                        else if(name.getText().toString().isEmpty() || cno.getText().toString().isEmpty()){
                            Toast.makeText(getApplicationContext(),"Empty fields not allowed",Toast.LENGTH_SHORT).show();
                            ubtn.setEnabled(true);
                        }
                        else if(cno.getText().toString().length() != 10){
                            Toast.makeText(getApplicationContext(),"Contact Number not of 10 digits",Toast.LENGTH_SHORT).show();
                            ubtn.setEnabled(true);
                        }
                        else{
                            Map<String,Object> map = new HashMap<>();
                            map.put("name",name.getText().toString());
                            map.put("cno",cno.getText().toString());
                            if(!cno.getText().toString().equals(scno)){
                                FirebaseDatabase.getInstance().getReference().child("admin").orderByChild("cno").equalTo(cno.getText().toString())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.getChildrenCount() > 0){
                                                    Toast.makeText(getApplicationContext(),"Already an Admin exists with this contact no.,Please enter new one!!",Toast.LENGTH_SHORT).show();
                                                    cno.setText("");
                                                    ubtn.setEnabled(true);
                                                }
                                                else{
                                                    FirebaseDatabase.getInstance().getReference().child("admin").orderByChild("id").equalTo(sid)
                                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                                                                        ds.getRef().updateChildren(map)
                                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                    @Override
                                                                                    public void onSuccess(Void aVoid) {
                                                                                        String up_user = name.getText().toString() +"( Admin Id:"+sid+")";
                                                                                        SharedPreferences.Editor editor = getSharedPreferences("credentials",MODE_PRIVATE).edit();
                                                                                        editor.putString("uname",up_user);
                                                                                        editor.putString("username",name.getText().toString());
                                                                                        editor.apply();
                                                                                        Toast.makeText(getApplicationContext(),"Updated successfully..!!",Toast.LENGTH_SHORT).show();
                                                                                        ubtn.setEnabled(true);
                                                                                        dialog.dismiss();
                                                                                    }
                                                                                })
                                                                                .addOnFailureListener(new OnFailureListener() {
                                                                                    @Override
                                                                                    public void onFailure(@NonNull Exception e) {
                                                                                        Toast.makeText(getApplicationContext(),"Current User cannot be Updated",Toast.LENGTH_SHORT).show();
                                                                                        ubtn.setEnabled(true);
                                                                                    }
                                                                                });
                                                                    }
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                                    Toast.makeText(getApplicationContext(),"Updation Process Failed..!!",Toast.LENGTH_SHORT).show();
                                                                    ubtn.setEnabled(true);
                                                                }
                                                            });
                                                }
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) { }
                                        });
                            }
                            else{
                                FirebaseDatabase.getInstance().getReference().child("admin").orderByChild("id").equalTo(sid)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                for(DataSnapshot ds : dataSnapshot.getChildren()){
                                                    ds.getRef().updateChildren(map)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    String up_user = name.getText().toString() +"( Admin Id:"+sid+")";
                                                                    SharedPreferences.Editor editor = getSharedPreferences("credentials",MODE_PRIVATE).edit();
                                                                    editor.putString("uname",up_user);
                                                                    editor.putString("username",name.getText().toString());
                                                                    editor.apply();
                                                                    Toast.makeText(getApplicationContext(),"Updated successfully..!!",Toast.LENGTH_SHORT).show();
                                                                    ubtn.setEnabled(true);
                                                                    dialog.dismiss();
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Toast.makeText(getApplicationContext(),"Current User cannot be Updated",Toast.LENGTH_SHORT).show();
                                                                    ubtn.setEnabled(true);
                                                                }
                                                            });
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                Toast.makeText(getApplicationContext(),"Updation Process Failed..!!",Toast.LENGTH_SHORT).show();
                                                ubtn.setEnabled(true);
                                            }
                                        });
                            }

                        }
                    }
                });
                cbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    { dialog.dismiss(); }
                });
                dialog.show();
                return true;
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                SharedPreferences sh = getSharedPreferences("credentials",0);
                sh.edit().remove("uname").apply();
                sh.edit().remove("username").apply();
                Toast.makeText(getApplicationContext(),"Logged Out!!",Toast.LENGTH_SHORT).show();
                startActivity(new Intent(homepage.this, verify_user.class));
                finish();
                default:
                return super.onOptionsItemSelected(item);
        }
    }
}