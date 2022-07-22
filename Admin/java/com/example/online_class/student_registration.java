
package com.example.online_class;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
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
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.CircularPropagation;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.drjacky.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class student_registration extends AppCompatActivity {
    AutoCompleteTextView stnd,mode;
    SimpleDateFormat datePatternformat,datePatternformat1;
    FirebaseStorage storage;
    FirebaseDatabase database;
    DatabaseReference ref,ref2,ref3;
    int mon=0;
    int check = 0;
    int stud_id=0;
    int recpt_id=0;
    double n;
    FloatingActionButton brow;
    Button reg;
    Uri url,down_url,filepath;
    CircleImageView imgv;
    //ImageView imgv;
    EditText name,mail,phone,date,fees,downp,pass;
    ProgressDialog dialog;
    String paidm,rec_name,sel_date,filesize,loggedin_user,loguser_no,imgpath,no,upd_string;
    File f;
    boolean isvalidated = false;
    long filesizeInBytes,filesizeinKB,filesizeinMB;
    String[] std={"1","2","3","4","5","6","7","8","9","10"};
    String[] pmode={"By Cash","By Cheque"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_registration);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().setTitle("Add Student...");
        storage = FirebaseStorage.getInstance();
        stnd = findViewById(R.id.std);
        name = findViewById(R.id.name);
        mail = findViewById(R.id.mail);
        phone = findViewById(R.id.phone);
        date = findViewById(R.id.date);
        fees = findViewById(R.id.fees);
        downp = findViewById(R.id.downp);
        mode = findViewById(R.id.mode);
        pass = findViewById(R.id.pass);
        datePatternformat = new SimpleDateFormat("dd - MM - yyyy hh:mm a");
        datePatternformat1 = new SimpleDateFormat("dd MMM yyyy ");

        Calendar cal = Calendar.getInstance();
        final int year = cal.get(Calendar.YEAR);
        final int month = cal.get(Calendar.MONTH);
        final int day = cal.get(Calendar.DAY_OF_MONTH);

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(student_registration.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        month = month+1;
                        mon = month;
                        String exact_date = day+"/"+month+"/"+year;
                        sel_date = exact_date;
                        date.setText(exact_date);
                    }
                },year,month,day);
                datePickerDialog.show();
            }
        });
        downp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(downp.getText().toString().trim().equals("")){
                    mode.setVisibility(View.GONE);
                }
                else {
                    Integer dp = Integer.parseInt(downp.getText().toString().trim());
                    if(dp == 0){
                        mode.setVisibility(View.GONE);
                    }
                    else{
                        mode.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        database=FirebaseDatabase.getInstance();
        ref=database.getReference("students");
        ref2=database.getReference("fees");
        ref3=database.getReference("receipt");

        imgv=(CircleImageView) findViewById(R.id.imageView);
        brow=(FloatingActionButton) findViewById(R.id.browse);
        reg=(Button)findViewById(R.id.register);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.select_dialog_item,std);
        stnd.setThreshold(1);
        stnd.setAdapter(adapter);

        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.select_dialog_item,pmode);
        mode.setThreshold(1);
        mode.setAdapter(adapter1);

        brow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dexter.withActivity(student_registration.this)
                        .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                        .withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse response) {
                                //onchoosefile(view);
                                ImagePicker.Companion.with(student_registration.this)
                                        .compress(1024)
                                        .saveDir(new File(String.valueOf(getCacheDir())))
                                        .cropSquare()   			//Crop image(Optional), Check Customization for more option
                                        .start(12);
                            }
                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse response) {

                            }
                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                                token.continuePermissionRequest();
                            }
                        }).check();
            }
        });

        reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isvalidated = validate_stud();
                if(isvalidated){
                    dialog = new ProgressDialog(student_registration.this);
                    dialog.show();
                    dialog.setContentView(R.layout.progress_dialog);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    generate_id();
                }
            }
        });

        /*no = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        loguser_no = no.substring(3);
        Toast.makeText(getApplicationContext(),"User cont no with cc:"+no,Toast.LENGTH_LONG).show();
        Toast.makeText(getApplicationContext(),"User cont no without cc:"+loguser_no,Toast.LENGTH_LONG).show();
        Query query = FirebaseDatabase.getInstance().getReference().child("admin").orderByChild("cno").equalTo(loguser_no);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Toast.makeText(getApplicationContext(),"Inside onDatachange()",Toast.LENGTH_LONG).show();
                if(dataSnapshot.getChildrenCount() > 0){
                    //loggedin_user = dataSnapshot.child("name").getValue().toString();
                    for(DataSnapshot rec: dataSnapshot.getChildren()) {
                        loggedin_user = rec.child("name").getValue().toString();
                        Toast.makeText(getApplicationContext(),"User :"+loggedin_user,Toast.LENGTH_LONG).show();
                }}
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });*/
        SharedPreferences sh = getSharedPreferences("credentials",getApplicationContext().MODE_PRIVATE);
        loggedin_user = sh.getString("uname",null);
        //Toast.makeText(getApplicationContext(),"User :"+loggedin_user,Toast.LENGTH_LONG).show();
    }

    private boolean validate_stud() {
        reg.setEnabled(false);
        if(name.getText().toString().trim().equals("") || stnd.getText().toString().trim().equals("") || mail.getText().toString().trim().equals("") || phone.getText().toString().trim().equals("") || date.getText().toString().trim().equals("") || fees.getText().toString().trim().equals("") || pass.getText().toString().trim().equals("")){
            Toast.makeText(getApplicationContext(),"Some data is missing out !! Please fill out them..",Toast.LENGTH_SHORT).show();
            reg.setEnabled(true);
            return false;
        }
        if(imgv.getDrawable().getConstantState() == getResources().getDrawable(R.drawable.profile_pic).getConstantState()) {
            Toast.makeText(getApplicationContext(),"No Profile image selected..Please select any Profile image!!",Toast.LENGTH_SHORT).show();
            reg.setEnabled(true);
            //name.setText("");
            return false;
        }
        if(!name.getText().toString().trim().matches("^[A-Za-z ]+$")) {
            Toast.makeText(getApplicationContext(),"Name entered might contain characters except letters..Please check!!",Toast.LENGTH_SHORT).show();
            reg.setEnabled(true);
            //name.setText("");
            return false;
        }
        if(!stnd.getText().toString().trim().equals("1") && !stnd.getText().toString().trim().equals("2") && !stnd.getText().toString().trim().equals("3") && !stnd.getText().toString().trim().equals("4") && !stnd.getText().toString().trim().equals("5") && !stnd.getText().toString().trim().equals("6") && !stnd.getText().toString().trim().equals("7") && !stnd.getText().toString().trim().equals("8") && !stnd.getText().toString().trim().equals("9") && !stnd.getText().toString().trim().equals("10")){
            Toast.makeText(getApplicationContext(),"Not entered valid Standard..Please check!!",Toast.LENGTH_SHORT).show();
            reg.setEnabled(true);
            //stnd.setText("");
            return false;
        }
        if(!mail.getText().toString().trim().matches("^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$")){
            Toast.makeText(getApplicationContext(),"Mail entered is in incorrect format..Please check!!",Toast.LENGTH_SHORT).show();
            reg.setEnabled(true);
            //mail.setText("");
            return false;
        }
        if(!phone.getText().toString().trim().matches("^[6789]\\d{9}$")){
            Toast.makeText(getApplicationContext(),"Contact No. entered is in incorrect format..Please check!!",Toast.LENGTH_SHORT).show();
            reg.setEnabled(true);
            //phone.setText("");
            return false;
        }
        if(!date.getText().toString().trim().matches("^\\d{1,2}\\/\\d{1,2}\\/\\d{4}$")){
            Toast.makeText(getApplicationContext(),"Date entered is in incorrect format..Please check!!",Toast.LENGTH_SHORT).show();
            reg.setEnabled(true);
            //date.setText("");
            return false;
        }
        if(!fees.getText().toString().trim().matches("\\d+")){
            Toast.makeText(getApplicationContext(),"Fees entered might contain characters except numbers..Please check!!",Toast.LENGTH_SHORT).show();
            reg.setEnabled(true);
            //fees.setText("");
            return false;
        }
        if(downp.getText().toString().trim().equals("")){
            Toast.makeText(getApplicationContext(),"Downpayment is not entered..If you dont have anything as downpayment please enter 0 !!",Toast.LENGTH_LONG).show();
            reg.setEnabled(true);
            return false;
        }
        else{
            if(!downp.getText().toString().trim().matches("\\d+")){
                Toast.makeText(getApplicationContext(),"Downpayment entered might contain characters except numbers..Please check!!",Toast.LENGTH_SHORT).show();
                reg.setEnabled(true);
                return false;
            }
            else{
                Integer dp = Integer.parseInt(downp.getText().toString().trim());
                if(dp != 0){
                    if(!mode.getText().toString().trim().equals("By Cash") && !mode.getText().toString().trim().equals("By Cheque")){
                        Toast.makeText(getApplicationContext(),"Mode should be either 'By Cash' or 'By Cheque'!!",Toast.LENGTH_SHORT).show();
                        reg.setEnabled(true);
                        return false;
                    }
                }
            }
        }
        if(pass.getText().toString().trim().length() < 8){
            Toast.makeText(getApplicationContext(),"Length of password should be of mimimum 8 characters!!",Toast.LENGTH_SHORT).show();
            reg.setEnabled(true);
            return false;
        }
        if(!pass.getText().toString().trim().matches("^(?=.*[0-9]).{1,}$")){
            Toast.makeText(getApplicationContext(),"Password must contain atleast one numeric character",Toast.LENGTH_SHORT).show();
            reg.setEnabled(true);
            return false;
        }
        if(!pass.getText().toString().trim().matches("^(?=.*[!@#$%^&+=]).{1,}$")){
            Toast.makeText(getApplicationContext(),"Password must contain atleast one special character out of [!@#$%^&+=]",Toast.LENGTH_SHORT).show();
            reg.setEnabled(true);
            return false;
        }
        return true;
    }

    public void generate_id(){
        FirebaseDatabase.getInstance().getReference().child("students").orderByChild("mail").equalTo(mail.getText().toString().trim())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getChildrenCount() > 0){
                            Toast.makeText(getApplicationContext(),"Already a student exists with given mail id!Please Change it !!",Toast.LENGTH_SHORT).show();
                            mail.setText("");
                            dialog.dismiss();
                            reg.setEnabled(true);
                        }
                        else{
                            //Toast.makeText(getApplicationContext(),"Inside generateid()....",Toast.LENGTH_LONG).show();
                            RandomGenerator rndgen = new RandomGenerator();
                            stud_id = rndgen.nextInt();
                            verify_unique(stud_id);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getApplicationContext(),"Some Error Occurred!!",Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        reg.setEnabled(true);
                    }
                });
    }
    public void verify_unique(int uniqueid){
        Query query = database.getReference().child("student").orderByChild("id").equalTo(Integer.toString(uniqueid));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount() >= 1){
                    generate_id();
                }
                else{
                    generate_rid();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                reg.setEnabled(true);
                dialog.dismiss();}
        });
    }
    public void generate_rid(){
        //Toast.makeText(getApplicationContext(),"Inside generaterid()....",Toast.LENGTH_LONG).show();
        RandomGenerator2 rndgen = new RandomGenerator2();
        recpt_id = rndgen.nextInt();
        //Toast.makeText(this,""+recpt_id,Toast.LENGTH_SHORT).show();
        verify_unique_rid(recpt_id);
    }
    public void verify_unique_rid(int uniqueid){
        //Toast.makeText(getApplicationContext(),"Inside verify unique rid()....",Toast.LENGTH_LONG).show();
        Query query = database.getReference().child("receipt").orderByChild("invoice_no").equalTo(Integer.toString(uniqueid));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount() >= 1){
                    //Toast.makeText(getApplicationContext(),uniqueid+" occurrences found...",Toast.LENGTH_LONG).show();
                    generate_rid();
                }
                else{
                    upload();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                reg.setEnabled(true);
                dialog.dismiss();
            }
        });
    }

    private boolean typcheck(double d) {
        double tol = 1E-5;
        return Math.abs(Math.floor(d)-d) < tol;
    }

    @Override
    protected void onActivityResult(int requestcode, int resultcode, @Nullable Intent data) {
        super.onActivityResult(requestcode, resultcode, data);
        /*if(requestcode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultcode == RESULT_OK){
                url= result.getUri();
                imgpath = result.getUri().getPath();
                Toast.makeText(getApplicationContext(),""+imgpath,Toast.LENGTH_LONG).show();
                imgv.setImageURI(url);
            }
            else if(resultcode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Exception e= result.getError();
                Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }*/
        if(requestcode == 12 && resultcode == RESULT_OK){
            url = data.getData();
            imgpath = data.getData().getPath();
            imgv.setImageURI(url);
        }
    }

    private void upload(){
        //Toast.makeText(getApplicationContext(),"Inside upload()....",Toast.LENGTH_LONG).show();
        double dp = Double.parseDouble(downp.getText().toString().trim());
        double feesm = Double.parseDouble(fees.getText().toString().trim());
        n=dp/feesm;
        if(typcheck(n) == true) {
            if(n <= 12){
                final StorageReference uploader = storage.getReference("profile/Image" +  Integer.toString(stud_id));
                uploader.putFile(url)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                uploader.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        down_url=uri;
                                        upd_string = "PAID ,"+sel_date+" ,"+mode.getText().toString().trim()+" , By "+loggedin_user;
                                        dataholder obj = new dataholder(Integer.toString(stud_id), name.getText().toString().trim(), name.getText().toString().trim().toLowerCase(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(), downp.getText().toString().trim(), uri.toString().trim(),pass.getText().toString().trim(),loggedin_user);
                                        ref.child(String.valueOf(stud_id)).setValue(obj);

                                        if(n == 0){
                                            if(mon == 6){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),"NOT PAID","NOT PAID","NOT PAID","NOT PAID","NOT PAID","NOT PAID","NOT PAID","NOT PAID","NOT PAID","NOT PAID","NOT PAID","NOT PAID");
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                finalcall();
                                            }
                                            if(mon == 7){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),"NOT PAID","NOT PAID","NOT PAID","NOT PAID","NOT PAID","Not Joined","NOT PAID","NOT PAID","NOT PAID","NOT PAID","NOT PAID","NOT PAID");
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                finalcall();
                                            }
                                            if(mon == 8){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),"NOT PAID","NOT PAID","NOT PAID","NOT PAID","NOT PAID","Not Joined","Not Joined","NOT PAID","NOT PAID","NOT PAID","NOT PAID","NOT PAID");
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                finalcall();
                                            }
                                            if(mon == 9){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),"NOT PAID","NOT PAID","NOT PAID","NOT PAID","NOT PAID","Not Joined","Not Joined","Not Joined","NOT PAID","NOT PAID","NOT PAID","NOT PAID");
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                finalcall();
                                            }
                                            if(mon == 10){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),"NOT PAID","NOT PAID","NOT PAID","NOT PAID","NOT PAID","Not Joined","Not Joined","Not Joined","Not Joined","NOT PAID","NOT PAID","NOT PAID");
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                finalcall();
                                            }
                                            if(mon == 11){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),"NOT PAID","NOT PAID","NOT PAID","NOT PAID","NOT PAID","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined","NOT PAID","NOT PAID");
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                finalcall();
                                            }
                                            if(mon == 12){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),"NOT PAID","NOT PAID","NOT PAID","NOT PAID","NOT PAID","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined","NOT PAID");
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                finalcall();
                                            }
                                            if(mon == 1){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),"NOT PAID","NOT PAID","NOT PAID","NOT PAID","NOT PAID","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined");
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                finalcall();
                                            }
                                            if(mon == 2){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),"Not Joined","NOT PAID","NOT PAID","NOT PAID","NOT PAID","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined");
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                finalcall();
                                            }
                                            if(mon == 3){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),"Not Joined","Not Joined","NOT PAID","NOT PAID","NOT PAID","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined");
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                finalcall();
                                            }
                                            if(mon == 4){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),"Not Joined","Not Joined","Not Joined","NOT PAID","NOT PAID","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined");
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                finalcall();
                                            }
                                            if(mon == 5){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),"Not Joined","Not Joined","Not Joined","Not Joined","NOT PAID","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined");
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                finalcall();
                                            }
                                        }
                                        else if(n == 1){
                                            if(mon == 6){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),"NOT PAID","NOT PAID","NOT PAID","NOT PAID","NOT PAID",upd_string,"NOT PAID","NOT PAID","NOT PAID","NOT PAID","NOT PAID","NOT PAID");
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Jun";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 7){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),"NOT PAID","NOT PAID","NOT PAID","NOT PAID","NOT PAID","Not Joined",upd_string,"NOT PAID","NOT PAID","NOT PAID","NOT PAID","NOT PAID");
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Jul";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 8){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),"NOT PAID","NOT PAID","NOT PAID","NOT PAID","NOT PAID","Not Joined","Not Joined",upd_string,"NOT PAID","NOT PAID","NOT PAID","NOT PAID");
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Aug";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 9){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),"NOT PAID","NOT PAID","NOT PAID","NOT PAID","NOT PAID","Not Joined","Not Joined","Not Joined",upd_string,"NOT PAID","NOT PAID","NOT PAID");
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Sep";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 10){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),"NOT PAID","NOT PAID","NOT PAID","NOT PAID","NOT PAID","Not Joined","Not Joined","Not Joined","Not Joined",upd_string,"NOT PAID","NOT PAID");
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Oct";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 11){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),"NOT PAID","NOT PAID","NOT PAID","NOT PAID","NOT PAID","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined",upd_string,"NOT PAID");
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Nov";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 12){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),"NOT PAID","NOT PAID","NOT PAID","NOT PAID","NOT PAID","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined",upd_string);
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Dec";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 1){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),upd_string,"NOT PAID","NOT PAID","NOT PAID","NOT PAID","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined");
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Jan";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 2){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),"Not Joined",upd_string,"NOT PAID","NOT PAID","NOT PAID","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined");
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Feb";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 3){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),"Not Joined","Not Joined",upd_string,"NOT PAID","NOT PAID","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined");
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Mar";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 4){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),"Not Joined","Not Joined","Not Joined",upd_string,"NOT PAID","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined");
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Apr";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 5){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),"Not Joined","Not Joined","Not Joined","Not Joined",upd_string,"Not Joined","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined");
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "May";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                        else if(n == 2){
                                            if(mon == 6){

                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),"NOT PAID","NOT PAID","NOT PAID","NOT PAID","NOT PAID",upd_string,upd_string,"NOT PAID","NOT PAID","NOT PAID","NOT PAID","NOT PAID");
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Jun, Jul";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 7){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),"NOT PAID","NOT PAID","NOT PAID","NOT PAID","NOT PAID","Not Joined",upd_string,upd_string,"NOT PAID","NOT PAID","NOT PAID","NOT PAID");
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Jul, Aug";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 8){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),"NOT PAID","NOT PAID","NOT PAID","NOT PAID","NOT PAID","Not Joined","Not Joined",upd_string,upd_string,"NOT PAID","NOT PAID","NOT PAID");
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Aug, Sep";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 9){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),"NOT PAID","NOT PAID","NOT PAID","NOT PAID","NOT PAID","Not Joined","Not Joined","Not Joined",upd_string,upd_string,"NOT PAID","NOT PAID");
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Sep, Oct";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 10){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),"NOT PAID","NOT PAID","NOT PAID","NOT PAID","NOT PAID","Not Joined","Not Joined","Not Joined","Not Joined",upd_string,upd_string,"NOT PAID");
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Oct, Nov";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 11){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),"NOT PAID","NOT PAID","NOT PAID","NOT PAID","NOT PAID","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined",upd_string,upd_string);
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Nov, Dec";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 12){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),upd_string,"NOT PAID","NOT PAID","NOT PAID","NOT PAID","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined",upd_string);
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Dec, Jan";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 1){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),upd_string,upd_string,"NOT PAID","NOT PAID","NOT PAID","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined");
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Jan, Feb";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 2){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),"Not Joined",upd_string,upd_string,"NOT PAID","NOT PAID","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined");
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Feb, Mar";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 3){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),"Not Joined","Not Joined",upd_string,upd_string,"NOT PAID","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined");
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Mar, Apr";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 4){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),"Not Joined","Not Joined","Not Joined",upd_string,upd_string,"Not Joined","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined");
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Apr, May";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                        else if(n == 3){
                                            if(mon == 6){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),"NOT PAID","NOT PAID","NOT PAID","NOT PAID","NOT PAID",upd_string,upd_string,upd_string,"NOT PAID","NOT PAID","NOT PAID","NOT PAID");
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Jun, Jul, Aug";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 7){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),"NOT PAID","NOT PAID","NOT PAID","NOT PAID","NOT PAID","Not Joined",upd_string,upd_string,upd_string,"NOT PAID","NOT PAID","NOT PAID");
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Jul, Aug, Sep";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 8){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),"NOT PAID","NOT PAID","NOT PAID","NOT PAID","NOT PAID","Not Joined","Not Joined",upd_string,upd_string,upd_string,"NOT PAID","NOT PAID");
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Aug, Sep, Oct";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 9){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),"NOT PAID","NOT PAID","NOT PAID","NOT PAID","NOT PAID","Not Joined","Not Joined","Not Joined",upd_string,upd_string,upd_string,"NOT PAID");
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Sep, Oct, Nov";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 10){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),"NOT PAID","NOT PAID","NOT PAID","NOT PAID","NOT PAID","Not Joined","Not Joined","Not Joined","Not Joined",upd_string,upd_string,upd_string);
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Oct, Nov, Dec";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 11){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),upd_string,"NOT PAID","NOT PAID","NOT PAID","NOT PAID","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined",upd_string,upd_string);
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Nov, Dec, Jan";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 12){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),upd_string,upd_string,"NOT PAID","NOT PAID","NOT PAID","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined",upd_string);
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Dec, Jan, Feb";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 1){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),upd_string,upd_string,upd_string,"NOT PAID","NOT PAID","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined");
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Jan, Feb, Mar";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 2){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),"Not Joined",upd_string,upd_string,upd_string,"NOT PAID","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined");
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Feb, Mar, Apr";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 3){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),"Not Joined","Not Joined",upd_string,upd_string,upd_string,"Not Joined","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined");
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Mar, Apr, May";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                        else if(n == 4){
                                            if(mon == 6){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),"NOT PAID","NOT PAID","NOT PAID","NOT PAID","NOT PAID",upd_string,upd_string,upd_string,upd_string,"NOT PAID","NOT PAID","NOT PAID");
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Jun, Jul, Aug, Sep";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 7){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),"NOT PAID","NOT PAID","NOT PAID","NOT PAID","NOT PAID","Not Joined",upd_string,upd_string,upd_string,upd_string,"NOT PAID","NOT PAID");
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Jul, Aug, Sep, Oct";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 8){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),"NOT PAID","NOT PAID","NOT PAID","NOT PAID","NOT PAID","Not Joined","Not Joined",upd_string,upd_string,upd_string,upd_string,"NOT PAID");
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Aug, Sep, Oct, Nov";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 9){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),"NOT PAID","NOT PAID","NOT PAID","NOT PAID","NOT PAID","Not Joined","Not Joined","Not Joined",upd_string,upd_string,upd_string,upd_string);
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Sep, Oct, Nov, Dec";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 10){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),upd_string,"NOT PAID","NOT PAID","NOT PAID","NOT PAID","Not Joined","Not Joined","Not Joined","Not Joined",upd_string,upd_string,upd_string);
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Oct, Nov, Dec, Jan";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 11){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),upd_string,upd_string,"NOT PAID","NOT PAID","NOT PAID","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined",upd_string,upd_string);
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Nov, Dec, Jan, Feb";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 12){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),upd_string,upd_string,upd_string,"NOT PAID","NOT PAID","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined",upd_string);
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Dec, Jan, Feb, Mar";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 1){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),upd_string,upd_string,upd_string,upd_string,"NOT PAID","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined");
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Jan, Feb, Mar, Apr";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 2){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),"Not Joined",upd_string,upd_string,upd_string,upd_string,"Not Joined","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined");
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Feb, Mar, Apr, May";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                        else if(n == 5){
                                            if(mon == 6){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),"NOT PAID","NOT PAID","NOT PAID","NOT PAID","NOT PAID",upd_string,upd_string,upd_string,upd_string,upd_string,"NOT PAID","NOT PAID");
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Jun, Jul, Aug, Sep, Oct";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 7){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),"NOT PAID","NOT PAID","NOT PAID","NOT PAID","NOT PAID","Not Joined",upd_string,upd_string,upd_string,upd_string,upd_string,"NOT PAID");
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Jul, Aug, Sep, Oct, Nov";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 8){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),"NOT PAID","NOT PAID","NOT PAID","NOT PAID","NOT PAID","Not Joined","Not Joined",upd_string,upd_string,upd_string,upd_string,upd_string);
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Aug, Sep, Oct, Nov, Dec";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 9){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),upd_string,"NOT PAID","NOT PAID","NOT PAID","NOT PAID","Not Joined","Not Joined","Not Joined",upd_string,upd_string,upd_string,upd_string);
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Sep, Oct, Nov, Dec, Jan";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 10){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),upd_string,upd_string,"NOT PAID","NOT PAID","NOT PAID","Not Joined","Not Joined","Not Joined","Not Joined",upd_string,upd_string,upd_string);
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Oct, Nov, Dec, Jan, Feb";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 11){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),upd_string,upd_string,upd_string,"NOT PAID","NOT PAID","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined",upd_string,upd_string);
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Nov, Dec, Jan, Feb, Mar";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 12){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),upd_string,upd_string,upd_string,upd_string,"NOT PAID","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined",upd_string);
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Dec, Jan, Feb, Mar, Apr";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 1){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),upd_string,upd_string,upd_string,upd_string,upd_string,"Not Joined","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined");
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Jan, Feb, Mar, Apr, May";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                        else if(n == 6){
                                            if(mon == 6){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),"NOT PAID","NOT PAID","NOT PAID","NOT PAID","NOT PAID",upd_string,upd_string,upd_string,upd_string,upd_string,upd_string,"NOT PAID");
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Jun, Jul, Aug, Sep, Oct, Nov";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 7){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),"NOT PAID","NOT PAID","NOT PAID","NOT PAID","NOT PAID","Not Joined",upd_string,upd_string,upd_string,upd_string,upd_string,upd_string);
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Jul, Aug, Sep, Oct, Nov, Dec";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 8){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),upd_string,"NOT PAID","NOT PAID","NOT PAID","NOT PAID","Not Joined","Not Joined",upd_string,upd_string,upd_string,upd_string,upd_string);
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Aug, Sep, Oct, Nov, Dec, Jan";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 9){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),upd_string,upd_string,"NOT PAID","NOT PAID","NOT PAID","Not Joined","Not Joined","Not Joined",upd_string,upd_string,upd_string,upd_string);
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Sep, Oct, Nov, Dec, Jan, Feb";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 10){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),upd_string,upd_string,upd_string,"NOT PAID","NOT PAID","Not Joined","Not Joined","Not Joined","Not Joined",upd_string,upd_string,upd_string);
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Oct, Nov, Dec, Jan, Feb, Mar";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 11){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),upd_string,upd_string,upd_string,upd_string,"NOT PAID","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined",upd_string,upd_string);
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Nov, Dec, Jan, Feb, Mar, Apr";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 12){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),upd_string,upd_string,upd_string,upd_string,upd_string,"Not Joined","Not Joined","Not Joined","Not Joined","Not Joined","Not Joined",upd_string);
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Dec, Jan, Feb, Mar, Apr, May";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                        else if(n == 7){
                                            if(mon == 6){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),"NOT PAID","NOT PAID","NOT PAID","NOT PAID","NOT PAID",upd_string,upd_string,upd_string,upd_string,upd_string,upd_string,upd_string);
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Jun, Jul, Aug, Sep, Oct, Nov, Dec";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 7){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),upd_string,"NOT PAID","NOT PAID","NOT PAID","NOT PAID","Not Joined",upd_string,upd_string,upd_string,upd_string,upd_string,upd_string);
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Jul, Aug, Sep, Oct, Nov, Dec, Jan";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 8){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),upd_string,upd_string,"NOT PAID","NOT PAID","NOT PAID","Not Joined","Not Joined",upd_string,upd_string,upd_string,upd_string,upd_string);
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Aug, Sep, Oct, Nov, Dec, Jan, Feb";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 9){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),upd_string,upd_string,upd_string,"NOT PAID","NOT PAID","Not Joined","Not Joined","Not Joined",upd_string,upd_string,upd_string,upd_string);
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Sep, Oct, Nov, Dec, Jan, Feb, Mar";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 10){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),upd_string,upd_string,upd_string,upd_string,"NOT PAID","Not Joined","Not Joined","Not Joined","Not Joined",upd_string,upd_string,upd_string);
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Oct, Nov, Dec, Jan, Feb, Mar, Apr";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 11){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),upd_string,upd_string,upd_string,upd_string,upd_string,"Not Joined","Not Joined","Not Joined","Not Joined","Not Joined",upd_string,upd_string);
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Nov, Dec, Jan, Feb, Mar, Apr, May";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                        else if(n == 8){
                                            if(mon == 6){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),upd_string,"NOT PAID","NOT PAID","NOT PAID","NOT PAID",upd_string,upd_string,upd_string,upd_string,upd_string,upd_string,upd_string);
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Jun, Jul, Aug, Sep, Oct, Nov, Dec, Jan";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 7){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),upd_string,upd_string,"NOT PAID","NOT PAID","NOT PAID","Not Joined",upd_string,upd_string,upd_string,upd_string,upd_string,upd_string);
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Jul, Aug, Sep, Oct, Nov, Dec, Jan, Feb";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 8){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),upd_string,upd_string,upd_string,"NOT PAID","NOT PAID","Not Joined","Not Joined",upd_string,upd_string,upd_string,upd_string,upd_string);
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Aug, Sep, Oct, Nov, Dec, Jan, Feb, Mar";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 9){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),upd_string,upd_string,upd_string,upd_string,"NOT PAID","Not Joined","Not Joined","Not Joined",upd_string,upd_string,upd_string,upd_string);
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Sep, Oct, Nov, Dec, Jan, Feb, Mar, Apr";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 10){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),upd_string,upd_string,upd_string,upd_string,upd_string,"Not Joined","Not Joined","Not Joined","Not Joined",upd_string,upd_string,upd_string);
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Oct, Nov, Dec, Jan, Feb, Mar, Apr, May";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                        else if(n == 9){
                                            if(mon == 6){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),upd_string,upd_string,"NOT PAID","NOT PAID","NOT PAID",upd_string,upd_string,upd_string,upd_string,upd_string,upd_string,upd_string);
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Jun, Jul, Aug, Sep, Oct, Nov, Dec, Jan, Feb";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 7){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),upd_string,upd_string,upd_string,"NOT PAID","NOT PAID","Not Joined",upd_string,upd_string,upd_string,upd_string,upd_string,upd_string);
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Jul, Aug, Sep, Oct, Nov, Dec, Jan, Feb, Mar";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 8){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),upd_string,upd_string,upd_string,upd_string,"NOT PAID","Not Joined","Not Joined",upd_string,upd_string,upd_string,upd_string,upd_string);
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Aug, Sep, Oct, Nov, Dec, Jan, Feb, Mar, Apr";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 9){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),upd_string,upd_string,upd_string,upd_string,upd_string,"Not Joined","Not Joined","Not Joined",upd_string,upd_string,upd_string,upd_string);
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Sep, Oct, Nov, Dec, Jan, Feb, Mar, Apr, May";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                        else if(n == 10){
                                            if(mon == 6){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),upd_string,upd_string,upd_string,"NOT PAID","NOT PAID",upd_string,upd_string,upd_string,upd_string,upd_string,upd_string,upd_string);
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Jun, Jul, Aug, Sep, Oct, Nov, Dec, Jan, Feb, Mar";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();

                                                }
                                            }
                                            else if(mon == 7){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),upd_string,upd_string,upd_string,upd_string,"NOT PAID","Not Joined",upd_string,upd_string,upd_string,upd_string,upd_string,upd_string);
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Jul, Aug, Sep, Oct, Nov, Dec, Jan, Feb, Mar, Apr";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 8){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),upd_string,upd_string,upd_string,upd_string,upd_string,"Not Joined","Not Joined",upd_string,upd_string,upd_string,upd_string,upd_string);
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Aug, Sep, Oct, Nov, Dec, Jan, Feb, Mar, Apr, May";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                        else if(n == 11){
                                            if(mon == 6){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),upd_string,upd_string,upd_string,upd_string,"NOT PAID",upd_string,upd_string,upd_string,upd_string,upd_string,upd_string,upd_string);
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Jun, Jul, Aug, Sep, Oct, Nov, Dec, Jan, Feb, Mar, Apr";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(mon == 7){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),upd_string,upd_string,upd_string,upd_string,upd_string,"Not Joined",upd_string,upd_string,upd_string,upd_string,upd_string,upd_string);
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Jul, Aug, Sep, Oct, Nov, Dec, Jan, Feb, Mar, Apr,May";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                        else if(n == 12){
                                            if(mon == 6){
                                                feesupload fe = new feesupload(Integer.toString(stud_id), name.getText().toString().trim(), stnd.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), fees.getText().toString().trim(),pass.getText().toString().trim(),downp.getText().toString().trim(), uri.toString().trim(),upd_string,upd_string,upd_string,upd_string,upd_string,upd_string,upd_string,upd_string,upd_string,upd_string,upd_string,upd_string);
                                                ref2.child(String.valueOf(stud_id)).setValue(fe);
                                                paidm = "Jun, Jul, Aug, Sep, Oct, Nov, Dec, Jan, Feb, Mar, Apr, May";
                                                try {
                                                    createPDF();
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    }
                                });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                dialog.dismiss();
                                Toast.makeText(getApplicationContext(), "Some Error Occurred!! Upload Failed..", Toast.LENGTH_LONG).show();
                                reg.setEnabled(true);
                            }
                        });
            }
            else{
                dialog.dismiss();
                Toast.makeText(getApplicationContext(), "Downpayment exceeding than total fees", Toast.LENGTH_LONG).show();
                downp.setText("");
                reg.setEnabled(true);
            }
        }
        else{
            dialog.dismiss();
            Toast.makeText(getApplicationContext(), "Please enter proper Downpayment...", Toast.LENGTH_LONG).show();
            downp.setText("");
            reg.setEnabled(true);
        }
    }

    private void finalcall() {
        dialog.dismiss();
        name.setText("");
        stnd.setText("");
        mail.setText("");
        phone.setText("");
        date.setText("");
        fees.setText("");
        downp.setText("");
        mode.setText("");
        pass.setText("");
        downp.setCursorVisible(false);
        imgv.setImageResource(R.drawable.profile_pic);
        Toast.makeText(getApplicationContext(), "Uploaded", Toast.LENGTH_LONG).show();
        reg.setEnabled(true);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void createPDF() throws PackageManager.NameNotFoundException {
        //Toast.makeText(getApplicationContext(),"Inside createpdf",Toast.LENGTH_LONG).show();
        f= new File(imgpath);
        //Toast.makeText(getApplicationContext(),""+f.getName(),Toast.LENGTH_LONG).show();
        PackageManager m = getPackageManager();
        String s = getPackageName();
        PackageInfo p = m.getPackageInfo(s,0);
        s = p.applicationInfo.dataDir;
        //Toast.makeText(getApplicationContext(),""+s,Toast.LENGTH_LONG).show();
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
            File myimg = new File(s+"/cache/"+f.getName());
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(myimg),null,opt);
            Bitmap scaledBitmap1=BITMAP_RESIZER(b,90,110);
            canvas.drawBitmap(scaledBitmap1,20,103,paint);
        }
        catch(Exception e){
            Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT).show();
        }

        paint.setTextSize(12.3f);
        paint.setColor(Color.rgb(204,51,139));
        canvas.drawText("ID : ",115,108,paint);

        paint.setTextSize(11.7f);
        paint.setColor(Color.rgb(0,0,0));
        paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.NORMAL));
        canvas.drawText(Integer.toString(stud_id),138,108,paint);

        paint.setTextSize(12.3f);
        paint.setColor(Color.rgb(204,51,139));
        paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
        canvas.drawText("NAME : ",115,130,paint);

        paint.setTextSize(11.7f);
        paint.setColor(Color.rgb(0,0,0));
        paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.NORMAL));
        canvas.drawText(""+name.getText(),159,130,paint);

        paint.setTextSize(12.3f);
        paint.setColor(Color.rgb(204,51,139));
        paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
        canvas.drawText("STD : ",115,152,paint);

        paint.setTextSize(11.7f);
        paint.setColor(Color.rgb(0,0,0));
        paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.NORMAL));
        canvas.drawText(""+stnd.getText(),147,152,paint);

        paint.setTextSize(12.3f);
        paint.setColor(Color.rgb(204,51,139));
        paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
        canvas.drawText("CONT. NO : ",115,176,paint);

        paint.setTextSize(11.7f);
        paint.setColor(Color.rgb(0,0,0));
        paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.NORMAL));
        canvas.drawText(""+phone.getText(),176,176,paint);

        paint.setTextSize(12.3f);
        paint.setColor(Color.rgb(204,51,139));
        paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
        canvas.drawText("MAIL ID : ",115,198,paint);

        paint.setTextSize(11.7f);
        paint.setColor(Color.rgb(0,0,0));
        paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.NORMAL));
        canvas.drawText(""+mail.getText(),170,198,paint);

        paint.setTextSize(12.3f);
        paint.setColor(Color.rgb(204,51,139));
        paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
        canvas.drawText("FEES/MON : ",115,220,paint);

        paint.setTextSize(11.7f);
        paint.setColor(Color.rgb(0,0,0));
        paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.NORMAL));
        canvas.drawText("₹"+fees.getText(),184,220,paint);

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
        canvas.drawText(""+datePatternformat.format(new Date().getTime()),60,289,paint);

        paint.setTextSize(12.3f);
        paint.setColor(Color.rgb(204,51,139));
        paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
        canvas.drawText("AMOUNT PAID : ",20,311,paint);

        paint.setTextSize(11.7f);
        paint.setColor(Color.rgb(0,0,0));
        paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.NORMAL));
        canvas.drawText("₹"+downp.getText(),111,311,paint);

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
        canvas.drawText(""+loggedin_user,110,377,paint);

        rec_name = Integer.toString(recpt_id)+"_"+name.getText().toString().trim()+"_of_"+stnd.getText().toString().trim()+"_"+System.currentTimeMillis()+".pdf";

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
            reg.setEnabled(true);
            dialog.dismiss();
        }
        document.close();
    }

    private void pdfstore() {
        //Toast.makeText(getApplicationContext(),"Inside pdfstore...",Toast.LENGTH_LONG).show();
        final StorageReference reference=storage.getReference("receipts/"+rec_name);
        reference.putFile(filepath)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Toast.makeText(getApplicationContext(),"Successfully uploaded",Toast.LENGTH_LONG).show();
                                receiptupload r_up = new receiptupload(Integer.toString(recpt_id), rec_name, uri.toString().trim(), Integer.toString(stud_id),name.getText().toString().trim(),filesize,datePatternformat1.format(new Date().getTime()),""+System.currentTimeMillis());
                                ref3.child(String.valueOf(recpt_id)).setValue(r_up);
                                endofall(uri.toString().trim());
                                dialog.dismiss();
                                //Toast.makeText(getApplicationContext(),"Uri "+uri.toString().trim(),Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(student_registration.this, pdfviewer.class);
                                intent.putExtra("stu_mail", mail.getText().toString().trim());
                                intent.putExtra("stu_cno", phone.getText().toString().trim());
                                intent.putExtra("filename", rec_name);
                                intent.putExtra("down_url", uri.toString().trim());
                                intent.putExtra("context", "student_registration");
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
                        reg.setEnabled(true);
                        dialog.dismiss();
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

    private void endofall(String s) {
        if(check == 0){
            receipt_check(s);
        }}

    public void receipt_check(String s) {
        //Toast.makeText(getApplicationContext(),"Inside receipt_check...",Toast.LENGTH_LONG).show();
        Query query = FirebaseDatabase.getInstance().getReference().child("receipt").orderByChild("url").equalTo(s);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount() > 0){
                    check=1;
                    //Toast.makeText(getApplicationContext(),"Inside if part...",Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                    name.setText("");
                    stnd.setText("");
                    mail.setText("");
                    phone.setText("");
                    date.setText("");
                    fees.setText("");
                    downp.setText("");
                    mode.setText("");
                    pass.setText("");
                    mode.setCursorVisible(false);
                    imgv.setImageResource(R.drawable.profile_pic);
                    Toast.makeText(getApplicationContext(), "Uploaded", Toast.LENGTH_LONG).show();
                    reg.setEnabled(true);
                }
                else{
                    //Toast.makeText(getApplicationContext(),"Inside else part...",Toast.LENGTH_LONG).show();
                    //Toast.makeText(getApplicationContext(),"Inside re load",Toast.LENGTH_LONG).show();
                    final StorageReference reference=storage.getReference("receipts/"+rec_name);
                    reference.putFile(filepath)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    receiptupload r_up = new receiptupload(Integer.toString(recpt_id), rec_name, s, Integer.toString(stud_id),name.getText().toString().trim(), filesize,datePatternformat1.format(new Date().getTime()),""+System.currentTimeMillis());
                                    ref3.child(String.valueOf(recpt_id)).setValue(r_up);
                                    endofall(s);
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                reg.setEnabled(true);
            }
        });
    }
}