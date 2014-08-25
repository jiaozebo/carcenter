package com.harbinpointech.carcenter.fragment;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.zxing.client.android.CaptureActivity;
import com.harbinpointech.carcenter.R;
import com.harbinpointech.carcenter.activity.ScanActivity;
import com.harbinpointech.carcenter.data.WebHelper;
import com.harbinpointech.carcenter.util.AsyncTask;

import org.json.JSONException;

import java.io.IOException;

/**
 * A simple {@link Fragment} subclass.
 */
public class FixCarFragment extends Fragment implements View.OnClickListener {


    private static final int REQUEST_SCAN_VEHICLE = 0x1000;
    private ProgressDialog mDlg;

    public FixCarFragment() {
        // Required empty public constructor
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
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
        mDlg = new ProgressDialog(getActivity());
        mDlg.setMessage("请稍等...");
        mDlg.setCancelable(false);

        getView().findViewById(R.id.fix_car_fix).setOnClickListener(this);
        getView().findViewById(R.id.fix_car_log).setOnClickListener(this);
    }

    public void startSignin(final String carName, final double latitude, final double longitude) {

        new AsyncTask<Void, Integer, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                try {
                    return WebHelper.singin(carName, latitude, longitude);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return -1;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mDlg.show();
            }

            @Override
            protected void onPostExecute(Integer integer) {
                super.onPostExecute(integer);
                mDlg.dismiss();
                if (integer != 0) {
                    Toast.makeText(getActivity(), "签到失败", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "签到成功", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    @Override
    public void onClick(final View v) {
        new AsyncTask<Void, Integer,Integer>(){
            @Override
            protected Integer doInBackground(Void... params) {
                if (v.getId() == R.id.fix_car_fix) {
                    WebHelper.AddRepaireRecord();
                } else if (v.getId() == R.id.fix_car_log) {
                    WebHelper.mobileGetRepaireRecords2();
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return 0;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mDlg.show();
            }

            @Override
            protected void onPostExecute(Integer integer) {
                super.onPostExecute(integer);mDlg.dismiss();
            }
        }.execute();
    }
}
