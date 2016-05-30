package com.hbm.haeboomi;

import android.app.TabActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;

public class ProfessorMainActivity extends TabActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professor_main);

        Class[] classes = {Tab_ProfessorVPActivity.class, Tab_ClassSettingActivity.class, pfTimetable.class, Tab_TimeSettingActivity.class};
        String[] str = {"강의", "강의 설정", "시간표", "강의 알림"};

        Intent intent = getIntent();
        String pfNum = intent.getStringExtra("professor_number");
        String password = intent.getStringExtra("password");

        Intent[] sendIntent = new Intent[classes.length];

        TabHost tabhost = getTabHost();

        //아래 for문은 탭을 추가해주는 역할 수행
        for(int i = 0; i < classes.length; i++) {
            TabHost.TabSpec spec = tabhost.newTabSpec("tag" + (i+1));   //tag1, tag2, tag3, tag4이 들어감
            sendIntent[i] = new Intent(this, classes[i]);
            sendIntent[i].putExtra("professor_number", pfNum);
            sendIntent[i].putExtra("password", password);
            spec.setContent(sendIntent[i]);
            spec.setIndicator(str[i]); //탭 이름
            tabhost.addTab(spec);
        }

        //아래 for문은 탭의 크기, 색상 등을 지정
        for(int i = 0; i < tabhost.getTabWidget().getChildCount(); i++) {
            tabhost.getTabWidget().getChildAt(i).getLayoutParams().height = 160;
            tabhost.getTabWidget().getChildAt(i).setBackgroundColor(getResources().getColor(R.color.hallymBlue));
            RelativeLayout relLayout = (RelativeLayout)tabhost.getTabWidget().getChildAt(i);
            TextView tv = (TextView)relLayout.getChildAt(1);
            tv.setTextSize(25);
            tv.setTextColor(Color.WHITE);
        }
        //초기화면을 탭의 0번째 화면을 보여줌
        tabhost.setCurrentTab(0);
    }
}
