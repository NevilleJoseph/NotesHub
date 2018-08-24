package com.pnpj.noteshub;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {

    Spinner collegeSpinner,branchSpinner,subjectSpinner,semesterSpinner;
    ArrayList<String> collegeList,branchList,subjectList,semesterList;
    ArrayAdapter<String> collegeAdapter,branchAdapter,subjectAdapter,semAdapter;
    DatabaseReference collegeRef,branchRef,subjectRef,semRef;
    String collegeSelected="",branchSelected="",subjectSelected="",semSelected="";

    Button button;

    LinearLayout semLayout,subjectLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setTitle("Search Notes");

        AdView mAdView = (AdView) findViewById(R.id.searchadView);
        final AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        collegeSpinner = findViewById(R.id.searchFragCollegeSpinner);
        branchSpinner = findViewById(R.id.searchFragBranchSpinner);
        subjectSpinner = findViewById(R.id.searchFragSubjectSpinner);
        semesterSpinner = findViewById(R.id.searchFragSemesterSpinner);

        button = findViewById(R.id.searchFragSubmitButton);

        semLayout = findViewById(R.id.searchFragSemesterLinearLayout);
        subjectLayout = findViewById(R.id.searchFragSubjectLinearLayout);

        collegeList = new ArrayList<>();
        branchList = new ArrayList<>();
        subjectList = new ArrayList<>();
        semesterList = new ArrayList<>();

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        collegeRef = FirebaseDatabase.getInstance().getReference().child("colleges");
        collegeList.add("Any");
        collegeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot names : dataSnapshot.getChildren())
                {
                    collegeList.add(names.getKey());
                }
                if(collegeList.isEmpty())
                {
                    collegeList.add("No Data Found");
                }
                collegeAdapter = new ArrayAdapter<String>(SearchActivity.this,android.R.layout.simple_list_item_1,collegeList);
                collegeSpinner.setAdapter(collegeAdapter);
                if(progressDialog.isShowing())
                    progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        collegeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, final View view, int i, long l) {
                collegeSelected = collegeSpinner.getSelectedItem().toString();
                if(collegeSelected.matches("Any"))
                {
                    semLayout.setVisibility(View.GONE);
                    subjectLayout.setVisibility(View.VISIBLE);
                    progressDialog.show();
                    branchRef = FirebaseDatabase.getInstance().getReference().child("branches");
                    branchList.clear();
                    branchRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for(DataSnapshot names : dataSnapshot.getChildren())
                            {
                                branchList.add(names.getKey());
                            }
                            if(branchList.isEmpty())
                            {
                                branchList.add("No Data Found");
                            }
                            branchAdapter = new ArrayAdapter<String>(SearchActivity.this,android.R.layout.simple_list_item_1,branchList);
                            branchSpinner.setAdapter(branchAdapter);
                            if(progressDialog.isShowing())
                                progressDialog.dismiss();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                else
                {
                    semLayout.setVisibility(View.VISIBLE);
                    subjectLayout.setVisibility(View.GONE);
                    progressDialog.show();
                    branchRef = FirebaseDatabase.getInstance().getReference().child("colleges").child(collegeSelected);
                    branchList.clear();
                    branchRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for(DataSnapshot names : dataSnapshot.getChildren())
                            {
                                branchList.add(names.getKey());
                            }
                            if(branchList.isEmpty())
                            {
                                branchList.add("No Data Found");
                            }
                            branchAdapter = new ArrayAdapter<String>(SearchActivity.this,android.R.layout.simple_list_item_1,branchList);
                            branchSpinner.setAdapter(branchAdapter);
                            if(progressDialog.isShowing())
                                progressDialog.dismiss();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        branchSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, final View view, int i, long l) {
                branchSelected = branchSpinner.getSelectedItem().toString();
                if(collegeSelected.matches("Any"))
                {
                    progressDialog.show();
                    subjectRef = FirebaseDatabase.getInstance().getReference().child("branches").child(branchSelected);
                    subjectList.clear();
                    subjectRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for(DataSnapshot names : dataSnapshot.getChildren())
                            {
                                subjectList.add(names.getKey());
                            }
                            if(subjectList.isEmpty())
                            {
                                subjectList.add("No Data Found");
                            }
                            else {
                                subjectList.remove("allnotes");
                                subjectList.add("All Notes");
                            }
                            subjectAdapter = new ArrayAdapter<String>(SearchActivity.this,android.R.layout.simple_list_item_1,subjectList);
                            subjectSpinner.setAdapter(subjectAdapter);
                            if(progressDialog.isShowing())
                                progressDialog.dismiss();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                else
                {
                    progressDialog.show();
                    semRef = FirebaseDatabase.getInstance().getReference().child("colleges").child(collegeSelected).child(branchSelected);
                    semesterList.clear();
                    semRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for(DataSnapshot sem : dataSnapshot.getChildren())
                            {
                                semesterList.add(sem.getKey());
                            }
                            if(semesterList.isEmpty())
                            {
                                semesterList.add("No Data Found");
                            }
                            else {
                                semesterList.remove("allnotes");
                                semesterList.add("All Notes");
                            }
                            semAdapter = new ArrayAdapter<String>(view.getContext(),android.R.layout.simple_list_item_1,semesterList);
                            semesterSpinner.setAdapter(semAdapter);
                            if(progressDialog.isShowing())
                                progressDialog.dismiss();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(collegeSelected.matches("Any"))
                {
                    subjectSelected = subjectSpinner.getSelectedItem().toString();
                }
                else
                {
                    semSelected = semesterSpinner.getSelectedItem().toString();
                }
                if((collegeSelected.matches("No Data Found")) || branchSelected.matches("No Data Found") || semSelected.matches("No Data Found") || subjectSelected.matches("No Data Found"))
                    Toast.makeText(view.getContext(), "No Data Found", Toast.LENGTH_SHORT).show();
                else
                {
                    Intent intent = new Intent(view.getContext(),ViewSearchedNotesActivity.class);
                    intent.putExtra("selectedCollege",collegeSelected);
                    intent.putExtra("selectedBranch",branchSelected);
                    if(semSelected.matches("All Notes"))
                        semSelected = "allnotes";
                    if(subjectSelected.matches("All Notes"))
                        subjectSelected = "allnotes";
                    intent.putExtra("selectedSem",semSelected);
                    intent.putExtra("selectedSubject",subjectSelected);
                    startActivity(intent);
                }
            }
        });
    }
}
