package com.harbinpointech.carcenter.activity;

import android.app.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.harbinpointech.carcenter.CarApp;
import com.harbinpointech.carcenter.QueryInfosService;
import com.harbinpointech.carcenter.R;
import com.harbinpointech.carcenter.data.Message;
import com.harbinpointech.carcenter.data.WebHelper;
import com.harbinpointech.carcenter.fragment.BBSFragment;
import com.harbinpointech.carcenter.fragment.ContactlistFragment;
import com.harbinpointech.carcenter.fragment.FixCarFragment;
import com.harbinpointech.carcenter.fragment.MapFragment;

import org.json.JSONException;

import java.io.IOException;
import java.util.HashMap;

public class MainActivity extends ActionBarActivity {

    public static final int REQUEST_SETTING = 1000;
    public static final int RESULT_QUIT = 1000;
    private int mCurrentSelectId;
    private NewMessageBroadcastReceiver mMsgReceiver;
    private HashMap<Integer, Fragment> mFragmentsMap = new HashMap<Integer, Fragment>();
    private double[] mLastLocation;
    private int mMyIndex;

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

        Bundle args = new Bundle();
        Intent intent = getIntent();
        args.putString(LoginActivity.KEY_USER_NAME, intent.getStringExtra(LoginActivity.KEY_USER_NAME));
        args.putString(LoginActivity.KEY_PWD, intent.getStringExtra(LoginActivity.KEY_PWD));
        mMyIndex = intent.getIntExtra(LoginActivity.KEY_USER_INDEX, 0);
        args.putInt(LoginActivity.KEY_USER_INDEX, intent.getIntExtra(LoginActivity.KEY_USER_INDEX, 0));
        Fragment chatList = Fragment.instantiate(this, ContactlistFragment.class.getName(), args);
        Fragment bbs = Fragment.instantiate(this, BBSFragment.class.getName(), null);
        mFragmentsMap.put(R.id.main_btn_view_cars, map);
//        mFragmentsMap.put(R.id.main_btn_fix_car, fixCar);
        mFragmentsMap.put(R.id.main_btn_chat, chatList);
        mFragmentsMap.put(R.id.main_btn_bbs, bbs);
        trx.add(R.id.fragment_container, map).add(R.id.fragment_container, chatList).add(R.id.fragment_container, bbs).hide(chatList).hide(bbs).commit();

        setTitle("查看车辆");

        Intent i = new Intent(this, QueryInfosService.class);
        startService(i);


        fixUnread();

        String user = intent.getStringExtra(QueryInfosService.EXTRA_REQUEST_FRIEND_ANSWERED_USER_ID);
        if (!TextUtils.isEmpty(user)) {
            boolean accepted = intent.getBooleanExtra(QueryInfosService.EXTRA_REQUEST_FRIEND_ANSWERED_ACCEPTED, false);
//            new AlertDialog.Builder(this).setTitle(getString(R.string.app_name)).setMessage(String.format("%s %s了您的请求", user, accepted ? "接受" : "拒绝")).show();
        } else {
            user = intent.getStringExtra(QueryInfosService.EXTRA_REQUEST_FRIEND_USER_ID);
            String name = intent.getStringExtra(QueryInfosService.EXTRA_REQUEST_FRIEND_USER_NAME);
            if (!TextUtils.isEmpty(user))
                answerAddRequest(this, user, name);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMsgReceiver = new NewMessageBroadcastReceiver();
        IntentFilter inf = new IntentFilter(QueryInfosService.ACTION_NOTIFICATIONS_RECEIVED);
        inf.addAction(QueryInfosService.ACTION_REQUEST_FRIEND_ANSWERED);
        inf.addAction(QueryInfosService.ACTION_REQUEST_FRIEND);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMsgReceiver, inf);
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMsgReceiver);
        mMsgReceiver = null;
        super.onPause();
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
            Intent i = new Intent(this, QueryInfosService.class);
            stopService(i);
            WebHelper.logout();
            finish();
        } else if (requestCode == REQUEST_SCAN_VEHICLE && resultCode == RESULT_OK) {
            final String carName = data.getStringExtra("text");
            final ProgressDialog dlg = ProgressDialog.show(this, getString(R.string.app_name), "正在签到，请稍候...", false, false);
            Thread t = new Thread("SINGIN") {
                @Override
                public void run() {
                    double latitude, longitude;
                    int result = -1;
                    try {
                        synchronized (MainActivity.this) {
                            latitude = mLastLocation[0];
                            longitude = mLastLocation[1];
                        }
                        long begin = System.currentTimeMillis();
                        result = WebHelper.singin(carName, latitude, longitude);
                        long time = System.currentTimeMillis() - begin;
                        if (1000 - time > 0) {
                            Thread.sleep(1000 - time);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    dlg.dismiss();
                    if (result == 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "签到成功", Toast.LENGTH_SHORT).show();
                                onSingIn(carName);
                            }
                        });
                    } else {
                        Toast.makeText(MainActivity.this, "签到失败", Toast.LENGTH_SHORT).show();
                    }
                }
            };
            t.start();
        }
    }

    /**
     * 表示签到成功
     *
     * @param carName
     */
    private void onSingIn(final String carName) {
        new AlertDialog.Builder(this).setTitle(R.string.app_name).setItems(new CharSequence[]{"添加维修记录", "查看维修日志"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {

                } else {
                    Intent i = new Intent(MainActivity.this, ViewFixCarLogActivity.class);
                    i.putExtra("carName", carName);
                    startActivity(i);
                }
            }
        }).show();
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
            if (intent.getAction().equals(QueryInfosService.ACTION_NOTIFICATIONS_RECEIVED)) {
                final Fragment frag = mFragmentsMap.get(mCurrentSelectId);
                if (frag instanceof ContactlistFragment) {

                } else {
                    fixUnread();
                }
            }
            handleOnReceive(intent, MainActivity.this);
        }
    }

    public static void handleOnReceive(Intent intent, Activity activity) {
        if (intent.getAction().equals(QueryInfosService.ACTION_REQUEST_FRIEND)) {
            String id = intent.getStringExtra(QueryInfosService.EXTRA_REQUEST_FRIEND_USER_ID);
            String name = intent.getStringExtra(QueryInfosService.EXTRA_REQUEST_FRIEND_USER_NAME);
            if (!TextUtils.isEmpty(id)) {
                answerAddRequest(activity, id, name);
            }
        } else if (intent.getAction().equals(QueryInfosService.ACTION_REQUEST_FRIEND_ANSWERED)) {
            String user = intent.getStringExtra(QueryInfosService.EXTRA_REQUEST_FRIEND_ANSWERED_USER_NAME);
            if (!TextUtils.isEmpty(user)) {
                boolean accepted = intent.getBooleanExtra(QueryInfosService.EXTRA_REQUEST_FRIEND_ANSWERED_ACCEPTED, false);
                new AlertDialog.Builder(activity).setTitle(activity.getString(R.string.app_name)).setMessage(String.format("%s %s了您的请求", user, accepted ? "接受" : "拒绝")).setPositiveButton("确定", null).show();
            } else {

            }
        }
    }

    private static void answerAddRequest(final Activity activity, final String id, final String name) {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, final int which) {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            String myName = PreferenceManager.getDefaultSharedPreferences(activity).getString(LoginActivity.KEY_USER_NAME, "");
                            WebHelper.sendMessage(which == DialogInterface.BUTTON_POSITIVE ? Message.MSG_ADD_FRIEND_ACCEPT + myName : Message.MSG_ADD_FRIEND_REJECT + myName, false, id);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();

            }
        };
        new AlertDialog.Builder(activity).setTitle(activity.getString(R.string.app_name)).setMessage(String.format("%s 请求添加您为好友", name)).setPositiveButton("接收", listener).setNegativeButton("拒绝", listener).show();
    }

    public void fixUnread() {
        Cursor c = null;
        try {
            String sql = String.format("select %s from %s where %s =0 and %s=%d", BaseColumns._ID, Message.TABLE, Message.STATE, Message.RECEIVER, mMyIndex);
            Log.i("SQL", sql);
            c = CarApp.lockDataBase().rawQuery(sql, null);
            int count = c.getCount();
            if (count > 0) {
                TextView unread = (TextView) findViewById(R.id.unread_msg_number);
                unread.setText(String.valueOf(count));
                unread.setVisibility(View.VISIBLE);
            } else {
                TextView unread = (TextView) findViewById(R.id.unread_msg_number);
                unread.setVisibility(View.INVISIBLE);
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

}
