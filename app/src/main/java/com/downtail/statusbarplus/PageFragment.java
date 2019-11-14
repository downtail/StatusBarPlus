package com.downtail.statusbarplus;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.downtail.plus.StatusBarPlus;

public class PageFragment extends Fragment {

    TextView tvBtn;

    public static PageFragment newInstance(int index) {
        Bundle args = new Bundle();
        args.putInt("index", index);
        PageFragment fragment = new PageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_page, container, false);
        int index = getArguments().getInt("index");

        if (index == 0) {
            view = StatusBarPlus.setColor(view, Color.YELLOW);
        } else if (index == 1) {
            view = StatusBarPlus.setColor(view, Color.parseColor("#18ce94"));
        } else if (index == 2) {
            view = StatusBarPlus.setColor(view, Color.parseColor("#ff0000"));
        } else if (index == 3) {
            view = StatusBarPlus.setColor(view, Color.parseColor("#61c1fe"));
        }
        tvBtn = view.findViewById(R.id.tv_btn);
        tvBtn.setText("click" + index);
        tvBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), OtherActivity.class));
            }
        });
        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            int index = getArguments().getInt("index");
//            if (index % 2 == 0) {
//                StatusBarPlus.setStatusBarMode(this, true);
//            } else {
//                StatusBarPlus.setStatusBarMode(this, false);
//            }
            if (getActivity() != null) {
//                if (index == 0) {
//                    statusView.setVisibility(View.GONE);
//                } else if (index == 1) {
//                    statusView.setVisibility(View.VISIBLE);
//                    statusView.setBackgroundColor(Color.parseColor("#18ce94"));
//                } else if (index == 2) {
//                    statusView.setVisibility(View.VISIBLE);
//                    statusView.setBackgroundColor(Color.parseColor("#ff0000"));
//                } else if (index == 3) {
//                    statusView.setVisibility(View.VISIBLE);
//                    statusView.setBackgroundColor(Color.parseColor("#61c1fe"));
//                }
            }
        }
    }
}
