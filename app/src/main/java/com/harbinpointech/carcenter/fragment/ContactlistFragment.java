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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import com.harbinpointech.carcenter.data.WebHelper;
import com.harbinpointech.carcenter.util.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * 联系人列表页
 */
public class ContactlistFragment extends ListFragment {


    private SharedPreferences mPref;
    private String mMyName;
    private String mMyIndex;
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
        super.onActivityCreated(savedInstanceState);
        mPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

//        mGroupDao = new
        registerForContextMenu(getListView());
        setListAdapter(new CursorAdapter(getActivity(), null, false) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                View convertView = getLayoutInflater(null).inflate(android.R.layout.simple_list_item_1, parent, false);
                TextView text = (TextView) convertView.findViewById(android.R.id.text1);
                text.setCompoundDrawablesWithIntrinsicBounds(R.drawable.default_avatar, 0, 0, 0);
                return convertView;
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                TextView text = (TextView) view.findViewById(android.R.id.text1);
                text.setText(cursor.getString(cursor.getColumnIndex(Contacts.NAME)));
            }
        });
        new AsyncTask<Void, Integer, Integer>() {
            JSONArray mUserSets;

            @Override
            protected Integer doInBackground(Void... params) {
                try {
                    JSONObject[] users = new JSONObject[1];
                    int result = WebHelper.getAllUsers(users);
                    mUserSets = users[0].getJSONArray("d");
                    // 找到自己
                    for (int i = 0; i < mUserSets.length(); i++) {
                        JSONObject user = mUserSets.getJSONObject(i);
                        if (mMyName.equals(user.getString("Name"))) {
                            mMyIndex = user.getString(Contacts.ID);
                            break;
                        }
                    }
                    CarSQLiteOpenHelper.insert(Contacts.TABLE, mUserSets);
                    return 0;
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return -1;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mCursor = CarApp.lockDataBase().rawQuery(String.format("select * from %s where %s != '%s'", Contacts.TABLE, Contacts.NAME, mMyName), null);
                CursorAdapter cursorAdapter = (CursorAdapter) getListAdapter();
                cursorAdapter.changeCursor(mCursor);
            }

            @Override
            protected void onPostExecute(Integer result) {
                super.onPostExecute(result);
                if (result == 0) {
                    mCursor = CarApp.lockDataBase().rawQuery(String.format("select * from %s where %s != '%s'", Contacts.TABLE, Contacts.NAME, mMyName), null);
                    CursorAdapter cursorAdapter = (CursorAdapter) getListAdapter();
                    cursorAdapter.changeCursor(mCursor);
                }
            }
        }.execute();
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
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
