package com.example.online_class;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.github.drjacky.imagepicker.ImagePicker;
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
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class teacher_registration extends AppCompatActivity {
    FirebaseStorage storage;
    FirebaseDatabase database;
    DatabaseReference ref;
    int mon=0;
    int check = 0;
    int teach_id;
    double n;
    FloatingActionButton brow;
    Button reg;
    Uri url,down_url,filepath;
    CircleImageView imgv;
    EditText name,mail,phone,date,qual;
    ProgressDialog dialog;
    String paidm,rec_name,sel_date,filesize,loggedin_user,loguser_no,imgpath,no,upd_string;
    File f;
    boolean validat_teach = false;
    long filesizeInBytes,filesizeinKB,filesizeinMB;
    String[] std={"1","2","3","4","5","6","7","8","9","10"};
    String[] pmode={"By Cash","By Cheque"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_registration);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().setTitle("Add Teacher...");

        SharedPreferences sh = getSharedPreferences("credentials",getApplicationContext().MODE_PRIVATE);
        loggedin_user = sh.getString("uname",null);
        //Toast.makeText(getApplicationContext(),"User :"+loggedin_user,Toast.LENGTH_LONG).show();

        storage = FirebaseStorage.getInstance();
        database=FirebaseDatabase.getInstance();
        ref=database.getReference("teachers");

        name = findViewById(R.id.name);
        mail = findViewById(R.id.mail);
        phone = findViewById(R.id.phone);
        date = findViewById(R.id.date);
        qual = findViewById(R.id.qual);
        imgv=(CircleImageView) findViewById(R.id.imageView);
        brow=(FloatingActionButton) findViewById(R.id.browse);
        reg=(Button)findViewById(R.id.register);


        Calendar cal = Calendar.getInstance();
        final int year = cal.get(Calendar.YEAR);
        final int month = cal.get(Calendar.MONTH);
        final int day = cal.get(Calendar.DAY_OF_MONTH);

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(teacher_registration.this, new DatePickerDialog.OnDateSetListener() {
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

        brow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dexter.withActivity(teacher_registration.this)
                        .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                        .withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse response) {
                                //onchoosefile(view);
                                ImagePicker.Companion.with(teacher_registration.this)
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
                validat_teach = validate_teach();
                if(validat_teach){
                    dialog = new ProgressDialog(teacher_registration.this);
                    dialog.show();
                    dialog.setContentView(R.layout.progress_dialog);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    generate_tid();
                }
            }
        });
    }

    private boolean validate_teach() {
        reg.setEnabled(false);
        if(name.getText().toString().trim().equals("") || mail.getText().toString().trim().equals("") || phone.getText().toString().trim().equals("") || date.getText().toString().trim().equals("") || qual.getText().toString().trim().equals("")){
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
        if(!qual.getText().toString().trim().matches("^[A-Za-z ]+$")){
            Toast.makeText(getApplicationContext(),"Qualification entered might contain characters except letters..Please check!!",Toast.LENGTH_SHORT).show();
            reg.setEnabled(true);
            //fees.setText("");
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestcode, int resultcode, @Nullable Intent data) {
        /*if(requestcode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultcode == RESULT_OK){
                url= result.getUri();
                //imgpath = result.getUri().getPath();
                //Toast.makeText(getApplicationContext(),""+imgpath,Toast.LENGTH_LONG).show();
                imgv.setImageURI(url);
            }
            else if(resultcode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Exception e= result.getError();
                Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }*/
        super.onActivityResult(requestcode, resultcode, data);
        if(requestcode == 12 && resultcode == RESULT_OK){
            url = data.getData();
            //imgpath = data.getData().getPath();
            imgv.setImageURI(url);
        }
    }

    private void generate_tid() {
        FirebaseDatabase.getInstance().getReference().child("teachers").orderByChild("cno").equalTo(phone.getText().toString().trim())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getChildrenCount() > 0){
                            Toast.makeText(getApplicationContext(),"Already a teacher exists with given Contact No!Please Change it !!",Toast.LENGTH_SHORT).show();
                            phone.setText("");
                            reg.setEnabled(true);
                            dialog.dismiss();
                        }
                        else{
                            FirebaseDatabase.getInstance().getReference().child("admin").orderByChild("cno").equalTo(phone.getText().toString().trim())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.getChildrenCount() > 0){
                                                Toast.makeText(getApplicationContext(),"Already an admin exists with given Contact No!Please Change it !!",Toast.LENGTH_SHORT).show();
                                                phone.setText("");
                                                reg.setEnabled(true);
                                                dialog.dismiss();
                                            }
                                            else{
                                                //Toast.makeText(getApplicationContext(),"Inside generateid()....",Toast.LENGTH_LONG).show();
                                                RandomGenerator3 rndgen = new RandomGenerator3();
                                                teach_id = rndgen.nextInt();
                                                verify_unique(teach_id);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            Toast.makeText(getApplicationContext(),"Some Error Occurred!!",Toast.LENGTH_SHORT).show();
                                            reg.setEnabled(true);
                                            dialog.dismiss();
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getApplicationContext(),"Some Error Occurred!!",Toast.LENGTH_SHORT).show();
                        reg.setEnabled(true);
                        dialog.dismiss();
                    }
                });
    }

    private void verify_unique(int teach_id) {
        Query query = database.getReference().child("teachers").orderByChild("id").equalTo(Integer.toString(teach_id));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount() >= 1){
                    generate_tid();
                }
                else{
                    upload();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                reg.setEnabled(true);
                dialog.dismiss();}
        });
    }

    public void onchoosefile(View v){
        CropImage.activity().setAspectRatio(1,1).start(teacher_registration.this);
    }

    private void upload() {
        final StorageReference uploader = storage.getReference("teacherprofile/Image" +  Integer.toString(teach_id));
        uploader.putFile(url)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        uploader.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                teachupload obj = new teachupload(Integer.toString(teach_id), name.getText().toString().trim(), name.getText().toString().trim().toLowerCase(), mail.getText().toString().trim(), phone.getText().toString().trim(), date.getText().toString().trim(), qual.getText().toString().trim(), uri.toString().trim(),loggedin_user);
                                ref.child(String.valueOf(teach_id)).setValue(obj);
                                dialog.dismiss();
                                name.setText("");
                                mail.setText("");
                                phone.setText("");
                                date.setText("");
                                qual.setText("");
                                imgv.setImageResource(R.drawable.profile_pic);
                                Toast.makeText(getApplicationContext(), "Uploaded", Toast.LENGTH_LONG).show();
                                reg.setEnabled(true);
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Some Error Occurred!! Upload Failed..", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                        reg.setEnabled(true);
                    }
                });
    }
}