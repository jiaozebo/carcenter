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
package com.harbinpointech.carcenter.activity;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMGroupManager;
import com.easemob.exceptions.EaseMobException;
import com.harbinpointech.carcenter.Constant;
import com.harbinpointech.carcenter.DemoApplication;
import com.harbinpointech.carcenter.R;
import com.harbinpointech.carcenter.db.UserDao;
import com.harbinpointech.carcenter.domain.User;
import com.harbinpointech.carcenter.utils.CommonUtils;
import com.harbinpointech.carcenter.util.AsyncTask;
import com.easemob.util.HanziToPinyin;
import com.harbinpointech.carcenter.data.WebHelper;

import org.json.JSONException;

/**
 * 登陆页面
 * 
 */
public class LoginActivity extends BaseActivity {
	private EditText usernameEditText;
	private EditText passwordEditText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		usernameEditText = (EditText) findViewById(R.id.username);
		passwordEditText = (EditText) findViewById(R.id.password);
		// 如果用户名密码都有，直接进入主页面
		if (DemoApplication.getInstance().getUserName() != null && DemoApplication.getInstance().getPassword() != null) {
			startActivity(new Intent(this, MainActivity.class));
			finish();
		}
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
		if (!CommonUtils.isNetWorkConnected(this)) {
			Toast.makeText(this, R.string.network_isnot_available, Toast.LENGTH_SHORT).show();
			return;
		}
		final String username = usernameEditText.getText().toString();
		final String password = passwordEditText.getText().toString();
		if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {

            AsyncTask<Void, Integer, Integer> task = new AsyncTask<Void, Integer, Integer>() {
                public ProgressDialog mProgress;

                @Override
                protected Integer doInBackground(Void... params) {
                    try {

                        EMChatManager.getInstance().createAccountOnServer(username, password);
                        return WebHelper.login(username, password);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (EaseMobException e) {
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

                    if (integer == 0){

                        // 调用sdk登陆方法登陆聊天服务器
                        EMChatManager.getInstance().login(username, password, new EMCallBack() {

                            @Override
                            public void onSuccess() {
                                // 登陆成功，保存用户名密码
                                DemoApplication.getInstance().setUserName(username);
                                DemoApplication.getInstance().setPassword(password);
                                mProgress.dismiss();
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
//
                            }

                            @Override
                            public void onProgress(int progress, final String status) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProgress.setMessage(status);
                                    }
                                });
                            }

                            @Override
                            public void onError(int code, final String message) {
                                mProgress.dismiss();
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        if (message.indexOf("not support the capital letters") != -1) {
                                            Toast.makeText(getApplicationContext(), "用户名不支持大写字母", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getApplicationContext(), "登录失败: " + message, Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });
                            }
                        });
                    }else{
                        mProgress.dismiss();
                        Toast.makeText(LoginActivity.this, "登录不成功",Toast.LENGTH_SHORT).show();
                    }
                }
            }.execute();



		}
	}

	/**
	 * 注册
	 * 
	 * @param view
	 */
	public void register(View view) {
		startActivityForResult(new Intent(this, RegisterActivity.class), 0);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (DemoApplication.getInstance().getUserName() != null) {
			usernameEditText.setText(DemoApplication.getInstance().getUserName());
		}
	}

}