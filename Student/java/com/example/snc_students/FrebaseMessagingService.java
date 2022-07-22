package com.example.snc_students;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.net.URLEncoder;

public class FrebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    String umail;
    NotificationManager mNotificationManager;
    Intent resultIntent;
    String title="",body="",icon="",topic="",url="",type="";
    Context cont;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        cont = this;
        if(remoteMessage.getData().size() > 0){
           title = remoteMessage.getData().get("title");
           body = remoteMessage.getData().get("body");
           icon = remoteMessage.getData().get("icon");
           topic = remoteMessage.getData().get("topic");
           url = remoteMessage.getData().get("url");
           type = remoteMessage.getData().get("type");
        }

        SharedPreferences shp = getSharedPreferences("credentials",MODE_PRIVATE);
        if(shp.contains("uid")) {
            SharedPreferences sh = getSharedPreferences("credentials", getApplicationContext().MODE_PRIVATE);
            umail = sh.getString("umail", null);
            Query query = FirebaseDatabase.getInstance().getReference().child("students").orderByChild("mail").equalTo(umail);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getChildrenCount() > 0) {
                        // playing audio and vibration when user se reques
                        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                        r.play();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            r.setLooping(false);
                        }

                        // vibration
                        Vibrator v = (Vibrator) getSystemService(getApplicationContext().VIBRATOR_SERVICE);
                        long[] pattern = {100, 300, 300, 300};
                        v.vibrate(pattern, -1);


                        //Uri defaultsounduri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                        int resourceImage = getResources().getIdentifier(icon, "drawable", getPackageName());

                        //String click_action = remoteMessage.getNotification().getClickAction();

                        NotificationCompat.Builder builder = new NotificationCompat.Builder(cont, "CHANNEL_ID");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            //builder.setSmallIcon(R.drawable.icontrans);
                            builder.setSmallIcon(resourceImage);
                            //builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.snclass));
                        } else {
                            //builder.setSmallIcon(R.drawable.icon_kritikar);
                            builder.setSmallIcon(resourceImage);
                            //builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.snclass));
                        }

                        if (type.equals("pdf")) {
                            resultIntent = new Intent(cont, viewpdf.class);
                        } else if (type.equals("video")) {
                            resultIntent = new Intent(cont, viewvideo.class);
                        }
                        resultIntent.putExtra("filename", topic);
                        resultIntent.putExtra("fileurl", url);
                        resultIntent.setAction(Long.toString(System.currentTimeMillis()));
                        //resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        PendingIntent pendingIntent = PendingIntent.getActivity(cont, 1, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                        builder.setContentTitle(title);
                        builder.setContentText(body);
                        builder.setContentIntent(pendingIntent);
                        //builder.setSound(defaultsounduri);
                        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(body));
                        builder.setAutoCancel(true);
                        builder.setPriority(Notification.PRIORITY_MAX);

                        mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            String channelId = "Stud Notification";
                            NotificationChannel channel = new NotificationChannel(
                                    channelId,
                                    "Student Notification Channel",
                                    NotificationManager.IMPORTANCE_HIGH);
                            mNotificationManager.createNotificationChannel(channel);
                            builder.setChannelId(channelId);
                        }

                        // notificationId is a  int for each notification that you must define
                        mNotificationManager.notify((int) System.currentTimeMillis(), builder.build());
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });
        }
    }
}


