package com.example.dell.datalayer_sample;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.widget.ImageView;

public class ImageActivity extends WearableActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        setAmbientEnabled();

        ImageView ımageView=(ImageView) findViewById(R.id.imageView);

        Bitmap bitmap=getIntent().getParcelableExtra("bitmap");

        ımageView.setImageBitmap(bitmap);
    }
}
