package com.hbm.haeboomi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pass.Spass;
import com.samsung.android.sdk.pass.SpassFingerprint;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends Activity implements View.OnClickListener {
    private SpassFingerprint mSpassFingerprint;
    private Spass mSpass;
    private boolean isFeatureEnabled;
    private int passindex = -1;

    private Context context = RegisterActivity.this;

    //아래부터 DB
    private DBManager db;
    public String StuNumber, Password;

    private SpassFingerprint.IdentifyListener listener = new SpassFingerprint.IdentifyListener() {
        @Override
        public void onFinished(int eventStatus) {
            if (eventStatus == SpassFingerprint.STATUS_AUTHENTIFICATION_SUCCESS) {
                //지문인식 성공
                //passindex에 해당 지문의 index를 넣는다.
                passindex = mSpassFingerprint.getIdentifiedFingerprintIndex();

            } else if (eventStatus == SpassFingerprint.STATUS_AUTHENTIFICATION_PASSWORD_SUCCESS) {
                //지문대신 비밀번호를 입력해서 통과
            } else {
                /*  실패사유
                switch(eventStatus)
                {
                case SpassFingerprint.STATUS_USER_CANCELLED:
                case SpassFingerprint.STATUS_USER_CANCELLED_BY_TOUCH_OUTSIDE:
                break;
                }
                /*
                 STATUS_TIMEOUT_FAILED
                 STATUS_AUTHENTIFICATION_FAILE
                 STATUS_QUALITY_FAILED
                 STATUS_BUTTON_PRESSED
                 STATUS_OPERATION_DENIED
                */
            }
        }

        @Override
        public void onReady() {
            // It is called when fingerprint identification is ready after
            // startIdentify() is called.
        }

        @Override
        public void onStarted() {
            // It is called when the user touches the fingerprint sensor after
            // startIdentify() is called.
        }

        @Override
        public void onCompleted() {
            //It is called when identify request is completed.
        }
    };

    private void passInit() {
        mSpass = new Spass();

        try { mSpass.initialize(context); } //지문인식을 위한 객체 초기화
        catch (SsdkUnsupportedException e) {}   //SsdkUnsupportedException을 항상 검사해야함 삼성 sdk를 지원하지 않을 때 발생하는것 같다.
        catch (UnsupportedOperationException e) {}  //운영체제가 지원하지 않을 때
        finally {
            isFeatureEnabled = mSpass.isFeatureEnabled(Spass.DEVICE_FINGERPRINT);
            //지문인식을 지원하는 핸드폰인지 확인
            if (isFeatureEnabled)
                mSpassFingerprint = new SpassFingerprint(context);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //배경색을 흰색으로 하려고 만든 객체
        RelativeLayout mRelativeLayout = (RelativeLayout)findViewById(R.id.registerlayout);
        mRelativeLayout.setBackgroundColor(Color.WHITE);

        //각 버튼, 입력한 값들을 처리하기위한 객체
        Button btnPass = (Button)findViewById(R.id.btnPassR);
        Button btnOk = (Button)findViewById(R.id.btnOkR);
        btnPass.setOnClickListener(this);
        btnOk.setOnClickListener(this);

        //url.openStream() 메소드를 실행할 때 필요
        //다음 문장이 없다면 NetworkOnMainThreadException 이 발생한다.
        if(Build.VERSION.SDK_INT > 9){
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        //db
        db = new DBManager(this);

        //지문인식을 지원하는 핸드폰인지 확인
        passInit();
        if (isFeatureEnabled) {
            btnPass.setVisibility(View.VISIBLE);
        } else {
            btnPass.setVisibility(View.INVISIBLE);
            Toast.makeText(context, "지문인식을 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnPassR:
                //지문 등록 버튼을 눌렀을 때 이벤트 처리
                //삼성 지문인식 Dialog를 띄워준다.
                mSpassFingerprint.startIdentifyWithDialog(context, listener, false);    //boolean값은 비밀번호 입력창 유무
                break;
            case R.id.btnOkR:
                EditText txtStuNumber = (EditText)findViewById(R.id.txtStuNumberR);
                EditText txtPassword = (EditText)findViewById(R.id.txtPasswordR);
                EditText txtPwdCompare = (EditText)findViewById(R.id.txtPwdCompareR);

                //사번 : 5자리, 학번 8자리
                if (txtStuNumber.length() == 5 || txtStuNumber.length() == 8) {
                    if (txtPassword.length() < 4)
                        Toast.makeText(context, "비밀번호를 4자리 이상 입력해주세요.", Toast.LENGTH_SHORT).show();
                    else if (!txtPassword.getText().toString().equals(txtPwdCompare.getText().toString()))
                        Toast.makeText(context, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                    else {   //
                        StuNumber = txtStuNumber.getText().toString();
                        Password = txtPassword.getText().toString();
                        if(txtStuNumber.length() == 8) {
                            db.DBRegister(StuNumber, Password, passindex, 0);   //학생
                        }
                        else {
                            db.DBRegister(StuNumber, Password, passindex, 1);   //교수
                        }
                        finish();
                        startActivity(new Intent(getBaseContext(), StudentMainActivity.class));
                    }
                }
                else Toast.makeText(context, "학번 혹은 사번을 입력해주세요.", Toast.LENGTH_SHORT).show();
                break;
        }
    }

}