package com.pnpj.noteshub;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

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

    private TextView NoNotesFound;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ((MainActivity)getActivity()).setActionBarTitle("NotesHub");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NetworkConnectionChecker networkConnectionChecker = new NetworkConnectionChecker(getActivity());
        boolean isNetwork = networkConnectionChecker.isNetworkAvailable();
        if(!isNetwork)
        {
            Toast.makeText(view.getContext(), "No Network Available", Toast.LENGTH_LONG).show();
            startActivity(new Intent(view.getContext(),DownloadsActivity.class));
        }

        mFirebaseAuth = FirebaseAuth.getInstance();

        mReference = FirebaseDatabase.getInstance().getReference().child("branches");
        noteRef = FirebaseDatabase.getInstance().getReference().child("notes");

        recyclerViewTopNotes = view.findViewById(R.id.homeFragmentrecyclerView_TopBranch);
        recyclerViewBranch = view.findViewById(R.id.homeFragmentBranchRecyclerView);
        progressTopNotes = view.findViewById(R.id.homeFragmentprogressTopNotes);
        progressBranch = view.findViewById(R.id.homeFragmentprogressBranch);
        topNotes = view.findViewById(R.id.homeFragmentTopNotesTextView);
        NoNotesFound = view.findViewById(R.id.homeFragNotesNoFound);

        SharedPreferences sharedPreferences = view.getContext().getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        String branch = sharedPreferences.getString(mFirebaseAuth.getUid()+"branch","");
        topNotes.setText("Top Notes From "+branch);

        pushIDRef = FirebaseDatabase.getInstance().getReference().child("branches").child(branch).child("allnotes");
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
                {
                    setTopNotesAdapter();
                }
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
                progressBranch.setVisibility(View.GONE);
                recyclerViewBranch.setVisibility(View.VISIBLE);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        branchAdapter = new HomeFragBranchAdapter(branchList,true,"",getActivity());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext())
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

    @Override
    public void onPause() {
        super.onPause();
        branchList.clear();
        notesList.clear();
    }

    public void setTopNotesAdapter()
    {
        if(notesList.isEmpty())
        {
            progressTopNotes.setVisibility(View.GONE);
            recyclerViewTopNotes.setVisibility(View.GONE);
            NoNotesFound.setVisibility(View.VISIBLE);
            return;
        }
        Collections.reverse(notesList);
        topNotesAdapter = new NotesAdapter(notesList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false);
        recyclerViewTopNotes.setLayoutManager(mLayoutManager);
        recyclerViewTopNotes.setItemAnimator(new DefaultItemAnimator());
        recyclerViewTopNotes.setAdapter(topNotesAdapter);

        progressTopNotes.setVisibility(View.GONE);
        recyclerViewTopNotes.setVisibility(View.VISIBLE);
    }
}
