package com.harbinpointech.carcenter.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.baidu.mapapi.SDKInitializer;
import com.easemob.chat.EMChat;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.harbinpointech.carcenter.R;
import com.harbinpointech.carcenter.fragment.FixCarFragment;
import com.harbinpointech.carcenter.fragment.MapFragment;

import java.util.HashMap;

public class MainActivity extends ActionBarActivity {

    private int mCurrentSelectId;
    private boolean useDemo = false;
    private NewMessageBroadcastReceiver mMsgReceiver;
    private HashMap<Integer, Fragment> mFragmentsMap = new HashMap<Integer, Fragment>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        findViewById(R.id.main_btn_view_cars).setSelected(true);
        mCurrentSelectId = R.id.main_btn_view_cars;

        FragmentTransaction trx = getSupportFragmentManager().beginTransaction();
        Fragment map = new MapFragment();
        Fragment fixCar = Fragment.instantiate(this, FixCarFragment.class.getName(), null);
        Fragment chatList = Fragment.instantiate(this, ContactlistFragment.class.getName(), null);
        mFragmentsMap.put(R.id.main_btn_view_cars, map);
        mFragmentsMap.put(R.id.main_btn_fix_car, fixCar);
        mFragmentsMap.put(R.id.main_btn_chat, chatList);
        trx.add(R.id.fragment_container, map).add(R.id.fragment_container, fixCar).add(R.id.fragment_container, chatList).hide(fixCar).hide(chatList).commit();

        mMsgReceiver = new NewMessageBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(EMChatManager.getInstance().getNewMessageBroadcastAction());
        intentFilter.setPriority(3);
        registerReceiver(mMsgReceiver, intentFilter);
        EMChat.getInstance().setAppInited();


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
        updateUnreadLabel();
        EMChatManager.getInstance().activityResumed();
    }

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
            case R.id.btn_conversation:

                break;
            case R.id.btn_address_list:

                break;
            case R.id.btn_setting:


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
            return true;
        }
        return super.onOptionsItemSelected(item);
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
            // EMMessage message =
            // EMChatManager.getInstance().getMessage(msgId);

            // 刷新bottom bar消息未读数
            updateUnreadLabel();
//            if (currentTabIndex == 0) {
//                // 当前页面如果为聊天历史页面，刷新此页面
//                if (chatHistoryFragment != null) {
//                    chatHistoryFragment.refresh();
//                }
//            }
            // 注销广播，否则在MainChatActivity中会收到这个广播
            abortBroadcast();
        }
    }

    /**
     * 刷新未读消息数
     */
    public void updateUnreadLabel() {
        int count = getUnreadMsgCountTotal();
        TextView unreadLabel = (TextView) findViewById(R.id.unread_msg_number);
        if (count > 0) {
            unreadLabel.setText(String.valueOf(count));
            unreadLabel.setVisibility(View.VISIBLE);
        } else {
            unreadLabel.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 获取未读消息数
     *
     * @return
     */
    public int getUnreadMsgCountTotal() {
        int unreadMsgCountTotal = 0;
        unreadMsgCountTotal = EMChatManager.getInstance().getUnreadMsgsCount();
        return unreadMsgCountTotal;
    }

    /**
     * 消息回执BroadcastReceiver
     */
    private BroadcastReceiver ackMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String msgid = intent.getStringExtra("msgid");
            String from = intent.getStringExtra("from");
            EMConversation conversation = EMChatManager.getInstance().getConversation(from);
            if (conversation != null) {
                // 把message设为已读
                EMMessage msg = conversation.getMessage(msgid);
                if (msg != null) {
                    msg.isAcked = true;
                }
            }
            abortBroadcast();
        }
    };


}
