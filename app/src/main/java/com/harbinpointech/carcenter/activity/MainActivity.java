package com.harbinpointech.carcenter.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.baidu.mapapi.SDKInitializer;
import com.harbinpointech.carcenter.R;
import com.harbinpointech.carcenter.data.WebHelper;
import com.harbinpointech.carcenter.fragment.ContactlistFragment;
import com.harbinpointech.carcenter.fragment.FixCarFragment;
import com.harbinpointech.carcenter.fragment.MapFragment;

import java.util.HashMap;

public class MainActivity extends ActionBarActivity {

    public static final int REQUEST_SETTING = 1000;
    public static final int RESULT_QUIT = 1000;
    private int mCurrentSelectId;
    private boolean useDemo = false;
    private NewMessageBroadcastReceiver mMsgReceiver;
    private HashMap<Integer, Fragment> mFragmentsMap = new HashMap<Integer, Fragment>();
    private double[] mLastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLastLocation = new double[2];
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        findViewById(R.id.main_btn_view_cars).setSelected(true);
        mCurrentSelectId = R.id.main_btn_view_cars;

        FragmentTransaction trx = getSupportFragmentManager().beginTransaction();
        Fragment map = new MapFragment();
        Fragment fixCar = Fragment.instantiate(this, FixCarFragment.class.getName(), null);
        Bundle args = new Bundle();
        args.putString(LoginActivity.KEY_USER_NAME, getIntent().getStringExtra("username"));
        args.putString(LoginActivity.KEY_PWD, getIntent().getStringExtra("password"));
        Fragment chatList = Fragment.instantiate(this, ContactlistFragment.class.getName(), args);
        mFragmentsMap.put(R.id.main_btn_view_cars, map);
        mFragmentsMap.put(R.id.main_btn_fix_car, fixCar);
        mFragmentsMap.put(R.id.main_btn_chat, chatList);
        trx.add(R.id.fragment_container, map).add(R.id.fragment_container, fixCar).add(R.id.fragment_container, chatList).hide(fixCar).hide(chatList).commit();

        mMsgReceiver = new NewMessageBroadcastReceiver();


        setTitle("查看车辆");
    }

    @Override
    protected void onDestroy() {
        if (mMsgReceiver != null) {
            unregisterReceiver(mMsgReceiver);
            mMsgReceiver = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private static final int REQUEST_SCAN_VEHICLE = 0x1000;

    private void initFragmentWithId(int oldId) {
        Fragment frag = null;
        FragmentTransaction trx = getSupportFragmentManager().beginTransaction();
        frag = mFragmentsMap.get(mCurrentSelectId);
        Fragment old = mFragmentsMap.get(oldId);
        trx.show(frag).hide(old).commit();
    }

    /**
     * button点击事件
     *
     * @param view
     */
    public void onTabClicked(View view) {
        switch (view.getId()) {
            case R.id.main_btn_fix_car:
                startActivityForResult(new Intent(this, ScanActivity.class), REQUEST_SCAN_VEHICLE);
                return;
        }
        if (mCurrentSelectId != view.getId()) {
            findViewById(mCurrentSelectId).setSelected(false);
            view.setSelected(true);
            int old = mCurrentSelectId;
            mCurrentSelectId = view.getId();
            initFragmentWithId(old);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivityForResult(new Intent(this, SettingsActivity.class), REQUEST_SETTING);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SETTING && resultCode == RESULT_QUIT) {
            WebHelper.logout();
            finish();
        } else if (requestCode == REQUEST_SCAN_VEHICLE && resultCode == RESULT_OK) {
            int old = mCurrentSelectId;
            findViewById(mCurrentSelectId).setSelected(false);
            mCurrentSelectId = R.id.main_btn_fix_car;
            View view = findViewById(mCurrentSelectId);
            view.setSelected(true);
            initFragmentWithId(old);

            final FixCarFragment frag = (FixCarFragment) mFragmentsMap.get(mCurrentSelectId);
            final String text = data.getStringExtra("text");
            view.post(new Runnable() {
                @Override
                public void run() {
                    double latitude, longitude;
                    synchronized (MainActivity.this) {
                        latitude = mLastLocation[0];
                        longitude = mLastLocation[1];
                    }
                    frag.startSignin(text, latitude, longitude);
                }
            });
        }
    }

    public void setLastLocation(double latitude, double longitude) {
        synchronized (this) {
            mLastLocation[0] = latitude;
            mLastLocation[1] = longitude;
        }
    }

    /**
     * 新消息广播接收者
     */
    private class NewMessageBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 消息id
            String msgId = intent.getStringExtra("msgid");
            // 收到这个广播的时候，message已经在db和内存里了，可以通过id获取mesage对象
            // 刷新bottom bar消息未读数
//                // 当前页面如果为聊天历史页面，刷新此页面
//                if (chatHistoryFragment != null) {
//                    chatHistoryFragment.refresh();
//                }
//            }
            // 注销广播，否则在MainChatActivity中会收到这个广播
            abortBroadcast();
        }
    }

}
