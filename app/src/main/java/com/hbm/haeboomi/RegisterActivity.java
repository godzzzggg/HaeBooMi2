package com.hbm.haeboomi;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pass.Spass;
import com.samsung.android.sdk.pass.SpassFingerprint;

public class RegisterActivity extends Activity implements View.OnClickListener {
	private SpassFingerprint mSpassFingerprint;
	private Spass mSpass;
	private boolean isFeatureEnabled;
	private int passindex = -1;

	private ProgressDialog pd;
	private Handler[] handler = new Handler[3];

	//아래부터 DB
	private DBManager db;
	private DBManager.innerDB innerDB;

	private SpassFingerprint.IdentifyListener listener = new SpassFingerprint.IdentifyListener() {
		@Override
		public void onFinished(int eventStatus) {
			switch(eventStatus) {
				case SpassFingerprint.STATUS_AUTHENTIFICATION_SUCCESS:	//지문인식 성공
					//passindex에 해당 지문의 index를 넣는다.
					passindex = mSpassFingerprint.getIdentifiedFingerprintIndex();
					break;
				case SpassFingerprint.STATUS_AUTHENTIFICATION_PASSWORD_SUCCESS:	//지문대신 비밀번호를 입력해서 통과
					break;
				//실패사유
				case SpassFingerprint.STATUS_TIMEOUT_FAILED:
				case SpassFingerprint.STATUS_USER_CANCELLED:
				case SpassFingerprint.STATUS_AUTHENTIFICATION_FAILED:
				case SpassFingerprint.STATUS_QUALITY_FAILED:
				case SpassFingerprint.STATUS_USER_CANCELLED_BY_TOUCH_OUTSIDE:
				case SpassFingerprint.STATUS_BUTTON_PRESSED:
				case SpassFingerprint.STATUS_OPERATION_DENIED:
					break;
			}
		}
		@Override
		public void onReady() { }
		@Override
		public void onStarted() { }
		@Override
		public void onCompleted() { }
	};
	private void passInit() {
		mSpass = new Spass();

		try { mSpass.initialize(this); }	//지문인식을 위한 객체 초기화
		catch (SsdkUnsupportedException e) {}	//SsdkUnsupportedException을 항상 검사해야함 삼성 sdk를 지원하지 않을 때 발생하는것 같다.
		catch (UnsupportedOperationException e) {}	//운영체제가 지원하지 않을 때
		finally {
			isFeatureEnabled = mSpass.isFeatureEnabled(Spass.DEVICE_FINGERPRINT);
			//지문인식을 지원하는 핸드폰인지 확인
			if (isFeatureEnabled)
				mSpassFingerprint = new SpassFingerprint(this);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		//각 버튼, 입력한 값들을 처리하기위한 객체
		Button btnPass = (Button)findViewById(R.id.btnPassR);
		Button btnOk = (Button)findViewById(R.id.btnOkR);
		btnPass.setOnClickListener(this);
		btnOk.setOnClickListener(this);

		pd = new ProgressDialog(this);
		pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pd.setMessage("회원 가입 중입니다");

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

		db = new DBManager(this);
		innerDB = new DBManager.innerDB(this);

		//지문인식을 지원하는 핸드폰인지 확인
		passInit();
		if (isFeatureEnabled) {
			btnPass.setVisibility(View.VISIBLE);
		} else {
			btnPass.setVisibility(View.INVISIBLE);
			Toast.makeText(this, "지문인식을 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.btnPassR:
				//지문 등록 버튼을 눌렀을 때 이벤트 처리
				//삼성 지문인식 Dialog를 띄워준다.
				mSpassFingerprint.startIdentifyWithDialog(this, listener, false);	//boolean값은 비밀번호 입력창 유무
				break;
			case R.id.btnOkR:
				new Thread(new Runnable() {
					@Override
					public void run() {
						handlerRun(0);

						String stuNum = ((EditText)findViewById(R.id.txtStuNumberR)).getText().toString();
						String password = ((EditText)findViewById(R.id.txtPasswordR)).getText().toString();
						String pwdCompare = ((EditText)findViewById(R.id.txtPwdCompareR)).getText().toString();

						//디버깅용
						if(MainSplash.DEBUG_MODE) {
							stuNum = "20115169";
							password = pwdCompare = "1234";
						}
						//사번 : 5자리, 학번 8자리
						if (stuNum.length() == 5 || stuNum.length() == 8) {
							if (password.length() < 4)
								handlerRun(2, "비밀번호를 4자리 이상 입력해주세요.");
							else if (!password.equals(pwdCompare))
								handlerRun(2, "비밀번호가 일치하지 않습니다.");
							else {
								boolean reg;
								if(stuNum.length() == 8)
									reg = db.DBRegister(stuNum, password, passindex, 0);	//학생
								else
									reg = db.DBRegister(stuNum, password, passindex, 1);	//교수

								if(reg) {   //가입에 성공하면(DB에 존재하지 않으면)
									if(innerDB.execSQL("INSERT INTO user VALUES ('" + stuNum + "', '" + password + "')"))
										innerDB.onDestroy();

									db.putSchedule();
									handlerRun(1);

									finish();
									if(stuNum.length() == 8)
										startActivity(new Intent(RegisterActivity.this, StudentMainActivity.class));
									else
										startActivity(new Intent(RegisterActivity.this, ProfessorMainActivity.class));
								}
								else handlerRun(2, "학번/사번 및 비밀번호를 확인 해 주세요");
							}
							handlerRun(1);
						}
						else {
							handlerRun(1);
							handlerRun(2, "학번 혹은 사번을 입력해주세요.");
						}
					}
				}).start();
				break;
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
				Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_SHORT).show();
			}
		};
		handler[index].sendMessage(handler[index].obtainMessage());
	}

	@Override
	public void onBackPressed() {
		finish();
		startActivity(new Intent(this, LoginActivity.class));
	}
}