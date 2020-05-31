package com.eddystonesdkexample.activity;

import com.axaet.device.DeviceListener;
import com.axaet.device.EddystoneClass.Eddystone;
import com.axaet.device.EddystoneSDK;
import com.axaet.eddystonesdkexample.R;
import com.eddystonesdkexample.application.MyApplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ModifyPassActivity extends Activity implements OnClickListener,DeviceListener{

	private Eddystone eddystone;
	
	private EditText editOldPass;
	private EditText editNewPass;
	private EditText editConfirmPass;
	private Button btnModifyPass;
	
	private String oldpassword;
	private String newpassword;
	private String confirmpassword;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_modifypass);
		eddystone=MyApplication.eddystone;
		eddystone.setDeviceListener(this);
		initView();
	}

	private void initView() {
		editOldPass=(EditText) findViewById(R.id.edit_input_oldpass);
		editNewPass=(EditText) findViewById(R.id.edit_input_newpass);
		editConfirmPass=(EditText) findViewById(R.id.edit_confirm_pass);
		btnModifyPass=(Button) findViewById(R.id.btn_modifypass);
		btnModifyPass.setOnClickListener(this);
	}

	@Override
	public void onConnected(String tag, BluetoothDevice device) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDisconnected(String tag, BluetoothDevice device) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onGetRssi(String tag, int rssi, BluetoothDevice device) {
		// TODO Auto-generated method stub
		
	}
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		@SuppressLint("HandlerLeak")
		public void handleMessage(android.os.Message msg) {

			switch (msg.what) {
			case 0:
				Toast.makeText(ModifyPassActivity.this, getString(R.string.toast_password_error), Toast.LENGTH_SHORT).show();
				break;

			case 1:
				sendData();
				break;
			}

		};
	};
	@Override
	public void onGetValue(String tag, byte[] value, BluetoothDevice device) {
		String datas = EddystoneSDK.bytesToHexString(value);
		String flag = datas.substring(0, 2);
		if (flag.contains("05")) {
			Message message = handler.obtainMessage();
			message.what = 1;
			message.sendToTarget();
		} else if (flag.contains("06")) {
			Message message = handler.obtainMessage();
			message.what = 0;
			message.sendToTarget();
		}
	}

	@Override
	public void onSendSuccess(String tag, byte[] value, BluetoothDevice device) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClick(View arg0) {
		oldpassword = editOldPass.getText().toString();
		newpassword = editNewPass.getText().toString();
		confirmpassword = editConfirmPass.getText().toString();
		if (!TextUtils.isEmpty(oldpassword) && oldpassword.length() == 6) {
		} else {
			Toast.makeText(this, getString(R.string.toast_password_error), Toast.LENGTH_SHORT).show();
			return;
		}
		if (!TextUtils.isEmpty(newpassword)&& newpassword.length() == 6 && oldpassword.length() == 6) {

		} else {
			Toast.makeText(this, getString(R.string.toast_pass_lenght), Toast.LENGTH_SHORT).show();
			return;
		}
		if (!TextUtils.isEmpty(confirmpassword) && confirmpassword.equals(newpassword)) {

		} else {
			Toast.makeText(this, getString(R.string.toast_pass_confirm), Toast.LENGTH_SHORT).show();
			return;
		}
		Toast.makeText(this, getString(R.string.toast_modify), Toast.LENGTH_SHORT).show();
		
		byte[] bs = EddystoneSDK.str2Byte(oldpassword, (byte) 0x04);
		eddystone.sendDatatoDevice(bs);
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		
	}
	
	private void sendData() {
		byte[] bs2 = EddystoneSDK.str2Byte(oldpassword + newpassword, (byte) 0x09);
		eddystone.sendDatatoDevice(bs2);
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		bs2[0] = (byte) 0x03;
		eddystone.sendDatatoDevice(bs2);
		Toast.makeText(ModifyPassActivity.this, getString(R.string.toast_modify_success), Toast.LENGTH_SHORT).show();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Intent intent2 = new Intent(ModifyPassActivity.this, DeviceScanActivity.class);
		intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent2);
		finish();
	}
}
