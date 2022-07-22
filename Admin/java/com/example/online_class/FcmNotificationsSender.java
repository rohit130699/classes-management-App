package com.example.online_class;

import android.app.Activity;
import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FcmNotificationsSender  {

    String userFcmToken;
    String title;
    String type;
    String body;
    String fileurl;
    Context mContext;
    Activity mActivity;

    private RequestQueue requestQueue;
    private final String postUrl = "https://fcm.googleapis.com/fcm/send";
    private final String fcmServerKey ="AAAArmmG8Ng:APA91bFqf_a1nMMVbjziTEqfCsRSd10sfQaQ5ho_djRANQcdqmAZ88lM8tJa5HeXayoe9HhQjHZr4TqbhAYjcn2nWoRTywVvxMTmdjw8wzHB5jTXkP7lS6vjUA6-reQWYA6j4z3qZtXu";

    public FcmNotificationsSender(String userFcmToken, String title, String type, String body, String fileurl, Context mContext, Activity mActivity) {
        this.userFcmToken = userFcmToken;
        this.title = title;
        this.type = type;
        this.body = body;
        this.fileurl = fileurl;
        this.mContext = mContext;
        this.mActivity = mActivity;
    }

    public void SendNotifications() {
        requestQueue = Volley.newRequestQueue(mActivity);
        JSONObject mainObj = new JSONObject();
        try {
            mainObj.put("to", userFcmToken);

            /*JSONObject notiObject = new JSONObject();
            notiObject.put("title", title);
            notiObject.put("body", body);
            notiObject.put("channel_id", "Stud Notification");
            notiObject.put("android_channel_id", "Stud Notification");
            notiObject.put("sound","default");
            //notiObject.put("click_action", "android.action.ALL_APPS");
            notiObject.put("icon", "snclass"); // enter icon that exists in drawable only*/

            JSONObject msgObject = new JSONObject();
            msgObject.put("title", title);
            msgObject.put("body", body);
            msgObject.put("icon", "snclass");
            msgObject.put("topic", body);
            msgObject.put("url", fileurl);
            msgObject.put("type", type);

            //mainObj.put("notification", notiObject);
            mainObj.put("data", msgObject);
            mainObj.put("priority", "high");
            //mainObj.put("collapse_key", "s"+i);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, postUrl, mainObj, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

                    // code run is got response

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // code run is got error

                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> header = new HashMap<>();
                    header.put("content-type", "application/json");
                    header.put("authorization", "key=" + fcmServerKey);
                    return header;
                }
            };
            requestQueue.add(request);


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
