package com.example.dell.datalayer_sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.internal.Objects;

public class NameActivity extends WearableActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name);

        setAmbientEnabled();

        TextView textView=(TextView) findViewById(R.id.textView);

        Bundle data=getIntent().getBundleExtra("datamap");
        String yourName=data.getString("name");
        textView.append(yourName);
    }
}
