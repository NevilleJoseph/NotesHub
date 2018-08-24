package com.pnpj.noteshub;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Neville on 10-02-2018.
 */

public class myUploadsAdapter extends RecyclerView.Adapter<myUploadsAdapter.MyViewHolder> {

    private ArrayList<showNotes> notesList = new ArrayList<>();
    private String calledInstance;
    View myView;
    Activity activity;

    public myUploadsAdapter(ArrayList<showNotes> notesList, String calledInstance , Activity activity) {
        this.notesList = notesList;
        this.calledInstance = calledInstance;
        this.activity = activity;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder
    {
        TextView branch,semester,subject,college,updatedOn,viewsNum,likes,dislikes,downloads,uploader;
        RelativeLayout relativeLayout;
        CardView cardView;
        ImageView addFile,deleteNote;
        LinearLayout linearLayout,uploaderLayout;

        public MyViewHolder(View view) {
            super(view);
            branch = view.findViewById(R.id.myUploadsBranch);
            semester = view.findViewById(R.id.myUploadsSemester);
            subject = view.findViewById(R.id.myUploadsSubject);
            college = view.findViewById(R.id.myUploadsCollege);
            updatedOn = view.findViewById(R.id.myUploadsUpdatedDate);
            viewsNum = view.findViewById(R.id.myUploadsViews);
            likes = view.findViewById(R.id.myUploadsLikes);
            dislikes = view.findViewById(R.id.myUploadsDislikes);
            downloads = view.findViewById(R.id.myUploadsDownloads);
            relativeLayout = view.findViewById(R.id.myUploadsRelativeLayout);
            cardView = view.findViewById(R.id.notesCardView);
            addFile = view.findViewById(R.id.myUploadsDesignAddFilesImageView);
            deleteNote = view.findViewById(R.id.myUploadsDesignDeleteNoteImageView);
            linearLayout = view.findViewById(R.id.myUploadsDesignLinearLayout);
            uploader = view.findViewById(R.id.myUploadsUploaderName);
            uploaderLayout = view.findViewById(R.id.line6);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.myuploads_design, parent, false);

        return new myUploadsAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final showNotes Show = notesList.get(position);
        holder.branch.setText(Show.getBranch());
        holder.semester.setText(Show.getSemester());
        holder.subject.setText(Show.getSubject());
        holder.college.setText(Show.getNameOfCollege());
        holder.updatedOn.setText(Show.getUploadDate());
        holder.uploader.setText(Show.getUploaderName());
        if(calledInstance.matches("downloads"))
        {
            holder.linearLayout.setVisibility(View.GONE);
            holder.addFile.setVisibility(View.GONE);
            holder.deleteNote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    myView = view;
                    AlertDialog.Builder builder;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        builder = new AlertDialog.Builder(view.getContext(), android.R.style.Theme_Material_Dialog_Alert);
                    } else {
                        builder = new AlertDialog.Builder(view.getContext());
                    }
                    builder.setTitle("Confirm Delete")
                            .setMessage("Do you really want to delete this note?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    SharedPreferences sharedPreferences = myView.getContext().getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
                                    int numDownloads = sharedPreferences.getInt(FirebaseAuth.getInstance().getUid()+"downloads",0);
                                    Gson gson = new Gson();
                                    showNotes test = notesList.get(position);
                                    notesList.remove(position);
                                    numDownloads--;
                                    notifyDataSetChanged();

                                    String pushID = test.getPushId();
                                    String path;
                                    for(int i=1;i<=test.getNoOfPages();i++)
                                    {
                                        path = myView.getContext().getFilesDir().getAbsolutePath() + File.separator + pushID + File.separator + i + ".jpg";
                                        File file = new File(path);
                                        boolean subFilesCheck = file.delete();
                                    }
                                    path = myView.getContext().getFilesDir().getAbsolutePath() + File.separator + pushID;
                                    File file =new File(path);
                                    boolean DirCheck = file.delete();

                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putInt(FirebaseAuth.getInstance().getUid()+"downloads",numDownloads);
                                    String json = gson.toJson(notesList);
                                    editor.putString(FirebaseAuth.getInstance().getUid()+"downloadedFiles",json);
                                    editor.commit();
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
            });
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(),ViewNotesOnlineActivity.class);
                    intent.putExtra("pushID",Show.getPushId());
                    intent.putExtra("subject",Show.getSubject());
                    intent.putExtra("numberOfPages",Show.getNoOfPages());
                    intent.putExtra("isDownloaded",true);
                    view.getContext().startActivity(intent);
                }
            });
            holder.cardView.setCardBackgroundColor(activity.getResources().getColor(R.color.myDownloads));
        }
        else
        if(calledInstance.matches("uploads"))
        {
            holder.viewsNum.setText(Show.getViews()+"");
            holder.likes.setText(Show.getLikes()+"");
            holder.dislikes.setText(Show.getDislikes()+"");
            holder.downloads.setText(Show.getDownloads()+"");
            holder.uploaderLayout.setVisibility(View.GONE);
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(),NotesDetailsActivity.class);
                    intent.putExtra("subject",Show.getSubject());
                    intent.putExtra("uploaderName",Show.getUploaderName());
                    intent.putExtra("college",Show.getNameOfCollege());
                    intent.putExtra("branch",Show.getBranch());
                    intent.putExtra("semester",Show.getSemester());
                    intent.putExtra("updatedOn",Show.getUploadDate());
                    intent.putExtra("numberOfPages",Show.getNoOfPages());
                    intent.putExtra("areNotesComplete",Show.isCompleted());
                    intent.putExtra("views",Show.getViews());
                    intent.putExtra("likes",Show.getLikes());
                    intent.putExtra("dislikes",Show.getDislikes());
                    intent.putExtra("downloads",Show.getDownloads());
                    intent.putExtra("pushID",Show.getPushId());
                    intent.putExtra("uploaderUID",Show.getUploaderUid());
                    view.getContext().startActivity(intent);
                }
            });
            holder.addFile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(),UploadNotesActivity.class);
                    intent.putExtra("hasUserUploaded",true);
                    intent.putExtra("college",Show.getNameOfCollege());
                    intent.putExtra("branch",Show.getBranch());
                    intent.putExtra("semester",Show.getSemester());
                    intent.putExtra("subject",Show.getSubject());
                    intent.putExtra("pushId",Show.getPushId());
                    intent.putExtra("numPages",Show.getNoOfPages());
                    view.getContext().startActivity(intent);
                }
            });
         holder.deleteNote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    AlertDialog.Builder builder;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        builder = new AlertDialog.Builder(view.getContext(), android.R.style.Theme_Material_Dialog_Alert);
                    } else {
                        builder = new AlertDialog.Builder(view.getContext());
                    }
                    builder.setTitle("Confirm Delete")
                            .setMessage("Do you really want to delete this note? It CAN NOT be recovered later.")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    DatabaseReference databaseReference,ref1,ref2,ref3;
                                    StorageReference storageReference;
                                    databaseReference = FirebaseDatabase.getInstance().getReference().child("notes").child(Show.getPushId());
                                    databaseReference.removeValue();
                                    ref1 = FirebaseDatabase.getInstance().getReference().child("colleges").child(Show.getNameOfCollege()).child(Show.getBranch()).child(Show.getSemester()).child(Show.getPushId());
                                    ref1.removeValue();
                                    ref2 = FirebaseDatabase.getInstance().getReference().child("branches").child(Show.getBranch()).child(Show.getSubject()).child(Show.getPushId());
                                    ref2.removeValue();
                                    ref3 = FirebaseDatabase.getInstance().getReference().child("users").child(Show.getUploaderUid()).child("uploads").child(Show.getPushId());
                                    ref3.removeValue();
                                    DatabaseReference allNotesRef = FirebaseDatabase.getInstance().getReference().child("branches").child(Show.getBranch()).child("allnotes").child(Show.getPushId());
                                    allNotesRef.removeValue();
                                    DatabaseReference allNotesBranchRef = FirebaseDatabase.getInstance().getReference().child("colleges").child(Show.getNameOfCollege()).child(Show.getBranch()).child("allnotes").child(Show.getPushId());
                                    allNotesBranchRef.removeValue();
                                    storageReference = FirebaseStorage.getInstance().getReference().child("notes").child(Show.getPushId());
                                    for(int x=1;x<=Show.getNoOfPages();x++)
                                    {
                                        storageReference.child(x+"").delete();
                                    }
                                    notesList.remove(position);
                                    notifyDataSetChanged();
                                    Toast.makeText(view.getContext(), "File Deleted", Toast.LENGTH_SHORT).show();
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
            });
            holder.cardView.setCardBackgroundColor(activity.getResources().getColor(R.color.myUploads));
        }
        else
        {
            holder.viewsNum.setText(Show.getViews()+"");
            holder.likes.setText(Show.getLikes()+"");
            holder.dislikes.setText(Show.getDislikes()+"");
            holder.downloads.setText(Show.getDownloads()+"");
            holder.addFile.setVisibility(View.GONE);
            holder.deleteNote.setVisibility(View.GONE);
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(),NotesDetailsActivity.class);
                    intent.putExtra("subject",Show.getSubject());
                    intent.putExtra("uploaderName",Show.getUploaderName());
                    intent.putExtra("college",Show.getNameOfCollege());
                    intent.putExtra("branch",Show.getBranch());
                    intent.putExtra("semester",Show.getSemester());
                    intent.putExtra("updatedOn",Show.getUploadDate());
                    intent.putExtra("numberOfPages",Show.getNoOfPages());
                    intent.putExtra("areNotesComplete",Show.isCompleted());
                    intent.putExtra("views",Show.getViews());
                    intent.putExtra("likes",Show.getLikes());
                    intent.putExtra("dislikes",Show.getDislikes());
                    intent.putExtra("downloads",Show.getDownloads());
                    intent.putExtra("pushID",Show.getPushId());
                    intent.putExtra("uploaderUID",Show.getUploaderUid());
                    view.getContext().startActivity(intent);
                }
            });
            holder.cardView.setCardBackgroundColor(activity.getResources().getColor(R.color.searchNotes));
        }
    }

    @Override
    public int getItemCount() {
        return notesList.size();

    }

    public void removeDownloaded(ArrayList<showNotes> removenoteslist,showNotes Show)
    {

    }


}
