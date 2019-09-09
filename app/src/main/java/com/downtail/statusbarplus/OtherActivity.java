package com.downtail.statusbarplus;

import android.graphics.Color;
import android.os.Bundle;

import com.downtail.plus.StatusBarPlus;

import me.yokeyword.fragmentation_swipeback.SwipeBackActivity;

public class OtherActivity extends SwipeBackActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other);
//        StatusBarPlus.setColor(this, Color.parseColor("#ff0000"));
        StatusBarPlus.setColor(this, Color.parseColor("#ff0000"));
        StatusBarPlus.setStatusBarMode(this, false);
        setSwipeBackEnable(true);
    }
}
