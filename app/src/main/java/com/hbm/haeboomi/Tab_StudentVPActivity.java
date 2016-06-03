package com.hbm.haeboomi;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.bitnpulse.beacon.util.BeaconConstant;

import java.io.Serializable;
import java.util.ArrayList;

public class Tab_StudentVPActivity extends FragmentActivity implements View.OnClickListener {
    // Debugging
    private static final String TAG = "EndHBM_StuMain";

    private BackPressCloseHandler bpch;
    private ViewPager vp;
	private ViewPagerAdapter sub_adapter;
    private int COUNT = -1;

	private DBManager db;

    private BTService btService;
    private DeviceAdapter adapter;
	private ArrayList<ContentValues> device_array;

	private boolean check_arraysize = false;

	private String[] schedule;
    ///////////////////////
    private int temp = 4;
    public void newCalendar(int t) {
        Intent intent = new Intent(this, CalendarActivity.class);
        temp = t;
        intent.putExtra("random", temp);
        startActivity(intent);
    }
/////////////////
    public void btInit() {  //블루투스 초기화
        if(btService == null)
            btService = new BTService(this, this);
	    else if(btService.isNull()) {
	        btService = null;
	        btService = new BTService(this, this);
        }
	    device_array = new ArrayList<>();
        adapter = new DeviceAdapter(this, device_array);
    }

    public boolean btStart() {  //블루투스 실행, 켜기, 검색
	    boolean returnVal = false;
        if (!btService.isScanning()) {
            try {
                Thread.sleep(500);
            }catch(InterruptedException e) {}
            Log.d(TAG, "스캔 시작");
            if(btService.Start()) {
                adapter.clear();    //어댑터 목록 초기화
                adapter.notifyDataSetChanged();
	            returnVal = true;
            }
            else {
                Log.e(TAG, "스캔 실패... 블루투스가 꺼져있는지 확인");
	            btStart();  //블루투스가 미처 다 켜지지 않았을 때 이 메소드를 다시 부른다.
            }
        }
        else {
            btService.Stop(false);
        }
	    return returnVal;
    }

	public boolean isCheck_arraysize() {
		return check_arraysize;
	}

    public void UpdateList(ArrayList<ContentValues> arrayList) {    //비콘 목록 업데이트
        adapter.UpdateList(arrayList);
	    if(arrayList.size() != 0) check_arraysize = true;
    }

    public boolean isEqual(String mac) {    //검색되는 비콘들중에 현재 강의실에 설치된 비콘을 찾는 메소드
        boolean equal = false;

        for(ContentValues content : device_array)
            if((equal = content.getAsString(BeaconConstant.MAC_ADDRESS).equalsIgnoreCase(mac))) break;

        return equal;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_studentvpactivity);

        bpch = new BackPressCloseHandler(this);

        vp = (ViewPager)findViewById(R.id.viewPager);

	    db = new DBManager(this);
	    DBManager.innerDB innerDB = new DBManager.innerDB(this);

	    final String[] idpw = innerDB.getData().split("!"); //내부DB에 저장된 아이디, 패스워드를 가져옴
	    innerDB.onDestroy();

	    final Button btnAttendanceStatus = (Button)findViewById(R.id.btnAttendanceStatus);  //출결 현황 버튼
	    btnAttendanceStatus.setOnClickListener(this);

	    new Thread(new Runnable() {
		    @Override
		    public void run() {
			    Tab_StudentVPActivity.this.runOnUiThread(new Runnable() {
				    @Override
				    public void run() {
					    String[] days_en = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
					    String[] now = db.nowTime();

					    int i = 0;
					    while (i < days_en.length)
						    if (days_en[i++].equalsIgnoreCase(now[2]))
							    break;
					    try {
						    String[] temp_schedule = db.getSelectData("cname, building, classno, time, st_schedule.5075", "st_schedule", "id = " + idpw[0] + " and day = " + (--i), DBManager.GetTable.ST_SCHEDULE).split("\n");
						    if(temp_schedule[0].equals("")) throw new ArrayIndexOutOfBoundsException();
						    else COUNT = temp_schedule.length;
						    schedule = new String[COUNT];

						    for (i = 0; i < COUNT; i++) {
							    String[] temp = temp_schedule[i].split("!");
							    schedule[i] = temp[0] + "/" + temp[1] + "/" + temp[2] + "/" + temp[3] + "/" + temp[4];
						    }
					    } catch (ArrayIndexOutOfBoundsException e) {
						    COUNT = 0;
						    btnAttendanceStatus.setVisibility(View.INVISIBLE);
					    }
					    sub_adapter = new ViewPagerAdapter(Tab_StudentVPActivity.this, vp, schedule, COUNT);
					    vp.setAdapter(sub_adapter);
					    vp.setOffscreenPageLimit(COUNT);
					    setPosition();
				    }
			    });
		    }
	    }).start();
    }

	public void setPosition() {
		for (int i = 0; schedule != null && i < schedule.length; i++) {
			String[] temp = schedule[i].split("/")[3].split(":");
			int h = Integer.parseInt(temp[0]), m = Integer.parseInt(temp[1]);
			String[] now = db.nowTime();
			int nowh = Integer.parseInt(now[1].split(":")[0]), nowm = Integer.parseInt(now[1].split(":")[1]);
			boolean gubun = schedule[i].split("/")[4].equals("0");   //50분 강의 : 75분 강의

			if (i == 0 && nowh < h) {
				vp.setCurrentItem(0);
				break;
			}
			else if (i == schedule.length - 1) {
				if(nowh - h == -1 && nowm < 50)
					vp.setCurrentItem(i - 1);
				else
					vp.setCurrentItem(i);
				break;
			}
			else {
				if (gubun) { //50분 강의
					if (h > nowh) {  //현재 시간이 이전 강의시간(Hour)들보다 커지게 될 때
						if (nowm > 49) { //49분이 지났는지 확인
							vp.setCurrentItem(i);
							break;
						}
						else {  //50분 이전이라면 이전과목을 보여준다.
							vp.setCurrentItem(i - 1);
							break;
						}
					}
				}
				else {  //75분 강의
					/*if(nowm > 14 && nowm < ) {

					}*/
				}
			}
		}
	}
    @Override
    protected void onDestroy() {
        if(btService != null)
            btService.Stop(true);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
	        case R.id.btnAttendanceStatus:
		        startActivity(new Intent(this, CalendarActivity.class).putExtra("random", temp));
		        break;
        }
    }

    @Override
    public void onBackPressed() {
        bpch.onBackPressed();
    }
}
