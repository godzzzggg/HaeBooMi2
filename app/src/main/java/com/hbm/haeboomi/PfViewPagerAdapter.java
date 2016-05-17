package com.hbm.haeboomi;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pass.Spass;
import com.samsung.android.sdk.pass.SpassFingerprint;


public class PfViewPagerAdapter extends PagerAdapter {
    private LayoutInflater minflater;
    private ViewPager viewP;
    private int posi;

    private SpassFingerprint mSpassFingerprint;
    private Spass mSpass;
    private int passindex;
    private boolean isFeatureEnabled;
    private Context context;
    private Tab_StudentVPActivity stu_main_activity;

    private SpassFingerprint.IdentifyListener listener = new SpassFingerprint.IdentifyListener() {
        @Override
        public void onFinished(int eventStatus) {
            if (eventStatus == SpassFingerprint.STATUS_AUTHENTIFICATION_SUCCESS) {
                //지문인식 성공
                //passindex에 해당 지문의 index를 넣는다.
                passindex = mSpassFingerprint.getIdentifiedFingerprintIndex();

            }
            else if (eventStatus == SpassFingerprint.STATUS_AUTHENTIFICATION_PASSWORD_SUCCESS) {
                //지문대신 비밀번호를 입력해서 통과
            }
            else {
                /*  실패사유
                 STATUS_TIMEOUT_FAILED
                 STATUS_USER_CANCELLED
                 STATUS_AUTHENTIFICATION_FAILE
                 STATUS_QUALITY_FAILED
                 STATUS_USER_CANCELLED_BY_TOUCH_OUTSIDE
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
    /*
        public ViewPagerAdapter(Context context, ViewPager v){
            super();
            minflater = LayoutInflater.from(context);
            this.context = context;
            viewP = v;
            posi = 0;
        }*/
    public PfViewPagerAdapter(Tab_StudentVPActivity activity, ViewPager v){
        super();
        minflater = LayoutInflater.from(activity);
        context = activity;
        stu_main_activity = activity;
        viewP = v;
        posi = 0;
    }
    @Override
    public int getCount() {
        return Integer.MAX_VALUE;
    }

    public int getPosition(){ return posi; }
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View vw = minflater.inflate(R.layout.activity_vpager_attendance, null);

        passInit();

        final int p = position;
        this.posi = p;

        Button btnPrev = (Button)vw.findViewById(R.id.btnPrev);
        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewP.setCurrentItem(p - 1);
            }
        });

        Button btnNext = (Button)vw.findViewById(R.id.btnNext);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewP.setCurrentItem(p + 1);
            }
        });

        Button btnAttendance = (Button) vw.findViewById(R.id.btnAttendance);
        btnAttendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stu_main_activity.btInit();
                if (isFeatureEnabled) {
                    mSpassFingerprint.startIdentifyWithDialog(context, listener, false);    //boolean값은 비밀번호 입력창 유무
                }
                else
                    Toast.makeText(context, "지문인식 미지원", Toast.LENGTH_SHORT).show();
                stu_main_activity.btStart();
            }
        });
        ((ViewPager)container).addView(vw);
        return vw;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View)object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
