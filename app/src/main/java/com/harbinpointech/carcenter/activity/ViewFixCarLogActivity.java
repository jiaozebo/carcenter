package com.harbinpointech.carcenter.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.harbinpointech.carcenter.CarApp;
import com.harbinpointech.carcenter.CarSQLiteOpenHelper;
import com.harbinpointech.carcenter.R;
import com.harbinpointech.carcenter.data.FixLog;
import com.harbinpointech.carcenter.data.WebHelper;
import com.harbinpointech.carcenter.util.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ViewFixCarLogActivity extends ActionBarActivity implements AdapterView.OnItemClickListener {

    private ListView mListView;
    private ProgressDialog mProgress;
    private Cursor mCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_fix_car_log);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mListView = (ListView) findViewById(android.R.id.list);

        final String carName = getIntent().getStringExtra("carName");

        new AsyncTask<Void, Integer, Integer>() {

            public JSONArray mContent;

            @Override
            protected Integer doInBackground(Void... params) {
                JSONObject[] content = new JSONObject[1];
                try {
                    int result = WebHelper.mobileGetRepaireRecords2(content, carName, 0, 100);
                    if (result == 0) {
                        mContent = content[0].getJSONArray("d");
                        CarSQLiteOpenHelper.insert(FixLog.TABLE, mContent);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mCursor = CarApp.lockDataBase().rawQuery("select * from " + FixLog.TABLE, null);
                return -1;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgress = ProgressDialog.show(ViewFixCarLogActivity.this, getString(R.string.app_name), "正在获取维修日志，请稍等...", false, false);
            }

            @Override
            protected void onPostExecute(Integer integer) {
                super.onPostExecute(integer);
                mProgress.dismiss();
                CursorAdapter adapter = (CursorAdapter) mListView.getAdapter();
                adapter.changeCursor(mCursor);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        mListView.setAdapter(new CursorAdapter(this, null, false) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                return getLayoutInflater().inflate(R.layout.fix_car_log_item, parent, false);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                String reason = cursor.getString(cursor.getColumnIndex(FixLog.Faultreason));
                String time = cursor.getString(cursor.getColumnIndex(FixLog.Time));
                TextView reasonText = (TextView) view.findViewById(R.id.fix_car_log_item_fault_reason);
                TextView timeText = (TextView) view.findViewById(R.id.fix_car_log_item_fix_time);
                reasonText.setText(reason);
                timeText.setText(time);
            }
        });
        mListView.setOnItemClickListener(this);
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

    @Override
    protected void onDestroy() {
        if (mCursor != null) {
            mCursor.close();
        }
        super.onDestroy();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor c = (Cursor) parent.getItemAtPosition(position);
        View detailView = getLayoutInflater().inflate(R.layout.fix_car_log_detail, null);
        TextView employ = (TextView) detailView.findViewById(R.id.fix_car_log_detail_employ);
        employ.setText(c.getString(c.getColumnIndex(FixLog.Repairemployee)));

        TextView reason = (TextView) detailView.findViewById(R.id.fix_car_log_detail_reason);
        reason.setText(c.getString(c.getColumnIndex(FixLog.Faultreason)));

        TextView method = (TextView) detailView.findViewById(R.id.fix_car_log_detail_method);
        method.setText(c.getString(c.getColumnIndex(FixLog.Method)));

        TextView remark = (TextView) detailView.findViewById(R.id.fix_car_log_detail_remark);
        remark.setText(c.getString(c.getColumnIndex(FixLog.Remark)));

        new AlertDialog.Builder(this).setTitle(c.getString(c.getColumnIndex(FixLog.CarName))).setView(detailView).setPositiveButton("确定", null).show();
    }
}
