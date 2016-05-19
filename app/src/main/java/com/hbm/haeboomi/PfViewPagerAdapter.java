package com.hbm.haeboomi;

import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pass.Spass;
import com.samsung.android.sdk.pass.SpassFingerprint;

import java.util.Calendar;


public class PfViewPagerAdapter extends PagerAdapter implements View.OnClickListener {
    private final String TAG = "EndHBM_VPAdapter";

    private LayoutInflater minflater;
    private ViewPager viewP;
    private int posi;

    private DBManager db;
    private Tab_ProfessorVPActivity activity;

    public PfViewPagerAdapter(Tab_ProfessorVPActivity activity, ViewPager v){
        super();
        this.activity = activity;
        minflater = LayoutInflater.from(activity);
        viewP = v;
        posi = 0;
        db = new DBManager(activity);
    }
    @Override
    public int getCount() {
        return Integer.MAX_VALUE;
    }

    public int getPosition(){ return posi; }
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View vw = minflater.inflate(R.layout.activity_pf_vpager, null);

        final int p = position;
        this.posi = p;

        Button btnPfPrev = (Button)vw.findViewById(R.id.btnPfPrev);
        Button btnPfNext = (Button)vw.findViewById(R.id.btnPfNext);
        Button btnInOut = (Button)vw.findViewById(R.id.btnInOut);
        btnPfPrev.setOnClickListener(this);
        btnPfNext.setOnClickListener(this);
        btnInOut.setOnClickListener(this);

        ((ViewPager)container).addView(vw);
        return vw;
    }
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnPfPrev:
                viewP.setCurrentItem(posi - 1);
                break;
            case R.id.btnPfNext:
                viewP.setCurrentItem(posi + 1);
                break;
            case R.id.btnInOut:
                activity.startActivity(new Intent(activity, Check_state.class));
        }
    }
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View)object);
    }
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}