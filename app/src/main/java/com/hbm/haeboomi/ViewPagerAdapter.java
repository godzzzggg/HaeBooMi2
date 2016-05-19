package com.hbm.haeboomi;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pass.Spass;
import com.samsung.android.sdk.pass.SpassFingerprint;

import java.util.Calendar;


public class ViewPagerAdapter extends PagerAdapter implements View.OnClickListener {
	private final String TAG = "EndHBM_VPAdapter";

    private LayoutInflater minflater;
    private ViewPager viewP;
    private int posi;

    private SpassFingerprint mSpassFingerprint;
    private Spass mSpass;
    private int passindex;
    private boolean isFeatureEnabled;
    private Tab_StudentVPActivity stu_main_activity;

    private DBManager db;

    private int temp =0;

    private SpassFingerprint.IdentifyListener listener = new SpassFingerprint.IdentifyListener() {
        @Override
        public void onFinished(int eventStatus) {
            switch(eventStatus) {
                case SpassFingerprint.STATUS_AUTHENTIFICATION_SUCCESS:  //지문인식 성공
                    stu_main_activity.btStart();
                    //passindex에 해당 지문의 index를 넣는다.
                    passindex = mSpassFingerprint.getIdentifiedFingerprintIndex();
                    db.getData(DBManager.GetTable.BEACON);
                    temp = (int)(Math.random()*4)+1;
                    Log.d("VPAdapter", "" + temp);
                    stu_main_activity.newCalendar(temp);
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
	    Button btnNext = (Button)vw.findViewById(R.id.btnNext);
	    Button btnAttendance = (Button) vw.findViewById(R.id.btnAttendance);
        btnPrev.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        btnAttendance.setOnClickListener(this);

        ((ViewPager)container).addView(vw);
        return vw;
    }
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.btnPrev:
				viewP.setCurrentItem(posi - 1);
				break;
			case R.id.btnNext:
				viewP.setCurrentItem(posi + 1);
				break;
			case R.id.btnAttendance:
				stu_main_activity.btInit();
				if (isFeatureEnabled) {
					mSpassFingerprint.startIdentifyWithDialog(stu_main_activity, listener, false);    //boolean값은 비밀번호 입력창 유무
				}
				else {
					Toast.makeText(stu_main_activity, "지문인식 미지원", Toast.LENGTH_SHORT).show();
				}
				stu_main_activity.btStart();
				attendance();
				break;
		}
	}
	@Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View)object);
    }
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

	private void attendance() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(!stu_main_activity.isCheck_arraysize());
				DBManager.innerDB innerDB = new DBManager.innerDB(stu_main_activity);
				Calendar calendar = Calendar.getInstance();
				String[] beacon = db.getData(DBManager.GetTable.BEACON).split("!");
				String bc_mac = beacon[0];
				String bc_classno = beacon[1];
				String id = innerDB.getData("select * from user").split("!")[0];
				String[] days = {"일", "월", "화", "수", "목", "금", "토"};

				if(stu_main_activity.isEqual(bc_mac)) {
					//if(db.attendance(id, "월", "14:00:00", bc_classno)) {
					String day = days[calendar.get(Calendar.DAY_OF_WEEK) - 1];  //1(일)~7(토)의 결과가 나오므로 -1해줌
					int minute = calendar.get(Calendar.MINUTE);
					int hour = calendar.get(Calendar.HOUR);
					if(minute != 0) hour++;
					String time = hour + ":00:00";
					if(db.attendance(id, day, time, bc_classno)) {
						Log.d(TAG, "출석체크 완료");
					}
				}
			}
		}).start();
	}
}