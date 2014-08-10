package com.harbinpointech.carcenter.fragment;



import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.SupportMapFragment;
import com.harbinpointech.carcenter.R;
import com.harbinpointech.carcenter.util.AsyncTask;
import com.harbinpointech.carcenter.data.WebHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * A simple {@link Fragment} subclass.
 *
 */
public class MapFragment extends SupportMapFragment {

    JSONObject mCarPos;
    public MapFragment() {
        // Required empty public constructor
        super();
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);

        new AsyncTask<Void, Integer, Integer>(){
            @Override
            protected Integer doInBackground(Void... params) {
                int result = 0;
                JSONObject[] cars = new JSONObject[1];
                try {
                    result = WebHelper.getAllCarPositions(cars);
                    if (result == 0){
                        mCarPos = cars[0];
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    result = -1;
                } catch (JSONException e) {
                    e.printStackTrace();
                    result = -2;
                }
                return result;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(Integer integer) {
                super.onPostExecute(integer);
                if (integer == 0){
                    //
                }
            }
        }.execute();

    }
}
