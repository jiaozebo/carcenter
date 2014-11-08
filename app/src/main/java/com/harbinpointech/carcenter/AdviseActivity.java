package com.harbinpointech.carcenter;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.harbinpointech.carcenter.data.WebHelper;

import org.json.JSONException;

import java.io.IOException;


public class AdviseActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advise);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_advise, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void onSendAdvise(View view) {
        EditText editAdvise = (EditText) findViewById(R.id.edit_advise);
        final String advise = editAdvise.getText().toString();
        if (TextUtils.isEmpty(advise)) {
            Toast.makeText(this, "请输入反馈意见", Toast.LENGTH_SHORT).show();
            return;
        }
        new AsyncTask<Void, Integer, Integer>() {
            public ProgressDialog mProgress;

            @Override
            protected Integer doInBackground(Void... params) {
                try {
                    return WebHelper.addFanKui(advise);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return -1;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgress = ProgressDialog.show(AdviseActivity.this, getString(R.string.app_name), "请稍候...", false, false);
            }

            @Override
            protected void onPostExecute(Integer integer) {
                super.onPostExecute(integer);
                mProgress.dismiss();
                if (integer == 0) {
                    finish();
                    Toast.makeText(AdviseActivity.this, "发送反馈意见成功。", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(AdviseActivity.this, "发送反馈意见未成功。", Toast.LENGTH_SHORT).show();
                return;
            }
        }.execute();
    }
}
