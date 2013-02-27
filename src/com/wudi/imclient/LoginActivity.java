package com.wudi.imclient;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class LoginActivity extends Activity {
	/** Called when the activity is first created. */
	private EditText login_username, login_pwd;
	private LinearLayout login_layout1, login_layout2;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		login_layout1 = (LinearLayout) findViewById(R.id.login_layout1);
		login_layout2 = (LinearLayout) findViewById(R.id.login_layout2);
		login_username = (EditText) findViewById(R.id.login_username);
		login_pwd = (EditText) findViewById(R.id.login_pwd);
		Button login_submit = (Button) findViewById(R.id.login_btsubmit);
		final IMData imData=(IMData) this.getApplication();
		final XMPPConnection con=imData.getConnection();
		login_submit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final String username = login_username.getText().toString();
				if (username.equals("")) {
					Toast.makeText(LoginActivity.this, "用户名不能为空",
							Toast.LENGTH_LONG).show();
				} else {
					final String password = login_pwd.getText().toString();
					new Thread() {
						@Override
						public void run() {
							handler.sendEmptyMessage(1);
							try {
								con.connect();
								con.login(username, password, "IMClient");
								Log.i("info", "Login succeed");
								imData.setUserName(username);
								DBOpenHelper helper=imData.getHelper();
								helper.createTable(username, imData.getDb());
								Intent intent = new Intent(LoginActivity.this,
										RosterActivity.class);
								startActivity(intent);
								LoginActivity.this.finish();
							} catch (XMPPException e) {
								con.disconnect();
								Log.e("error", e.getMessage());
								Message msg = new Message();
								msg.what = 2;
								msg.obj = e.getMessage();
								handler.sendMessage(msg);
							}
						}

					}.start();

				}
			}
		});
	}

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				login_layout2.setVisibility(View.GONE);
				login_layout1.setVisibility(View.VISIBLE);
				break;
			case 2:
				login_layout1.setVisibility(View.GONE);
				login_layout2.setVisibility(View.VISIBLE);
				String message = (String) msg.obj;
				if (message.contains("authentication")) {
					Toast.makeText(LoginActivity.this, "用户名或密码错误",
							Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(LoginActivity.this, message,
							Toast.LENGTH_LONG).show();
				}
				break;
			default:
				break;
			}
		}

	};
}