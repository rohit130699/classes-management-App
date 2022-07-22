package com.example.snc_teachers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.snc_teachers.otp_test;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;

public class verify_user extends AppCompatActivity {
    EditText phn;
    Button b1;
    CountryCodePicker ccp;
    ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_user);
        pb = (ProgressBar)findViewById(R.id.simpleProgressBar);
        phn = (EditText)findViewById(R.id.phn_no);
        ccp = (CountryCodePicker) findViewById(R.id.ccp);
        ccp.setCcpClickable(false);
        ccp.registerCarrierNumberEditText(phn);
        b1 = (Button) findViewById(R.id.button1);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                b1.setEnabled(false);
                pb.setVisibility(View.VISIBLE);
                if (phn.getText().toString().equals("")) {
                    pb.setVisibility(View.GONE);
                    Toast.makeText(verify_user.this, "Not entered anything !!", Toast.LENGTH_SHORT).show();
                    phn.setText("");
                    b1.setEnabled(true);
                } else if (phn.getText().toString().replace(" ","").length() != 10) {
                    pb.setVisibility(View.GONE);
                    Toast.makeText(verify_user.this, "Not a valid Phone number !!", Toast.LENGTH_SHORT).show();
                    phn.setText("");
                    b1.setEnabled(true);
                } else {
                    String cno = phn.getText().toString().replace(" ", "");
                    Query query = FirebaseDatabase.getInstance().getReference().child("teachers").orderByChild("cno").equalTo(cno);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getChildrenCount() >= 1) {
                                Intent inte = new Intent(verify_user.this, otp_test.class);
                                inte.putExtra("mobile", ccp.getFullNumberWithPlus().replace(" ", ""));
                                //inte.putExtra("mobile", "+91"+phn.getText().toString().trim());
                                pb.setVisibility(View.GONE);
                                startActivity(inte);
                                finish();
                            } else {
                                pb.setVisibility(View.GONE);
                                Toast.makeText(getApplicationContext(), "You are not a registered Teacher", Toast.LENGTH_SHORT).show();
                                phn.setText("");
                                b1.setEnabled(true);
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(getApplicationContext(), "Some Error Occurred!!", Toast.LENGTH_SHORT).show();
                            b1.setEnabled(true);
                        }
                    });
                }
            }
        });
    }

    /**
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are <em>not</em> resumed.
     */
    @Override
    protected void onResume() {
        super.onResume();
        phn.setVisibility(View.VISIBLE);
    }
}