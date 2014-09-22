package com.harbinpointech.carcenter.activity;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ListView;

import com.harbinpointech.carcenter.R;


public class ChatActivity extends ActionBarActivity {

    public static final String SENDER_ID = "sender_id";
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
        String text = mEditor.getText().toString();
        if (TextUtils.isEmpty(text)) {
            mEditor.requestFocus();
        } else {

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
            int result = 0;

//            Cursor c = null;
//            try {
//                SQLiteDatabase db = MyCameraApp.lockDataBase();
//                c = db.rawQuery(String.format("select * from chat where %s=? or %s=?", Chat.SENDER_INDEX, Chat.RECEIVER_INDEX), new String[]{String.valueOf(mSenderId)});
//                if (c.moveToFirst()) {
//                    mCursor = c;
//                    c = null;
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            } finally {
//                if (c != null) {
//                    c.close();
//                }
//            }
            return 0;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            switch (values[0]) {
                case 0:
                    break;
                case 1:
                    break;
            }
        }

        @Override
        protected void onPostExecute(final Integer result) {
            showProgress(false);
//            MessageAdapter ma = new MessageAdapter(ChatActivity.this, mCursor, mSenderId);
//            mListView.setAdapter(ma);
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
