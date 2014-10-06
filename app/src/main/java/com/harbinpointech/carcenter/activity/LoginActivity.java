
package com.harbinpointech.carcenter.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.harbinpointech.carcenter.R;
import com.harbinpointech.carcenter.data.WebHelper;
import com.harbinpointech.carcenter.util.AsyncTask;
import com.harbinpointech.carcenter.utils.CommonUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * 登陆页面
 */
public class LoginActivity extends BaseActivity {
    public static final String KEY_USER_NAME = "key_user_name";
    public static final String KEY_PWD = "key_pwd";
    private EditText usernameEditText;
    private EditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (WebHelper.hasLogined()) {
            String username = PreferenceManager.getDefaultSharedPreferences(this).getString(KEY_USER_NAME, null);
            String password = PreferenceManager.getDefaultSharedPreferences(this).getString(KEY_PWD, null);
            Intent i = new Intent(LoginActivity.this, MainActivity.class);
            i.putExtra("username", username);
            i.putExtra("password", password);
            startActivity(i);
            finish();
        }
        usernameEditText = (EditText) findViewById(R.id.username);
        passwordEditText = (EditText) findViewById(R.id.password);

        usernameEditText.setText(PreferenceManager.getDefaultSharedPreferences(this).getString(KEY_USER_NAME, null));
        passwordEditText.setText(PreferenceManager.getDefaultSharedPreferences(this).getString(KEY_PWD, null));
        // 如果用户名改变，清空密码
        usernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                passwordEditText.setText(null);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    /**
     * 登陆
     *
     * @param view
     */
    public void login(View view) {
        usernameEditText.setError(null);
        if (!CommonUtils.isNetWorkConnected(this)) {
            Toast.makeText(this, R.string.network_isnot_available, Toast.LENGTH_SHORT).show();
            return;
        }
        final String username = usernameEditText.getText().toString();
        final String password = passwordEditText.getText().toString();
        if (TextUtils.isEmpty(username)) {
            usernameEditText.setError("请输入用户名");
            return;
        }
        {

            AsyncTask<Void, Integer, Integer> task = new AsyncTask<Void, Integer, Integer>() {
                public ProgressDialog mProgress;

                @Override
                protected Integer doInBackground(Void... params) {
                    try {
                        int result =  WebHelper.login(username, password);
                        if (result == 0){
                            JSONObject []p = new JSONObject[1];
                            result = WebHelper.getUser(p, username);
                            if (result == 0){

                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                    return -1;
                }

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    mProgress = new ProgressDialog(LoginActivity.this);
                    mProgress.setCancelable(false);
                    mProgress.setMessage("正在登录...");
                    mProgress.show();
                }

                @Override
                protected void onPostExecute(Integer integer) {
                    super.onPostExecute(integer);

                    if (integer == 0) {
                        PreferenceManager.getDefaultSharedPreferences(LoginActivity.this).edit().putString(KEY_USER_NAME, username).putString(KEY_PWD, password).commit();
//                        PreferenceManager.getDefaultSharedPreferences(LoginActivity.this).getString(KEY_PWD, null));

                        // 如果用户名密码都有，直接进入主页面
                        mProgress.dismiss();
                        Intent i = new Intent(LoginActivity.this, MainActivity.class);
                        i.putExtra("username", username);
                        i.putExtra("password", password);
                        i.putExtra("index", 10);
                        startActivity(i);
                        finish();
                        return;
                    } else {
                        mProgress.dismiss();
                        Toast.makeText(LoginActivity.this, "登录不成功", Toast.LENGTH_SHORT).show();
                    }
                }
            }.execute();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

}
