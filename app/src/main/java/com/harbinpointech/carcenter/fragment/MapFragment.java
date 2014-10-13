package com.harbinpointech.carcenter.fragment;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MapViewLayoutParams;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.SupportMapFragment;
import com.baidu.mapapi.map.Text;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;
import com.harbinpointech.carcenter.R;
import com.harbinpointech.carcenter.activity.MainActivity;
import com.harbinpointech.carcenter.activity.VehicleInfoActivity;
import com.harbinpointech.carcenter.data.WebHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends SupportMapFragment {

    private static final String TAG = "MAPFRAGMENT";
    JSONObject mCarPos;
    private LocationClient mLocationClient;
    private BDLocationListener mListener;
    private BDLocation mLastLocation;
    private MapView mMapView;
    private List<Thread> mFixPositionThreads;
    private Thread mQueryCarPosThread;

    private List<Marker> mCars = new ArrayList<Marker>();
    private List<Text> mCarNames = new ArrayList<Text>();


    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private static final int MAXIMUM_POOL_SIZE = 128;
    private static final int KEEP_ALIVE = 1;

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "THREAD_POOL_EXECUTOR #" + mCount.getAndIncrement());
        }
    };

    private static final BlockingQueue<Runnable> sPoolWorkQueue =
            new LinkedBlockingQueue<Runnable>(MAXIMUM_POOL_SIZE);

    /**
     * An {@link java.util.concurrent.Executor} that can be used to execute tasks in parallel.
     */
    public static final Executor THREAD_POOL_EXECUTOR
            = new ThreadPoolExecutor(30, MAXIMUM_POOL_SIZE, 10,
            TimeUnit.SECONDS, sPoolWorkQueue, sThreadFactory, new ThreadPoolExecutor.DiscardOldestPolicy());

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
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        mMapView = (MapView) super.onCreateView(layoutInflater, viewGroup, bundle);
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.layout_goto_my_location, mMapView, false);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BaiduMap map = getBaiduMap();
                if (mLastLocation != null) {
                    LatLng ll = new LatLng(mLastLocation.getLatitude(),
                            mLastLocation.getLongitude());
                    MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                    map.animateMapStatus(u);
                }
            }
        });
        mMapView.post(new Runnable() {
            @Override
            public void run() {
                Point point = new Point((int) (mMapView.getWidth() - getResources().getDisplayMetrics().density * 10), (int) (getResources().getDisplayMetrics().density * 10));
                mMapView.addView(view, new MapViewLayoutParams.Builder().layoutMode(MapViewLayoutParams.ELayoutMode.absoluteMode).point(point).align(MapViewLayoutParams.ALIGN_RIGHT, MapViewLayoutParams.ALIGN_TOP).build());
            }
        });
        return mMapView;
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        mFixPositionThreads = new ArrayList<Thread>();

        mQueryCarPosThread = new Thread("QueryCarsPosT") {
            @Override
            public void run() {
                int result = 0;
                do {
                    JSONObject[] cars = new JSONObject[1];
                    try {
                        result = WebHelper.getCars(cars);
                        if (result == 0) {
                            mCarPos = cars[0];

                            try {
                                JSONArray js = mCarPos.getJSONArray("d");
                                Log.w(TAG, "queryed cars num:" + js.length());

                                // 先删除先前有，这次没查到了的
                                synchronized (mCars) {
                                    Iterator<Marker> it = mCars.iterator();
                                    while (it.hasNext()) {
                                        Marker mk = it.next();
                                        String name = mk.getExtraInfo().getString("CarName");
                                        boolean found = false;
                                        for (int i = 0; i < js.length(); i++) {
                                            final JSONObject item = js.getJSONObject(i);
                                            String carName = item.getString("CarName");
                                            if (name.equals(carName)) {
                                                found = true;
                                                break;
                                            }
                                        }
                                        if (!found) {
                                            mk.remove();
                                            it.remove();
                                            Log.w(TAG, "remove carTxt :" + name);
                                            Iterator<Text> t = mCarNames.iterator();
                                            while (t.hasNext()) {
                                                Text txt = t.next();
                                                String name2 = txt.getExtraInfo().getString("CarName");
                                                if (name2.equals(name)) {
                                                    txt.remove();
                                                    t.remove();
                                                    Log.w(TAG, "remove carTxt :" + name);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }

                                for (int i = 0; mQueryCarPosThread != null && i < js.length(); i++) {
                                    final JSONObject item = js.getJSONObject(i);
                                    THREAD_POOL_EXECUTOR.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                //定义Maker坐标点
                                                double[] data = new double[]{item.getDouble("Lattitude"), item.getDouble("Longtitude")};//*///
                                                WebHelper.gps2lnglat(data);
//                            Object []objData = new Object[]{data[0],data[1]};

                                                int result = WebHelper.fixPoint("http://api.map.baidu.com/ag/coord/convert", data);
                                                if (result == 0 && mQueryCarPosThread != null && isResumed()) {
                                                    LatLng point = new LatLng(data[0], data[1]);
//构建Marker图标
                                                    BitmapDescriptor bitmap = BitmapDescriptorFactory
                                                            .fromResource(R.drawable.icon_marka);
//构建MarkerOption，用于在地图上添加Marker

                                                    boolean found = false;
                                                    String carName = item.getString("CarName");
                                                    synchronized (mCars) {
                                                        for (Marker mk : mCars) {
                                                            String name = mk.getExtraInfo().getString("CarName");
                                                            if (name.equals(carName)) {
                                                                mk.setPosition(point);
                                                                Log.w(TAG, "mov car :" + carName);
                                                                found = true;
                                                                break;
                                                            }
                                                        }
                                                        if (found) {
                                                            for (Text txt : mCarNames) {
                                                                String name = txt.getExtraInfo().getString("CarName");
                                                                if (name.equals(carName)) {
                                                                    txt.setPosition(point);
                                                                    Log.w(TAG, "mov carTxt :" + carName);
                                                                    break;
                                                                }
                                                            }
                                                            return;
                                                        }


                                                        Log.w(TAG, "add car :" + carName);
                                                        Bundle extrInfo = new Bundle();
                                                        extrInfo.putString("CarName", carName);
                                                        OverlayOptions option = new MarkerOptions()
                                                                .position(point)
                                                                .icon(bitmap).title(carName).extraInfo(extrInfo);
                                                        Marker m = (Marker) getBaiduMap().addOverlay(option);
                                                        mCars.add(m);
                                                        option = new TextOptions().position(point).text(carName).extraInfo(extrInfo).align(TextOptions.ALIGN_CENTER_HORIZONTAL, TextOptions.ALIGN_TOP).fontSize(getResources().getDimensionPixelSize(R.dimen.abc_action_bar_title_text_size));
                                                        Text name = (Text) getBaiduMap().addOverlay(option);
                                                        mCarNames.add(name);
                                                    }
                                                }
                                            } catch (JSONException ex) {
                                                ex.printStackTrace();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
//                                    mFixPositionThreads.add(t);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        result = -1;
                    } catch (JSONException e) {
                        e.printStackTrace();
                        result = -2;
                    }
                    if (mQueryCarPosThread != null) {
                        try {
                            Thread.sleep(10000);    // 10秒更新一次
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } while (mQueryCarPosThread != null);
            }
        };
        mQueryCarPosThread.start();
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
                                               }

        );
    }

    @Override
    public void onDestroy() {
        if (mQueryCarPosThread != null) {
            mQueryCarPosThread.interrupt();
            mQueryCarPosThread = null;
        }
        uninitLocation();
        super.onDestroy();
    }

//    private void addCars2Map() {
//        try {
//            JSONArray js = mCarPos.getJSONArray("d");
//            for (int i = 0; i < js.length(); i++) {
//                JSONObject item = js.getJSONObject(i);
//                //定义Maker坐标点
//                double[] data = new double[]{item.getDouble("Lattitude"), item.getDouble("Longtitude")};//*///
//                WebHelper.gps2lnglat(data);
//                LatLng point = new LatLng(data[0], data[1]);
////构建Marker图标
//                BitmapDescriptor bitmap = BitmapDescriptorFactory
//                        .fromResource(R.drawable.icon_marka);
////构建MarkerOption，用于在地图上添加Marker
//                Bundle extrInfo = new Bundle();
//                extrInfo.putInt("index", i);
//                OverlayOptions option = new MarkerOptions()
//                        .position(point)
//                        .icon(bitmap).title(item.getString("CarName")).extraInfo(extrInfo);
////在地图上添加Marker，并显示
//                Marker m = (Marker) getBaiduMap().addOverlay(option);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            ;
//        }
//    }

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
                if (activity == null) {
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

                MainActivity main = (MainActivity) activity;
                main.setLastLocation(location.getLatitude(), location.getLongitude());
            }
        };
        mLocationClient.registerLocationListener(mListener);
        mLocationClient.start();
    }
}
