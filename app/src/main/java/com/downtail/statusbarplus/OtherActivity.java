package com.downtail.statusbarplus;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;

import com.downtail.plus.StatusBarPlus;

public class OtherActivity extends Activity {

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other);
        StatusBarPlus.setColor(this,Color.parseColor("#ff0000"));
    }
}
