package com.pnpj.noteshub;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

/**
 * Created by Neville on 13-02-2018.
 */

public class FullScreenImageAdapter extends PagerAdapter {

    private Activity _activity;
    private LayoutInflater inflater;
    private String[] paths;
    private int numOfPages;
    private String pushID;
    private StorageReference storageReference;

    // constructor


    public FullScreenImageAdapter(Activity _activity, int numOfPages, String pushID) {
        this._activity = _activity;
        this.numOfPages = numOfPages;
        this.pushID = pushID;
        paths = new String[numOfPages];
        for(int x=0;x<numOfPages;x++)
            paths[x]="";
        storageReference= FirebaseStorage.getInstance().getReference();
    }

    public FullScreenImageAdapter(Activity _activity, String[] paths, int numOfPages) {
        this._activity = _activity;
        this.paths = paths;
        this.numOfPages = numOfPages;
    }

    @Override
    public int getCount() {
        return numOfPages;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        final TouchImageView imgDisplay;

        inflater = (LayoutInflater) _activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewLayout = inflater.inflate(R.layout.layout_fullscreen_image, container,
                false);

        imgDisplay = viewLayout.findViewById(R.id.imgDisplay);

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        if(paths[position].matches("")) {
            imgDisplay.setImageDrawable(_activity.getDrawable(R.drawable.loadingnoback));
            try {
                final File localFile = File.createTempFile("images", "jpg");
                String path = "notes/"+pushID+"/"+(position+1);
                storageReference.child(path).getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        paths[position]=localFile.getAbsolutePath();
                        Bitmap bitmap = BitmapFactory.decodeFile(paths[position], options);
                        imgDisplay.setImageBitmap(bitmap);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            Bitmap bitmap = BitmapFactory.decodeFile(paths[position], options);
            imgDisplay.setImageBitmap(bitmap);
        }

        // close button click event

        ((ViewPager) container).addView(viewLayout);

        return viewLayout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((RelativeLayout) object);

    }
}

