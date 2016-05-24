package com.hbm.haeboomi;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Tab_TimeSettingActivity extends Activity {
    private final String TAG = "EndHBM_TimeSetting";
    private BackPressCloseHandler bpch;
    private static int ONE_MINUTE=5626;
    private int h = 20;//시간
    private int m = 50;//분
    private int time ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_time_setting);
        bpch = new BackPressCloseHandler(this);



        Button five = (Button)findViewById(R.id.five);
        Button ten = (Button)findViewById(R.id.ten);
        five.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {//확인 버튼 눌렀을 경우
                m=m-1;
                new AlarmHATT(getApplicationContext()).Alarm();//푸시 알람 부름
            }
        });
        ten.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                m=m-10;
                new AlarmHATT(getApplicationContext()).Alarm();//푸시 알람 부름
            }
        });


    }
    public class AlarmHATT{
        private Context context;
        public AlarmHATT(Context context){
            this.context = context;
        }
        public void Alarm(){
            long firstTime = SystemClock.elapsedRealtime();
            Log.d("TTTT","TTTT1 :" + SystemClock.elapsedRealtime());
            firstTime = firstTime+(60*1000); // add 1 second
            Log.d("TTTT","TTTT2 :" + firstTime);
            AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(Tab_TimeSettingActivity.this,BroadcastD.class);

            PendingIntent sender = PendingIntent.getBroadcast(Tab_TimeSettingActivity.this,0,intent,0);
            Calendar calendar = Calendar.getInstance();
            //알람 시간 calendar 에 set해주기
            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), h, m, 0);
            Log.d("CAL","mTIME"+m);
            //알람 예약
            am.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),sender);

            m=40;
        }
    }

    @Override
    public void onBackPressed() {
        bpch.onBackPressed();
    }

}