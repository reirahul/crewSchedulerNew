package com.iconsolutions.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.iconsolutions.crewschedular.R;
import com.iconsolutions.helper.ImageViewActivity;

import java.io.File;
import java.util.ArrayList;

import com.iconsolutions.helper.UserPreferences;

/**
 * Created by kashif on 4/18/16.
 */
public class JobImagesAdapter extends BaseAdapter {
    ArrayList<String> data;
    Context context;
    int resourceId;

    public JobImagesAdapter(Context context, ArrayList<String> filesList, int resourceID) {
        this.context = context;
        this.resourceId = resourceID;
        this.data = filesList;
        UserPreferences.reLoadPrefernces(context);
    }

    @Override
    public int getCount() {
        return this.data.size();
    }

    @Override
    public Object getItem(int position) {
        return this.data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        try {
//    View view;
            ImageViewHolder holder;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(resourceId, parent, false);
//        view = convertView;
                holder = new ImageViewHolder();
                holder.imageView = (ImageView) convertView.findViewById(R.id.jobImageView);
                convertView.setTag(holder);
            } else {
//        view = convertView;
                holder = (ImageViewHolder) convertView.getTag();
            }

            if (this.data != null) {
                final String imagePath = this.data.get(position);
                File imgFile = new File(this.data.get(position));
                if (imgFile != null && imgFile.exists()) {
                    try {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        options.inPreferredConfig = Bitmap.Config.RGB_565;
                        options.inDither = true;
                        BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options);

                        options.inSampleSize = calculateInSampleSize(options, 250, 250);

                        options.inJustDecodeBounds = false;
                        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options);
                        holder.imageView.setImageBitmap(myBitmap);
                    } catch (Exception e) {
                        Log.d("crash on position" + position, "");
                        e.getStackTrace();
                    }

                }

                holder.imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent((Activity)context, ImageViewActivity.class);
                        intent.putExtra("path", imagePath);
                        context.startActivity(intent);
                    }
                });
            }

        } catch (Exception e) {

        }
        return convertView;
    }

    public void updateReceiptsList(ArrayList<String> newlist) {
        this.data.clear();
        this.data.addAll(newlist);
        this.notifyDataSetChanged();
    }

    static class ImageViewHolder {
        ImageView imageView;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }
}
