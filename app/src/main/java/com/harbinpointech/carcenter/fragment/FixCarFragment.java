package com.harbinpointech.carcenter.fragment;



import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.zxing.client.android.CaptureActivity;
import com.harbinpointech.carcenter.R;
import com.harbinpointech.carcenter.activity.ScanActivity;

/**
 * A simple {@link Fragment} subclass.
 *
 */
public class FixCarFragment extends Fragment {


    private static final int REQUEST_SCAN_VEHICLE = 0x1000;

    public FixCarFragment() {
        // Required empty public constructor
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            Activity activity = getActivity();
            if (activity != null) {
                activity.setTitle("维修");
                startActivityForResult(new Intent(getActivity(), ScanActivity.class), REQUEST_SCAN_VEHICLE);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_fix_car, container, false);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        

    }
}
