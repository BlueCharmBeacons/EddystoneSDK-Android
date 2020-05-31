package com.eddystonesdkexample.activity;

import com.axaet.device.DeviceListener;
import com.axaet.device.EddystoneClass.Eddystone;
import com.axaet.eddystonesdkexample.R;
import com.eddystonesdkexample.application.MyApplication;
import com.axaet.device.EddystoneSDK;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ModifyBeaconActivity extends Activity implements OnClickListener, DeviceListener {

	// Time delay time for writing data
	private int time = 100;

	private EditText editUUID;
	private EditText editMajor;
	private EditText editMinor;
	private EditText editPeriod;
	private EditText editTxPower;
	private EditText editDeviceName;
	private EditText editPassword;
	private Button btnModify;
	private Button btnModifyPass;

	private Eddystone eddystone;

	private String deviceName = "";
	private String UUID = "";
	private String major = "";
	private String minor = "";
	private String period = "";
	private int txPower;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_modifybeacon);
		eddystone = MyApplication.eddystone;
		deviceName = eddystone.deviceName;
		initView();
		initData();
		btnModify.setOnClickListener(this);

		// Must set the DeviceListener callback, we can get the status and data
		// returned by Bluetooth
		eddystone.setDeviceListener(this);
		// Connect Bluetooth device
		eddystone.connect();
	}

	private void initView() {
		editUUID = (EditText) findViewById(R.id.edit_uuid);
		editMajor = (EditText) findViewById(R.id.edit_major);
		editMinor = (EditText) findViewById(R.id.edit_minor);
		editPeriod = (EditText) findViewById(R.id.edit_period);
		editTxPower = (EditText) findViewById(R.id.edit_txPower);
		editDeviceName = (EditText) findViewById(R.id.edit_deviceName);
		editPassword = (EditText) findViewById(R.id.edit_password);
		btnModify = (Button) findViewById(R.id.btn_modify);
		btnModifyPass = (Button) findViewById(R.id.btn_modifypass);
		btnModify.setOnClickListener(this);
		btnModifyPass.setOnClickListener(this);
	}

	private void initData() {
		editDeviceName.setText(deviceName);
	}

	@Override
	public void onClick(View arg0) {
		if (arg0 == btnModify) {
			judgmentBoundary();
		} else {
			if (eddystone.isConnected()) {
				Intent intent = new Intent(this, ModifyPassActivity.class);
				startActivity(intent);
			}else {
				Toast.makeText(this, getString(R.string.toast_disconnet_tip), Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 * To judge the data to be sent.
	 */
	private void judgmentBoundary() {
		if (TextUtils.isEmpty(editPassword.getText().toString())) {
			Toast.makeText(ModifyBeaconActivity.this, getString(R.string.toast_input_pass), Toast.LENGTH_LONG).show();
			return;
		}
		// Gets the data to be sent
		major = editMajor.getText().toString();
		minor = editMinor.getText().toString();
		period = editPeriod.getText().toString();
		UUID = editUUID.getText().toString();
		deviceName = editDeviceName.getText().toString();
		if (!TextUtils.isEmpty(UUID) && UUID.length() == 32) {

		} else {
			Toast.makeText(ModifyBeaconActivity.this, getString(R.string.toast_input_uuid), Toast.LENGTH_SHORT).show();
			return;
		}
		if (TextUtils.isEmpty(major)) {
			Toast.makeText(ModifyBeaconActivity.this, getString(R.string.toast_input_major), Toast.LENGTH_SHORT).show();
			return;
		} else if (Integer.parseInt(major) > 65535) {// Major must be less than
														// 65535,similar below
			Toast.makeText(ModifyBeaconActivity.this, getString(R.string.toast_major_less), Toast.LENGTH_SHORT).show();
			return;
		}
		if (TextUtils.isEmpty(minor)) {
			Toast.makeText(ModifyBeaconActivity.this, getString(R.string.toast_input_minor), Toast.LENGTH_SHORT).show();
			return;
		}
		if (Integer.parseInt(minor) > 65535) {
			Toast.makeText(ModifyBeaconActivity.this, getString(R.string.toast_mainor_less), Toast.LENGTH_SHORT).show();
			return;
		}
		if (TextUtils.isEmpty(period)) {
			Toast.makeText(ModifyBeaconActivity.this, getString(R.string.toast_input_period), Toast.LENGTH_SHORT)
					.show();
			return;
		} else {
			if (Integer.parseInt(period) < 100 || Integer.parseInt(period) > 9800) {
				Toast.makeText(ModifyBeaconActivity.this, "9800>=Period>=100", Toast.LENGTH_SHORT).show();
				return;
			}
		}
		Toast.makeText(this, getString(R.string.toast_modify), Toast.LENGTH_SHORT).show();

		// We can get an array of bytes used to verify the password by password
		// and command.

		byte[] bs = EddystoneSDK.str2Byte(editPassword.getText().toString(), (byte) 0x04);
		// Verify password，In the onGetValue() callback can get the results, 05
		// said the password is correct, 06, said the password error
		eddystone.sendDatatoDevice(bs);
		// There is a delay to prevent the data from being sent too fast. You
		// can also do this in an asynchronous thread.Below the same
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onConnected(String tag, BluetoothDevice device) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), "onConnected", Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	public void onDisconnected(String tag, BluetoothDevice device) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), "onDisconnected", Toast.LENGTH_SHORT).show();
			}
		});

	}

	@Override
	public void onGetRssi(String tag, int rssi, BluetoothDevice device) {

	}

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {

			switch (msg.what) {
			case 0:
				// Password error prompt
				Toast.makeText(ModifyBeaconActivity.this, getString(R.string.toast_password_error), Toast.LENGTH_SHORT)
						.show();
				break;

			case 1:
				// Password success, and then modify the data
				sendData();
				break;
			}
		}
	};

	@SuppressLint("DefaultLocale")
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
		} else if (flag.contains("11")) {
			UUID = datas.substring(2, 34).toUpperCase();
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					editUUID.setText(UUID);
				}
			});

		} else if (flag.contains("12")) {
			major = Long.parseLong(datas.substring(2, 6), 16) + "";
			minor = Long.parseLong(datas.substring(6, 10), 16) + "";
			period = Long.parseLong(datas.substring(10, 14), 16) + "";
			txPower = Integer.parseInt(datas.substring(14, 16), 16);
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					editMajor.setText(major);
					editMinor.setText(minor);
					editPeriod.setText(period);
					editTxPower.setText(txPower + "");
				}

			});

		}
	}

	@Override
	public void onSendSuccess(String tag, byte[] value, BluetoothDevice device) {

	}

	/**
	 * Modify data
	 */
	private void sendData() {
		/**
		 * modify the UUID
		 */
		byte[] bs2 = EddystoneSDK.hex2Byte(UUID);
		for (int i = 0; i < bs2.length; i++) {
			Log.i("yushu", bs2[i]+"");
		}
		eddystone.sendDatatoDevice(bs2);
		try {
			Thread.sleep(time);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		/**
		 * modify the major ,minor,period,txpower
		 */
		byte[] data = new byte[8];
		data[0] = (byte) 0x02;
		data[1] = (byte) (Integer.parseInt(major) / 256);
		data[2] = (byte) (Integer.parseInt(major) % 256);
		data[3] = (byte) (Integer.parseInt(minor) / 256);
		data[4] = (byte) (Integer.parseInt(minor) % 256);
		data[5] = (byte) (Integer.parseInt(period) / 256);
		data[6] = (byte) (Integer.parseInt(period) % 256);
		data[7] = (byte) txPower;
		eddystone.sendDatatoDevice(data);
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		/**
		 * modify the deviceName
		 */
		if (!TextUtils.isEmpty(deviceName) && !deviceName.equals("pBeacon_n")) {
			byte[] Namebs = EddystoneSDK.str2ByteDeviceName(deviceName);
			eddystone.sendDatatoDevice(Namebs);
			try {
				Thread.sleep(time + 50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		/**
		 * close the device
		 */
		data[0] = (byte) 0x03;
		eddystone.sendDatatoDevice(data);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (eddystone.isConnected()) {
			eddystone.disConnect();
		}
	}
}
