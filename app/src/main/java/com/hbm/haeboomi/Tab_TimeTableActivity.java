package com.hbm.haeboomi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Tab_TimeTableActivity extends Activity {
	private final String TAG = "EndHBM_Tab_TimeTable";
	private WebView web;
	private String source;
	private Handler handler;
	private BackPressCloseHandler bpch;

	private DBManager db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tab_time_table);

		bpch = new BackPressCloseHandler(this);
		db = new DBManager(this);
		DBManager.innerDB innerDB = new DBManager.innerDB(this);

		String[] idpw = innerDB.getData().split("!");
		innerDB.onDestroy();

		final String stuNum = idpw[0];

		//UI를 건드리는 코드는 스레드 내부가 아닌 핸들러로 처리해야 한다.
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				TextView txt = (TextView)findViewById(R.id.txtWait);
				txt.setVisibility(View.INVISIBLE);
				//WebView를 source의 코드로 불러옴, UTF-8로 인코딩
				web.loadData(source, "text/html; charset=UTF-8;", null);
				web.setVisibility(View.VISIBLE);
			}
		};

		web = (WebView)findViewById(R.id.timeTable);
		//줌이 가능하게함
		web.getSettings().setBuiltInZoomControls(true);
		//실행하면 화면에 꽉 차게 보여주는코드 / 1로 설정해야 시작하자마자 꽉차게보임
		web.getSettings().setUseWideViewPort(true);
		web.setInitialScale(1);

		new Thread() {
			@Override
			public void run() {
				source = db.SuccessFail(DBManager.SERVER_ADDRESS + "timetable.php?id=" + stuNum);
				//web.loadData(source, "text/html; charset=UTF-8;", null); 코드를 핸들러에서 실행함
				handler.sendMessage(handler.obtainMessage());
			}
		}.start();
	}

	@Override
	public void onBackPressed() {
		bpch.onBackPressed();
	}
}
