package com.downtail.statusbarplus;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.downtail.plus.StatusBarPlus;

public class MainActivity extends AppCompatActivity {

    TextView tvBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StatusBarPlus.setColor(this,Color.parseColor("#18ce94"));
//        StatusBarPlus.setStatusBarMode(this,false);

        tvBtn=findViewById(R.id.tv_btn);
        tvBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StatusBarPlus.setStatusBarMode(MainActivity.this,true);
            }
        });
    }
}
