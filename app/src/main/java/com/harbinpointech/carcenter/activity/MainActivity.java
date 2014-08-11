package com.harbinpointech.carcenter.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.baidu.mapapi.SDKInitializer;
import com.easemob.chat.EMChat;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMGroupManager;
import com.easemob.chat.EMMessage;
import com.easemob.exceptions.EaseMobException;
import com.easemob.util.HanziToPinyin;
import com.harbinpointech.carcenter.Constant;
import com.harbinpointech.carcenter.DemoApplication;
import com.harbinpointech.carcenter.R;
import com.harbinpointech.carcenter.db.UserDao;
import com.harbinpointech.carcenter.domain.User;
import com.harbinpointech.carcenter.fragment.FixCarFragment;
import com.harbinpointech.carcenter.fragment.MapFragment;
import com.harbinpointech.carcenter.util.AsyncTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends ActionBarActivity {

    private int mCurrentSelectId;
    private List<String> mIMUSers;
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

                if (useDemo) {
                    if (mIMUSers == null) {
                        new AsyncTask<Void, Integer, Integer>() {
                            ProgressDialog mProgress;

                            @Override
                            protected Integer doInBackground(Void... params) {
                                try {
                                    List<String> usernames = EMChatManager.getInstance().getContactUserNames();
                                    mIMUSers.addAll(usernames);
                                    Map<String, User> userlist = new HashMap<String, User>();
                                    for (String username : usernames) {
                                        User user = new User();
                                        user.setUsername(username);
                                        setUserHearder(username, user);
                                        userlist.put(username, user);
                                    }
                                    // 添加user"申请与通知"
                                    User newFriends = new User();
                                    newFriends.setUsername(Constant.NEW_FRIENDS_USERNAME);
                                    newFriends.setNick("申请与通知");
                                    newFriends.setHeader("");
                                    userlist.put(Constant.NEW_FRIENDS_USERNAME, newFriends);
                                    // 添加"群聊"
                                    User groupUser = new User();
                                    groupUser.setUsername(Constant.GROUP_USERNAME);
                                    groupUser.setNick("群聊");
                                    groupUser.setHeader("");
                                    userlist.put(Constant.GROUP_USERNAME, groupUser);

                                    // 存入内存
                                    DemoApplication.getInstance().setContactList(userlist);
                                    // 存入db
                                    UserDao dao = new UserDao(MainActivity.this);
                                    List<User> users = new ArrayList<User>(userlist.values());
                                    dao.saveContactList(users);

                                    // 获取群聊列表,sdk会把群组存入到EMGroupManager和db中
                                    EMGroupManager.getInstance().getGroupsFromServer();
                                    // after login, we join groups in separate threads;
                                    EMGroupManager.getInstance().joinGroupsAfterLogin();
                                    return 0;
                                } catch (EaseMobException e) {
                                    e.printStackTrace();
                                }
                                return -1;
                            }

                            @Override
                            protected void onPreExecute() {
                                super.onPreExecute();
                                mIMUSers = new ArrayList<String>();
                                mProgress = new ProgressDialog(MainActivity.this);
                                mProgress.setMessage("正在获取好友和群聊列表...");
                                mProgress.setCancelable(false);
                            }

                            @Override
                            protected void onPostExecute(Integer integer) {
                                super.onPostExecute(integer);
                                if (integer == 0) {
                                    startActivity(new Intent(MainActivity.this, MainChatActivity.class));
                                }
                            }
                        }.execute();
                    } else {
                        startActivity(new Intent(MainActivity.this, MainChatActivity.class));
                    }
                    return;
                } else {

                }

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
     * 设置hearder属性，方便通讯中对联系人按header分类显示，以及通过右侧ABCD...字母栏快速定位联系人
     *
     * @param username
     * @param user
     */
    protected void setUserHearder(String username, User user) {
        String headerName = null;
        if (!TextUtils.isEmpty(user.getNick())) {
            headerName = user.getNick();
        } else {
            headerName = user.getUsername();
        }
        if (username.equals(Constant.NEW_FRIENDS_USERNAME)) {
            user.setHeader("");
        } else if (Character.isDigit(headerName.charAt(0))) {
            user.setHeader("#");
        } else {
            user.setHeader(HanziToPinyin.getInstance().get(headerName.substring(0, 1)).get(0).target.substring(0, 1).toUpperCase());
            char header = user.getHeader().toLowerCase().charAt(0);
            if (header < 'a' || header > 'z') {
                user.setHeader("#");
            }
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
