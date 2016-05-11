package com.hbm.haeboomi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class Tab_StudentVPActivity extends Activity {
    private BackPressCloseHandler bpch;
    private ViewPager vp;
    private int COUNT = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_studentvpactivity);

        bpch = new BackPressCloseHandler(this);

        vp = (ViewPager)findViewById(R.id.viewPager);

        final ViewPagerAdapter sub_adapter = new ViewPagerAdapter(this, vp);
        //sub_adapter.setCount(COUNT);
        vp.setAdapter(sub_adapter);
        int c = Integer.MAX_VALUE / 2 - Integer.MAX_VALUE % COUNT;
        vp.setCurrentItem(c);

        vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                Toast to = null;
                switch(sub_adapter.getPosition() % 3) {
                    case 0:
                        if(to != null) to.cancel();
                        to = Toast.makeText(Tab_StudentVPActivity.this, "0", Toast.LENGTH_SHORT);
                        to.show();
                        break;
                    case 1:
                        if(to != null) to.cancel();
                        to = Toast.makeText(Tab_StudentVPActivity.this, "1", Toast.LENGTH_SHORT);
                        to.show();
                        break;
                    case 2:
                        if(to != null) to.cancel();
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
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        Button btnLeft = (Button) findViewById(R.id.btnLeft);
        //다른 화면을 보여주기 위한 온 클릭 리스너
        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {   //출결현황 버튼을 누르면 달력 화면으로 이동
                startActivity(new Intent(Tab_StudentVPActivity.this, CalendarActivity.class));
            }
        });
        Button btnCenter = (Button)findViewById(R.id.btnCenter);
        //다른 화면을 보여주기 위한 온 클릭 리스너
        btnCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {   //출결현황 버튼을 누르면 달력 화면으로 이동
                startActivity(new Intent(Tab_StudentVPActivity.this, CalendarActivity.class));
            }
        });
        Button btnRight = (Button)findViewById(R.id.btnRight);
        //다른 화면을 보여주기 위한 온 클릭 리스너
        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {   //출결현황 버튼을 누르면 달력 화면으로 이동
                startActivity(new Intent(Tab_StudentVPActivity.this, CalendarActivity.class));
            }
        });
    }

    @Override
    public void onBackPressed() {
        bpch.onBackPressed();
    }
}
