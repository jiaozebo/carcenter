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
package com.harbinpointech.carcenter.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMContact;
import com.easemob.chat.EMContactManager;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.exceptions.EaseMobException;
import com.easemob.util.HanziToPinyin;
import com.harbinpointech.carcenter.Constant;
import com.harbinpointech.carcenter.DemoApplication;
import com.harbinpointech.carcenter.R;
import com.harbinpointech.carcenter.adapter.ContactAdapter;
import com.harbinpointech.carcenter.db.UserDao;
import com.harbinpointech.carcenter.domain.User;
import com.harbinpointech.carcenter.util.AsyncTask;
import com.harbinpointech.carcenter.widget.Sidebar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 联系人列表页
 */
public class ContactlistFragment extends Fragment {
    private ContactAdapter adapter;
    private List<EMContact> contactList;
    private ListView listView;
    private boolean hidden;
    private Sidebar sidebar;
    private InputMethodManager inputMethodManager;

    private List<String> mIMUSers;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contact_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        listView = (ListView) getView().findViewById(R.id.list);
        sidebar = (Sidebar) getView().findViewById(R.id.sidebar);
        sidebar.setListView(listView);
        contactList = new ArrayList<EMContact>();
        // 获取设置contactlist
        getContactList();

        listView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // 隐藏软键盘
                if (getActivity().getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
                    if (getActivity().getCurrentFocus() != null)
                        inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);
                }
                return false;
            }
        });

        ImageView addContactView = (ImageView) getView().findViewById(R.id.iv_new_contact);
        // 进入添加好友页
        addContactView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AddContactActivity.class));
            }
        });
        registerForContextMenu(listView);

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
                    UserDao dao = new UserDao(getActivity());
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
                mProgress = new ProgressDialog(getActivity());
                mProgress.setMessage("正在获取好友和群聊列表...");
                mProgress.setCancelable(false);
            }

            @Override
            protected void onPostExecute(Integer integer) {
                super.onPostExecute(integer);

                // 设置adapter
                adapter = new ContactAdapter(getActivity(), R.layout.row_contact, contactList, sidebar);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String username = adapter.getItem(position).getUsername();
                        if (Constant.NEW_FRIENDS_USERNAME.equals(username)) {
                            // 进入申请与通知页面
                            User user = DemoApplication.getInstance().getContactList().get(Constant.NEW_FRIENDS_USERNAME);
                            user.setUnreadMsgCount(0);
                            startActivity(new Intent(getActivity(), NewFriendsMsgActivity.class));
                        } else if (Constant.GROUP_USERNAME.equals(username)) {
                            // 进入群聊列表页面
                            startActivity(new Intent(getActivity(), GroupsActivity.class));
                        } else {
                            // demo中直接进入聊天页面，实际一般是进入用户详情页
                            startActivity(new Intent(getActivity(), ChatActivity.class).putExtra("userId", adapter.getItem(position).getUsername()));
                        }
                    }
                });

            }
        }.execute();

    }

//    @Override
//    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
//        super.onCreateContextMenu(menu, v, menuInfo);
//        // 长按前两个不弹menu
//        if (((AdapterContextMenuInfo) menuInfo).position > 2) {
//            getActivity().getMenuInflater().inflate(R.menu.context_contact_list, menu);
//        }
//    }
//
//    @Override
//    public boolean onContextItemSelected(MenuItem item) {
//        if (item.getItemId() == R.id.delete_contact) {
//            EMContact contact = adapter.getItem(((AdapterContextMenuInfo) item.getMenuInfo()).position);
//            if (cont)
//            User tobeDeleteUser = adapter.getItem(((AdapterContextMenuInfo) item.getMenuInfo()).position);
//            // 删除此联系人
//            deleteContact(tobeDeleteUser);
//            // 删除相关的邀请消息
//            InviteMessgeDao dao = new InviteMessgeDao(getActivity());
//            dao.deleteMessage(tobeDeleteUser.getUsername());
//            return true;
//        } else if (item.getItemId() == R.id.add_to_blacklist) {
//            User user = adapter.getItem(((AdapterContextMenuInfo) item.getMenuInfo()).position);
//            try {
//                //加入到黑名单
//                EMContactManager.getInstance().addUserToBlackList(user.getUsername(), true);
//                Toast.makeText(getActivity(), "移入黑名单成功", Toast.LENGTH_SHORT).show();
//            } catch (EaseMobException e) {
//                e.printStackTrace();
//                Toast.makeText(getActivity(), "移入黑名单失败", Toast.LENGTH_SHORT).show();
//            }
//        }
//        return super.onContextItemSelected(item);
//    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        this.hidden = hidden;
        if (!hidden) {
            refresh();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!hidden) {
            refresh();
        }
    }

    /**
     * 删除联系人
     *
     */
    public void deleteContact(final User tobeDeleteUser) {
        final ProgressDialog pd = new ProgressDialog(getActivity());
        pd.setMessage("正在删除...");
        pd.setCanceledOnTouchOutside(false);
        pd.show();
        new Thread(new Runnable() {
            public void run() {
                try {
                    EMContactManager.getInstance().deleteContact(tobeDeleteUser.getUsername());
                    // 删除db和内存中此用户的数据
                    UserDao dao = new UserDao(getActivity());
                    dao.deleteContact(tobeDeleteUser.getUsername());
                    DemoApplication.getInstance().getContactList().remove(tobeDeleteUser.getUsername());
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            pd.dismiss();
                            adapter.remove(tobeDeleteUser);
                            adapter.notifyDataSetChanged();

                        }
                    });
                } catch (final Exception e) {
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            pd.dismiss();
                            Toast.makeText(getActivity(), "删除失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }

            }
        }).start();

    }

    // 刷新ui
    public void refresh() {
        try {
            // 可能会在子线程中调到这方法
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    getContactList();
                    adapter.notifyDataSetChanged();

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getContactList() {
        contactList.clear();
        Map<String, User> users = DemoApplication.getInstance().getContactList();
        Iterator<Entry<String, User>> iterator = users.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, User> entry = iterator.next();
            if (!entry.getKey().equals(Constant.NEW_FRIENDS_USERNAME) && !entry.getKey().equals(Constant.GROUP_USERNAME))
                contactList.add(entry.getValue());
        }
        // 排序
        Collections.sort(contactList, new Comparator<EMContact>() {

            @Override
            public int compare(EMContact lhs, EMContact rhs) {
                if (lhs instanceof EMGroup && rhs instanceof  User){
                    return -1;
                }else if (rhs instanceof EMGroup && lhs instanceof  User){
                    return 1;
                }
                return lhs.getUsername().compareTo(rhs.getUsername());
            }
        });

        // 加入"申请与通知"和"群聊"
        contactList.add(0, users.get(Constant.GROUP_USERNAME));
        // 把"申请与通知"添加到首位
        contactList.add(0, users.get(Constant.NEW_FRIENDS_USERNAME));
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

}
