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
	private final String TAG = "EndHBM_PagerAdapter";

	private SparseArray<View> views = new SparseArray<>();

	private int COUNT;
	private boolean isZero = false;

	private LayoutInflater minflater;
	private Tab_StudentVPActivity stu_main_activity;
	private ViewPager viewP;

    private SpassFingerprint mSpassFingerprint;
    private Spass mSpass;
    private int passindex;
    private boolean isFeatureEnabled;

    private DBManager db;
	private DBManager.innerDB innerDB;
	private String[] idpw;
	private String[] schedule;
	private String[] attendance;
	private int limit_min_late = 5;     //지각 시작
	private int limit_max_late = 15;    //지각 끝, 이후부터는 결석처리
	private String[] now;
	private int hour;
	private int minute;
	private String date;
	private int day = 0;    //0 ~ 6(일 ~ 토)
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
                    String pass = db.getSelectData("pass", "student", "id = " + idpw[0], DBManager.GetTable.STUDENT);
	                if(pass.equals("" + passindex)) {
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

	private void Init() {
		passInit();
		InitDB();
		setView();
	}

	public ViewPagerAdapter(Tab_StudentVPActivity activity, ViewPager v, String[] s, int count) {
		stu_main_activity = activity;
		viewP = v;

		minflater = LayoutInflater.from(stu_main_activity);
		db = new DBManager(stu_main_activity);
		innerDB = new DBManager.innerDB(stu_main_activity);

		schedule = s;

		if(count == 0) {
			isZero = true;
			count++;
		}
		COUNT = count;

		now = db.nowTime();
		Init();
	}

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
	    container.addView(views.get(position), 0);
        return views.get(position);
    }

	@Override
	public int getCount() {
		return COUNT;  //총 보여질 페이지의 수
	}

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {}

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
							}
						});
					}
				}).start();
				break;
			case R.id.btnAttendance:
				String[] temp = schedule[viewP.getCurrentItem()].split("/")[3].split(":");
				hour = Integer.parseInt(temp[0]);
				minute = Integer.parseInt(temp[1]);
				String[] now = db.nowTime()[1].split(":");  //시, 분, 초
				int nowh = Integer.parseInt(now[0]);
				int nowm = Integer.parseInt(now[1]);

				if(schedule[viewP.getCurrentItem()].split("/")[4].equals("0")) { //50분 수업, 75분 수업
					if (hour - nowh > 1) {   //50분 수업일 때, 1시간 이상 차이가 난다면
						Toast.makeText(stu_main_activity, "현재 시간의 과목이 아닙니다.", Toast.LENGTH_SHORT).show();
						return;
					}
					else {
						if(hour - nowh == 1 && nowm < 50) {
							Toast.makeText(stu_main_activity, "현재 시간의 과목이 아닙니다.", Toast.LENGTH_SHORT).show();
							return;
						}
						else {
							attendanceRun(v);
						}
					}
				}
				//75분 수업은 00분인지 30분인지를 따져서 시간을 측정한다.
				//30분 시작인 수업은 시간이 같을 수 있다.
				//00분 시작인 수업은 시간이 1시간 차이가 나게 조건을 걸어준다.
				else {
					if((minute == 30? hour - nowh > 0 : hour - nowh > 1)) {
						if (minute == 30? minute - nowm <= 15 : 60 - nowm <= 15) {
							Toast.makeText(stu_main_activity, "현재 시간의 과목이 아닙니다.", Toast.LENGTH_SHORT).show();
							return;
						}
						else {
							attendanceRun(v);
						}
					}
				}
				break;
		}
	}

	private void attendanceRun(final View v) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				stu_main_activity.btInit();
				stu_main_activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (isFeatureEnabled) {
							mSpassFingerprint.startIdentifyWithDialog(stu_main_activity, listener, false);    //boolean값은 비밀번호 입력창 유무
						}
						else {
							stu_main_activity.btStart();
							Toast.makeText(stu_main_activity, "지문인식 미지원", Toast.LENGTH_SHORT).show();
							v.setEnabled(false);
							attendance("3");    //임시 출석
						}
					}
				});
			}
		}).start();
	}
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
	private void InitDB() {
		idpw = innerDB.getData().split("!");
		innerDB.onDestroy();

		while(day < days_en.length)
			if(days_en[day++].equalsIgnoreCase(now[2]))
				break;
		day--;
		date = now[0];
		try {
			String[] temp_attendance = db.getSelectData("result, attendance.time", "attendance", "date = '" + date + "' and id = " + idpw[0], DBManager.GetTable.ATTENDANCE).split("\n");
			if(temp_attendance[0].equals("")) throw new ArrayIndexOutOfBoundsException();
			attendance = new String[temp_attendance.length];

			for (int i = 0; i < temp_attendance.length; i++) {
				String[] temp = temp_attendance[i].split("!");
				attendance[i] = temp[0] + "/" + temp[1];
			}
		}catch(ArrayIndexOutOfBoundsException e) {}
	}
	private void setView() {
		for(int i = 0; i < COUNT; i++) {
			View vw = minflater.inflate(R.layout.activity_vpager_attendance, null); // 생성될 뷰
			Button btnPrev = (Button)vw.findViewById(R.id.btnPrev);
			Button btnNext = (Button)vw.findViewById(R.id.btnNext);
			Button btnAttendance = (Button)vw.findViewById(R.id.btnAttendance);

			if (COUNT == 1) {
				btnPrev.setVisibility(View.INVISIBLE);
				btnNext.setVisibility(View.INVISIBLE);
				if (isZero) {
					schedule = new String[1];
					schedule[0] = " / / / ";
					btnAttendance.setVisibility(View.INVISIBLE);
				}
			}
			btnPrev.setOnClickListener(this);
			btnNext.setOnClickListener(this);
			btnAttendance.setOnClickListener(this);

			if (COUNT > 1 && i == 0)
				btnPrev.setVisibility(View.INVISIBLE);
			else if (COUNT > 1 && i == COUNT - 1)
				btnNext.setVisibility(View.INVISIBLE);

			hour = Integer.parseInt(schedule[i].split("/")[3].split(":")[0]);
			minute = Integer.parseInt(schedule[i].split("/")[3].split(":")[1]);
			int nowh = Integer.parseInt(now[1].split(":")[0]);
			int nowm = Integer.parseInt(now[1].split(":")[1]);

			if(attendance != null && i < attendance.length) {   //출석체크를 완료해서 다시 어플을 켰을 때 출석체크 완료를 유지하기 위함
				String[] temp = attendance[i].split("/");
				String time = temp[1];
				if (schedule[i].split("/")[3].equals(time)) {
					btnAttendance.setEnabled(false);
					btnAttendance.setText("출석 체크 완료");
				}
			}
			else if(hour < nowh) { //아래부터는 출석한 기록이 없고 시간이 지나서 자동결석처리 되었을 때
				attendance(i);
			}
			else if(hour == nowh) {
				if(schedule[i].split("/")[4].equals("0")) { //50분 수업
					if(nowm > 49)   //시간이 같은데 50분이 넘었으면 결석처리
						attendance(i);
				}
				else {  //75분 수업
					if(hour % 3 == 0) { //A,C,E,G 교시
						if (nowm > 44)
							attendance(i);
					}
					else {  //B,D,F,H교시
						if(nowm > 14)
							attendance(i);
					}
				}
			}
			views.put(i, vw);
		}
		notifydata();
	}
	private void notifydata() { //각 과목 정보를 설정
		for (int i = 0; i < views.size(); i++) {
			int key = views.keyAt(i);
			String[] data = schedule[key].split("/");
			View view = views.get(key);

			if(isZero) {
				data[0] = "강의가 없습니다";
				data[1] = data[2] = data[3] = "";
			}
			TextView lblToday = (TextView)view.findViewById(R.id.lblToday);
			TextView lblCname = (TextView)view.findViewById(R.id.lblClassName);
			TextView lblBuilding = (TextView)view.findViewById(R.id.lblClassRoom);
			TextView lblTime = (TextView)view.findViewById(R.id.lblClassTime);


			lblToday.setText(date + " (" + days[day] + ")");
			lblCname.setText(data[0]);
			if(isZero)
				lblBuilding.setText("");
			else
				lblBuilding.setText(data[1] + " / " + data[2]);
			lblTime.setText(data[3]);
		}
	}
	private void attendance(final int i) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				String id = idpw[0];
				String classno = schedule[i].split("/")[2];
				String[] temp = schedule[i].split("/")[3].split(":");
				hour = Integer.parseInt(temp[0]);
				minute = Integer.parseInt(temp[1]);

				if(db.attendance(id, now[0], day, schedule[i].split("/")[3], classno, "0", "00:00:00")) {
					stu_main_activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Button btn = (Button)views.get(i).findViewById(R.id.btnAttendance);
							btn.setEnabled(false);
							btn.setText("출석 체크 완료");
						}
					});
				}
				else {
					Button btn = (Button)views.get(i).findViewById(R.id.btnAttendance);
					btn.setEnabled(true);
				}
			}
		}).start();
	}
	public void attendance(final String rst) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(!stu_main_activity.isCheck_arraysize());  //비콘이 연결 될 때 까지 빈 루프를 실행
				Calendar calendar = Calendar.getInstance();
				String[] beacon = db.getData(DBManager.GetTable.BEACON).split("\n");
				String bc_mac;
				String bc_classno;
				String id = idpw[0];
				String result = rst;
				final int pos = viewP.getCurrentItem();

				innerDB.onDestroy();
				for(int i = 0; i < beacon.length; i++) {
					bc_mac = beacon[i].split("!")[0];
					bc_classno = beacon[i].split("!")[1];
					if (stu_main_activity.isEqual(bc_mac)) {
						int h = Integer.parseInt(now[1].split(":")[0]);
						int m = Integer.parseInt(now[1].split(":")[1]);

						String[] temp = schedule[pos].split("/")[3].split(":");
						hour = Integer.parseInt(temp[0]);
						minute = Integer.parseInt(temp[1]);

						if (hour == h && (m - minute > limit_min_late && m - minute <= limit_max_late))  //지각 허용범위
							result = "1";
						else if (hour == h && m - minute > limit_max_late)   //결석처리 되는 시간(지각처리가 허용되는 시점을 넘으면 결석으로 처리)
							result = "0";

						if (db.attendance(id, now[0], day, schedule[pos].split("/")[3], bc_classno, result, now[1])) {
							stu_main_activity.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									Button btn = (Button)views.get(pos).findViewById(R.id.btnAttendance);
									btn.setEnabled(false);
									btn.setText("출석 체크 완료");
								}
							});
						}
						else {
							stu_main_activity.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									Button btn = (Button)views.get(pos).findViewById(R.id.btnAttendance);
									btn.setEnabled(true);
								}
							});
						}
					}
				}
			}
		}).start();
	}
}