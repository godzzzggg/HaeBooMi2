package com.hbm.haeboomi;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pass.Spass;
import com.samsung.android.sdk.pass.SpassFingerprint;


public class ViewPagerAdapter extends PagerAdapter {
    private LayoutInflater minflater;
    private ViewPager viewP;
    private int posi;

    private SpassFingerprint mSpassFingerprint;
    private Spass mSpass;
    private int passindex;
    private boolean isFeatureEnabled;

    private Tab_StudentVPActivity stu_main_activity;
    //private Context context;

    private DBManager db;

    private SpassFingerprint.IdentifyListener listener = new SpassFingerprint.IdentifyListener() {
        @Override
        public void onFinished(int eventStatus) {
            switch(eventStatus) {
                case SpassFingerprint.STATUS_AUTHENTIFICATION_SUCCESS:  //지문인식 성공
                    stu_main_activity.btStart();
                    //passindex에 해당 지문의 index를 넣는다.
                    passindex = mSpassFingerprint.getIdentifiedFingerprintIndex();
                    db.getData("getdata.php", DBManager.GetTable.BEACON);
                    break;
                case SpassFingerprint.STATUS_AUTHENTIFICATION_PASSWORD_SUCCESS: //지문대신 비밀번호를 입력해서 통과
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

        try { mSpass.initialize(stu_main_activity); } //지문인식을 위한 객체 초기화
        catch (SsdkUnsupportedException e) {}   //SsdkUnsupportedException을 항상 검사해야함 삼성 sdk를 지원하지 않을 때 발생하는것 같다.
        catch (UnsupportedOperationException e) {}  //운영체제가 지원하지 않을 때
        finally {
            isFeatureEnabled = mSpass.isFeatureEnabled(Spass.DEVICE_FINGERPRINT);
            //지문인식을 지원하는 핸드폰인지 확인
            if (isFeatureEnabled)
                mSpassFingerprint = new SpassFingerprint(stu_main_activity);
        }
    }

    public ViewPagerAdapter(Tab_StudentVPActivity activity, ViewPager v){
        super();
        stu_main_activity = activity;
        minflater = LayoutInflater.from(stu_main_activity);
        viewP = v;
        posi = 0;
        db = new DBManager(activity);
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
                    mSpassFingerprint.startIdentifyWithDialog(stu_main_activity, listener, false);    //boolean값은 비밀번호 입력창 유무
                }
                else {
                    Toast.makeText(stu_main_activity, "지문인식 미지원", Toast.LENGTH_SHORT).show();
                }
                stu_main_activity.btStart();

                db.getData("getdata.php", DBManager.GetTable.BEACON);
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
