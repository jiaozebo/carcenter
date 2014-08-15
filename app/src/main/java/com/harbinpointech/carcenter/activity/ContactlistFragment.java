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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.easemob.chat.EMChat;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMContact;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.chat.EMMessage;
import com.easemob.exceptions.EaseMobException;
import com.harbinpointech.carcenter.R;
import com.harbinpointech.carcenter.domain.User;
import com.harbinpointech.carcenter.util.AsyncTask;

import java.util.ArrayList;
import java.util.List;

/**
 * 联系人列表页
 */
public class ContactlistFragment extends ListFragment {

    private boolean hidden;
    private InputMethodManager inputMethodManager;

    private List<EMContact> mIMUSers;

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
        registerForContextMenu(getListView());

        new AsyncTask<Void, Integer, Integer>() {

            @Override
            protected Integer doInBackground(Void... params) {
                try {
                    List<String> usernames = EMChatManager.getInstance().getContactUserNames();
                    for (String name : usernames) {
                        mIMUSers.add(new User(name));
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
                    List<EMGroup> groups = EMGroupManager.getInstance().getGroupsFromServer();

                    mIMUSers.addAll(0, groups);

                    // after login, we join groups in separate threads;
                    return 0;
                } catch (EaseMobException e) {
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
            }
        }.execute();

    }

    private void initView(View convertView, EMContact c) {
        TextView text = (TextView) convertView.findViewById(android.R.id.text1);

        TextView unread = (TextView) convertView.findViewById(R.id.unread_msg_number);
        unread.setVisibility(View.INVISIBLE);
        if (c instanceof User) {
            text.setText(c.getUsername());
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
            startActivityForResult(new Intent(getActivity(), ChatActivity.class).putExtra("userId", c.getUsername()), 1000);
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
        if (adapter == null){
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


}
