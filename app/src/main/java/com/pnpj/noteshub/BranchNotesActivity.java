package com.pnpj.noteshub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class BranchNotesActivity extends AppCompatActivity {

    private NotesAdapter topNotesAdapter;
    private HomeFragBranchAdapter branchAdapter;
    private RecyclerView recyclerViewTopNotes, recyclerViewBranch;
    private ProgressBar progressTopNotes,progressBranch;
    private TextView topNotes;
    private ArrayList<showNotes> notesList = new ArrayList<>();
    private ArrayList<String> pushIdList = new ArrayList<>();
    private FirebaseAuth mFirebaseAuth;
    private ArrayList<String> branchList = new ArrayList<>();

    private DatabaseReference mReference,noteRef,pushIDRef;

    private TextView noTopNotes,noSubjects;

    AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_branch_notes);

        Intent intent = getIntent();
        String branch = intent.getStringExtra("branch");
        setTitle(branch);

        mFirebaseAuth = FirebaseAuth.getInstance();

        mReference = FirebaseDatabase.getInstance().getReference().child("branches").child(branch);
        noteRef = FirebaseDatabase.getInstance().getReference().child("notes");

        mAdView = findViewById(R.id.branchNotesadView);
        final AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        recyclerViewTopNotes = findViewById(R.id.branchNotesrecyclerView_TopBranch);
        recyclerViewBranch = findViewById(R.id.branchNotesBranchRecyclerView);
        progressTopNotes = findViewById(R.id.branchNotesprogressTopNotes);
        progressBranch = findViewById(R.id.branchNotesprogressBranch);
        topNotes = findViewById(R.id.branchNotesTopNotesTextView);
        noTopNotes = findViewById(R.id.branchNotesTopNotesNoFound);
        noSubjects = findViewById(R.id.branchNotesSubjectNoFound);

        SharedPreferences prefs = getSharedPreferences("myPrefs",MODE_PRIVATE);
        String college = prefs.getString(mFirebaseAuth.getUid()+"college","");

        topNotes.setText("Top Notes From "+branch+" in "+college);

        pushIDRef = FirebaseDatabase.getInstance().getReference().child("colleges").child(college).child(branch).child("allnotes");
        Query query = pushIDRef.orderByValue().limitToLast(10);
        pushIdList.clear();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren())
                {
                    pushIdList.add(data.getKey());
                }
                if(pushIdList.isEmpty())
                    setTopNotesAdapter();
                for(int i=0;i<pushIdList.size();i++)
                {
                    final int finalI = i;
                    noteRef.child(pushIdList.get(i)).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            showNotes Show = dataSnapshot.getValue(showNotes.class);
                            Show.setPushId(pushIdList.get(finalI));
                            notesList.add(Show);
                            if((finalI+1) == pushIdList.size())
                            {
                                setTopNotesAdapter();
                            }
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

        progressTopNotes.setVisibility(View.VISIBLE);
        recyclerViewTopNotes.setVisibility(View.GONE);
        progressBranch.setVisibility(View.VISIBLE);
        recyclerViewBranch.setVisibility(View.GONE);

        mReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot getBranch : dataSnapshot.getChildren())
                {
                    branchList.add(getBranch.getKey());
                }
                branchList.remove("allnotes");
                if(branchList.isEmpty())
                {
                    progressBranch.setVisibility(View.GONE);
                    recyclerViewBranch.setVisibility(View.GONE);
                    noSubjects.setVisibility(View.VISIBLE);
                }
                else
                {
                    progressBranch.setVisibility(View.GONE);
                    recyclerViewBranch.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        branchAdapter = new HomeFragBranchAdapter(branchList,false,branch,this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this)
        {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        recyclerViewBranch.setLayoutManager(linearLayoutManager);
        recyclerViewBranch.setItemAnimator(new DefaultItemAnimator());
        recyclerViewBranch.setAdapter(branchAdapter);

    }

    public void setTopNotesAdapter()
    {
        if(notesList.isEmpty())
        {
            progressTopNotes.setVisibility(View.GONE);
            recyclerViewTopNotes.setVisibility(View.GONE);
            noTopNotes.setVisibility(View.VISIBLE);
            return;
        }
        Collections.reverse(notesList);
        topNotesAdapter = new NotesAdapter(notesList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        recyclerViewTopNotes.setLayoutManager(mLayoutManager);
        recyclerViewTopNotes.setItemAnimator(new DefaultItemAnimator());
        recyclerViewTopNotes.setAdapter(topNotesAdapter);

        progressTopNotes.setVisibility(View.GONE);
        recyclerViewTopNotes.setVisibility(View.VISIBLE);
    }
}
