package com.example.snc_teachers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class browse_pdf extends AppCompatActivity {
    private Button uploadbtn;
    FloatingActionButton choosebtn;
    private PDFView pdfv;
    private EditText name;
    AutoCompleteTextView std;
    SimpleDateFormat datePatternformat;
    Uri filepath;
    Context cont;
    StorageReference storagereference;
    DatabaseReference databasereference;
    boolean isvalid = false;
    String filesize,filename,loggedin_user,username;
    long filesizeInBytes;
    double filesizeinKB,filesizeinMB;
    File mypdf;
    String[] stnd={"1","2","3","4","5","6","7","8","9","10"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_pdf);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().setTitle("Select PDF...");
        storagereference= FirebaseStorage.getInstance().getReference();
        databasereference= FirebaseDatabase.getInstance().getReference("documents");
        choosebtn=(FloatingActionButton) findViewById(R.id.cbtn);
        std=(AutoCompleteTextView)findViewById(R.id.std);
        uploadbtn=(Button)findViewById(R.id.upbtn);
        pdfv=(PDFView) findViewById(R.id.pdfview);
        name=(EditText) findViewById(R.id.name);


        datePatternformat = new SimpleDateFormat("dd MMM yyyy");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.select_dialog_item,stnd);
        std.setThreshold(1);
        std.setAdapter(adapter);

        SharedPreferences sh = getSharedPreferences("credentials",getApplicationContext().MODE_PRIVATE);
        loggedin_user = sh.getString("uname",null);
        username = sh.getString("username",null);
        //Toast.makeText(getApplicationContext(),"User :"+loggedin_user,Toast.LENGTH_LONG).show();

        try {
            cont = createPackageContext("com.example.snc_students", Context.CONTEXT_IGNORE_SECURITY);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        choosebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dexter.withActivity(browse_pdf.this)
                        .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                        .withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse response) {
                                Intent intent=new Intent();
                                intent.setType("application/pdf");
                                intent.setAction(Intent.ACTION_GET_CONTENT);
                                startActivityForResult(Intent.createChooser(intent,"Please Select PDF"),101);
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
        uploadbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                //compute();
                isvalid = validate();
                if(isvalid){
                    processupload(filepath);
                }
            }
        });
    }

    private boolean validate() {
        uploadbtn.setEnabled(false);
        if(filepath == null){
            Toast.makeText(getApplicationContext(),"No PDF selected..",Toast.LENGTH_SHORT).show();
            uploadbtn.setEnabled(true);
            return false;
        }
        if(name.getText().toString().trim().equals("") || std.getText().toString().trim().equals("")){
            Toast.makeText(getApplicationContext(),"Some data is missing out !! Please fill out them..",Toast.LENGTH_SHORT).show();
            uploadbtn.setEnabled(true);
            return false;
        }
        if(!name.getText().toString().trim().matches("^[A-Za-z0-9 ]+$")) {
            Toast.makeText(getApplicationContext(),"Name entered might contain some special characters.Only Numbers and Letters allowed ..Please check!!",Toast.LENGTH_LONG).show();
            uploadbtn.setEnabled(true);
            return false;
        }
        if(!std.getText().toString().trim().equals("1") && !std.getText().toString().trim().equals("2") && !std.getText().toString().trim().equals("3") && !std.getText().toString().trim().equals("4") && !std.getText().toString().trim().equals("5") && !std.getText().toString().trim().equals("6") && !std.getText().toString().trim().equals("7") && !std.getText().toString().trim().equals("8") && !std.getText().toString().trim().equals("9") && !std.getText().toString().trim().equals("10")){
            Toast.makeText(getApplicationContext(),"Not entered valid Standard..Please check!!",Toast.LENGTH_SHORT).show();
            uploadbtn.setEnabled(true);
            return false;
        }
        return true;
    }

    private void compute(Long filesizeInBytes) {
        try{
            DecimalFormat df = new DecimalFormat("####0.00");
            filesizeinKB = filesizeInBytes / 1024;
            filesizeinMB = filesizeinKB / 1024;
            if(filesizeInBytes < 1024){
                filesize = filesizeInBytes+" Bytes";
            }
            else if((filesizeInBytes >= 1024) && (filesizeInBytes < 1048576)){
                if(filesizeinKB % 1 != 0) {
                    //Toast.makeText(getApplicationContext(),"Inside decimal KB", Toast.LENGTH_LONG).show();
                    filesize = df.format(filesizeinKB) + " KB";
                }
                else{
                    //Toast.makeText(getApplicationContext(),"Inside normal KB", Toast.LENGTH_LONG).show();
                    filesize = (long)filesizeinKB + " KB";
                }
            }
            else{
                if(filesizeinMB % 1 != 0) {
                    //Toast.makeText(getApplicationContext(),"Inside decimal MB", Toast.LENGTH_LONG).show();
                    filesize = df.format(filesizeinMB) + " MB";
                }
                else{
                    //Toast.makeText(getApplicationContext(),"Inside normal MB", Toast.LENGTH_LONG).show();
                    filesize = (long)filesizeinMB + " MB";
                }
            }
        }
        catch(Exception ex){
            Toast.makeText(this,"Error Occurred.....",Toast.LENGTH_SHORT).show();
            ex.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 101 && resultCode == RESULT_OK && data != null)
        {
            // Get the Uri of the selected file
            filepath=data.getData();
            if (data.getData().getScheme().equals("file")) {
                File file = new File(filepath.toString());
                long fileSize = (int) file.length();
                compute(fileSize);
                //Toast.makeText(getApplicationContext(),"This is the file size: " + fileSize, Toast.LENGTH_LONG).show();
                //System.out.println("This is the file size: " + fileSize);
            } else if (data.getData().getScheme().equals("content")) {
                Cursor returnCursor = this.getContentResolver().query(filepath, null, null, null, null);
                assert returnCursor != null;
                int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                returnCursor.moveToFirst();
                String fileSize = returnCursor.getString(sizeIndex);
                long filelen = Long.parseLong(fileSize);
                compute(filelen);
                //Toast.makeText(getApplicationContext(),"This is the file size: " + fileSize, Toast.LENGTH_LONG).show();
                //System.out.println("This is the file size: " + fileSize);
            }
            if(filesizeinMB > 15.00){
                Toast.makeText(getApplicationContext(),"File size should be less than 15MB!!..Please compress your file and try to upload..", Toast.LENGTH_LONG).show();
                uploadbtn.setEnabled(false);
            }
            else{
                pdfv.fromUri(filepath)
                        .enableSwipe(true)
                        .swipeHorizontal(false)
                        .enableDoubletap(true)
                        .enableAntialiasing(true)
                        .spacing(0)
                        .load();
                uploadbtn.setEnabled(true);
            }
        }
    }
    public void processupload(Uri uri){
        final ProgressDialog dialog=new ProgressDialog(this);
        dialog.setTitle("Uploading File");
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        String mst = String.valueOf(System.currentTimeMillis());
        String filename = name.getText().toString().trim()+"_"+mst+".pdf";
        final StorageReference reference=storagereference.child("uploads/"+filename);
        reference.putFile(filepath)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                pdfupload obj=new pdfupload(mst,name.getText().toString().trim(),filename,name.getText().toString().toLowerCase().trim(),loggedin_user+"_"+name.getText().toString().toLowerCase().trim(),std.getText().toString().trim()+"_"+name.getText().toString().toLowerCase().trim(),uri.toString(),filesize,datePatternformat.format(new Date().getTime()),std.getText().toString().trim(),loggedin_user);
                                databasereference.child(String.valueOf(mst)).setValue(obj);
                                FcmNotificationsSender notificationsSender = new FcmNotificationsSender("/topics/"+std.getText().toString().trim(),"New PDF from "+username,"pdf",name.getText().toString().trim(),uri.toString(),cont,browse_pdf.this);
                                notificationsSender.SendNotifications();
                                name.setText("");
                                std.setText("");
                                dialog.dismiss();
                                uploadbtn.setEnabled(true);
                                Toast.makeText(getApplicationContext(),"File Uploaded",Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        float percent=(100*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                        dialog.setMessage("Uploaded : "+(int)percent+" %");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                        uploadbtn.setEnabled(true);
                        Toast.makeText(getApplicationContext(),"PDF Upload Failed",Toast.LENGTH_LONG).show();
                    }
                });
    }
}