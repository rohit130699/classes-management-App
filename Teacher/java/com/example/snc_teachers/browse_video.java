package com.example.snc_teachers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iceteck.silicompressorr.SiliCompressor;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class browse_video extends AppCompatActivity {
    private Button uploadbtn;
    Context cont;
    FloatingActionButton choosebtn;
    private VideoView videoView;
    private Uri videoUri,finaluri;
    MediaController mediaController;
    private StorageReference storageref;
    private DatabaseReference dbref;
    SimpleDateFormat datePatternformat;
    private EditText name;
    AutoCompleteTextView std;
    String filesize,loggedin_user,username;
    double filesizeinKB,filesizeinMB;
    boolean isvalid = false;
    String[] stnd={"1","2","3","4","5","6","7","8","9","10"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_video);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().setTitle("Select VIDEO...");
        choosebtn=(FloatingActionButton) findViewById(R.id.cbtn);
        std=(AutoCompleteTextView)findViewById(R.id.std);
        uploadbtn=(Button)findViewById(R.id.upbtn);
        videoView=(VideoView)findViewById(R.id.vview);
        name=(EditText) findViewById(R.id.name);
        mediaController=new MediaController(this);
        //FirebaseMessaging.getInstance().subscribeToTopic("all");
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

        storageref= FirebaseStorage.getInstance().getReference("videos");
        dbref= FirebaseDatabase.getInstance().getReference("videos");
        videoView.setMediaController(mediaController);
        mediaController.setAnchorView(videoView);

        choosebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dexter.withActivity(browse_video.this)
                        .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                        .withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse response) {
                                Intent intent=new Intent();
                                intent.setType("video/*");
                                intent.setAction(Intent.ACTION_GET_CONTENT);
                                startActivityForResult(Intent.createChooser(intent,"Please Select VIDEO"),101);
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
                isvalid = validate();
                if(isvalid){
                    uploadVideo();
                }
            }
        });
    }

    private boolean validate() {
        uploadbtn.setEnabled(false);
        if(finaluri == null){
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 101 && resultCode == RESULT_OK && data != null && data.getData() != null){
            videoUri=data.getData();
            //Toast.makeText(getApplicationContext(),""+videoUri.toString(), Toast.LENGTH_LONG).show();
            if (data.getData().getScheme().equals("file")) {
                File file = new File(videoUri.toString());
                long fileSize = (int) file.length();
                compute(fileSize);
                //Toast.makeText(getApplicationContext(),"This is the file size: " + fileSize, Toast.LENGTH_LONG).show();
                //System.out.println("This is the file size: " + fileSize);
            } else if (data.getData().getScheme().equals("content")) {
                Cursor returnCursor = this.getContentResolver().query(videoUri, null, null, null, null);
                assert returnCursor != null;
                int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                returnCursor.moveToFirst();
                String fileSize = returnCursor.getString(sizeIndex);
                long filelen = Long.parseLong(fileSize);
                compute(filelen);
                //Toast.makeText(getApplicationContext(),"This is the file size: " + fileSize, Toast.LENGTH_LONG).show();
                //System.out.println("This is the file size: " + fileSize);
            }

            if(filesizeinMB > 17.0){
                File file = new File(this.getExternalFilesDir("/"), "movies/");
                new CompressVideo().execute("false",videoUri.toString(),file.getPath());
            }
            else{
                finaluri= videoUri;
                videoView.setVideoURI(finaluri);
                videoView.start();
            }
        }
    }
    private String getFileExt(Uri VideoUri){
        ContentResolver contentResolver=getContentResolver();
        MimeTypeMap mimeTypeMAp=MimeTypeMap.getSingleton();
        return mimeTypeMAp.getExtensionFromMimeType(contentResolver.getType(videoUri));
    }
    private void uploadVideo(){
        if(finaluri != null){
            final ProgressDialog dialog=new ProgressDialog(this);
            dialog.setTitle("Uploading Video");
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            if (finaluri.getScheme().equals("file")) {
                //Toast.makeText(getApplicationContext(),""+finaluri.getLastPathSegment(), Toast.LENGTH_LONG).show();
                File file = new File(finaluri.getPath());
                long fileSize = (int) file.length();
                compute(fileSize);
                //Toast.makeText(getApplicationContext(),"Inside File This is the file size: " + fileSize, Toast.LENGTH_LONG).show();
                //System.out.println("This is the file size: " + fileSize);
            } else if (finaluri.getScheme().equals("content")) {
                Cursor returnCursor = this.getContentResolver().query(finaluri, null, null, null, null);
                assert returnCursor != null;
                int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                returnCursor.moveToFirst();
                String fileSize = returnCursor.getString(sizeIndex);
                long filelen = Long.parseLong(fileSize);
                compute(filelen);
                //Toast.makeText(getApplicationContext(),"Inside Content This is the file size: " + fileSize, Toast.LENGTH_LONG).show();
                //System.out.println("This is the file size: " + fileSize);
            }
            String mst = String.valueOf(System.currentTimeMillis());
            String filename = name.getText().toString().trim()+mst+"."+getFileExt(videoUri);
            File fold = new File(this.getExternalFilesDir("/"), "movies");
            final StorageReference reference=storageref.child(filename);
            reference.putFile(finaluri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    videoupload obj=new videoupload(mst,name.getText().toString().trim(),filename,name.getText().toString().toLowerCase().trim(),loggedin_user+"_"+name.getText().toString().toLowerCase().trim(),std.getText().toString().trim()+"_"+name.getText().toString().toLowerCase().trim(),uri.toString(),filesize,datePatternformat.format(new Date().getTime()),std.getText().toString().trim(),loggedin_user);
                                    dbref.child(mst).setValue(obj);
                                    videoView.stopPlayback();
                                    FcmNotificationsSender notificationsSender = new FcmNotificationsSender("/topics/"+std.getText().toString().trim(),"New video from "+username,"video",name.getText().toString().trim(),uri.toString(),cont,browse_video.this);
                                    notificationsSender.SendNotifications();
                                    name.setText("");
                                    std.setText("");
                                    dialog.dismiss();
                                    if(fold.exists()){
                                        String[] entries = fold.list();
                                        for (String s : entries) {
                                            File currentfile = new File(fold.getPath(), s);
                                            currentfile.delete();
                                        }
                                        fold.delete();
                                    }
                                    uploadbtn.setEnabled(true);
                                    Toast.makeText(getApplicationContext(),"Video Uploaded",Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();
                            uploadbtn.setEnabled(true);
                            Toast.makeText(getApplicationContext(),"Video Upload Failed",Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            float percent=(100*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                            dialog.setMessage("Uploaded : "+(int)percent+" %");
                        }
                    });
        }
        else{
            Toast.makeText(getApplicationContext(),"No Video Selected",Toast.LENGTH_LONG).show();
        }
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

    private class CompressVideo  extends AsyncTask<String,String,String> {
        Dialog dialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialog.show(browse_video.this,"Please Wait..","Compressing...");
        }

        /**
         * @param strings
         * @deprecated
         */


        @Override
        protected String doInBackground(String... strings) {
            String videopath = null;
            try{
                Uri uri = Uri.parse(strings[1]);
                videopath = SiliCompressor.with(browse_video.this)
                        .compressVideo(uri,strings[2]);
            }catch (URISyntaxException e){
                e.printStackTrace();
            }
            return videopath;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            dialog.dismiss();
            File file = new File(s);
            finaluri= Uri.fromFile(file);
            videoView.setVideoURI(finaluri);
            videoView.start();
        }
    }
}