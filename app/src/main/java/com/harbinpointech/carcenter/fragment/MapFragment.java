package com.harbinpointech.carcenter.fragment;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.SupportMapFragment;
import com.baidu.mapapi.model.LatLng;
import com.harbinpointech.carcenter.R;
import com.harbinpointech.carcenter.activity.VehicleInfoActivity;
import com.harbinpointech.carcenter.data.WebHelper;
import com.harbinpointech.carcenter.util.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends SupportMapFragment {

    JSONObject mCarPos;
    private LocationClient mLocationClient;
    private BDLocationListener mListener;
    private BDLocation mLastLocation;

    public MapFragment() {
        // Required empty public constructor
        super();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            Activity activity = getActivity();
            if (activity != null) {
                activity.setTitle("查看车辆");
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);

        new AsyncTask<Void, Integer, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                int result = 0;
                JSONObject[] cars = new JSONObject[1];
                try {
                    result = WebHelper.getCars(cars);
                    if (result == 0) {
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
                if (integer == 0) {
                    //

                    addCars2Map();
                }
            }
        }.execute();

        initlocation();
        getBaiduMap().setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Bundle bundle = marker.getExtraInfo();
                int index = bundle.getInt("index");
                try {
                    JSONObject car = mCarPos.getJSONArray("d").getJSONObject(index);
                    Intent i = new Intent(getActivity(), VehicleInfoActivity.class);
                    i.putExtra(VehicleInfoActivity.EXTRA_CARNAME, car.getString("CarName"));
                    startActivity(i);
                } catch (Exception e) {
                    e.printStackTrace();
                    ;
                }
                return true;
            }
        });
    }

    @Override
    public void onDestroy() {
        uninitLocation();
        super.onDestroy();
    }

    private void addCars2Map() {
        try {
            JSONArray js = mCarPos.getJSONArray("d");
            for (int i = 0; i < js.length(); i++) {
                JSONObject item = js.getJSONObject(i);
                //定义Maker坐标点
                LatLng point = new LatLng(item.getDouble("Lattitude"), item.getDouble("Longtitude"));
//构建Marker图标
                BitmapDescriptor bitmap = BitmapDescriptorFactory
                        .fromResource(R.drawable.icon_marka);
//构建MarkerOption，用于在地图上添加Marker
                Bundle extrInfo = new Bundle();
                extrInfo.putInt("index", i);
                OverlayOptions option = new MarkerOptions()
                        .position(point)
                        .icon(bitmap).title(item.getString("CarName")).extraInfo(extrInfo);
//在地图上添加Marker，并显示
                Marker m = (Marker) getBaiduMap().addOverlay(option);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ;
        }
    }

    private void uninitLocation() {
        if (mLocationClient != null) {
            mLocationClient.stop();
            mLocationClient.unRegisterLocationListener(mListener);
            mLocationClient = null;
        }
    }

    private void initlocation() {
        LocationClientOption option = new LocationClientOption();
//        option.setLocationMode(tempMode);//设置定位模式
        option.setCoorType("bd09ll");//返回的定位结果是百度经纬度，默认值gcj02
        int span = 1000;

        option.setScanSpan(span);//设置发起定位请求的间隔时间为5000ms
        option.setIsNeedAddress(true);

        BaiduMap map = getBaiduMap();

        // 开启定位图层
        map.setMyLocationEnabled(true);
        map.setMyLocationConfigeration(new MyLocationConfiguration(
                null, true, null));
        mLocationClient = new LocationClient(getActivity(), option);
        mListener = new BDLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation location) {
                String city = location.getCity();
                ActionBarActivity activity = (ActionBarActivity) getActivity();
                if (activity == null){
                    return;
                }
                activity.getSupportActionBar().setSubtitle(String.format("%s:%s", "当前城市", city));
                BaiduMap map = getBaiduMap();
                if (mLastLocation == null) {
                    mLastLocation = location;
                    LatLng ll = new LatLng(location.getLatitude(),
                            location.getLongitude());
                    MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                    map.animateMapStatus(u);
                }

                // 构造定位数据
                MyLocationData locData = new MyLocationData.Builder()
                        .accuracy(location.getRadius())
                                // 此处设置开发者获取到的方向信息，顺时针0-360
                        .direction(100).latitude(location.getLatitude())
                        .longitude(location.getLongitude()).build();
                // 设置定位数据
                map.setMyLocationData(locData);
            }
        };
        mLocationClient.registerLocationListener(mListener);
        mLocationClient.start();
    }
}
