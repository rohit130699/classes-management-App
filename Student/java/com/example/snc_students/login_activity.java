package com.example.snc_students;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

public class login_activity extends AppCompatActivity {
    Button btn2;
    EditText mail,pass;
    ProgressBar pb;
    ImageButton ibtn;
    //private ProgressDialog progressBar;
    //FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_activity);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        /*progressBar = new ProgressDialog(login_activity.this);
        progressBar.setTitle("Processing...");
        progressBar.setMessage("Please wait...");
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setCancelable(false);
        progressBar.setIndeterminate(true);*/

        pb = (ProgressBar)findViewById(R.id.simpleProgressBar);
        mail=(EditText)findViewById(R.id.email);
        ibtn=(ImageButton)findViewById(R.id.imgbtn);
        pass=(EditText)findViewById(R.id.password);
        btn2=(Button)findViewById(R.id.button2);

        pass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.toString().equals("")){
                    ibtn.setVisibility(View.GONE);
                }
                else {
                    ibtn.setVisibility(View.VISIBLE);
                }
            }
        });

        ibtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ibtn.getTag().toString().equals("1")){
                    pass.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    ibtn.setImageResource(R.drawable.ic_action_visibility);
                    ibtn.setTag("2");
                    pass.setSelection(pass.length());
                }
                else if(ibtn.getTag().toString().equals("2")){
                    pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    ibtn.setImageResource(R.drawable.ic_action_visibility_off);
                    ibtn.setTag("1");
                    pass.setSelection(pass.length());
                }
            }
        });

        //auth = FirebaseAuth.getInstance();
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //progressBar.show();
                pb.setVisibility(View.VISIBLE);
                String txt_mail = mail.getText().toString();
                String txt_pass = pass.getText().toString();
                loginUser(txt_mail, txt_pass);
                btn2.setEnabled(false);
            }
        });
    }
    private void loginUser(String email, String password) {
        /*auth.signInWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                startActivity(new Intent(login_activity.this,homepage.class));
                //progressBar.dismiss();
                pb.setVisibility(View.GONE);
                mail.setText("");
                pass.setText("");
                pass.setCursorVisible(false);
                finish();
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(login_activity.this,"Login Unsuccessful", Toast.LENGTH_SHORT).show();
                        btn2.setEnabled(true);
                        //progressBar.dismiss();
                        pb.setVisibility(View.GONE);
                    }
                });*/
        FirebaseDatabase.getInstance().getReference().child("students").orderByChild("mail").equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getChildrenCount() > 0){
                            for(DataSnapshot ds : dataSnapshot.getChildren()){
                                if(ds.child("pass").getValue().toString().equals(password)){
                                    pb.setVisibility(View.GONE);
                                    SharedPreferences shp = getSharedPreferences("credentials",MODE_PRIVATE);
                                    SharedPreferences.Editor myedit = shp.edit();
                                    myedit.putString("uid",ds.child("id").getValue().toString());
                                    myedit.putString("uname",ds.child("name").getValue().toString());
                                    myedit.putString("umail",email);
                                    myedit.putString("passwd",password);
                                    myedit.putString("ustd",ds.child("std").getValue().toString());
                                    myedit.putString("pimg",ds.child("pimage").getValue().toString());
                                    myedit.commit();
                                    FirebaseMessaging.getInstance().subscribeToTopic(ds.child("std").getValue().toString());
                                    Intent loginintent = new Intent(login_activity.this,home_page.class);
                                    startActivity(loginintent);
                                    finish();
                                }
                                else{
                                    Toast.makeText(getApplicationContext(),"Entered wrong Password !!",Toast.LENGTH_LONG).show();
                                    btn2.setEnabled(true);
                                    pb.setVisibility(View.GONE);
                                    pass.setText("");
                                }
                            }
                        }
                        else{
                            Toast.makeText(getApplicationContext(),"No such Student Exists !!",Toast.LENGTH_LONG).show();
                            btn2.setEnabled(true);
                            pb.setVisibility(View.GONE);
                            mail.setText("");
                            pass.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getApplicationContext(),"Some Error Occurred..Please try again later... !!",Toast.LENGTH_LONG).show();
                        btn2.setEnabled(true);
                        pb.setVisibility(View.GONE);
                        mail.setText("");
                        pass.setText("");
                    }
                });
    }
}