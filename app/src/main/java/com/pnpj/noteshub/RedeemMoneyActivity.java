package com.pnpj.noteshub;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DecimalFormat;

public class RedeemMoneyActivity extends AppCompatActivity {

    TextView maxAmt;
    EditText amountRedeemed, numEntered, upiEntered;
    RelativeLayout numLayout, upiLayout;
    Spinner paymentMode;
    Button submit;

    DatabaseReference redeemRef;

    double amt;
    DecimalFormat df;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redeem_money);

        df = new DecimalFormat("#.00");

        maxAmt = findViewById(R.id.redeemMoneyMaxAmt);
        numLayout = findViewById(R.id.redeemMoneyEnterNumberLayout);
        upiLayout = findViewById(R.id.redeemMoneyEnterUPILayout);
        paymentMode = findViewById(R.id.redeemMoneyPaymentModeSpinner);
        submit = findViewById(R.id.redeemMoneySubmitButton);
        amountRedeemed = findViewById(R.id.redeemMoneyAmountEditText);
        numEntered = findViewById(R.id.redeemMoneyEnterNumberEditText);
        upiEntered = findViewById(R.id.redeemMoneyEnterUPIEditText);

        Intent intent = getIntent();
        amt = intent.getDoubleExtra("maxAmount", 0.0);
        maxAmt.setText("Maximum amount that can be redeemed : ₹ " + df.format(amt));

        amountRedeemed.setText(df.format(amt));

        paymentMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                    numLayout.setVisibility(View.VISIBLE);
                    upiLayout.setVisibility(View.GONE);
                    numEntered.requestFocus();
                } else {
                    numLayout.setVisibility(View.GONE);
                    upiLayout.setVisibility(View.VISIBLE);
                    upiEntered.requestFocus();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        redeemRef = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getUid()).child("redeemMoney");

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitDetails();
            }
        });
    }

    public void submitDetails() {
        double userAmt = Double.parseDouble(amountRedeemed.getText().toString());
        long phoneNum = 0;
        boolean isUpiId;
        if (userAmt > amt) {
            Toast.makeText(this, "Enter amount less than or equal to ₹ " + df.format(amt), Toast.LENGTH_SHORT).show();
            return;
        }
        String modeSel = paymentMode.getSelectedItem().toString();
        if (modeSel.matches("PayTM")) {
            String number = numEntered.getText().toString();
            if (number.length() != 10) {
                Toast.makeText(this, "Enter a valid number", Toast.LENGTH_SHORT).show();
                return;
            }
            phoneNum = Long.parseLong(number);
            redeemRef.child("method").setValue("PayTM");
            redeemRef.child("amount").setValue(userAmt);
            redeemRef.child("recipient").setValue(phoneNum);
        }
        else
            {
            String number = upiEntered.getText().toString();
            if (!number.contains("@")) {
                if (number.length() != 10) {
                    Toast.makeText(this, "Enter a valid UPI ID / Number", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    phoneNum = Long.parseLong(upiEntered.getText().toString());
                    isUpiId = false;
                }
            } else {
                isUpiId = true;
            }
            redeemRef.child("method").setValue("UPI");
            redeemRef.child("amount").setValue(userAmt);
            if (isUpiId)
                redeemRef.child("recipient").setValue(number);
            else
                redeemRef.child("recipient").setValue(phoneNum);
        }
        Toast.makeText(this, "Your request will be processed within 7 working days", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(RedeemMoneyActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}
