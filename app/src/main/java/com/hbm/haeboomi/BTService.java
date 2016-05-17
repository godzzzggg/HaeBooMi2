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
	private static final String TAG = "EndHBM_BTService";

	private Tab_StudentVPActivity main;
	private BeaconScanManager bc_Manager;

	public BTService(Tab_StudentVPActivity main, Context context) {
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

		if (enable && !isEnabled) {	//현재 블루투스가 꺼져있으면 켜고
			Log.d(TAG, "블루투스를 켜는 중");
			temp = bluetoothAdapter.enable();
			Log.d(TAG, "블루투스 On");
			return temp;
		}
		else if(!enable && isEnabled) {	//현재 블루투스가 켜져있으면 끈다
			Log.d(TAG, "블루투스 종료중");
			temp = bluetoothAdapter.disable();
			Log.d(TAG, "블루투스 Off");
			return temp;
		}
		return true;	//둘다 해당되지 않으면 켜져있는상태
	}

	public boolean Start() {
		if(bc_Manager != null) {
			if(bc_Manager.start()) {	//true : 비콘 스캔을 성공적으로 수행 | false : 비콘 스캔 실패(예:블루투스 off)
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

	public boolean isNull() {
		return bc_Manager == null;
	}
	public boolean isScanning() {
		return bc_Manager.isScanning();
	}

	@Override
	public boolean onBeaconScanned(ArrayList<ContentValues> arrayList) {
		main.UpdateList(arrayList);
		if(arrayList.size() != 0) {
			Stop(true);
			return true;	//스캔 중지
		}
		else
			return false;
	}
	@Override
	public void onBeaconScanError(Exception e) {
		Log.e(TAG, e.toString());
	}
}
