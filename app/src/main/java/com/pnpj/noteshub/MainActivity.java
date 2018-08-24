package com.pnpj.noteshub;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    TextView usernameTextView , emailTextView;
    NavigationView navigationView;
    FragmentTransaction fragmentTransaction;

    public static final int RC_SIGN_IN = 1;

    private String mUserName = "Guest User";
    private String mEmailId = "Guest Account";

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseUser user;
    private DatabaseReference mUserDatabaseReference;
    private AdView mAdView;
    private InterstitialAd mInterAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUserDatabaseReference = mFirebaseDatabase.getReference().child("users");

        MobileAds.initialize(this,"ca-app-pub-9819992292681972~1228303845");

        mAdView = (AdView) findViewById(R.id.adView);
        final AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        final Menu menu = navigationView.getMenu();
        SharedPreferences sharedprefs = getSharedPreferences("myPrefs",MODE_PRIVATE);
        boolean uploader = sharedprefs.getBoolean(mFirebaseAuth.getUid()+"uploader",false);
        if(uploader)
        {
            if(sharedprefs.getBoolean(mFirebaseAuth.getUid()+"hasUploaded",false))
            {
                menu.findItem(R.id.nav_upload).setTitle("My Uploads");
            }
            else
            {
                menu.findItem(R.id.nav_upload).setTitle("Upload Notes");
            }
        }

      mAuthStateListener = new FirebaseAuth.AuthStateListener() {
          @Override
          public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
              user = firebaseAuth.getCurrentUser();
              if(user != null)
              {
                  OnSignedInInitialise(user.getDisplayName(),user.getEmail());
              }
              else
              {
                  OnSignedOutInitialise();
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

        final SharedPreferences prefs = getSharedPreferences("myPrefs",MODE_PRIVATE);
        boolean testVerified = prefs.getBoolean(mFirebaseAuth.getUid()+"isVerifiedUploader",false);
        if(!testVerified)
        {
            DatabaseReference isVerifiedRef = FirebaseDatabase.getInstance().getReference().child("users").child(mFirebaseAuth.getUid()).child("verifiedUploader");
            isVerifiedRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    boolean checkVerified;
                    if(dataSnapshot.exists())
                        checkVerified = dataSnapshot.getValue(boolean.class);
                    else
                        checkVerified = false;
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(mFirebaseAuth.getUid()+"isVerifiedUploader",checkVerified);
                    editor.commit();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }


      DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(mFirebaseAuth.getUid());
      SharedPreferences preferences = getSharedPreferences("myPrefs",MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();
      databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
          @Override
          public void onDataChange(DataSnapshot dataSnapshot) {
              if(dataSnapshot.hasChild("uploads")) {
                  editor.putBoolean(mFirebaseAuth.getUid() + "hasUploaded", true);
                  menu.findItem(R.id.nav_upload).setTitle("My Uploads");
                  editor.commit();
              }
              String college = dataSnapshot.child("college").getValue().toString();
              if(college.matches("NIT PATNA")||college.matches("Nit patna")||college.matches("national institute of technology, patna"))
              {
                  DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(mFirebaseAuth.getUid()).child("college");
                  ref.setValue("NIT Patna");
                  editor.putString(mFirebaseAuth.getUid()+"college","NIT Patna");
                  editor.commit();
              }
          }

          @Override
          public void onCancelled(DatabaseError databaseError) {

          }
      });
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

    @Override
    protected void onPause() {
        super.onPause();
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentframe);
            if (currentFragment instanceof HomeFragment)
            {
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(MainActivity.this);
                }
                SharedPreferences preferences = getSharedPreferences("myPrefs",MODE_PRIVATE);
                boolean uploads = preferences.getBoolean(mFirebaseAuth.getUid()+"hasActiveUploads",false);
                if(uploads)
                {
                    builder.setTitle("Uploads In Progress")
                            .setMessage("Some files are being uploaded in the background. Do you really want to exit the app?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
                else
                {
                    builder.setTitle("Confirm Exit")
                            .setMessage("Do you really want to exit the app?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            }
            else
            {
                fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragmentframe, new HomeFragment());
                fragmentTransaction.commit();
                navigationView.setCheckedItem(R.id.nav_home);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.signout) {

            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_Material_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(MainActivity.this);
            }
            builder.setTitle("Confirm Sign Out")
                    .setMessage("Do you really want to sign out of this app?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(MainActivity.this,SplashScreenActivity.class);
                            startActivity(intent);
                            AuthUI.getInstance().signOut(MainActivity.this);
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        else if(id == R.id.mainActivitySearch)
        {
            startActivity(new Intent(MainActivity.this,SearchActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if(id == R.id.nav_home)
        {
            fragmentTransaction.replace(R.id.fragmentframe, new HomeFragment());
        }
        else
        if(id == R.id.nav_downloads)
        {
            startActivity(new Intent(MainActivity.this,DownloadsActivity.class));
        }
        else
        if(id == R.id.nav_aboutus)
        {
            fragmentTransaction.replace(R.id.fragmentframe, new AboutUsFragment());
        }
        else
        if(id==R.id.nav_upload)
        {
            SharedPreferences sharedPreferences = getSharedPreferences("myPrefs",MODE_PRIVATE);
            boolean uploads = sharedPreferences.getBoolean(mFirebaseAuth.getUid()+"hasActiveUploads",false);
            if(uploads)
            {
                Toast.makeText(this, "Please wait while files are being uploaded", Toast.LENGTH_SHORT).show();
            }
            else
            {
                boolean uploader = sharedPreferences.getBoolean(mFirebaseAuth.getUid()+"uploader",false);
                if(uploader)
                {
                    if(sharedPreferences.getBoolean(mFirebaseAuth.getUid()+"isVerifiedUploader",false))
                    {
                        if(sharedPreferences.getBoolean(mFirebaseAuth.getUid()+"hasUploaded",false))
                        {
                            startActivity(new Intent(MainActivity.this,MyUploadsActivity.class));
                        }
                        else
                        {
                            startActivity(new Intent(MainActivity.this,UploadNotesActivity.class));
                        }
                    }
                    else
                    {
                        Toast.makeText(this, "Your uploader account is under verification", Toast.LENGTH_LONG).show();
                    }

                }
                else
                {
                    startActivity(new Intent(MainActivity.this,FirstSignInActivity.class));
                }
            }
        }
        else
        if(id == R.id.my_account)
        {
            startActivity(new Intent(MainActivity.this,MyAccountActivity.class));
        }
        else
        if(id == R.id.nav_rateus)
        {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=com.pnpj.noteshub"));
            startActivity(intent);
        }
        else
        if(id == R.id.nav_share)
        {
            try {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_SUBJECT, "NotesHub");
                String sAux = "Hey! I have found a great app to view and upload college notes and earn some cool money.\n";
                sAux = sAux + "http://play.google.com/store/apps/details?id=com.pnpj.noteshub";
                i.putExtra(Intent.EXTRA_TEXT, sAux);
                startActivity(Intent.createChooser(i, "Choose an App"));
            } catch(Exception e) {
                //e.toString();
            }
        }

        fragmentTransaction.commit();


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void OnSignedInInitialise(String username, String email)
    {
        mUserName = username;
        mEmailId = email;
        View view = navigationView.getHeaderView(0);
        usernameTextView = view.findViewById(R.id.userName);
        usernameTextView.setText(mUserName);
        emailTextView = view.findViewById(R.id.emailId);
        emailTextView.setText(mEmailId);
        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragmentframe , new HomeFragment());
        fragmentTransaction.commit();
        Menu menu = navigationView.getMenu();
        menu.findItem(R.id.nav_home).setChecked(true);
    }

    private void OnSignedOutInitialise()
    {
        mUserName = "Guest User";
        mEmailId = "Guest Account";
    }

    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }
}
