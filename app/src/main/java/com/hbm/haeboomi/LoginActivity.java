package com.hbm.haeboomi;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity implements View.OnClickListener {
	private final String TAG = "EndHBM_LoginActivity";
	private ProgressDialog pd;
	private Handler[] handler = new Handler[3];
	private DBManager db;
	private DBManager.innerDB innerDB;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		db = new DBManager(this);
		innerDB = new DBManager.innerDB(this);

		setContentView(R.layout.activity_login);

		//각 버튼들을 객체와 연결
		Button btnRegister = (Button) findViewById(R.id.btnRegisterL);
		Button btnLogin = (Button) findViewById(R.id.btnLoginL);

		pd = new ProgressDialog(this);
		pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pd.setMessage("로그인 중입니다");

		handler[0] = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				pd.show();
			}
		};
		handler[1] = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				pd.dismiss();
			}
		};

		//다른 화면을 보여주기 위한 온 클릭 리스너
		btnRegister.setOnClickListener(this);
		btnLogin.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.btnRegisterL:
				finish();
				innerDB.onDestroy();
				startActivity(new Intent(getBaseContext(), RegisterActivity.class));
				break;
			case R.id.btnLoginL:
				new Thread(new Runnable() {
					@Override
					public void run() {
						handlerRun(0);
						String stuNum = ((EditText)findViewById(R.id.txtStuNumberL)).getText().toString();
						String password = ((EditText)findViewById(R.id.txtPasswordL)).getText().toString();

						//디버깅용
						if(MainSplash.DEBUG_MODE) {
							String ip;
							if((ip = innerDB.getData()).split("!").length >= 2) {
								stuNum = ip.split("!")[0];
								password = ip.split("!")[1];
							}
						}

						//사번 : 5자리 / 학번 8자리
						if (stuNum.length() == 5 || stuNum.length() == 8) {
							if (password.length() < 4)
								handlerRun(2, "비밀번호를 4자리 이상 입력해주세요.");
							else {
								if(stuNum.length() == 8)
									db.DBLogin(stuNum, password, "0");	//학생
								else
									db.DBLogin(stuNum, password, "1");	//교수
							}
						}
						else handlerRun(2, "학번 혹은 사번을 입력해주세요.");
						handlerRun(1);
					}
				}).start();
				break;
		}
	}

	public void saveInfo(String stuNum, String password) {
		//Thread에서 이 메소드를 호출하는데 가끔 innerDB가 Create되기 전에 메소드가 불리는 경우가 생긴다.
		//innerDB은 null이 될 수 없기 때문에 초기화가 될 때까지 빈 루프를 돌려준다.
		while(innerDB == null);
		if(innerDB.execSQL("INSERT INTO user VALUES ('" + stuNum + "', '" + password + "')"))
			destroyDB();
	}
	private void destroyDB() {
		if(innerDB != null) {
			innerDB.onDestroy();
			innerDB = null;
		}
	}
	//핸들러의 내용을 실행해주는 메소드
	public void handlerRun(int index) {
		handler[index].sendMessage(handler[index].obtainMessage());
	}
	public void handlerRun(int index, final String msg) {
		handler[index] = new Handler(Looper.getMainLooper()) {
			@Override
			public void handleMessage(Message m) {
				super.handleMessage(m);
				Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
			}
		};
		handler[index].sendMessage(handler[index].obtainMessage());
	}
}
