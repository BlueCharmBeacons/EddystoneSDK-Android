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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ModifyURLctivity extends Activity implements OnClickListener, DeviceListener {
	// Time delay time for writing data
	private int time = 100;
	private EditText editUrl;
	private EditText editPeriod;
	private EditText editTxPower;
	private EditText editDeviceName;
	private EditText editPassword;
	private Button btnModify;
	private Button btnModifyPass;
	private Eddystone eddystone;

	private String deviceName = "";
	private String url = "";
	private String period = "";
	private int txPower;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_modifyurl);
		url = getIntent().getStringExtra("url");
		eddystone = MyApplication.eddystone;
		deviceName = eddystone.deviceName;
		initView();
		initData();
		btnModify.setOnClickListener(this);
		eddystone.setDeviceListener(this);
		eddystone.connect();
	}

	private void initData() {
		editDeviceName.setText(deviceName);
	}

	private void initView() {
		editUrl = (EditText) findViewById(R.id.edit_url);
		editPeriod = (EditText) findViewById(R.id.edit_period);
		editTxPower = (EditText) findViewById(R.id.edit_txPower);
		editDeviceName = (EditText) findViewById(R.id.edit_deviceName);
		editPassword = (EditText) findViewById(R.id.edit_password);
		btnModify = (Button) findViewById(R.id.btn_modify);
		btnModifyPass = (Button) findViewById(R.id.btn_modifypass);
		btnModify.setOnClickListener(this);
		btnModifyPass.setOnClickListener(this);
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
				Toast.makeText(ModifyURLctivity.this, getString(R.string.toast_password_error), Toast.LENGTH_SHORT)
						.show();
				break;

			case 1:
				sendData();
				break;
			}
		}
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
		} else if (flag.contains("11")) {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					// Here the data we do not have an analysis, but the data
					// obtained from the previous scan
					editUrl.setText(url);
				}

			});

		} else if (flag.contains("12")) {
			period = Integer.parseInt(datas.substring(2, 6), 16) + "";
			txPower = Integer.parseInt(datas.substring(6, 8), 16);
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					editPeriod.setText(period);
					editTxPower.setText(txPower + "");
				}
			});
		}
	}

	@Override
	public void onSendSuccess(String tag, byte[] value, BluetoothDevice device) {

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
		if (!TextUtils.isEmpty(editPassword.getText().toString()) && editPassword.getText().toString().length() == 6) {

		} else {
			Toast.makeText(ModifyURLctivity.this, getString(R.string.toast_input_pass), Toast.LENGTH_LONG).show();
			return;
		}
		period = editPeriod.getText().toString();
		url = editUrl.getText().toString();
		deviceName = editDeviceName.getText().toString();

		// URL to conduct a variety of judgments, whether it is in line with the
		// rules
		if (!TextUtils.isEmpty(url) && !EddystoneSDK.hasFullSize(url)) {
			if (url.contains("http://www.")) {
				String str[] = url.split("http://www.");
				if (!determineUrl(str)) {
					return;
				}
			} else if (url.contains("https://www.")) {
				String str[] = url.split("https://www.");
				if (!determineUrl(str)) {
					return;
				}
			} else if (url.contains("http://")) {
				String str[] = url.split("http://");
				if (!determineUrl(str)) {
					return;
				}
			} else if (url.contains("https://")) {
				String str[] = url.split("https://");
				if (!determineUrl(str)) {
					return;
				}
			} else {
				Toast.makeText(ModifyURLctivity.this, getString(R.string.toast_input_url), Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(ModifyURLctivity.this, getString(R.string.toast_input_righturl), Toast.LENGTH_SHORT).show();
			return;
		}
		if (TextUtils.isEmpty(period)) {
			Toast.makeText(ModifyURLctivity.this, getString(R.string.toast_input_period), Toast.LENGTH_SHORT).show();
			return;
		} else {
			if (Integer.parseInt(period) < 100 || Integer.parseInt(period) > 9800) {
				Toast.makeText(ModifyURLctivity.this, "9800>=Period>=100", Toast.LENGTH_SHORT).show();
				return;
			}
		}
		Toast.makeText(this, getString(R.string.toast_modify), Toast.LENGTH_SHORT).show();
		// Verify password，In the onGetValue() callback can get the results, 05
		// said the password is correct, 06, said the password error
		byte[] bs = EddystoneSDK.str2Byte(editPassword.getText().toString(), (byte) 0x04);
		eddystone.sendDatatoDevice(bs);
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	private void sendData() {
		/**
		 * modify the url
		 */
		byte[] bs2 = EddystoneSDK.url2Byte(url);
		eddystone.sendDatatoDevice(bs2);
		try {
			Thread.sleep(time + 50);
		} catch (InterruptedException e1) {
		}
		/**
		 * modify the period , txPower
		 */
		byte[] data = new byte[4];
		data[0] = (byte) 0x02;
		data[1] = (byte) (Integer.parseInt(period) / 256);
		data[2] = (byte) (Integer.parseInt(period) % 256);
		data[3] = (byte) txPower;
		eddystone.sendDatatoDevice(data);
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if (!TextUtils.isEmpty(deviceName) && !deviceName.equals("pBeacon_n")) {
			byte[] Namebs = EddystoneSDK.str2ByteDeviceName(deviceName);
			eddystone.sendDatatoDevice(Namebs);
			try {
				Thread.sleep(time + 50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		byte[] data2 = new byte[1];
		data2[0] = (byte) 0x03;
		eddystone.sendDatatoDevice(data2);
		Toast.makeText(ModifyURLctivity.this, getString(R.string.toast_modify_success), Toast.LENGTH_SHORT).show();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		finish();
	}

	/**
	 * Depending on the suffix, you can enter the length of the different data
	 * 
	 * @param str
	 * @return
	 */
	private boolean determineUrl(String[] str) {
		if (str.length > 1) {
			if (str[1].contains(".info/")) {
				if (str[1].length() > 22) {
					Toast.makeText(ModifyURLctivity.this, getString(R.string.toast_suffix22), Toast.LENGTH_SHORT)
							.show();
					return false;
				}
			} else if (str[1].contains(".com/") || str[1].contains(".org/") || str[1].contains(".edu/")
					|| str[1].contains(".net/") || str[1].contains(".biz/") || str[1].contains(".gov/")
					|| str[1].contains(".info")) {
				if (str[1].length() > 21) {
					Toast.makeText(ModifyURLctivity.this, getString(R.string.toast_suffix22), Toast.LENGTH_SHORT)
							.show();
					return false;
				}

			} else if (str[1].contains(".com") || str[1].contains(".org") || str[1].contains(".edu")
					|| str[1].contains(".net") || str[1].contains(".biz") || str[1].contains(".gov")) {
				if (str[1].length() > 20) {
					Toast.makeText(ModifyURLctivity.this, getString(R.string.toast_suffix20), Toast.LENGTH_SHORT)
							.show();
					return false;
				}
			} else {
				if (str[1].length() > 17) {
					Toast.makeText(ModifyURLctivity.this, getString(R.string.toast_suffix17), Toast.LENGTH_SHORT)
							.show();
					return false;
				}
			}
		}
		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (eddystone.isConnected()) {
			eddystone.disConnect();
		}
	}
}
