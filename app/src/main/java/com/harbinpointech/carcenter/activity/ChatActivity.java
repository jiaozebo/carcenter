package com.harbinpointech.carcenter.activity;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ListView;

import com.harbinpointech.carcenter.CarApp;
import com.harbinpointech.carcenter.R;
import com.harbinpointech.carcenter.adapter.MessageAdapter;
import com.harbinpointech.carcenter.data.Message;
import com.harbinpointech.carcenter.data.WebHelper;

import org.json.JSONException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ChatActivity extends ActionBarActivity {

    public static final String SENDER_ID = "sender_id";
    public static final String SENDER_NAME = "sender_name";
    public static final String USER_ID = "my_id";
    private int mSenderId;
    private int mMyId;
    private Cursor mCursor;
    private ListView mListView;
    private AsyncTask<Void, Integer, Integer> mTask;
    private EditText mEditor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_chat);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mSenderId = getIntent().getIntExtra(SENDER_ID, 0);
        mMyId = getIntent().getIntExtra(USER_ID, 0);

        mListView = (ListView) findViewById(android.R.id.list);
        mListView.setEmptyView(findViewById(android.R.id.empty));

        mTask = new ChatLauncherTask().execute();
        mEditor = (EditText) findViewById(R.id.chat_et_message);
    }

    public void onSend(View view) {
        final String text = mEditor.getText().toString();
        if (TextUtils.isEmpty(text)) {
            mEditor.requestFocus();
        } else {
            new AsyncTask<Void, Integer, Integer>() {
                public long mId;

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    SQLiteDatabase db = CarApp.lockDataBase();
                    ContentValues cv = new ContentValues();
                    cv.put(Message.CONTENT, text);
                    cv.put(Message.SENDER, mMyId);
                    cv.put(Message.RECEIVER, mSenderId);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    cv.put(Message.DATETIME, sdf.format(new Date()));
                    mId = db.insert(Message.TABLE, null, cv);

                }

                @Override
                protected void onPostExecute(Integer integer) {
                    super.onPostExecute(integer);
                    SQLiteDatabase db = CarApp.lockDataBase();
                    ContentValues cv = new ContentValues();
                    cv.put(Message.STATE, integer == 0 ? 1 : -1);
                    db.update(Message.TABLE, cv, BaseColumns._ID + "=?", new String[]{String.valueOf(mId)});
                }

                @Override
                protected Integer doInBackground(Void... params) {
                    try {
                        return WebHelper.sendMessage(text, mSenderId);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return -1;
                }
            }.execute();

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

    public class ChatLauncherTask extends AsyncTask<Void, Integer, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgress(true);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            Cursor c = null;
            try {
                SQLiteDatabase db = CarApp.lockDataBase();
                c = db.rawQuery(String.format("select * from %s where %s=? or %s=?", Message.TABLE, Message.SENDER, Message.RECEIVER), new String[]{String.valueOf(mSenderId)});
                if (c.moveToFirst()) {
                    mCursor = c;
                    c = null;
                }
                return 0;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (c != null) {
                    c.close();
                }
            }
            return -1;
        }

        @Override
        protected void onPostExecute(final Integer result) {
            showProgress(false);
            MessageAdapter ma = new MessageAdapter(ChatActivity.this, mCursor, mSenderId);
            mListView.setAdapter(ma);
        }
    }

    private void showProgress(final boolean show) {
        setProgressBarIndeterminateVisibility(show);
    }

    @Override
    protected void onDestroy() {
        if (mTask != null) {
            mTask.cancel(true);
            mTask = null;
        }
        super.onDestroy();
    }
}

