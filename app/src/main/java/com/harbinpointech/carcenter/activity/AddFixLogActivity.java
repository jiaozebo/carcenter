package com.harbinpointech.carcenter.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.harbinpointech.carcenter.R;
import com.harbinpointech.carcenter.data.WebHelper;
import com.harbinpointech.carcenter.util.AsyncTask;

import org.json.JSONException;

import java.io.IOException;

public class AddFixLogActivity extends Activity {

    public static final String KEY_CAR_NAME = "key_car_name";
    private String mCarName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_fix_log);
        mCarName = getIntent().getStringExtra(KEY_CAR_NAME);
        if (TextUtils.isEmpty(mCarName)) {
            finish();
            return;
        }
    }

    public void onAddLog(View view) {
        EditText engineer = (EditText) findViewById(R.id.et_fixed_engineer);
        final String engineerText = engineer.getText().toString();
        if (engineerText.length() == 0) {
            Toast.makeText(this, "请输入工程师名字", Toast.LENGTH_SHORT).show();
            engineer.requestFocus();
            return;
        }

        final EditText error_reason = (EditText) findViewById(R.id.et_fixed_error_reason);
        final String reasonText = error_reason.getText().toString();
        if (reasonText.length() == 0) {
            Toast.makeText(this, "请输入故障原因", Toast.LENGTH_SHORT).show();
            error_reason.requestFocus();
            return;
        }

        EditText et_fixed_method = (EditText) findViewById(R.id.et_fixed_method);
        final String methodText = et_fixed_method.getText().toString();
        if (methodText.length() == 0) {
            Toast.makeText(this, "请输入维修方法", Toast.LENGTH_SHORT).show();
            et_fixed_method.requestFocus();
            return;
        }

        EditText et_fixed_remark = (EditText) findViewById(R.id.et_fixed_remark);
        final String remarkText = et_fixed_remark.getText().toString();

        EditText et_fixed_summary = (EditText) findViewById(R.id.et_fixed_remark);
        final String summaryText = et_fixed_summary.getText().toString();

        new AsyncTask<Void, Integer, Integer>() {
            ProgressDialog mDlg;

            @Override
            protected Integer doInBackground(Void... params) {
                try {
                    return WebHelper.addFixLog(mCarName, engineerText, reasonText, methodText, remarkText, summaryText);
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
                mDlg = ProgressDialog.show(AddFixLogActivity.this, getString(R.string.app_name), "请稍候...", true, true);
            }

            @Override
            protected void onPostExecute(Integer integer) {
                super.onPostExecute(integer);
                mDlg.dismiss();
                if (integer == 0) {
                    setResult(integer);
                    finish();
                } else {
                    Toast.makeText(AddFixLogActivity.this, "添加日志未成功", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }
}
