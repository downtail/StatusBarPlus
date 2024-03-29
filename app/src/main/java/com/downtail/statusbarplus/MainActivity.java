package com.downtail.statusbarplus;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import com.downtail.plus.StatusBarPlus;

import java.util.ArrayList;
import java.util.List;

import me.yokeyword.fragmentation_swipeback.SwipeBackActivity;

public class MainActivity extends SwipeBackActivity {

    BottomNavigationView bottomNavigationView;
    ViewPager vpContainer;
    View fakeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fakeView = findViewById(R.id.view_fake);
//        StatusBarPlus.setColor(this, Color.parseColor("#18ce94"));
        StatusBarPlus.setTransparent(this);
        StatusBarPlus.setStatusBarMode(this,true);

        vpContainer = findViewById(R.id.vp_container);
        vpContainer.setOffscreenPageLimit(4);
        vpContainer.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                bottomNavigationView.getMenu().getItem(i).setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        bottomNavigationView = findViewById(R.id.nav_bottom);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.item1:
                        vpContainer.setCurrentItem(0);
                        break;
                    case R.id.item2:
                        vpContainer.setCurrentItem(1);
                        break;
                    case R.id.item3:
                        vpContainer.setCurrentItem(2);
                        break;
                    case R.id.item4:
                        vpContainer.setCurrentItem(3);
                        break;

                }
                return true;
            }
        });

        List<Fragment> fragments = new ArrayList<>();
        for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
            fragments.add(PageFragment.newInstance(i));
        }
        FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager(), fragments);
        vpContainer.setAdapter(adapter);
    }
}
