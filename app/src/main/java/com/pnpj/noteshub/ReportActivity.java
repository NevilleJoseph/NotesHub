package com.pnpj.noteshub;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ReportActivity extends AppCompatActivity {

    EditText editText;
    Button report;

    Intent intentSent;

    String pushID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        editText = findViewById(R.id.reportNoteEditText);
        report = findViewById(R.id.reportButton);

        intentSent = getIntent();
        pushID = intentSent.getStringExtra("pushID");
        Log.d("text - ",pushID);

        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadReport();
            }
        });
    }

    public void uploadReport()
    {
        String text = editText.getText().toString();
        if(text.matches(""))
        {
            Toast.makeText(this, "Enter your Reason", Toast.LENGTH_SHORT).show();
            return;
        }
        DatabaseReference reportRef = FirebaseDatabase.getInstance().getReference().child("reports").child(pushID);
        String push = reportRef.push().getKey();
        reportRef.child(push).child(text).setValue("true");
        Log.d("text - ",text);
        Toast.makeText(this, "Report submitted successfully", Toast.LENGTH_LONG).show();
        finish();
    }

}
