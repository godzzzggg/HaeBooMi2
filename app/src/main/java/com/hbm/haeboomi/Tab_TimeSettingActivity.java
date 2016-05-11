package com.hbm.haeboomi;

import android.app.Activity;
import android.os.Bundle;

public class Tab_TimeSettingActivity extends Activity {
    private BackPressCloseHandler bpch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_time_setting);

        bpch = new BackPressCloseHandler(this);

    }

    @Override
    public void onBackPressed() {
        bpch.onBackPressed();
    }

}
