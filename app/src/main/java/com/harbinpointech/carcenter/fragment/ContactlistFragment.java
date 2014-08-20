/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.harbinpointech.carcenter.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.text.Layout;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChat;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMContact;
import com.easemob.chat.EMContactListener;
import com.easemob.chat.EMContactManager;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.chat.EMMessage;
import com.easemob.exceptions.EaseMobException;
import com.harbinpointech.carcenter.Constant;
import com.harbinpointech.carcenter.DemoApplication;
import com.harbinpointech.carcenter.R;

import android.app.AlertDialog;

import com.harbinpointech.carcenter.activity.ChatActivity;
import com.harbinpointech.carcenter.activity.MainActivity;
import com.harbinpointech.carcenter.db.UserDao;
import com.harbinpointech.carcenter.domain.InviteMessage;
import com.harbinpointech.carcenter.domain.User;
import com.harbinpointech.carcenter.util.AsyncTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 联系人列表页
 */
public class ContactlistFragment extends ListFragment {

    private boolean hidden;
    private InputMethodManager inputMethodManager;

    private List<EMContact> mIMUSers;
    private UserDao mUserDao;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        android.app.ListFragment f;
        return inflater.inflate(R.layout.fragment_contact_list, container, false);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            Activity activity = getActivity();
            if (activity != null) {
                activity.setTitle("通讯录");
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mUserDao = new UserDao(getActivity());
        registerForContextMenu(getListView());
        final String username = getActivity().getIntent().getStringExtra("username");
        final String password = getActivity().getIntent().getStringExtra("password");
        new AsyncTask<Void, Integer, Integer>() {
            boolean mLoginSuccessed = false;

            @Override
            protected Integer doInBackground(Void... params) {
                try {
                    if (username == null || (username.equals(DemoApplication.getInstance().getUserName()) && password.equals(DemoApplication.getInstance().getPassword()))) {
                        // 已经登录过，避免重复登录
                        mLoginSuccessed = true;
                    } else {
                        // 用户名、密码更换了，重新登出、登录
                        if (DemoApplication.getInstance().getUserName() != null && DemoApplication.getInstance().getPassword() != null) {
                            DemoApplication.getInstance().logout();
                        }
                        // 调用sdk登陆方法登陆聊天服务器
                        final byte[] lock = new byte[0];
                        EMChatManager.getInstance().login(username, password, new EMCallBack() {

                            @Override
                            public void onSuccess() {
                                // 登陆成功，保存用户名密码
                                DemoApplication.getInstance().setUserName(username);
                                DemoApplication.getInstance().setPassword(password);
                                mLoginSuccessed = true;
                                synchronized (lock) {
                                    lock.notify();
                                }
                            }

                            @Override
                            public void onProgress(int progress, final String status) {

                            }

                            @Override
                            public void onError(int code, final String message) {
                                mLoginSuccessed = false;
                                synchronized (lock) {
                                    lock.notify();
                                }
                            }
                        });

                        synchronized (lock) {
                            lock.wait();
                        }
                    }
                    if (!mLoginSuccessed) {
                        return -1;
                    }
                    try {
                        EMGroup group = EMGroupManager.getInstance().getGroupFromServer("140792997963939");
                        List<String> members = group.getMembers();//获取群成员
                        Iterator<String> it = members.iterator();
                        while (it.hasNext()) {
                            String mem = it.next();
                            EMContactManager.getInstance().addContact(mem, "i'm in vehicle group..");
                        }
                        EMGroupManager.getInstance().joinGroup("140792997963939");


                        EMGroupManager.getInstance().getGroupsFromServer();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    List<String> usernames = EMChatManager.getInstance().getContactUserNames();
                    for (String name : usernames) {
                        User u = new User(name);
                        u.setNick(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(name, name));
                        mIMUSers.add(u);
                    }

//                    Map<String, User> userlist = new HashMap<String, User>();
//                    for (String username : usernames) {
//                        User user = new User();
//                        user.setUsername(username);
//                        setUserHearder(username, user);
//                        userlist.put(username, user);
//                    }
//                    // 添加user"申请与通知"
//                    User newFriends = new User();
//                    newFriends.setUsername(Constant.NEW_FRIENDS_USERNAME);
//                    newFriends.setNick("申请与通知");
//                    newFriends.setHeader("");
//                    userlist.put(Constant.NEW_FRIENDS_USERNAME, newFriends);
//                    // 添加"群聊"
//                    User groupUser = new User();
//                    groupUser.setUsername(Constant.GROUP_USERNAME);
//                    groupUser.setNick("群聊");
//                    groupUser.setHeader("");
//                    userlist.put(Constant.GROUP_USERNAME, groupUser);
//
//                    // 存入内存
//                    DemoApplication.getInstance().setContactList(userlist);
//                    // 存入db
//                    UserDao dao = new UserDao(getActivity());
//                    List<User> users = new ArrayList<User>(userlist.values());
//                    dao.saveContactList(users);

                    // 获取群聊列表,sdk会把群组存入到EMGroupManager和db中
                    try {
//                         EMGroupManager.getInstance().getGroupsFromServer();
                        List<EMGroup> groups = EMGroupManager.getInstance().getAllGroups();
                        mIMUSers.addAll(0, groups);
                    } catch (Exception e) {
                        e.printStackTrace();
                        EMGroup group = new EMGroup("140792997963939");
                        group.setGroupName("群组");
                        group.setNick("群组");
                        mIMUSers.add(0, group);
                    }
                    // after login, we join groups in separate threads;
                    return 0;
                } catch (EaseMobException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return -1;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mIMUSers = new ArrayList<EMContact>();
//                setListShown(false);
            }

            @Override
            protected void onPostExecute(Integer integer) {
                super.onPostExecute(integer);

                setListAdapter(new ArrayAdapter<EMContact>(getActivity(), R.layout.contact_list_item, android.R.id.text1, mIMUSers) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        if (convertView == null) {
                            convertView = getLayoutInflater(null).inflate(R.layout.contact_list_item, parent, false);
                        }
                        EMContact c = getItem(position);
                        initView(convertView, c);

                        return convertView;
                    }
                });
                EMChat.getInstance().setAppInited();

                EMContactManager.getInstance().setContactListener(new MyContactListener());
            }
        }.execute();

    }

    private void initView(View convertView, EMContact c) {
        TextView text = (TextView) convertView.findViewById(android.R.id.text1);

        TextView unread = (TextView) convertView.findViewById(R.id.unread_msg_number);
        unread.setVisibility(View.INVISIBLE);
        if (c instanceof User) {
            String nik = c.getNick();
            if (TextUtils.isEmpty(nik)) {
                nik = c.getUsername();
            }
            text.setText(nik);
            text.setCompoundDrawablesWithIntrinsicBounds(R.drawable.default_avatar, 0, 0, 0);
            if (((User) c).getUnreadMsgCount() > 0) {
                unread.setText(String.valueOf(((User) c).getUnreadMsgCount()));
                unread.setVisibility(View.VISIBLE);
            }
        } else {
            EMGroup g = (EMGroup) c;
            text.setText(g.getGroupName());
            text.setCompoundDrawablesWithIntrinsicBounds(R.drawable.group_icon, 0, 0, 0);
            if (!TextUtils.isEmpty(g.getDescription())) {
                unread.setText("*");
                unread.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        EMContact c = (EMContact) l.getItemAtPosition(position);
        if (c instanceof EMGroup) {
            // 进入群聊列表页面//进入群聊
            Intent intent = new Intent(getActivity(), ChatActivity.class);
            // it is group chat
            intent.putExtra("chatType", ChatActivity.CHATTYPE_GROUP);
            EMGroup g = (EMGroup) c;
            intent.putExtra("groupId", g.getGroupId());
            startActivityForResult(intent, 1000);
            ((EMGroup) c).setDescription(null);
        } else {
            User u = (User) c;
            u.setUnreadMsgCount(0);
            // demo中直接进入聊天页面，实际一般是进入用户详情页
            startActivityForResult(new Intent(getActivity(), ChatActivity.class).putExtra("userId", c.getUsername()).putExtra("nick", c.getNick()), 1000);
        }
        initView(v, c);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000) {
            MainActivity activity = (MainActivity) getActivity();
            activity.updateUnreadLabel(null);

        }
    }

    public void updateUnreadLable(EMMessage message) {
        String userName = message.getFrom();
        EMMessage.ChatType ct = message.getChatType();
        ArrayAdapter<EMContact> adapter = (ArrayAdapter<EMContact>) getListAdapter();
        if (adapter == null) {
            return;
        }
        if (ct == EMMessage.ChatType.Chat) {

            for (int i = 0; i < adapter.getCount(); i++) {
                EMContact c = adapter.getItem(i);
                if (c instanceof User) {
                    if (userName.equals(c.getUsername())) {
                        ((User) c).setUnreadMsgCount(((User) c).getUnreadMsgCount() + 1);
                    }
                }
            }

        } else if (ct == EMMessage.ChatType.GroupChat) {
            EMContact c = adapter.getItem(0);
            if (c instanceof EMGroup) {
                EMGroup g = (EMGroup) c;
                if (g.getMembers().contains(userName)) {
                    g.setDescription(userName);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        // 长按前两个不弹menu
        EMContact c = (EMContact) getListView().getItemAtPosition(((AdapterView.AdapterContextMenuInfo) menuInfo).position);
        if (c instanceof User) {
            getActivity().getMenuInflater().inflate(R.menu.context_contact_list, menu);
        }
    }

    //
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final User u = (User) getListView().getItemAtPosition(info.position);
        final EditText nik = new EditText(getActivity());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        nik.setLayoutParams(lp);

        new AlertDialog.Builder(getActivity()).setTitle("请输入备注名称").setView(nik).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new AsyncTask<Void, Integer, Integer>() {
                    ProgressDialog mDlg = null;

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        mDlg = new ProgressDialog(getActivity());
                        mDlg.setMessage("请稍等...");
                        mDlg.setCancelable(false);
                        mDlg.show();
                    }

                    @Override
                    protected Integer doInBackground(Void... params) {
                        String nike = nik.getText().toString();
                        if (!TextUtils.isEmpty(nike)) {
                            u.setNick(nike);
                            return 0;
                        }
                        return -1;
                    }

                    @Override
                    protected void onPostExecute(Integer integer) {
                        super.onPostExecute(integer);
                        mDlg.dismiss();
                        if (integer == 0) {
                            PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putString(u.getUsername(), nik.getText().toString()).commit();
                            BaseAdapter ba = (BaseAdapter) getListAdapter();
                            ba.notifyDataSetChanged();
                        } else {
                            Toast.makeText(getActivity(), "昵称不能为空", Toast.LENGTH_SHORT).show();
                        }
                    }
                }.execute();
            }
        }).setNegativeButton("取消", null).show();
        return true;
    }


    /**
     * 联系人变化listener
     */
    private class MyContactListener implements EMContactListener {

        @Override
        public void onContactAdded(List<String> usernameList) {
            List<User> usrs = new ArrayList<User>();
            for (String userName : usernameList) {
                User u = new User(userName);
                mUserDao.saveContact(u);
                usrs.add(u);
            }

            ArrayAdapter adapter = (ArrayAdapter) getListAdapter();
            adapter.addAll(usrs);

        }

        @Override
        public void onContactDeleted(List<String> usernameList) {
            List<String> usrs = new ArrayList<String>();
            usrs.addAll(usernameList);
            ArrayAdapter adapter = (ArrayAdapter) getListAdapter();
            for (int i = 0; i < adapter.getCount(); i++) {
                for (String usr : usrs) {
                    EMContact c = (EMContact) adapter.getItem(i);
                    if (c.getUsername().equals(usr)) {
                        adapter.remove(c);
                        usrs.remove(usr);
                        mUserDao.deleteContact(usr);
                        break;
                    }
                }
            }
        }

        @Override
        public void onContactInvited(String username, String reason) {
            // 接到邀请的消息，如果不处理(同意或拒绝)，掉线后，服务器会自动再发过来，所以客户端不要重复提醒
            Log.i("Contact", String.format("recv invited request~ (%s,%s)", username, reason));
        }

        @Override
        public void onContactAgreed(String username) {
            Log.i("Contact", username + "同意了你的好友请求");

        }

        @Override
        public void onContactRefused(String username) {
            // 参考同意，被邀请实现此功能,demo未实现
        }
    }

}
