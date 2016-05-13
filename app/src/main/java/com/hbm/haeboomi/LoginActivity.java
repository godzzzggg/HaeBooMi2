package com.hbm.haeboomi;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class LoginActivity extends Activity implements View.OnClickListener {
	private final String TAG = "EndHBM_LoginActivity";
	private ProgressDialog pd;
	private Handler[] handler = new Handler[2];
	private DBManager db;
	private DBManager.innerDB innerDB;

	private String[] data;
	private String id, pw;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		//배경색을 흰색으로 하려고 만든 객체
		RelativeLayout mRelativeLayout = (RelativeLayout) findViewById(R.id.loginlayout);
		mRelativeLayout.setBackgroundColor(Color.WHITE);

		db = new DBManager(this);
		innerDB = new DBManager.innerDB(this);

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
				String stuNum = ((EditText)findViewById(R.id.txtStuNumberL)).getText().toString();
				String password = ((EditText)findViewById(R.id.txtPasswordL)).getText().toString();

				//사번 : 5자리 / 학번 8자리
				if (stuNum.length() == 5 || stuNum.length() == 8) {
					if (password.length() < 4)
						Toast.makeText(LoginActivity.this, "비밀번호를 4자리 이상 입력해주세요.", Toast.LENGTH_SHORT).show();
					else {
						handlerRun(0);
						innerDB.execSQL("INSERT INTO user VALUES (" + stuNum + ", " + password + ")");
						innerDB.onDestroy();
						if(stuNum.length() == 8)
							db.DBLogin(stuNum, password, "0");	//학생
						else
							db.DBLogin(stuNum, password, "1");	//교수
					}
				}
				else Toast.makeText(LoginActivity.this, "학번 혹은 사번을 입력해주세요.", Toast.LENGTH_SHORT).show();
				break;
		}
	}

	//핸들러의 내용을 실행해주는 메소드
	public void handlerRun(int index) {
		handler[index].sendMessage(handler[index].obtainMessage());
	}
}
