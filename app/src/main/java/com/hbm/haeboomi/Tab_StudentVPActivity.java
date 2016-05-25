package com.hbm.haeboomi;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.bitnpulse.beacon.util.BeaconConstant;

import java.util.ArrayList;

public class Tab_StudentVPActivity extends Activity implements View.OnClickListener {
    // Debugging
    private static final String TAG = "EndHBM_StuMain";

    private BackPressCloseHandler bpch;
    private ViewPager vp;
	private ViewPagerAdapter sub_adapter;
    private int COUNT;

	private DBManager db;

    private BTService btService;
    private DeviceAdapter adapter;
	private ArrayList<ContentValues> device_array;

	private boolean check_arraysize = false;

	private String[] schedule;
	private Toast to = null;
    ///////////////////////
    private int temp=4;
    public void newCalendar(int t) {
        Intent intent = new Intent(this, CalendarActivity.class);
        temp = t;
        intent.putExtra("random", temp);
        startActivity(intent);
    }
/////////////////
    public void btInit() {
        if(btService == null)
            btService = new BTService(this, this);
	    else if(btService.isNull()) {
	        btService = null;
	        btService = new BTService(this, this);
        }
	    device_array = new ArrayList<ContentValues>();
        adapter = new DeviceAdapter(this, device_array);
    }

    public boolean btStart() {
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
	            btStart();
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

    public void UpdateList(ArrayList<ContentValues> arrayList) {
        adapter.UpdateList(arrayList);
	    if(arrayList.size() != 0) check_arraysize = true;
    }

    public boolean isEqual(String mac) {
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

	    final String[] idpw = innerDB.getData().split("!");
	    innerDB.onDestroy();

	    new Thread(new Runnable() {
		    @Override
		    public void run() {
			    Tab_StudentVPActivity.this.runOnUiThread(new Runnable() {
				    @Override
				    public void run() {
					    String[] days = {"일", "월", "화", "수", "목", "금", "토"};
					    String[] days_en = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
					    String[] now = db.nowTime();

					    int i = 0;
					    while(i < days_en.length)
						    if(days_en[i++].equalsIgnoreCase(now[2]))
							    break;
					    try {
						    String[] temp_schedule = db.getSelectData("cname, building, classno, time", "st_schedule", "id = " + idpw[0] + " and day = " + (--i), DBManager.GetTable.ST_SCHEDULE).split("!");
						    schedule = new String[temp_schedule.length / 8];

						    i = 0;
						    for (int j = 0; i < temp_schedule.length; i++) {
							    if (!temp_schedule[i].equalsIgnoreCase("null") && !temp_schedule[i].equalsIgnoreCase("\n")) {
								    temp_schedule[i] = temp_schedule[i].replaceFirst("\\n", "");
								    schedule[j++] = temp_schedule[i++] + "/" + temp_schedule[i++] + "/" + temp_schedule[i++] + "/" + temp_schedule[i];
							    }
						    }
						    COUNT = schedule.length;
					    }catch(ArrayIndexOutOfBoundsException e) {
						    COUNT = 0;
					    }
					    sub_adapter = new ViewPagerAdapter(Tab_StudentVPActivity.this, vp);
					    sub_adapter.setCount(COUNT);
					    vp.setAdapter(sub_adapter);
					    vp.setCurrentItem(0);
				    }
			    });
		    }
	    }).start();

        vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
	        @Override
	        public void onPageSelected(int position) {
		        //if (to != null) to.cancel();
		        //to = Toast.makeText(Tab_StudentVPActivity.this, "" + vp.getCurrentItem() % COUNT, Toast.LENGTH_SHORT);
		        //to.show();
	        }
	        @Override
	        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
	        @Override
	        public void onPageScrollStateChanged(int state) {}
        });

        Button btnAttendanceStatus = (Button) findViewById(R.id.btnAttendanceStatus);
	    if(COUNT == 0)
		    btnAttendanceStatus.setVisibility(View.INVISIBLE);
	    btnAttendanceStatus.setOnClickListener(this);
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
		        startActivity(new Intent(Tab_StudentVPActivity.this, CalendarActivity.class).putExtra("random", temp));
		        break;
        }
    }

    @Override
    public void onBackPressed() {
        bpch.onBackPressed();
    }
}
