package com.pnpj.noteshub;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyUploadsActivity extends AppCompatActivity {

    FirebaseAuth mFirebaseAuth;
    DatabaseReference userUploadsReference,notesRef;

    ArrayList<showNotes> uploadlist = new ArrayList<>();
    ArrayList<String> uploads = new ArrayList<>();
    RecyclerView recyclerView;
    LinearLayout linearLayout;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_uploads);

        progressDialog = new ProgressDialog(MyUploadsActivity.this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        AdView mAdView = (AdView) findViewById(R.id.myUploadsadView);
        final AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        recyclerView = findViewById(R.id.myUploadsRecyclerView);
        linearLayout = findViewById(R.id.myUploadsNewFileLinearLayout);
        linearLayout.setVisibility(View.GONE);
        mFirebaseAuth =FirebaseAuth.getInstance();
        notesRef = FirebaseDatabase.getInstance().getReference().child("notes");
        userUploadsReference = FirebaseDatabase.getInstance().getReference().child("users").child(mFirebaseAuth.getUid()).child("uploads");
        userUploadsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren())
                {
                    uploads.add(data.getKey());
                }
                if(uploads.size() == 0)
                    setAdapter();
                for(int i=0;i<uploads.size();i++)
                {
                    final int finalI = i;
                    notesRef.child(uploads.get(i)).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            showNotes Show = dataSnapshot.getValue(showNotes.class);
                            Show.setPushId(uploads.get(finalI));
                            uploadlist.add(Show);
                            if((finalI +1)==uploads.size())
                                setAdapter();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MyUploadsActivity.this,UploadNotesActivity.class));
            }
        });
    }

    public void setAdapter()
    {
        myUploadsAdapter uploadAdapter = new myUploadsAdapter(uploadlist,"uploads",this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this)
        {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(uploadAdapter);
        if(progressDialog.isShowing())
            progressDialog.dismiss();
        linearLayout.setVisibility(View.VISIBLE);
    }

}
