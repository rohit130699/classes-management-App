package com.example.online_class;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class pdfviewer extends AppCompatActivity {
    String stu_mail,stu_cno,filename,down_url,msg,phn_no,context;
    ProgressBar pb;
    //private ProgressDialog progressBar;
    Uri filelocation;
    ProgressDialog dialog;
    FloatingActionButton fbtn,fmail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdfviewer);
        pb = (ProgressBar)findViewById(R.id.simpleProgressBar);
       /* progressBar = new ProgressDialog(pdfviewer.this);
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setCancelable(false);
        progressBar.setIndeterminate(true);*/
        PDFView pdfView = findViewById(R.id.pdfview);
        fbtn = (FloatingActionButton)findViewById(R.id.download);
        fmail = (FloatingActionButton)findViewById(R.id.gmail);

        stu_mail = getIntent().getStringExtra("stu_mail");
        stu_cno = getIntent().getStringExtra("stu_cno");
        filename = getIntent().getStringExtra("filename");
        down_url = getIntent().getStringExtra("down_url");
        context = getIntent().getStringExtra("context");

        phn_no = stu_cno.trim();
        msg="This message is from S.N. Classes,Fees Payment Successful,Receipt has been mailed to you..If not received, you can check in classes App or contact Classes Owner";
        //Toast.makeText(getApplicationContext(),stu_mail,Toast.LENGTH_SHORT).show();
        //Toast.makeText(this, down_url, Toast.LENGTH_LONG).show();
        //dshow();
        File myfile=new File(this.getExternalFilesDir("/"),filename);
        Uri filepath= Uri.fromFile(myfile);
        pdfView.fromUri(filepath)
                    .enableSwipe(true)
                    .swipeHorizontal(false)
                    .enableDoubletap(true)
                    .enableAntialiasing(true)
                    .spacing(0)
                    .load();
        pb.setVisibility(View.GONE);
        //dstop();
        /*Query query = FirebaseDatabase.getInstance().getReference().child("receipt").orderByChild("url").equalTo(down_url);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount() > 0){
                    Toast.makeText(getApplicationContext(),"file exists",Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(getApplicationContext(),"file doesn't exists",Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/
        //Toast.makeText(this, filepath.toString(), Toast.LENGTH_LONG).show();
        fbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*progressBar.setTitle("Downloading...");
                progressBar.setMessage("Please wait...");
                progressBar.show();*/
                String geturl = down_url.toString();
                //Toast.makeText(getApplicationContext(), geturl, Toast.LENGTH_LONG).show();
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(geturl));
                request.setTitle(filename);
                request.setDescription("Downloading Files...");
                String cookie = CookieManager.getInstance().getCookie(geturl);
                request.addRequestHeader("cookie",cookie);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,filename);

                DownloadManager dm = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
                dm.enqueue(request);
                //progressBar.dismiss();
                Toast.makeText(pdfviewer.this,"Downloading Started..",Toast.LENGTH_SHORT).show();
            }
        });

        fmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                        try {
                            Toast.makeText(getApplicationContext(),"Sending Mail...wait few moments..",Toast.LENGTH_LONG).show();
                            String username = "snclassesNSP@gmail.com";
                            String password = "snclasses@#1234";
                            String path = filepath.getPath();
                            Properties props = new Properties();
                            props.put("mail.smtp.auth", "true");
                            props.put("mail.smtp.starttls.enable", "true");
                            props.put("mail.smtp.host", "smtp.gmail.com");
                            props.put("mail.smtp.port", "587");

                            Session session = Session.getInstance(props,
                                    new javax.mail.Authenticator() {
                                        protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                                            //Toast.makeText(getApplicationContext(),"Inside Authentication",Toast.LENGTH_SHORT).show();
                                            return new javax.mail.PasswordAuthentication(
                                                    username, password);
                                        }
                                    });
                            // TODO Auto-generated method stub
                            Message message = new MimeMessage(session);
                            message.setFrom(new InternetAddress("snclassesNSP@gmail.com"));
                            message.setRecipients(Message.RecipientType.TO,
                                    InternetAddress.parse(stu_mail));
                            message.setSubject("Receipt of S.N classes");
                            message.setText("This mail contains attachment of PDF receipt of fees paid...");
                            if (!"".equals(path)) {
                               // Toast.makeText(getApplicationContext(),"Path found",Toast.LENGTH_SHORT).show();
                                Multipart _multipart = new MimeMultipart();
                                BodyPart messageBodyPart = new MimeBodyPart();
                                DataSource source = new FileDataSource(path);

                                messageBodyPart.setDataHandler(new DataHandler(source));
                                //Toast.makeText(getApplicationContext(),"file attached",Toast.LENGTH_SHORT).show();
                                messageBodyPart.setFileName(filename);

                                _multipart.addBodyPart(messageBodyPart);
                                message.setContent(_multipart);
                            }
                            Transport.send(message);
                            //System.out.println("Done");
                            Toast.makeText(getApplicationContext(),""+"Mail sent successfully..For better details you can check email app..!!",Toast.LENGTH_LONG).show();
                        }
                        /*catch (Exception e) {
                            throw new RuntimeException(e);
                        }*/
                        catch (NoSuchProviderException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(),"No such provider found...",Toast.LENGTH_SHORT).show();
                        } catch (MessagingException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(),"Some Error occurred while sending mail..!!",Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(),"Some Error occurred...",Toast.LENGTH_SHORT).show();
                        }
                        mesgbtn();
            }
        });
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    public void mesgbtn(){
        int permissionchk = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);
        if(permissionchk == PackageManager.PERMISSION_GRANTED) {
            MyMessage();
        }
        else{
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.SEND_SMS},0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case 0:
                if(grantResults.length>=0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    MyMessage();
                }
                else{
                    Toast.makeText(getApplicationContext(),"You don't have Required Permission..",Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void MyMessage() {
        SmsManager smsmang = SmsManager.getDefault();
        smsmang.sendTextMessage(phn_no,null,msg,null,null);
        //smsmang.sendTextMessage(phn_no,null,down_url.substring(0,160),null,null);
        Toast.makeText(getApplicationContext(),"Message sent to contact number",Toast.LENGTH_SHORT).show();
    }

    /*private void dstop() {
            dialog.dismiss();
        }

        private void dshow() {
            dialog = new ProgressDialog(this);
            dialog.show();
            dialog.setContentView(R.layout.progress_dialog);
            dialog.getWindow().setBackgroundDrawableResource(
                    android.R.color.transparent
            );
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }/*

        /**
         * Called when the activity has detected the user's press of the back
         * key. The {@link #getOnBackPressedDispatcher() OnBackPressedDispatcher} will be given a
         * chance to handle the back button before the default behavior of
         * {@link Activity#onBackPressed()} is invoked.
         *
         * @see #getOnBackPressedDispatcher()
         */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        File myfile=new File(this.getExternalFilesDir("/"),filename);
        if(myfile.exists()){
            myfile.delete();
        }
        if(context.equals("fee_accept")) {
            Intent i = new Intent(pdfviewer.this, stu_data.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        }
        else if(context.equals("student_registration")){
            Intent i = new Intent(pdfviewer.this, show_data.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        }
    }
}