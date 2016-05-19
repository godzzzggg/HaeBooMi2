package com.hbm.haeboomi;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
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
    private int COUNT = 3;

    private BTService btService;
    private DeviceAdapter adapter;
	private ArrayList<ContentValues> device_array;
	private boolean check_arraysize = false;

    ///////////////////////
    private int temp;
    public void newCalendar(int t) {
        Log.d(TAG, "" + t);
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

	    int center = Integer.MAX_VALUE / 2 - Integer.MAX_VALUE % COUNT;  //2147483647의 중앙 / 2 - 1(%COUNT)
        final ViewPagerAdapter sub_adapter = new ViewPagerAdapter(this, vp);
        vp.setAdapter(sub_adapter);
        vp.setCurrentItem(center);

        vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
	        @Override
	        public void onPageSelected(int position) {
		        Toast to = null;
		        switch (sub_adapter.getPosition() % 3) {
			        case 0:
				        if (to != null) to.cancel();
				        to = Toast.makeText(Tab_StudentVPActivity.this, "0", Toast.LENGTH_SHORT);
				        to.show();
				        break;
			        case 1:
				        if (to != null) to.cancel();
				        to = Toast.makeText(Tab_StudentVPActivity.this, "1", Toast.LENGTH_SHORT);
				        to.show();
				        break;
			        case 2:
				        if (to != null) to.cancel();
				        to = Toast.makeText(Tab_StudentVPActivity.this, "2", Toast.LENGTH_SHORT);
				        to.show();
				        break;
		        }
                /*
                if (position < COUNT) {        //1번째 아이템에서 마지막 아이템으로 이동하면
                    //vp.setCurrentItem(position + COUNT, false); //이동 애니메이션을 제거 해야 한다
                }
                else if (position >= COUNT * 2) {    //마지막 아이템에서 1번째 아이템으로 이동하면
                    //vp.setCurrentItem(position - COUNT, false);
                }*/
	        }

	        @Override
	        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
	        }

	        @Override
	        public void onPageScrollStateChanged(int state) {
	        }
        });

        Button btnLeft = (Button) findViewById(R.id.btnLeft);
	    btnLeft.setOnClickListener(this);

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
            case R.id.btnLeft:
                startActivity(new Intent(Tab_StudentVPActivity.this, CalendarActivity.class).putExtra("random", temp));
                break;

        }
    }

    @Override
    public void onBackPressed() {
        bpch.onBackPressed();
    }
}
