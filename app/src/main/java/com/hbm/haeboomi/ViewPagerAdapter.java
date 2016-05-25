package com.hbm.haeboomi;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pass.Spass;
import com.samsung.android.sdk.pass.SpassFingerprint;

import java.util.Calendar;


public class ViewPagerAdapter extends PagerAdapter implements View.OnClickListener {
	private final String TAG = "EndHBM_VPAdapter";

    private LayoutInflater minflater;
    private ViewPager viewP;
	private SparseArray< View > views = new SparseArray<>();

	private int COUNT;
	private boolean isZero = false;

    private SpassFingerprint mSpassFingerprint;
    private Spass mSpass;
    private int passindex;
    private boolean isFeatureEnabled;
    private Tab_StudentVPActivity stu_main_activity;

    private DBManager db;
	private DBManager.innerDB innerDB;
	private String[] now;
	private int hour;
	private int minute;
	private String[] schedule;
	private String[] days = {"일", "월", "화", "수", "목", "금", "토"};
	private String[] days_en = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

    private SpassFingerprint.IdentifyListener listener = new SpassFingerprint.IdentifyListener() {
        @Override
        public void onFinished(int eventStatus) {
            switch(eventStatus) {
                case SpassFingerprint.STATUS_AUTHENTIFICATION_SUCCESS:  //지문인식 성공
                    stu_main_activity.btStart();
                    //passindex에 해당 지문의 index를 넣는다.
                    passindex = mSpassFingerprint.getIdentifiedFingerprintIndex();
                    String pass = db.getSelectData("pass", "student", "id = " + innerDB.getData().split("!")[0], DBManager.GetTable.STUDENT);
	                if(pass.equals(passindex)) {
		                attendance("2");
		                stu_main_activity.newCalendar((int)(Math.random() * 4));
	                }
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
        db = new DBManager(activity);
	    innerDB = new DBManager.innerDB(stu_main_activity);
	    passInit();
	    InitDB();
    }

	public void setCount(int count) {
		if(count == 0) {
			isZero = true;
			count++;
		}
		COUNT = count;
	}
    @Override
    public int getCount() {
        return COUNT;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final View vw = minflater.inflate(R.layout.activity_vpager_attendance, null);

	    Button btnPrev = (Button)vw.findViewById(R.id.btnPrev);
	    Button btnNext = (Button)vw.findViewById(R.id.btnNext);
	    Button btnAttendance = (Button) vw.findViewById(R.id.btnAttendance);
	    if(COUNT == 1) {
		    btnPrev.setVisibility(View.INVISIBLE);
		    btnNext.setVisibility(View.INVISIBLE);
		    if(isZero)
			    btnAttendance.setVisibility(View.INVISIBLE);
	    }
	    btnPrev.setOnClickListener(ViewPagerAdapter.this);
	    btnNext.setOnClickListener(ViewPagerAdapter.this);
	    btnAttendance.setOnClickListener(ViewPagerAdapter.this);

        container.addView(vw);
	    views.put(position, vw);
	    notifydata();
        return vw;
    }

	@Override
	public void onClick(final View v) {
		switch(v.getId()) {
			case R.id.btnPrev:
				new Thread(new Runnable() {
					@Override
					public void run() {
						stu_main_activity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								viewP.setCurrentItem(viewP.getCurrentItem() - 1);
								if(viewP.getCurrentItem() == 0)
									v.setVisibility(View.INVISIBLE);
								else if(viewP.getCurrentItem() < COUNT - 1)
									((Button)v.findViewById(R.id.btnNext)).setVisibility(View.VISIBLE);
							}
						});
					}
				}).start();
				break;
			case R.id.btnNext:
				new Thread(new Runnable() {
					@Override
					public void run() {
						stu_main_activity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								viewP.setCurrentItem(viewP.getCurrentItem() + 1);
								if(viewP.getCurrentItem() == COUNT - 1)
									v.setVisibility(View.INVISIBLE);
								else if(viewP.getCurrentItem() > 0)
									((Button)v.findViewById(R.id.btnPrev)).setVisibility(View.VISIBLE);
							}
						});
					}
				}).start();
				break;
			case R.id.btnAttendance:
				new Thread(new Runnable() {
					@Override
					public void run() {
						stu_main_activity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								stu_main_activity.btInit();
								if (isFeatureEnabled) {
									mSpassFingerprint.startIdentifyWithDialog(stu_main_activity, listener, false);    //boolean값은 비밀번호 입력창 유무
								} else {
									Toast.makeText(stu_main_activity, "지문인식 미지원", Toast.LENGTH_SHORT).show();
									attendance("3");    //임시 출석
								}
								stu_main_activity.btStart();
							}
						});
					}
				}).start();
				break;
		}
	}

	@Override
    public void destroyItem(ViewGroup container, int position, Object object) {
		View v = (View)object;
        container.removeView(v);
		views.remove(viewP.getCurrentItem());
		v = null;
    }
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}

	private void InitDB() {
		DBManager.innerDB innerDB = new DBManager.innerDB(stu_main_activity);

		String[] idpw = innerDB.getData().split("!");
		innerDB.onDestroy();

		now = db.nowTime();
		int day = 0;
		while(day < days_en.length)
			if(days_en[day++].equalsIgnoreCase(now[2]))
				break;

		String[] temp_schedule = db.getSelectData("cname, building, classno, time", "st_schedule", "id = " + idpw[0] + " and day = " + (--day), DBManager.GetTable.ST_SCHEDULE).split("!");
		schedule = new String[temp_schedule.length / 8];

		for(int i = 0, j = 0; i < temp_schedule.length; i++) {
			if(!temp_schedule[i].equalsIgnoreCase("null") && !temp_schedule[i].equalsIgnoreCase("\n")) {
				temp_schedule[i] = temp_schedule[i].replaceFirst("\\n", "");
				schedule[j++] = temp_schedule[i++] + "/" + temp_schedule[i++] + "/" + temp_schedule[i++] + "/" + temp_schedule[i];
			}
		}
	}
	private void notifydata() {
		for(int i = 0; i < views.size(); i++) {
			int key = views.keyAt(i);
			String[] data = schedule[key].split("/");
			View view = views.get(key);

			TextView txtToday = (TextView)view.findViewById(R.id.lblToday);
			TextView txtClassName = (TextView)view.findViewById(R.id.lblClassName);
			TextView txtClassRoom = (TextView)view.findViewById(R.id.lblClassRoom);
			TextView txtClassTime = (TextView)view.findViewById(R.id.lblClassTime);

			txtToday.setText(now[0]);
			if(isZero) {
				data[0] = "강의가 없습니다.";
				data[1] = data[2] = data[3] = "";
			}
			txtClassName.setText(data[0]);
			txtClassRoom.setText(data[1] + " / " + data[2]);
			txtClassTime.setText(data[3]);
			if(!isZero) {
				hour = Integer.parseInt(data[3].split(":")[0]);
				minute = Integer.parseInt(data[3].split(":")[1]);
			}
			else
				txtClassRoom.setText(data[1]);
		}
	}
	public void attendance(final String rst) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(!stu_main_activity.isCheck_arraysize());  //비콘이 연결 될 때 까지 빈 루프를 실행
				Calendar calendar = Calendar.getInstance();
				String[] beacon = db.getData(DBManager.GetTable.BEACON).split("!");
				String bc_mac = beacon[0];
				String bc_classno = beacon[1];
				String id = innerDB.getData().split("!")[0];
				String result = rst;

				innerDB.onDestroy();
				if(stu_main_activity.isEqual(bc_mac)) {
					String[] now = db.nowTime();
					int h = Integer.parseInt(now[1].split(":")[0]);
					int m = Integer.parseInt(now[1].split(":")[1]);
					int i = 0;

					/*if(hour == h && m ) {

					}*/
					while(i < days_en.length)
						if(days_en[i++].equalsIgnoreCase(now[2]))
							break;
					if(m != 0 && m > 49)
						if(db.attendance(id, now[0], days[--i], now[1], bc_classno, result))
							Log.d(TAG, "출석체크 완료");
				}
			}
		}).start();
	}
}