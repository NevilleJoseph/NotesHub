package com.pnpj.noteshub;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.util.ArrayList;

public class NotesDetailsActivity extends AppCompatActivity {

    private InterstitialAd interstitialAd,downloadAd;
    private RewardedVideoAd rewardedVideoAd;
    private AdView mAdView;

    private DatabaseReference likeref,dislikeref,viewref,userRespondedRef,downloadref;

    TextView subjectT,uploaderT,collegeT,branchT,semesterT,updatedOnT,numOfPagesT,isCompleteT,viewsT,likesT,dislikesT,downloadsT,viewNotesOnline;
    String pushID,uploaderUID;
    TextView like,dislike,reportNote;
    int noOfPagesDownloadedEarlier = 1;
    boolean isLiked,isDisliked,isNothing = true;
    boolean hasRespondedEarlier = false;
    String earlierResponse;

    boolean hasSeenAd = false;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes_details);

        AdView mAdView = (AdView) findViewById(R.id.notesDetailsadView);
        final AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        rewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);

        FloatingActionButton floatingActionButton = findViewById(R.id.fab);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading Notes...");
        progressDialog.show();

        like = findViewById(R.id.notesDetailsLikeTextView);
        dislike = findViewById(R.id.notesDetailsDislikeTextView);
        reportNote = findViewById(R.id.notesDetailsReportNote);


        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isNothing) {
                    isLiked = true;
                    isDisliked = false;
                    isNothing = false;
                    like.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_thumb_up_pressed, 0, 0);
                    like.setTextColor(getResources().getColor(R.color.pressed_blue));
                    Toast.makeText(NotesDetailsActivity.this,"Note Liked",Toast.LENGTH_SHORT).show();
                }
                else
                if(isDisliked)
                {
                    isLiked = true;
                    isDisliked = false;
                    dislike.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.ic_thumb_down_black_24dp,0,0);
                    like.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_thumb_up_pressed, 0, 0);
                    like.setTextColor(getResources().getColor(R.color.pressed_blue));
                    dislike.setTextColor(getResources().getColor(R.color.black));
                    Toast.makeText(NotesDetailsActivity.this,"Note Liked",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(NotesDetailsActivity.this, "Note Already Liked", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dislike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isNothing)
                {
                    isDisliked = true;
                    isLiked = false;
                    isNothing = false;
                    dislike.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.ic_thumb_down_pressed,0,0);
                    dislike.setTextColor(getResources().getColor(R.color.pressed_blue));
                    Toast.makeText(NotesDetailsActivity.this, "Note Disliked", Toast.LENGTH_SHORT).show();
                }
                else
                if(isLiked)
                {
                    isDisliked = true;
                    isLiked = false;
                    dislike.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.ic_thumb_down_pressed,0,0);
                    like.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.ic_thumb_up_black_24dp,0,0);
                    dislike.setTextColor(getResources().getColor(R.color.pressed_blue));
                    like.setTextColor(getResources().getColor(R.color.black));
                    Toast.makeText(NotesDetailsActivity.this, "Note Disliked", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(NotesDetailsActivity.this, "Note Already Disliked", Toast.LENGTH_SHORT).show();
                }
            }
        });

        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId("ca-app-pub-9819992292681972/6338936398");
        AdRequest adRequestInterstitial = new AdRequest.Builder().build();
        interstitialAd.loadAd(adRequestInterstitial);
        interstitialAd.setAdListener(new AdListener(){
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                interstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });

        final Intent intent = getIntent();
        final String subject = intent.getStringExtra("subject");
        final String uploaderName = intent.getStringExtra("uploaderName");
        final String college = intent.getStringExtra("college");
        final String branch = intent.getStringExtra("branch");
        final String semester = intent.getStringExtra("semester");
        final String updatedOn = intent.getStringExtra("updatedOn");
        final int numPages = intent.getIntExtra("numberOfPages",0);
        final boolean isCompleted = intent.getBooleanExtra("areNotesComplete",false);
        int views = intent.getIntExtra("views",0);
        int likes = intent.getIntExtra("likes",0);
        int dislikes = intent.getIntExtra("dislikes",0);
        int downloads = intent.getIntExtra("downloads",0);
        pushID = intent.getStringExtra("pushID");
        uploaderUID = intent.getStringExtra("uploaderUID");

        if(uploaderUID.matches(FirebaseAuth.getInstance().getUid()))
        {
            like.setEnabled(false);
            dislike.setEnabled(false);
            reportNote.setEnabled(false);
            floatingActionButton.setVisibility(View.GONE);
            like.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.ic_thumb_up_gray,0,0);
            dislike.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.ic_thumb_down_gray,0,0);
            reportNote.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.ic_report_problem_gray,0,0);
        }

        userRespondedRef = FirebaseDatabase.getInstance().getReference().child("notes").child(pushID).child("responds");
        userRespondedRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(FirebaseAuth.getInstance().getUid()))
                {
                    hasRespondedEarlier = true;
                    earlierResponse = dataSnapshot.child(FirebaseAuth.getInstance().getUid()).getValue().toString();
                    if(earlierResponse.matches("like"))
                    {
                        isLiked = true;
                        isDisliked = false;
                        isNothing = false;
                        like.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_thumb_up_pressed, 0, 0);
                        like.setTextColor(getResources().getColor(R.color.pressed_blue));
                        progressDialog.dismiss();
                    }
                    else
                    if(earlierResponse.matches("dislike"))
                    {
                        isDisliked = true;
                        isLiked = false;
                        isNothing = false;
                        dislike.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.ic_thumb_down_pressed,0,0);
                        dislike.setTextColor(getResources().getColor(R.color.pressed_blue));
                        if(progressDialog.isShowing())
                        progressDialog.dismiss();
                    }
                }
                else
                {
                    hasRespondedEarlier = false;
                    if(progressDialog.isShowing())
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        likeref = FirebaseDatabase.getInstance().getReference().child("notes").child(pushID).child("likes");
        dislikeref = FirebaseDatabase.getInstance().getReference().child("notes").child(pushID).child("dislikes");
        viewref = FirebaseDatabase.getInstance().getReference().child("notes").child(pushID).child("views");
        downloadref = FirebaseDatabase.getInstance().getReference().child("notes").child(pushID).child("downloads");

        reportNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(NotesDetailsActivity.this,ReportActivity.class);
                intent1.putExtra("pushID",pushID);
                startActivity(intent1);
            }
        });



        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.show();
                downloadAd = new InterstitialAd(NotesDetailsActivity.this);
                downloadAd.setAdUnitId("ca-app-pub-9819992292681972/6338936398");
                AdRequest adRequestInterstitial = new AdRequest.Builder().build();
                downloadAd.loadAd(adRequestInterstitial);
                downloadAd.setAdListener(new AdListener(){
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();

                    }

                    @Override
                    public void onAdFailedToLoad(int i) {
                        super.onAdFailedToLoad(i);
                        new AlertDialog.Builder(NotesDetailsActivity.this).setTitle("Error").setMessage("It seems you are on a slow network or that the network you are connected to is restricting ads. Please switch to another network or use mobile data to continue downloading the notes. You may however, continue viewing the notes online.").setNeutralButton("Close", null).show();
                    }

                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                        if(progressDialog.isShowing())
                            progressDialog.dismiss();
                        interstitialAd.show();
                        downloadref.runTransaction(new Transaction.Handler() {
                            @Override
                            public Transaction.Result doTransaction(MutableData mutableData) {
                                Integer getdownloads = mutableData.getValue(Integer.class);
                                if(getdownloads == null)
                                {
                                    mutableData.setValue(1);
                                }
                                else
                                {
                                    mutableData.setValue(getdownloads + 1);
                                }
                                return Transaction.success(mutableData);
                            }

                            @Override
                            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                            }
                        });
                        String dirPath = getFilesDir().getAbsolutePath() + File.separator + pushID;
                        SharedPreferences sharedPreferences = getSharedPreferences("myPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        ArrayList<showNotes> notes = new ArrayList<>();
                        showNotes Show = new showNotes(branch, subject, numPages, college, semester, isCompleted, uploaderName, FirebaseAuth.getInstance().getUid(), updatedOn, 0, 0, 0, 0,pushID);
                        File projDir = new File(dirPath);
                        if (!projDir.exists()) {
                            projDir.mkdirs();
                            int numDownloads = sharedPreferences.getInt(FirebaseAuth.getInstance().getUid() + "downloads", 0);
                            Gson gson = new Gson();
                            editor.putInt(FirebaseAuth.getInstance().getUid() + "downloads", numDownloads + 1);
                            if(numDownloads == 0)
                            {
                                notes.add(Show);
                                String json = gson.toJson(notes);
                                editor.putString(FirebaseAuth.getInstance().getUid()+"downloadedFiles",json);
                            }
                            else
                            {
                                notes = getDownloadList();
                                notes.add(Show);
                                String json = gson.toJson(notes);
                                editor.putString(FirebaseAuth.getInstance().getUid()+"downloadedFiles",json);
                            }
                            editor.apply();
                        }
                        else
                        {
                            int numDownloads = sharedPreferences.getInt(FirebaseAuth.getInstance().getUid() + "downloads", 0);
                            Gson gson = new Gson();
                            for(int i =0;i<numDownloads;i++) {
                                notes = getDownloadList();
                                if(notes.get(i).getPushId().matches(pushID))
                                {
                                    noOfPagesDownloadedEarlier = notes.get(i).getNoOfPages() + 1;
                                    notes.remove(i);
                                    notes.add(i,Show);
                                    String json = gson.toJson(notes);
                                    editor.putString(FirebaseAuth.getInstance().getUid()+"downloadedFiles",json);
                                    editor.commit();
                                    break;
                                }
                            }
                        }
                        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                        final int notificationID = 100;

                        Toast.makeText(NotesDetailsActivity.this, "Downloading Notes...", Toast.LENGTH_SHORT).show();

                        Intent myIntent = new Intent(NotesDetailsActivity.this, DownloadsActivity.class);
                        PendingIntent pendingIntent = PendingIntent.getActivity(
                                NotesDetailsActivity.this,
                                0,
                                myIntent,
                                PendingIntent.FLAG_ONE_SHOT);

                        final NotificationManager mNotifyManager =
                                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(NotesDetailsActivity.this);
                        mBuilder.setContentIntent(pendingIntent);
                        mBuilder.setContentTitle("Downloading Files")
                                .setContentText("Download in progress")
                                .setSmallIcon(R.drawable.ic_file_download_black_24dp);
                        final int totalProgress = 100*numPages;
                        final int[] numPagesDownloaded = {0};
                        final int[] current = {0};
                        for(int i=noOfPagesDownloadedEarlier;i<=numPages;i++) {
                            final int finalI = i;
                            File file = new File(dirPath,finalI+".jpg");
                            String path = "notes/"+pushID+"/"+finalI;
                            storageReference.child(path).getFile(file).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                    int currprogress = (int) (((finalI -1)*100)+progress);
                                    current[0] += currprogress;
                                    mBuilder.setProgress(totalProgress, current[0], false);
                                    // Displays the progress bar for the first time.
                                    mNotifyManager.notify(notificationID, mBuilder.build());
                                }
                            }).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    numPagesDownloaded[0]++;
                                    if(numPagesDownloaded[0] == numPages) {
                                        mBuilder.setContentText("Download complete")
                                                .setProgress(0, 0, false);
                                        mNotifyManager.notify(notificationID, mBuilder.build());
                                        Toast.makeText(NotesDetailsActivity.this, "Download Complete", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                });
            }
        });


        subjectT = findViewById(R.id.notesDetailsSubjectTextView);
        uploaderT = findViewById(R.id.notesDetailsUploaderName);
        collegeT = findViewById(R.id.notesDetailsCollege);
        branchT = findViewById(R.id.notesDetailsBranch);
        semesterT = findViewById(R.id.notesDetailsSemester);
        updatedOnT = findViewById(R.id.notesDetailsUpdatedOn);
        numOfPagesT = findViewById(R.id.notesDetailsNumOfPages);
        isCompleteT = findViewById(R.id.notesDetailsAreNotesComplete);
        viewsT = findViewById(R.id.notesDetailsViews);
        likesT = findViewById(R.id.notesDetailsLikes);
        dislikesT = findViewById(R.id.notesDetailsDislikes);
        downloadsT = findViewById(R.id.notesDetailsDownloads);
        viewNotesOnline = findViewById(R.id.notesDetailsViewNotesOnline);

        subjectT.setText(subject);
        uploaderT.setText(uploaderName);
        collegeT.setText(college);
        branchT.setText(branch);
        semesterT.setText(semester);
        updatedOnT.setText(updatedOn);
        numOfPagesT.setText(numPages+"");
        if(isCompleted)
        {
            isCompleteT.setText("Yes");
        }
        else
        {
            isCompleteT.setText("No");
        }
        viewsT.setText(views+"");
        likesT.setText(likes+"");
        dislikesT.setText(dislikes+"");
        downloadsT.setText(downloads+"");

        setTitle(subject);

        viewNotesOnline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.show();
                progressDialog.setMessage("Loading Notes...");
                InterstitialAd ad = new InterstitialAd(NotesDetailsActivity.this);
                ad.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
                AdRequest adRequestInterstitial = new AdRequest.Builder().build();
                ad.loadAd(adRequestInterstitial);
                ad.setAdListener(new AdListener()
                {
                    @Override
                    public void onAdFailedToLoad(int i) {
                        super.onAdFailedToLoad(i);
                        if(progressDialog.isShowing())
                            progressDialog.dismiss();
                        Intent intent1 = new Intent(NotesDetailsActivity.this,ViewNotesOnlineActivity.class);
                        intent1.putExtra("pushID",pushID);
                        intent1.putExtra("subject",subject);
                        intent1.putExtra("numberOfPages",7);
                        intent1.putExtra("isRestricted",true);
                        intent1.putExtra("isDownloaded",false);
                        startActivity(intent1);
                    }

                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                        if(progressDialog.isShowing())
                            progressDialog.dismiss();
                        Intent intent2 = new Intent(NotesDetailsActivity.this,ViewNotesOnlineActivity.class);
                        intent2.putExtra("pushID",pushID);
                        intent2.putExtra("subject",subject);
                        intent2.putExtra("numberOfPages",numPages);
                        intent2.putExtra("isRestricted",false);
                        intent2.putExtra("isDownloaded",false);
                        startActivity(intent2);
                    }
                });
            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences("myPrefs",MODE_PRIVATE);
        int check = sharedPreferences.getInt("numClicks",0);
        if(check==9)
        {
            check=0;
            interstitialAd.setAdListener(new AdListener(){
                @Override
                public void onAdLoaded() {
                    interstitialAd.show();
                }
            });
        }
        else
        {
            check++;
        }
        sharedPreferences.edit().putInt("numClicks",check).apply();


    }

    public ArrayList<showNotes> getDownloadList()
    {
        SharedPreferences sharedPreferences = getSharedPreferences("myPrefs",MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(FirebaseAuth.getInstance().getUid()+"downloadedFiles","");
        ArrayList<showNotes> getnotesList;
        getnotesList = gson.fromJson(json,new TypeToken<ArrayList<showNotes>>()
        {
        }.getType());
        return getnotesList;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
      if(!isNothing)
      {
          if(hasRespondedEarlier)
          {
            if(isLiked && earlierResponse.matches("like"))
            {

            }
            else if(isLiked && earlierResponse.matches("dislike"))
            {
                likeref.runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        Integer getlikes = mutableData.getValue(Integer.class);
                        if(getlikes == null)
                        {
                            mutableData.setValue(1);
                        }
                        else
                        {
                            mutableData.setValue(getlikes + 1);
                        }
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                    }
                });

                dislikeref.runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        Integer getdislikes = mutableData.getValue(Integer.class);
                        if(getdislikes == null)
                        {
                            mutableData.setValue(0);
                        }
                        else
                        {
                            mutableData.setValue(getdislikes - 1);
                        }
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                    }
                });

                userRespondedRef.child(FirebaseAuth.getInstance().getUid()).setValue("like");
            }
            else if (isDisliked && earlierResponse.matches("dislike"))
            {

            }
            else if (isDisliked && earlierResponse.matches("like"))
            {
                likeref.runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        Integer getlikes = mutableData.getValue(Integer.class);
                        if(getlikes == null)
                        {
                            mutableData.setValue(0);
                        }
                        else
                        {
                            mutableData.setValue(getlikes - 1);
                        }
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                    }
                });

                dislikeref.runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        Integer getdislikes = mutableData.getValue(Integer.class);
                        if(getdislikes == null)
                        {
                            mutableData.setValue(1);
                        }
                        else
                        {
                            mutableData.setValue(getdislikes + 1);
                        }
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                    }
                });

                userRespondedRef.child(FirebaseAuth.getInstance().getUid()).setValue("dislike");
            }
          }
          else
          {
              if(isLiked)
              {
                  likeref.runTransaction(new Transaction.Handler() {
                      @Override
                      public Transaction.Result doTransaction(MutableData mutableData) {
                          Integer getlikes = mutableData.getValue(Integer.class);
                          if(getlikes == null)
                          {
                              mutableData.setValue(1);
                          }
                          else
                          {
                              mutableData.setValue(getlikes + 1);
                          }
                          return Transaction.success(mutableData);
                      }

                      @Override
                      public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                      }
                  });

                  userRespondedRef.child(FirebaseAuth.getInstance().getUid()).setValue("like");
              }
              else
              {
                  dislikeref.runTransaction(new Transaction.Handler() {
                      @Override
                      public Transaction.Result doTransaction(MutableData mutableData) {
                          Integer getdislikes = mutableData.getValue(Integer.class);
                          if(getdislikes == null)
                          {
                              mutableData.setValue(1);
                          }
                          else
                          {
                              mutableData.setValue(getdislikes + 1);
                          }
                          return Transaction.success(mutableData);
                      }

                      @Override
                      public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                      }
                  });

                  userRespondedRef.child(FirebaseAuth.getInstance().getUid()).setValue("dislike");
              }
          }
      }
      if(!(uploaderUID.matches(FirebaseAuth.getInstance().getUid())))
      {
          viewref.runTransaction(new Transaction.Handler() {
              @Override
              public Transaction.Result doTransaction(MutableData mutableData) {
                  Integer getviews = mutableData.getValue(Integer.class);
                  if(getviews == null)
                  {
                      mutableData.setValue(1);
                  }
                  else
                  {
                      mutableData.setValue(getviews + 1);
                  }
                  return Transaction.success(mutableData);
              }

              @Override
              public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

              }
          });
      }

    }
}
