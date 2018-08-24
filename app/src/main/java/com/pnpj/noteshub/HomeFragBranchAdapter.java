package com.pnpj.noteshub;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Neville on 10-02-2018.
 */

public class HomeFragBranchAdapter extends RecyclerView.Adapter<HomeFragBranchAdapter.MyViewHolder> {

    private ArrayList<String> branchesList = new ArrayList<>();
    private boolean isSearchNotes;
    private String branch;
    Activity activity;

    public HomeFragBranchAdapter(ArrayList<String> branchesList, boolean isSearchNotes, String branch, Activity activity) {
        this.branchesList = branchesList;
        this.isSearchNotes = isSearchNotes;
        this.branch = branch;
        this.activity = activity;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView textView;
        LinearLayout linearLayout;
        CardView cardView;

        public MyViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.branchesNameTextView);
            linearLayout = view.findViewById(R.id.branchesNameLinearLayout);
            cardView = view.findViewById(R.id.branchNotesCardView);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.branchesname_design, parent, false);

        return new HomeFragBranchAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final String branchName = branchesList.get(position);
        holder.textView.setText(branchName);
        if(isSearchNotes)
        {
            holder.cardView.setCardBackgroundColor(activity.getResources().getColor(R.color.Branches));
        }
        else
        {
            holder.cardView.setCardBackgroundColor(activity.getResources().getColor(R.color.Subjects));
        }
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isSearchNotes)
                {
                    Intent intent = new Intent(view.getContext(),BranchNotesActivity.class);
                    intent.putExtra("branch",branchName);
                    view.getContext().startActivity(intent);
                }
                else
                {
                    Intent intent = new Intent(view.getContext(),ViewSearchedNotesActivity.class);
                    intent.putExtra("selectedCollege","Any");
                    intent.putExtra("selectedBranch",branch);
                    intent.putExtra("selectedSem","");
                    intent.putExtra("selectedSubject",branchName);
                    view.getContext().startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return branchesList.size();
    }
}
