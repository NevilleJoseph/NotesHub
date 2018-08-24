package com.pnpj.noteshub;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ViewSearchedNotesActivity extends AppCompatActivity {

    String selCollege,selBranch,selSubject,selSem;
    DatabaseReference subjectRef,semRef,notesRef;
    RecyclerView recyclerView;
    ArrayList<String> searchedList;
    ArrayList<showNotes> notesList;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_searched_notes);

        AdView mAdView = (AdView) findViewById(R.id.viewSearchedNotesadView);
        final AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        recyclerView = findViewById(R.id.viewSearchedNotesRecyclerView);

        searchedList = new ArrayList<>();
        notesList = new ArrayList<>();

        progressDialog = new ProgressDialog(ViewSearchedNotesActivity.this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        notesRef = FirebaseDatabase.getInstance().getReference().child("notes");

        Intent intent = getIntent();
        selCollege = intent.getStringExtra("selectedCollege");
        selBranch = intent.getStringExtra("selectedBranch");
        if(selCollege.matches("Any"))
        {
            selSubject = intent.getStringExtra("selectedSubject");
            subjectRef = FirebaseDatabase.getInstance().getReference().child("branches").child(selBranch).child(selSubject);
            searchedList.clear();
            subjectRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot notes : dataSnapshot.getChildren())
                    {
                        searchedList.add(notes.getKey());
                    }
                    notesList.clear();
                    for(int i=0;i<searchedList.size();i++)
                    {
                        final int finalI = i;
                        notesRef.child(searchedList.get(i)).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                showNotes Show = dataSnapshot.getValue(showNotes.class);
                                Show.setPushId(searchedList.get(finalI));
                                notesList.add(Show);
                                if((finalI+1) == searchedList.size())
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
        }
        else
        {
            selSem = intent.getStringExtra("selectedSem");
            semRef = FirebaseDatabase.getInstance().getReference().child("colleges").child(selCollege).child(selBranch).child(selSem);
            searchedList.clear();
            semRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot names : dataSnapshot.getChildren())
                    {
                        searchedList.add(names.getKey());
                    }
                    notesList.clear();
                    for(int i=0;i<searchedList.size();i++)
                    {
                        final int finalI = i;
                        notesRef.child(searchedList.get(i)).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                showNotes Show = dataSnapshot.getValue(showNotes.class);
                                Show.setPushId(searchedList.get(finalI));
                                notesList.add(Show);
                                if((finalI+1) == searchedList.size())
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
        }

    }

    public void setAdapter()
    {
        myUploadsAdapter uploadAdapter = new myUploadsAdapter(notesList,"searchNotes",this);
        recyclerView.setLayoutManager(new LinearLayoutManager(ViewSearchedNotesActivity.this,LinearLayoutManager.VERTICAL,false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(uploadAdapter);
        if(progressDialog.isShowing())
            progressDialog.dismiss();
    }
}
