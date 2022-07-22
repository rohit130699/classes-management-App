package com.example.online_class;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.net.URLEncoder;

public class viewpdf extends AppCompatActivity {
    ProgressBar pb;
    WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewpdf);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        pb = (ProgressBar)findViewById(R.id.simpleProgressBar);
        pb.setVisibility(View.VISIBLE);

        webView=(WebView)findViewById(R.id.viewpdf);
        webView.getSettings().setJavaScriptEnabled(true);
        //webView.setLayerType(View.LAYER_TYPE_HARDWARE,null);

        String filename=getIntent().getStringExtra("filename");
        String fileurl=getIntent().getStringExtra("fileurl");

        //Toast.makeText(this,fileurl,Toast.LENGTH_SHORT).show();

        webView.setWebViewClient(new WebViewClient()
                                 {
                                     @Override
                                     public void onPageStarted(WebView view, String url, Bitmap favicon) {
                                         super.onPageStarted(view, url, favicon);
                                         pb.setVisibility(View.GONE);
                                     }

                                     @Override
                                     public void onPageFinished(WebView view, String url) {
                                         super.onPageFinished(view, url);
                                     }
                                 });
        String url="";
        try{
            url= URLEncoder.encode(fileurl,"UTF-8");
        }
        catch(Exception ex){
        }
        webView.loadUrl("http://docs.google.com/gview?embedded=true&url=" + url);
    }
}