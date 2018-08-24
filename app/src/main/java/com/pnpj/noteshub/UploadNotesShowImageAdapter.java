package com.pnpj.noteshub;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by Neville on 06-02-2018.
 */

public class UploadNotesShowImageAdapter extends RecyclerView.Adapter<UploadNotesShowImageAdapter.MyViewHolder> {

    public ArrayList<String> imagesList;
    Activity activity;

    public String getImagePath(int pos) {
        return imagesList.get(pos);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public ImageView imageView,closeImageView;

        public MyViewHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.uploadNotesDesignImageView);
            closeImageView = view.findViewById(R.id.uploadNotesDesignCloseImageView);
            closeImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    imagesList.remove(getAdapterPosition());
                    notifyDataSetChanged();
                    notifyItemRangeChanged(getAdapterPosition(),imagesList.size());
                }
            });
        }
    }


    public UploadNotesShowImageAdapter(ArrayList<String> imagesList,Activity ac) {
        this.imagesList = imagesList;
        this.activity = ac;
    }

    @Override
    public UploadNotesShowImageAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.uploadnotes_design, parent, false);

        return new UploadNotesShowImageAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String path = imagesList.get(position);

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();

        bitmapOptions.inJustDecodeBounds = true; // obtain the size of the image, without loading it in memory
        BitmapFactory.decodeFile(path, bitmapOptions);

// find the best scaling factor for the desired dimensions
        int desiredWidth = 600;
        int desiredHeight = 800;
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
        Bitmap thumbnail = BitmapFactory.decodeFile(path, bitmapOptions);

        holder.imageView.setImageBitmap(thumbnail);
    }

    @Override
    public int getItemCount() {
        return imagesList.size();
    }
}
