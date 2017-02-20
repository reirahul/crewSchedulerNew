package com.iconsolutions.helper;

import java.io.File;
import java.io.InputStream;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar.OnMenuItemClickListener;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;

import com.iconsolutions.crewschedular.R;

@SuppressWarnings("rawtypes")
@SuppressLint("ResourceAsColor")
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ImageViewActivity extends Activity {

    public static Context context;
    TouchImageView imageView;
    String path;
    static Bitmap bitmap = null;

    // static AttachmentFragment attachmentPopup;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_imageview);
        context = ImageViewActivity.this;
        try {
            if (getIntent().getExtras() == null)
                finish();
            else {

                File imgFile = new File(getIntent().getExtras().getString("path"));
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                options.inDither = true;
                BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options);

                options.inJustDecodeBounds = false;
                path = imgFile.getAbsolutePath();
                bitmap = BitmapFactory.decodeFile(path, options);
            }

            imageView = (TouchImageView) findViewById(R.id.imageView);
//			is = getContentResolver().openInputStream(Uri.parse(path));
//			bitmap = BitmapFactory.decodeStream(is);
            imageView.setImageBitmap(bitmap);

        } catch (Exception e) {
            e.printStackTrace();
            // throw new MmsException(e);
        }

    }
}
