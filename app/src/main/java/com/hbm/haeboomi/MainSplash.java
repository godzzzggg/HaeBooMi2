package com.hbm.haeboomi;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

public class MainSplash extends Activity {
	private DBManager db;
	private DBManager.innerDB innerDB;

	private String id, pw;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_splash);

		final TextView splash_text = (TextView)findViewById(R.id.splash_text);
		splash_text.setTextColor(Color.LTGRAY);
		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				db = new DBManager(MainSplash.this);
				innerDB = new DBManager.innerDB(MainSplash.this);

				String[] data = innerDB.getData("select id, password from user").split("!");
				if(data.length != 1) {	//null인 경우를 제외
					splash_text.setText("로그인 중입니다.");
					id = data[0];
					pw = data[1];

					innerDB.onDestroy();
					startActivity(new Intent(MainSplash.this, LoginActivity.class));
					/*
					if(id.length() == 8)
						db.DBLogin(id, pw, "0");	//학생
					else
						db.DBLogin(id, pw, "1");	//교수*/
				}
				else {
					innerDB.onDestroy();
					finish();
					startActivity(new Intent(MainSplash.this, LoginActivity.class));
				}
			}
		};
		handler.sendEmptyMessageDelayed(0, 1000);
	}
}
