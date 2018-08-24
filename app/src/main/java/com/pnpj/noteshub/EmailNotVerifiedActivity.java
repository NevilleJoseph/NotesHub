package com.pnpj.noteshub;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;

public class EmailNotVerifiedActivity extends AppCompatActivity {

    String showMessage="A verification e-mail has been sent to \n";
    String showMessage2 = "\nPlease click on the link provided in the e-mail to verify your email.";
    FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_not_verified);

        mFirebaseAuth = FirebaseAuth.getInstance();

        TextView textView = findViewById(R.id.emailVerificationTextView);
        textView.setTextColor(getResources().getColor(R.color.black));
        textView.setText(showMessage+mFirebaseAuth.getCurrentUser().getEmail()+showMessage2);

        Button button = findViewById(R.id.goBack);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AuthUI.getInstance().signOut(EmailNotVerifiedActivity.this);
                Intent intent = new Intent(EmailNotVerifiedActivity.this,SplashScreenActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
