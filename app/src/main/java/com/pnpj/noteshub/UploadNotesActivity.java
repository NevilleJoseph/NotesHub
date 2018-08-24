package com.pnpj.noteshub;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class UploadNotesActivity extends AppCompatActivity {

    FirebaseAuth mFirebaseAuth;
    DatabaseReference branchNameRef, collegeNameRef, notesRef,subjectRef,semesterRef,userUploadsRef,getUploadDetails,getSubjectsRef;
    FirebaseDatabase mFirebaseDatabase;
    StorageReference storageReference,photoRef;

    private static final int RC_PHOTO_PICKER =  2;

    ArrayList<Image> images = new ArrayList<>();
    ArrayList<String> imagePaths = new ArrayList<>();
    ArrayList<String> college = new ArrayList<>();
    int numOfPages=0;
    String todayDate;

    AutoCompleteTextView collegeName, subject;
    Spinner branch,semester;
    Button selectImages, submit;
    CheckBox completed;
    Boolean hasUserUploaded;
    String ePushId,eCollege,eBranch,eSemester,eSubject;
    UploadNotesShowImageAdapter uploadAdapter;
    RecyclerView recyclerView;

    InterstitialAd ad;

    String collegesel,branchsel,semestersel,subjectsel;
    boolean isCompleted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_notes);

        ad = new InterstitialAd(UploadNotesActivity.this);
        ad.setAdUnitId("ca-app-pub-9819992292681972/6338936398");
        AdRequest adRequestInterstitial = new AdRequest.Builder().build();
        ad.loadAd(adRequestInterstitial);

        Intent intent = getIntent();
        hasUserUploaded = intent.getBooleanExtra("hasUserUploaded",false);
        if(hasUserUploaded) {
            ePushId = intent.getStringExtra("pushId");
            eCollege = intent.getStringExtra("college");
            eBranch = intent.getStringExtra("branch");
            eSemester = intent.getStringExtra("semester");
            eSubject = intent.getStringExtra("subject");
            numOfPages = intent.getIntExtra("numPages",0);
        }

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        branchNameRef = mFirebaseDatabase.getReference().child("branches");
        collegeNameRef = mFirebaseDatabase.getReference().child("colleges");
        notesRef = mFirebaseDatabase.getReference().child("notes");
        getSubjectsRef = mFirebaseDatabase.getReference().child("branches");

        collegeName = findViewById(R.id.uploadCollegeAutoComplete);
        subject = findViewById(R.id.uploadSubjectAutoComplete);
        branch = findViewById(R.id.uploadBranchSpinner);
        semester = findViewById(R.id.uploadSemesterSpinner);
        selectImages = findViewById(R.id.uploadSelectImagesButton);
        submit = findViewById(R.id.uploadSubmitButton);
        recyclerView = findViewById(R.id.recyclerViewUploadNotes);
        completed = findViewById(R.id.uploadCompletedCheckBox);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        todayDate = simpleDateFormat.format(calendar.getTime());

        if(hasUserUploaded) {
            collegeName.setText(eCollege);
            collegeName.setEnabled(false);
            ArrayList<String> branches = new ArrayList<>();
            branches.add(eBranch);
            ArrayAdapter adapterBranches = new ArrayAdapter(UploadNotesActivity.this,android.R.layout.simple_list_item_1,branches);
            branch.setAdapter(adapterBranches);
            branch.setSelection(0);
            branch.setEnabled(false);
            int sem = Integer.parseInt(eSemester.substring(0,1));
            semester.setSelection(sem-1);
            semester.setEnabled(false);
            subject.setText(eSubject);
            subject.setEnabled(false);
        }
        else
        {
            SharedPreferences sharedPreferences = getSharedPreferences("myPrefs",MODE_PRIVATE);

            collegeName.setText(sharedPreferences.getString(mFirebaseAuth.getUid()+"college",""));
            collegeName.dismissDropDown();
            String sub = sharedPreferences.getString(mFirebaseAuth.getUid()+"sem","").substring(0,1);
            int pos = Integer.parseInt(sub);
            semester.setSelection(pos-1);

            final ArrayAdapter collegeAdapter = new ArrayAdapter(UploadNotesActivity.this,android.R.layout.simple_list_item_1,college);
            collegeNameRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot getCollege : dataSnapshot.getChildren())
                    {
                        college.add(getCollege.getKey());
                    }
                    collegeName.setThreshold(1);
                    collegeName.setAdapter(collegeAdapter);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            final ProgressDialog progressDialog = new ProgressDialog(UploadNotesActivity.this);
            progressDialog.setMessage("Loading...");
            progressDialog.show();

            final ArrayList<String> branches = new ArrayList<>();
            final ArrayAdapter adapterBranches = new ArrayAdapter(UploadNotesActivity.this,android.R.layout.simple_list_item_1,branches);
            branchNameRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot getBranch : dataSnapshot.getChildren())
                    {
                        branches.add(getBranch.getKey());
                    }
                    branch.setAdapter(adapterBranches);
                    if(progressDialog.isShowing())
                        progressDialog.dismiss();
                    SharedPreferences sharedPreferences = getSharedPreferences("myPrefs",MODE_PRIVATE);
                    boolean check = sharedPreferences.getBoolean(mFirebaseAuth.getUid(),false);
                    if(check)
                    {
                        String branchName = sharedPreferences.getString(mFirebaseAuth.getUid()+"branch","");
                        int pos = adapterBranches.getPosition(branchName);
                        branch.setSelection(pos);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            branch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    String branchSelected = adapterView.getItemAtPosition(i).toString();
                    progressDialog.show();
                    subject.setText("");
                    getSubjectsRef.child(branchSelected).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            ArrayList<String> subjectList = new ArrayList<>();
                            ArrayAdapter adapterSubject = new ArrayAdapter(UploadNotesActivity.this,android.R.layout.simple_list_item_1,subjectList);
                            for(DataSnapshot data : dataSnapshot.getChildren())
                            {
                                subjectList.add(data.getKey());
                            }
                            subjectList.remove("allnotes");
                            subject.setThreshold(1);
                            subject.setAdapter(adapterSubject);
                            if(progressDialog.isShowing())
                                progressDialog.dismiss();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        }

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean check = uploadFile();
                if(check) {
                    Intent intent = new Intent(UploadNotesActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    ad.show();
                }
        }});

        selectImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.create(UploadNotesActivity.this)
                        .multi()
                        .limit(20)
                        .showCamera(false)
                        .start(RC_PHOTO_PICKER);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK)
        {
            images = (ArrayList<Image>) ImagePicker.getImages(data);
           for(Image image : images)
            {
                imagePaths.add(image.getPath());
            }
            uploadAdapter = new UploadNotesShowImageAdapter(imagePaths,this);
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(UploadNotesActivity.this,LinearLayoutManager.HORIZONTAL,false);
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(uploadAdapter);
            recyclerView.setVisibility(View.VISIBLE);

        }
    }

    public boolean uploadFile()
    {
        collegesel = collegeName.getText().toString();
        branchsel = branch.getSelectedItem().toString();
        semestersel = semester.getSelectedItem().toString();
        subjectsel = subject.getText().toString();
        isCompleted = completed.isChecked();
        if(collegesel.matches("")||subjectsel.matches("")) {
            Toast.makeText(UploadNotesActivity.this, "Enter Complete Details", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(imagePaths.isEmpty())
        {
            Toast.makeText(UploadNotesActivity.this,"Select Files To Upload",Toast.LENGTH_SHORT).show();
            return false;
        }

        UploadTasks uploadTasks = new UploadTasks();
        uploadTasks.execute();

        Toast.makeText(UploadNotesActivity.this,"Uploading...",Toast.LENGTH_SHORT).show();

        return true;
    }

    public class UploadTasks extends AsyncTask<Void,Void,Void>
    {
        @Override
        protected Void doInBackground(Void... voids) {

            final String pushId;
            if(hasUserUploaded)
            {
                pushId = ePushId;
            }
            else {
                pushId = notesRef.push().getKey();
            }

            final SharedPreferences sharedPreferences = getSharedPreferences("myPrefs",MODE_PRIVATE);
            sharedPreferences.edit().putBoolean(mFirebaseAuth.getUid()+"hasUploaded",true).apply();

            sharedPreferences.edit().putBoolean(mFirebaseAuth.getUid()+"hasActiveUploads",true).commit();

            storageReference = FirebaseStorage.getInstance().getReference().child("notes").child(pushId);
            for(int i=1;i<=uploadAdapter.getItemCount();i++)
            {
            /*final int notificationID = 100;

            final NotificationManager mNotifyManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
            mBuilder.setContentTitle("Uploading Files")
                    .setContentText("Upload in progress")
                    .setSmallIcon(R.drawable.ic_file_download_black_24dp);*/


                Uri uri = Uri.fromFile(new File(uploadAdapter.getImagePath(i-1)));
                photoRef = storageReference.child(numOfPages+i+"");
                final int finalI = i;

         /*  Bitmap bmp = null;
          try {
                bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();

            bitmapOptions.inJustDecodeBounds = true; // obtain the size of the image, without loading it in memory
            BitmapFactory.decodeFile(uploadAdapter.getImagePath(i-1), bitmapOptions);

// find the best scaling factor for the desired dimensions
            int desiredWidth = 720;
            int desiredHeight = 1280;
            float widthScale = (float)bitmapOptions.outWidth/desiredWidth;
            float heightScale = (float)bitmapOptions.outHeight/desiredHeight;
            float scale = Math.min(widthScale, heightScale);

            int sampleSize = 1;
            while (sampleSize < scale) {
                sampleSize *= 2;
            }
            bitmapOptions.inSampleSize = sampleSize; // this value must be a power of 2,
            // this is why you can not have an image scaled as you would like
            bitmapOptions.inJustDecodeBounds = false; // now we want to load the image

// Let's load just the part of the image necessary for creating the thumbnail, not the whole image
            Bitmap thumbnail = BitmapFactory.decodeFile(uploadAdapter.getImagePath(i-1), bitmapOptions);

          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          thumbnail.compress(Bitmap.CompressFormat.JPEG,100,baos);
          byte[] data = baos.toByteArray(); */

                byte[] data = ImageUtils.compressImage(uploadAdapter.getImagePath(i-1));


                photoRef.putBytes(data)
            /*photoRef.putFile(uri).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    int currprogress = (int) progress;
                    mBuilder.setProgress(100, currprogress, false);
                    // Displays the progress bar for the first time.
                    mNotifyManager.notify(notificationID, mBuilder.build());
                }
            })*/.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                   /* mBuilder.setContentText("Upload complete")
                            .setProgress(0,0,false);
                    mNotifyManager.notify(notificationID, mBuilder.build()); */
                        if(finalI == uploadAdapter.getItemCount()) {
                            if (hasUserUploaded) {
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("notes").child(pushId);
                                ref.child("noOfPages").setValue(numOfPages + uploadAdapter.getItemCount());
                                ref.child("uploadDate").setValue(todayDate);
                            } else {
                                subjectRef = mFirebaseDatabase.getReference().child("branches").child(branchsel).child(subjectsel);
                                semesterRef = mFirebaseDatabase.getReference().child("colleges").child(collegesel).child(branchsel).child(semestersel);
                                userUploadsRef = mFirebaseDatabase.getReference().child("users").child(mFirebaseAuth.getUid()).child("uploads");

                                UploadNotes uploadNotes = new UploadNotes(branchsel, subjectsel, numOfPages + uploadAdapter.getItemCount(), collegesel, semestersel, isCompleted, mFirebaseAuth.getCurrentUser().getDisplayName(), mFirebaseAuth.getUid(), todayDate);

                                notesRef.child(pushId).setValue(uploadNotes);
                                subjectRef.child(pushId).setValue(true);
                                semesterRef.child(pushId).setValue(true);
                                userUploadsRef.child(pushId).setValue(true);

                                DatabaseReference allNotesRef = FirebaseDatabase.getInstance().getReference().child("branches").child(branchsel).child("allnotes").child(pushId);
                                allNotesRef.setValue(0);

                                DatabaseReference testRef = FirebaseDatabase.getInstance().getReference().child("colleges").child(collegesel).child(branchsel).child("allnotes").child(pushId);
                                testRef.setValue(0);
                            }
                            sharedPreferences.edit().putBoolean(mFirebaseAuth.getUid() + "hasActiveUploads", false).commit();
                            Toast.makeText(UploadNotesActivity.this, "Upload Complete", Toast.LENGTH_SHORT).show();
                            finish();
                        }

                    }
                });
            }


            return null;
        }
    }


}
