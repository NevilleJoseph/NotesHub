package com.pnpj.noteshub;

import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Neville on 19-12-2017.
 */

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.MyViewHolder> {

    private ArrayList<showNotes> notesList = new ArrayList<>();


    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView subject,viewsNum,likes,dislikes,downloads,uploaderName,uploadDate;
        public CardView cardView;
        public View myView;

        public MyViewHolder(View view) {
            super(view);
            cardView = view.findViewById(R.id.notesCardView);
            subject = view.findViewById(R.id.notesDesignSubject);
            viewsNum = view.findViewById(R.id.notesDesignViews);
            likes = view.findViewById(R.id.notesDesignLikes);
            dislikes = view.findViewById(R.id.notesDesignDislikes);
            downloads = view.findViewById(R.id.notesDesignDownloads);
            uploaderName = view.findViewById(R.id.notesDesignUploaderName);
            uploadDate = view.findViewById(R.id.notesDesignUploadDate);
            myView = view;
        }
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final showNotes Show = notesList.get(position);
        holder.subject.setText(Show.getSubject());
        holder.viewsNum.setText(Show.getViews()+"");
        holder.likes.setText(Show.getLikes()+"");
        holder.dislikes.setText(Show.getDislikes()+"");
        holder.downloads.setText(Show.getDownloads()+"");
        holder.uploaderName.setText(Show.getUploaderName());
        holder.uploadDate.setText(Show.getUploadDate());
        holder.myView.setOnClickListener(new View.OnClickListener() {
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
    }


    public NotesAdapter(ArrayList<showNotes> notesList) {
        this.notesList = notesList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notes_design, parent, false);

        return new NotesAdapter.MyViewHolder(itemView);
    }

    @Override
    public int getItemCount() {
        return notesList.size();
    }
}
