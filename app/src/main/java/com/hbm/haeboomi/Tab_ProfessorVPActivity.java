package com.hbm.haeboomi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.hbm.haeboomi.BackPressCloseHandler;
import com.hbm.haeboomi.CalendarActivity;
import com.hbm.haeboomi.R;
import com.hbm.haeboomi.ViewPagerAdapter;

public class Tab_ProfessorVPActivity extends Activity implements View.OnClickListener {
    private BackPressCloseHandler bpch;
    private ViewPager vp;
    private int COUNT = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_professorvpactivity);
        bpch = new BackPressCloseHandler(this);

        vp = (ViewPager)findViewById(R.id.viewPager);

        final ViewPagerAdapter sub_adapter = new ViewPagerAdapter(this, vp);
        vp.setAdapter(sub_adapter);
        int center = Integer.MAX_VALUE / 2 - Integer.MAX_VALUE % COUNT;  //2147483647의 중앙 / 2 - 1(%COUNT)
        vp.setCurrentItem(center);

        vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                Toast to = null;
                switch (sub_adapter.getPosition() % 3) {
                    case 0:
                        if (to != null) to.cancel();
                        to = Toast.makeText(Tab_ProfessorVPActivity.this, "0", Toast.LENGTH_SHORT);
                        to.show();
                        break;
                    case 1:
                        if (to != null) to.cancel();
                        to = Toast.makeText(Tab_ProfessorVPActivity.this, "1", Toast.LENGTH_SHORT);
                        to.show();
                        break;
                    case 2:
                        if (to != null) to.cancel();
                        to = Toast.makeText(Tab_ProfessorVPActivity.this, "2", Toast.LENGTH_SHORT);
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
            public void onBackPressed() {
                bpch.onBackPressed();
            }
        }
