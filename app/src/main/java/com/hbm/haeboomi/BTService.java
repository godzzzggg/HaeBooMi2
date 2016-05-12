package com.hbm.haeboomi;

import java.util.ArrayList;

import android.app.*;
import android.bluetooth.*;
import android.content.*;
import android.util.*;

import com.bitnpulse.beacon.scan.*;
import com.bitnpulse.beacon.util.*;

public class BTService implements ListenerBeaconScan{
    // Debugging
    private static final String TAG = "BTService";

    private Tab_StudentVPActivity main;
    private BeaconScanManager bc_Manager;
    mBeacon m_beacon;

    public BTService(Tab_StudentVPActivity main, Activity activity, Context context) {
        this.main = main;
        try {
            Log.d(TAG, "블루투스 초기화");
            bc_Manager = new BeaconScanManager(context, this);
        } catch(IllegalStateException e) {
            Log.e(TAG, "블루투스 미지원 기기 | " + e.toString());
        }
        if(bc_Manager != null) {
            //의사를 묻지않고 블루투스 On
            setBluetooth(true);
        }
    }

    private boolean setBluetooth(boolean enable) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        boolean isEnabled = bluetoothAdapter.isEnabled();
        boolean temp;

        if (enable && !isEnabled) { //현재 블루투스가 꺼져있으면 켜고
            Log.d(TAG, "블루투스를 켜는 중");
            temp = bluetoothAdapter.enable();
            Log.d(TAG, "블루투스 On");
            return temp;
        }
        else if(!enable && isEnabled) { //현재 블루투스가 켜져있으면 끈다
            Log.d(TAG, "블루투스 종료중");
            temp = bluetoothAdapter.disable();
            Log.d(TAG, "블루투스 Off");
            return temp;
        }
        return true;    //둘다 해당되지 않으면 켜져있는상태
    }

    public boolean Start() {
        if(bc_Manager != null) {
            if(bc_Manager.start()) {  //true : 비콘 스캔을 성공적으로 수행 | false : 비콘 스캔 실패(예:블루투스 off)
                Log.d(TAG, "스캔 성공");
                return true;
            } else {
                Log.d(TAG, "스캔 실패");
                return false;
            }
        }
        return false;
    }

    public void Stop(boolean destroy) {
        if(bc_Manager != null) {
            Log.d(TAG, "스캔 중지");
            bc_Manager.stop();
            if(destroy) {
                bc_Manager = null;
                setBluetooth(!destroy);
            }
        }
    }

    public boolean isScanning() {
        return bc_Manager.isScanning();
    }

    @Override
    public boolean onBeaconScanned(ArrayList<ContentValues> arrayList) {
        main.UpdateList(arrayList);
        Log.d(TAG, "스캔 목록");
        for (ContentValues content : arrayList) {
            Log.d(TAG, content.toString());
            m_beacon = new mBeacon();
            m_beacon.strName = content.getAsString(BeaconConstant.NAME);
            m_beacon.strMac = content.getAsString(BeaconConstant.MAC_ADDRESS);
            m_beacon.nRSSI = content.getAsInteger(BeaconConstant.AVG_RSSI);
            m_beacon.strUUID = content.getAsString(BeaconConstant.UUID);
            m_beacon.nMajor = content.getAsInteger(BeaconConstant.MAJOR);
            m_beacon.nMinor = content.getAsInteger(BeaconConstant.MINOR);
            m_beacon.nRSSIat1M = content.getAsInteger(BeaconConstant.RSSIAt1M);
            m_beacon.nBatteryLevel = content.getAsInteger(BeaconConstant.SERVICE_DATA_BATTERY);
        }
        if(arrayList.size() != 0)
            return true;    //스캔 중지
        else
            return false;
    }

    @Override
    public void onBeaconScanError(Exception e) {
        Log.e(TAG, e.toString());
    }

    private class mBeacon {
        String strName;
        String strMac;
        int nRSSI;
        String strUUID;
        int nMajor;
        int nMinor;
        int nRSSIat1M;
        int nBatteryLevel;
    }
}
