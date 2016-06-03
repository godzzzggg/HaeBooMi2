package com.hbm.haeboomi;

import android.content.Intent;
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

public class PfViewPagerAdapter extends PagerAdapter implements View.OnClickListener {
	private final String TAG = "EndHBM_PagerAdapter";
	
	private SparseArray<View> views = new SparseArray<>();
	
	private int COUNT;
	private boolean isZero = false;
	
	private LayoutInflater minflater;
	private Tab_ProfessorVPActivity pro_main_activity;
	private ViewPager viewP;
	
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
	
	private void Init() {
		InitDB();
		setView();
	}
	
	public PfViewPagerAdapter(Tab_ProfessorVPActivity activity, ViewPager v, String[] s, int count) {
		pro_main_activity = activity;
		viewP = v;
		
		minflater = LayoutInflater.from(pro_main_activity);
		db = new DBManager(pro_main_activity);
		innerDB = new DBManager.innerDB(pro_main_activity);
		
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
			case R.id.btnPfPrev:
				new Thread(new Runnable() {
					@Override
					public void run() {
						pro_main_activity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								viewP.setCurrentItem(viewP.getCurrentItem() - 1);
							}
						});
					}
				}).start();
				break;
			case R.id.btnPfNext:
				new Thread(new Runnable() {
					@Override
					public void run() {
						pro_main_activity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								viewP.setCurrentItem(viewP.getCurrentItem() + 1);
							}
						});
					}
				}).start();
				break;
			case R.id.btnInOut:
				pro_main_activity.startActivity(new Intent(pro_main_activity, Check_state.class));
				break;
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
	}
	private void setView() {
		for(int i = 0; i < COUNT; i++) {
			View vw = minflater.inflate(R.layout.activity_pf_vpager, null); // 생성될 뷰
			
			Button btnPfPrev = (Button)vw.findViewById(R.id.btnPfPrev);
			Button btnPfNext = (Button)vw.findViewById(R.id.btnPfNext);
			Button btnInOut = (Button)vw.findViewById(R.id.btnInOut);
			btnPfPrev.setOnClickListener(this);
			btnPfNext.setOnClickListener(this);
			btnInOut.setOnClickListener(this);
			
			if (COUNT == 1) {
				btnPfPrev.setVisibility(View.INVISIBLE);
				btnPfNext.setVisibility(View.INVISIBLE);
				if (isZero) {
					schedule = new String[1];
					schedule[0] = " / / / ";
					btnInOut.setVisibility(View.INVISIBLE);
				}
			}
			btnPfPrev.setOnClickListener(this);
			btnPfNext.setOnClickListener(this);
			btnInOut.setOnClickListener(this);
			
			if (COUNT > 1 && i == 0)
				btnPfPrev.setVisibility(View.INVISIBLE);
			else if (COUNT > 1 && i == COUNT - 1)
				btnPfNext.setVisibility(View.INVISIBLE);

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
			TextView lblToday = (TextView)view.findViewById(R.id.lblPfToday);
			TextView lblCname = (TextView)view.findViewById(R.id.lblPfClassName);
			TextView lblBuilding = (TextView)view.findViewById(R.id.lblPfClassRoom);
			TextView lblTime = (TextView)view.findViewById(R.id.lblPfClassTime);
			
			
			lblToday.setText(date + " (" + days[day] + ")");
			lblCname.setText(data[0]);
			if(isZero)
				lblBuilding.setText("");
			else
				lblBuilding.setText(data[1] + " / " + data[2]);
			lblTime.setText(data[3]);
		}
	}
}