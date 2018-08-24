package com.pnpj.noteshub;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

public class DownloadsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<showNotes> noteslist = new ArrayList<>();
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloads);

        recyclerView = findViewById(R.id.downloadsRecyclerView);
        textView = findViewById(R.id.downloadsNoNotesTextView);

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        SharedPreferences preferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        int numDownloads = preferences.getInt(FirebaseAuth.getInstance().getUid()+"downloads",0);
        if(numDownloads > 0)
        {
            textView.setVisibility(View.GONE);
            noteslist = getDownloadList();
            myUploadsAdapter uploadAdapter = new myUploadsAdapter(noteslist,"downloads",this);
            recyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(uploadAdapter);
        }
        else
        {
            recyclerView.setVisibility(View.GONE);
        }
        if(progressDialog.isShowing())
            progressDialog.dismiss();
    }

    public ArrayList<showNotes> getDownloadList()
    {
        SharedPreferences sharedPreferences = getSharedPreferences("myPrefs",Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(FirebaseAuth.getInstance().getUid()+"downloadedFiles","");
        ArrayList<showNotes> getnotesList = gson.fromJson(json,new TypeToken<ArrayList<showNotes>>()
        {
        }.getType());
        return getnotesList;
    }

    public void removeDownloaded(ArrayList<showNotes> removenoteslist,showNotes Show)
    {
        SharedPreferences sharedPreferences = getSharedPreferences("myPrefs",Context.MODE_PRIVATE);
        int numDownloads = sharedPreferences.getInt(FirebaseAuth.getInstance().getUid()+"downloads",0);
        for(int i=0;i<numDownloads;i++)
        {
            showNotes test = removenoteslist.get(i);
            if(test.getPushId().matches(Show.getPushId()))
            {
                removenoteslist.remove(i);
                numDownloads--;
                break;
            }
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(FirebaseAuth.getInstance().getUid()+"downloads",numDownloads);
        Gson gson = new Gson();
        String json = gson.toJson(removenoteslist);
        editor.putString(FirebaseAuth.getInstance().getUid()+"downloadedFiles",json);
        editor.commit();
    }
}
