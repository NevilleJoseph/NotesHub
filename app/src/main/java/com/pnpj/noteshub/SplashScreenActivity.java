package com.pnpj.noteshub;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;

public class SplashScreenActivity extends Activity {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private DatabaseReference databaseReference;
    private FirebaseUser user;

    public static final int RC_SIGN_IN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        mFirebaseAuth = FirebaseAuth.getInstance();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull final FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if(user != null)
                {
                    SharedPreferences preferences = getSharedPreferences("myPrefs",MODE_PRIVATE);
                    boolean check = preferences.getBoolean(mFirebaseAuth.getUid(),false);
                    if(check)
                    {
                        Intent intent = new Intent(SplashScreenActivity.this,MainActivity.class);
                        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
                        startActivity(intent);
                        finish();
                    }
                    else
                    {
                        final Handler progresshandler = new Handler();
                        progresshandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                ProgressBar progressBar = findViewById(R.id.progressBar);
                                progressBar.setVisibility(View.VISIBLE);
                            }
                        }, 2000);

                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder builder;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    builder = new AlertDialog.Builder(SplashScreenActivity.this, android.R.style.Theme_Material_Dialog_Alert);
                                } else {
                                    builder = new AlertDialog.Builder(SplashScreenActivity.this);
                                }
                                builder.setTitle("Network Error")
                                        .setMessage("Please check your network connection and try again")
                                        .setNeutralButton("Retry", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                finish();
                                                startActivity(getIntent());
                                            }
                                        })
                                        .setIcon(android.R.drawable.ic_dialog_alert);
                                if(!((SplashScreenActivity.this)).isFinishing())
                                {
                                    builder.show();
                                }
                            }
                        }, 20000);
                        databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
                        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.hasChild(firebaseAuth.getUid()))
                                {
                                    String uid = mFirebaseAuth.getUid();
                                    SharedPreferences preferences1 = getSharedPreferences("myPrefs",MODE_PRIVATE);
                                    final SharedPreferences.Editor editor = preferences1.edit();
                                    editor.putBoolean(uid,true);
                                    userclass temp = dataSnapshot.child(uid).getValue(userclass.class);
                                    editor.putString(uid+"branch",temp.getBranch());
                                    editor.putString(uid+"sem",temp.getSemester());
                                    editor.putBoolean(uid+"uploader",temp.isUploader());
                                    editor.putString(uid+"college",temp.getCollege());
                                    editor.putString(uid+"roll",temp.getRollNo());
                                    editor.commit();
                                    startActivity(new Intent(SplashScreenActivity.this,MainActivity.class));
                                    finish();
                                }
                                else
                                {
                                    if(!(user.isEmailVerified()))
                                    {
                                        user.sendEmailVerification();
                                        startActivity(new Intent(SplashScreenActivity.this,EmailNotVerifiedActivity.class));
                                        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
                                    }
                                    else
                                    {
                                        Intent intent = new Intent(SplashScreenActivity.this,FirstSignInActivity.class);
                                        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
                                        startActivity(intent);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(SplashScreenActivity.this, "Check Your Network Connection", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
                    }
                }
                else
                {
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setLogo(R.drawable.logo)
                                    .setTheme(R.style.FirebaseLoginTheme)
                                    .setAvailableProviders(
                                            Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                                                    new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build()))
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN && resultCode == RESULT_CANCELED)
        {
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }
}
