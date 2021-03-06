package com.harbinpointech.carcenter.activity;


import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.provider.BaseColumns;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.harbinpointech.carcenter.CarApp;
import com.harbinpointech.carcenter.QueryInfosService;
import com.harbinpointech.carcenter.R;
import com.harbinpointech.carcenter.adapter.MessageAdapter;
import com.harbinpointech.carcenter.data.Message;
import com.harbinpointech.carcenter.data.WebHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ChatActivity extends ActionBarActivity {

    public static final String SENDER_ID = Message.SENDER;
    public static final String SENDER_NAME = "name";
    public static final String USER_ID = "my_id";
    public static final String IS_GROUP_CHAT = "IS_GROUP_CHAT";
    private String mOtherSideId;
    private String mMyId;
    private boolean mIsGroup;
    private Cursor mCursor;
    private ListView mListView;

    private EditText mEditor;
    private BroadcastReceiver mLocalReceiver;

    public static final int WHAT_QUERY_CURSOR = 1;
    public static final int WHAT_SEND_TXT_MESSAGE = 2;

    private static Handler sUIHandler = new Handler();
    private ChatHandler mHandler;


    static class ChatHandler extends Handler {
        WeakReference<ChatActivity> mActivityRef;

        public ChatHandler(ChatActivity activity) {
            mActivityRef = new WeakReference<ChatActivity>(activity);
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            ChatActivity activity = mActivityRef.get();
            switch (msg.what) {
                case WHAT_SEND_TXT_MESSAGE:
                    long id = msg.arg1;
                    String text = (String) msg.obj;
                    activity.sendTxtMessage(id, text);
                    sendEmptyMessage(WHAT_QUERY_CURSOR);
                    break;
                case WHAT_QUERY_CURSOR:
                    activity.queryCursor();
                    activity.postSwapCursor();
                    break;
            }
        }
    }

    private void postSwapCursor() {
        sUIHandler.post(new Runnable() {
            @Override
            public void run() {
                CursorAdapter adapter = (CursorAdapter) mListView.getAdapter();
                if (adapter.getCursor() == null) {
                    TextView empty = (TextView) findViewById(android.R.id.empty);
                    empty.setText("没有收到任何消息");
                }
                adapter.swapCursor(mCursor);
                mListView.setSelection(adapter.getCount() - 1);
            }
        });
    }

    private void queryCursor() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            mHandler.removeMessages(WHAT_QUERY_CURSOR);
            mHandler.sendEmptyMessage(WHAT_QUERY_CURSOR);
        } else {
            Cursor c = null;
            try {
                SQLiteDatabase db = CarApp.lockDataBase();
                if (mIsGroup) {
                    c = db.rawQuery(String.format("select * from %s where %s=? or %s=? order by %s;", Message.TABLE, Message.GROUPID, Message.RECEIVER, Message.DATETIME), new String[]{String.valueOf(mOtherSideId), String.valueOf(mOtherSideId)});
                } else {
                    c = db.rawQuery(String.format("select * from %s where %s=%s and (%s=? or %s=? )order by %s;", Message.TABLE, Message.ISGROUP, "'N'", Message.SENDER, Message.RECEIVER, Message.DATETIME), new String[]{String.valueOf(mOtherSideId), String.valueOf(mOtherSideId)});
                }
                if (c.moveToFirst()) {
                    mCursor = c;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private int sendTxtMessage(long id, String text) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            SQLiteDatabase db = CarApp.lockDataBase();
            ContentValues cv = new ContentValues();
            cv.put(Message.CONTENT, text);
            cv.put(Message.SENDER, mMyId);
            cv.put(Message.ISGROUP, mIsGroup ? "Y" : "N");
            cv.put(Message.RECEIVER, mOtherSideId);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            cv.put(Message.DATETIME, sdf.format(new Date()));
            cv.put(Message.STATE, -1);
            id = db.insert(Message.TABLE, null, cv);
            mHandler.obtainMessage(WHAT_SEND_TXT_MESSAGE, (int) id, 0, text).sendToTarget();
            queryCursor();
            return 0;
        } else {
            try {
                int result = WebHelper.sendMessage(text, mIsGroup, mOtherSideId);
                if (result == 0) {
                    SQLiteDatabase db = CarApp.lockDataBase();
                    ContentValues cv = new ContentValues();
                    cv.put(Message.STATE, 1);
                    db.update(Message.TABLE, cv, BaseColumns._ID + "=?", new String[]{String.valueOf(id)});
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return -1;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_chat);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mOtherSideId = getIntent().getStringExtra(SENDER_ID);
        mMyId = getIntent().getStringExtra(USER_ID);
        if (TextUtils.isEmpty(mMyId)) {
            mMyId = "";
        }
        mIsGroup = getIntent().getBooleanExtra(IS_GROUP_CHAT, false);
        mListView = (ListView) findViewById(android.R.id.list);
        mListView.setEmptyView(findViewById(android.R.id.empty));

        mEditor = (EditText) findViewById(R.id.chat_et_message);

        initNotifyReceiver();

        MessageAdapter ma = new MessageAdapter(ChatActivity.this, null, mOtherSideId, mIsGroup);
        mListView.setAdapter(ma);


        new HandlerThread("chat_looper") {
            @Override
            protected void onLooperPrepared() {
                super.onLooperPrepared();
                mHandler = new ChatHandler(ChatActivity.this);
                sUIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        queryCursor();
                    }
                });
            }
        }.start();
//        mTask = new ChatLauncherTask().execute();
    }

    /**
     * 初始化消息相关
     */
    private void initNotifyReceiver() {
        IntentFilter inf = new IntentFilter(QueryInfosService.ACTION_NOTIFICATIONS_RECEIVED);
        inf.addAction(QueryInfosService.ACTION_REQUEST_FRIEND_ANSWERED);
        inf.addAction(QueryInfosService.ACTION_REQUEST_FRIEND);
        mLocalReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (QueryInfosService.ACTION_NOTIFICATIONS_RECEIVED.equals(intent.getAction())) {
                    String chatsJsonArray = intent.getStringExtra(QueryInfosService.EXTRA_CHAT_ARRAY);
                    try {
                        ContentValues cv = new ContentValues();
                        cv.put(Message.STATE, 1);
                        int count = CarApp.lockDataBase().update(Message.TABLE, cv, String.format("%s!=%d and %s='%s'", Message.STATE, 1, Message.RECEIVER, mMyId), null);
                        Log.d("Chat", "update count : " + count);
                        JSONArray array = new JSONArray(chatsJsonArray);
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject chat = array.getJSONObject(i);
                            if (chat.getString(com.harbinpointech.carcenter.data.Message.SENDER).equals(mOtherSideId)) {
                                queryCursor();
                                break;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                MainActivity.handleOnReceive(intent, ChatActivity.this);
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocalReceiver, inf);
    }

    public void onSend(View view) {
        final String text = mEditor.getText().toString();
        if (TextUtils.isEmpty(text)) {
            mEditor.requestFocus();
        } else {
            mEditor.setText(null);
            sendTxtMessage(-1, text);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void showProgress(final boolean show) {
        setProgressBarIndeterminateVisibility(show);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onDestroy() {
        if (mHandler != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mHandler.getLooper().quitSafely();
            } else {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Looper.myLooper().quit();
                    }
                });
            }
            mHandler = null;
        }
        if (mCursor != null) {
            mCursor.close();
        }
        if (mLocalReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mLocalReceiver);
            mLocalReceiver = null;
        }
        super.onDestroy();
    }
}

