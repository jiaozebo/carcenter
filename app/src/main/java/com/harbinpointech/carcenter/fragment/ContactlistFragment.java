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
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.support.v4.app.ListFragment;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.harbinpointech.carcenter.CarApp;
import com.harbinpointech.carcenter.CarSQLiteOpenHelper;
import com.harbinpointech.carcenter.R;
import com.harbinpointech.carcenter.activity.ChatActivity;
import com.harbinpointech.carcenter.activity.LoginActivity;
import com.harbinpointech.carcenter.data.Contacts;
import com.harbinpointech.carcenter.data.Group;
import com.harbinpointech.carcenter.data.Message;
import com.harbinpointech.carcenter.data.WebHelper;
import com.harbinpointech.carcenter.util.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * 联系人列表页
 */
public class ContactlistFragment extends ListFragment {


    private SharedPreferences mPref;
    private String mMyName;
    private int mMyIndex;
    private Cursor mCursor;

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
        setHasOptionsMenu(true);

        mMyName = getArguments().getString(LoginActivity.KEY_USER_NAME);
        mMyIndex = getArguments().getInt(LoginActivity.KEY_USER_INDEX);
        super.onActivityCreated(savedInstanceState);
        mPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

//        mGroupDao = new
        registerForContextMenu(getListView());
        setListAdapter(new CursorAdapter(getActivity(), null, false) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                View convertView = getLayoutInflater(null).inflate(R.layout.contact_list_item, parent, false);
                TextView text = (TextView) convertView.findViewById(android.R.id.text1);
                text.setCompoundDrawablesWithIntrinsicBounds(R.drawable.default_avatar, 0, 0, 0);
                return convertView;
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                TextView text = (TextView) view.findViewById(android.R.id.text1);
                text.setText(cursor.getString(cursor.getColumnIndex(Contacts.NAME)));
                boolean isGroup = cursor.getInt(cursor.getColumnIndex(Group.ID)) != 0;
                String sql = String.format("select _id from %s where %s =0 and %s=%d and %s=%s", Message.TABLE, Message.STATE, Message.RECEIVER, mMyIndex, Message.ISGROUP, isGroup ? "'Y'" : "'N'");
                Log.i("SQL", sql);
                Cursor cc = CarApp.lockDataBase().rawQuery(sql, null);
                if (cc.moveToFirst()) {
                    TextView unread = (TextView) view.findViewById(R.id.unread_msg_number);
                    unread.setVisibility(View.VISIBLE);
                    unread.setText(String.valueOf(cc.getCount()));
                }
            }
        });
        final ListView listView = getListView();
        new Thread("QUERY_CONTACTS") {
            JSONArray mUserSets
                    ,
                    mGroupSets;

            @Override
            public void run() {
                try {
                    JSONObject[] users = new JSONObject[1];
                    int result = WebHelper.getAllUsers(users);
                    if (result == 0) {
                        mUserSets = users[0].getJSONArray("d");
                        CarSQLiteOpenHelper.insert(Contacts.TABLE, mUserSets);
                    }
                    users[0] = null;
                    result = WebHelper.getAllGroups(users);
                    if (result == 0) {
                        mGroupSets = users[0].getJSONArray("d");
                        CarSQLiteOpenHelper.insert(Group.TABLE, mGroupSets);
                    }
                    listView.post(new Runnable() {
                        @Override
                        public void run() {
                            String sql = String.format("select  %s, %s, %s, 0 as %s from %s where %s == 1 union select  %s,%s as %s, %s as %s, %s from %s", BaseColumns._ID, Contacts.ID, Contacts.NAME, Group.ID, Contacts.TABLE, Contacts.FRIEND
                                    , BaseColumns._ID, Group.ID, Contacts.ID, Group.NAME, Contacts.NAME, Group.ID, Group.TABLE);
                            Log.i("SQL", sql);
                            mCursor = CarApp.lockDataBase().rawQuery(sql, null);
                            CursorAdapter cursorAdapter = (CursorAdapter) getListAdapter();
                            cursorAdapter.changeCursor(mCursor);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        String sql = String.format("select  %s, %s, %s, 0 as %s from %s where %s == 1 union select  %s,%s as %s, %s as %s, %s from %s", BaseColumns._ID, Contacts.ID, Contacts.NAME, Group.ID, Contacts.TABLE, Contacts.FRIEND
                , BaseColumns._ID, Group.ID, Contacts.ID, Group.NAME, Contacts.NAME, Group.ID, Group.TABLE);
        Log.i("SQL", sql);
        mCursor = CarApp.lockDataBase().rawQuery(sql, null);
        CursorAdapter cursorAdapter = (CursorAdapter) getListAdapter();
        cursorAdapter.changeCursor(mCursor);
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Cursor c = (Cursor) l.getItemAtPosition(position);
        // 进入群聊列表页面//进入群聊
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        // it is group chat
        intent.putExtra(ChatActivity.SENDER_ID, c.getString(c.getColumnIndex(Contacts.ID)));
        intent.putExtra(ChatActivity.SENDER_NAME, c.getString(c.getColumnIndex(Contacts.NAME)));
        intent.putExtra(ChatActivity.IS_GROUP_CHAT, c.getInt(c.getColumnIndex(Group.ID)) != 0);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
//        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, R.id.mdy_nick, 0, "修改昵称");
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        Cursor c = (Cursor) getListView().getItemAtPosition(info.position);
        int groupID = c.getInt(c.getColumnIndex(Group.ID));
        if (groupID != 0) {
            menu.add(0, R.id.view_group_member, 0, "查看群成员");
            menu.add(0, R.id.add_group_member, 0, "添加群成员");
        }
    }

    //
    @Override
    public boolean onContextItemSelected(MenuItem item) {

        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (item.getItemId() == R.id.mdy_nick) {
            final EditText nik = new EditText(getActivity());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            nik.setLayoutParams(lp);

            new AlertDialog.Builder(getActivity()).setTitle("请输入备注名称").setView(nik).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    new AsyncTask<Void, Integer, Integer>() {

                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                        }

                        @Override
                        protected Integer doInBackground(Void... params) {
                            String nike = nik.getText().toString();
                            if (!TextUtils.isEmpty(nike)) {

                                return 0;
                            }
                            return -1;
                        }

                        @Override
                        protected void onPostExecute(Integer integer) {
                            super.onPostExecute(integer);
                            if (integer == 0) {
//                                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putString(u.getUsername(), nik.getText().toString()).commit();
//                                BaseAdapter ba = (BaseAdapter) getListAdapter();
//                                ba.notifyDataSetChanged();
                            } else {
                                Toast.makeText(getActivity(), "昵称不能为空", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }.execute();
                }
            }).setNegativeButton("取消", null).show();
        } else if (item.getItemId() == R.id.view_group_member) {
            final ProgressDialog dlg = ProgressDialog.show(getActivity(), getString(R.string.app_name), "请稍候...", false, false);
            Cursor c = (Cursor) getListView().getItemAtPosition(info.position);
            final int groupID = c.getInt(c.getColumnIndex(Group.ID));
            new Thread("QUERY_GROUP_MEMBER") {
                @Override
                public void run() {
                    try {
                        final JSONObject[] members = new JSONObject[1];
                        final int result = WebHelper.getGroupMembers(members, groupID);
                        getListView().post(new Runnable() {
                            @Override
                            public void run() {

                                if (result == 0) {
//                            JSONObject member = members[0];
                                    JSONArray member = null;
                                    try {
                                        member = members[0].getJSONArray("d");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    if (member != null && member.length() > 0) {
                                        final JSONArray fmember = member;
                                        BaseAdapter adapter = new BaseAdapter() {
                                            @Override
                                            public int getCount() {
                                                return fmember.length();
                                            }

                                            @Override
                                            public Object getItem(int position) {
                                                try {
                                                    return fmember.getJSONObject(position);
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                                return null;
                                            }

                                            @Override
                                            public long getItemId(int position) {
                                                return position;
                                            }

                                            @Override
                                            public View getView(int position, View convertView, ViewGroup parent) {
                                                if (convertView == null) {
                                                    convertView = getLayoutInflater(null).inflate(android.R.layout.simple_list_item_1, parent, false);
                                                }
                                                TextView text = (TextView) convertView.findViewById(android.R.id.text1);
                                                JSONObject item = (JSONObject) getItem(position);
                                                try {
                                                    text.setText(item.getString("Name"));
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                                return convertView;
                                            }
                                        };
                                        new AlertDialog.Builder(getActivity()).setAdapter(adapter, null).show();
                                    } else {
                                        Toast.makeText(getActivity(), "未获取到群成员", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    dlg.dismiss();
                }
            }.start();
        } else if (item.getItemId() == R.id.add_group_member) {
            final ProgressDialog dlg = ProgressDialog.show(getActivity(), getString(R.string.app_name), "请稍候...", false, false);
            Cursor c = (Cursor) getListView().getItemAtPosition(info.position);
            final int groupID = c.getInt(c.getColumnIndex(Group.ID));
            final String groupName = c.getString(c.getColumnIndex(Contacts.NAME));
            new Thread("QUERY_GROUP_MEMBER") {
                @Override
                public void run() {
                    try {
                        final JSONObject[] members = new JSONObject[1];
                        final int result = WebHelper.getGroupMembers(members, groupID);
                        final StringBuffer sb = new StringBuffer();
                        if (result == 0) {
                            try {
                                JSONArray member = members[0].getJSONArray("d");
                                for (int i = 0; i < member.length(); i++) {
                                    if (i != 0) {
                                        sb.append(',');
                                    }
                                    JSONObject m = member.getJSONObject(i);
                                    sb.append(m.getString(Contacts.ID));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        getListView().post(new Runnable() {
                            @Override
                            public void run() {
//                                final Cursor cursor = CarApp.lockDataBase().rawQuery("select _id, ID, Name,case when ID in (?) then 1 else 0 end as INGROUP from Contacts", new String[]{sb.toString()});
                                final Cursor cursor = CarApp.lockDataBase().rawQuery(String.format("select _id, ID, Name, 0 as INGROUP from Contacts where ID not in(%s)", sb.toString()), null);

                                final ArrayList<String> newMems = new ArrayList<String>();
                                new AlertDialog.Builder(getActivity()).setMultiChoiceItems(cursor, "INGROUP", "Name", new DialogInterface.OnMultiChoiceClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                        cursor.moveToPosition(which);
//                                        ContentValues cv = new ContentValues();
//                                        cv.put("INGROUP", isChecked ? 1 : 0);
//                                        CarApp.lockDataBase().update("GROUP_MEMBER", cv, "_id=?", new String[]{String.valueOf(cursor.getInt(cursor.getColumnIndex(BaseColumns._ID)))});
                                        String cid = cursor.getString(cursor.getColumnIndex(Contacts.ID));
                                        if (isChecked) {
                                            newMems.add(cid);
                                        } else {
                                            newMems.remove(cid);
                                        }
                                    }
                                }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (!newMems.isEmpty()) {
                                            final String[] ids = new String[newMems.size()];
                                            newMems.toArray(ids);
                                            dlg.show();
                                            new Thread("ADD_GROUPS") {
                                                @Override
                                                public void run() {
                                                    try {
                                                        WebHelper.addMembers(String.valueOf(groupID), groupName, ids);
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }
                                                    dlg.dismiss();
                                                }
                                            }.start();
                                        }
                                    }
                                }).setNegativeButton("取消", null).show();
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    dlg.dismiss();
                }
            }.start();
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_add, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add) {
            final EditText userEt = new EditText(getActivity());
            userEt.setHint("请输入对方的用户名");
            new AlertDialog.Builder(getActivity()).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final String id = userEt.getText().toString();
                    if (TextUtils.isEmpty(id)) {
                        return;
                    }
                    final AlertDialog dlg = ProgressDialog.show(getActivity(), getString(R.string.app_name), "请稍等...", false, false);
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                JSONObject[] p = new JSONObject[1];
                                int result = WebHelper.getLoginUser(p, id);
                                if (result == 0) {
                                    JSONObject userInfo = p[0].getJSONObject("d");
                                    int mUserIdx = userInfo.getInt(Contacts.ID);
                                    WebHelper.sendMessage(Message.MSG_ADD_FRIEND, false, String.valueOf(mUserIdx));
                                } else {
                                    getListView().post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Activity activity = getActivity();
                                            if (activity != null) {
                                                Toast.makeText(activity.getApplicationContext(), "您查询的用户不存在或查询未成功", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            dlg.dismiss();
                        }
                    }.start();

                }
            }).setTitle("添加好友").setNegativeButton("取消", null).setView(userEt).show();
        }
        return true;
    }
}
