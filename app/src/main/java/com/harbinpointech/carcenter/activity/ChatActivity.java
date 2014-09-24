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
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
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

    public static final String SENDER_ID = "sender_id";
    public static final String SENDER_NAME = "sender_name";
    public static final String USER_ID = "my_id";
    private int mOtherSideId;
    private int mMyId;
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
                c = db.rawQuery(String.format("select * from MSG where %s=? or %s=?", com.harbinpointech.carcenter.data.Message.SENDER, com.harbinpointech.carcenter.data.Message.RECEIVER), new String[]{String.valueOf(mOtherSideId), String.valueOf(mOtherSideId)});
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
            cv.put(com.harbinpointech.carcenter.data.Message.CONTENT, text);
            cv.put(com.harbinpointech.carcenter.data.Message.SENDER, mMyId);
            cv.put(com.harbinpointech.carcenter.data.Message.RECEIVER, mOtherSideId);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            cv.put(com.harbinpointech.carcenter.data.Message.DATETIME, sdf.format(new Date()));
            id = db.insert(com.harbinpointech.carcenter.data.Message.TABLE, null, cv);
            mHandler.obtainMessage(WHAT_SEND_TXT_MESSAGE, (int) id, 0, text).sendToTarget();
            queryCursor();
            return 0;
        } else {
            try {
                return WebHelper.sendMessage(text, mOtherSideId);
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
        mOtherSideId = getIntent().getIntExtra(SENDER_ID, 0);
        mMyId = getIntent().getIntExtra(USER_ID, 0);
        mListView = (ListView) findViewById(android.R.id.list);
        mListView.setEmptyView(findViewById(android.R.id.empty));

        mEditor = (EditText) findViewById(R.id.chat_et_message);

        initNotifyReceiver();

        MessageAdapter ma = new MessageAdapter(ChatActivity.this, null, mOtherSideId);
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
        mLocalReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (QueryInfosService.ACTION_NOTIFICATIONS_RECEIVED.equals(intent.getAction())) {
                    String chatsJsonArray = intent.getStringExtra(QueryInfosService.EXTRA_CHAT_ARRAY);
                    try {
                        JSONArray array = new JSONArray(chatsJsonArray);
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject chat = array.getJSONObject(i);
                            if (chat.getString(com.harbinpointech.carcenter.data.Message.SENDER).equals(mOtherSideId)) {
                                queryCursor();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
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
        super.onDestroy();
    }
}

