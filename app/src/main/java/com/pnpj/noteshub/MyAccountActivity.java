package com.pnpj.noteshub;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;

public class MyAccountActivity extends AppCompatActivity {

    TextView college,branchName,semester,name,money,rollNo,becomeUploader,uploadFiles,increaseMoney;
    Button redeemMoney;
    FirebaseAuth mFirebaseAuth;
    double earnings;
    DecimalFormat df;
    boolean uploader,hasUploaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);

        mFirebaseAuth = FirebaseAuth.getInstance();

        df = new DecimalFormat("#.00");

        college = findViewById(R.id.myAccountCollegeTextView);
        branchName = findViewById(R.id.myAccountBranchTextView);
        semester = findViewById(R.id.myAccountSemesterTextView);
        name = findViewById(R.id.myAccountNameTextView);
        money = findViewById(R.id.myAccountMoneyEarnedTextView);
        redeemMoney = findViewById(R.id.myAccountRedeemMoneyButton);
        rollNo = findViewById(R.id.myAccountRollNoTextView);
        becomeUploader = findViewById(R.id.myAccountBecomeUploaderTextView);
        uploadFiles = findViewById(R.id.myAccountUploadFilesTextView);
        increaseMoney = findViewById(R.id.myAccountIncreaseEarningsTextView);

        final SharedPreferences preferences = getSharedPreferences("myPrefs",MODE_PRIVATE);
        name.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        college.setText(preferences.getString(mFirebaseAuth.getUid()+"college",""));
        branchName.setText(preferences.getString(mFirebaseAuth.getUid()+"branch",""));
        semester.setText(preferences.getString(mFirebaseAuth.getUid()+"sem",""));
        rollNo.setText(preferences.getString(mFirebaseAuth.getUid()+"roll",""));
        uploader = preferences.getBoolean(mFirebaseAuth.getUid()+"uploader",false);
        hasUploaded = preferences.getBoolean(mFirebaseAuth.getUid()+"hasUploaded",false);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        DatabaseReference checkTransaction = FirebaseDatabase.getInstance().getReference().child("users").child(mFirebaseAuth.getUid());
        checkTransaction.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild("redeemMoney"))
                {
                    redeemMoney.setVisibility(View.VISIBLE);
                }
                else
                {
                    Toast.makeText(MyAccountActivity.this, "You have a redemption in progress", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DatabaseReference moneyEarned = FirebaseDatabase.getInstance().getReference().child("adminAccess").child(mFirebaseAuth.getUid()).child("earnings");
        moneyEarned.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
              if(!dataSnapshot.exists())
                {
                    earnings = 0.0;
                    money.setText("₹ 0.00");
                    if(uploader)
                    {
                        if(hasUploaded)
                        {
                            increaseMoney.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            uploadFiles.setVisibility(View.VISIBLE);
                        }
                    }
                    else
                    {
                        becomeUploader.setVisibility(View.VISIBLE);
                        redeemMoney.setVisibility(View.GONE);
                    }
                }
                else
                {
                    earnings = dataSnapshot.getValue(double.class);
                    money.setText("₹ "+df.format(earnings)+"");
                }

                if(progressDialog.isShowing())
                    progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        redeemMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(earnings<50)
                {
                    Toast.makeText(MyAccountActivity.this, "Minimum amount to redeem is Rs. 50", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Intent intent = new Intent(MyAccountActivity.this,RedeemMoneyActivity.class);
                    intent.putExtra("maxAmount",earnings);
                    startActivity(intent);
                }
            }
        });
    }
}
