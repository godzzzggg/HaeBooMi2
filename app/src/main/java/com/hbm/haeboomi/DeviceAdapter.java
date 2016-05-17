package com.hbm.haeboomi;

import java.util.*;

import android.content.*;
import android.util.*;
import android.view.*;
import android.widget.*;

public class DeviceAdapter extends ArrayAdapter<ContentValues> {
    private final String TAG = "EndHBM_DeviceAdapter";

    public DeviceAdapter(Context context, List<ContentValues> objects) {
        super(context, R.layout.beacon_device_list, objects);
        Log.d(TAG, "생성자 호출");
    }

    //비콘 정보 배열을 내부 배열에 설정한다.
    public void UpdateList(ArrayList<ContentValues> values)
    {
        Log.d(TAG, "UpdateList()\n검색된 디바이스 수 : " + values.size());
        this.clear();
        this.addAll(values);
        this.notifyDataSetChanged();
    }
}