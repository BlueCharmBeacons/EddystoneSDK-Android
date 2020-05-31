package com.eddystonesdkexample.activity;

import com.axaet.device.EddystoneClass;
import com.axaet.device.EddystoneClass.Eddystone;
import com.axaet.device.EddystoneSDK;
import com.axaet.eddystonesdkexample.R;
import com.eddystonesdkexample.adpter.LeDeviceListAdapter;
import com.eddystonesdkexample.application.MyApplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class DeviceScanActivity extends Activity {
	private static final int REQUEST_ENABLE_BT = 1;
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothLeScanner scanner;
	private ScanCallback scanCallback;

	private LeDeviceListAdapter adapter;
	private ListView listView;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_devicescan);
		// Use the EddystoneSDK must be initialized
		EddystoneSDK.initialize();

		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

		// android 5.0 system above using the method of scanning device
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			scanner = mBluetoothAdapter.getBluetoothLeScanner();
			// Higher than 5 using the scan callback
			scanCallback = new ScanCallback() {
				@Override
				public void onScanResult(int callbackType, ScanResult result) {
					super.onScanResult(callbackType, result);
					final Eddystone eddystone = EddystoneClass.fromScanData(result.getDevice(), result.getRssi(),
							result.getScanRecord().getBytes(), DeviceScanActivity.this);
					if (eddystone == null) {
						return;
					}
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							adapter.addDevice(eddystone);
						}
					});
				}
			};
		}

		listView = (ListView) findViewById(R.id.listview);
		adapter = new LeDeviceListAdapter(this);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Eddystone eddystone = adapter.getDevice(arg2);
				if (eddystone.deviceName != null && eddystone.deviceName.contains("_n")) {
					MyApplication.eddystone = eddystone;
					Intent intent = new Intent();
					// According to the type of broadcast data of Bluetooth
					// device, enter different modification activity.
					if (eddystone.type == 1) {
						intent.setClass(DeviceScanActivity.this, ModifyBeaconActivity.class);
					} else if (eddystone.type == 2) {
						intent.setClass(DeviceScanActivity.this, ModifyUIDActivity.class);
					} else if (eddystone.type == 3) {
						intent.setClass(DeviceScanActivity.this, ModifyURLctivity.class);
						intent.putExtra("url", eddystone.url);
					}
					startActivity(intent);
				} else {
					Toast.makeText(DeviceScanActivity.this, getString(R.string.toast_connect_tip), Toast.LENGTH_SHORT)
							.show();
				}
			}
		});
	}

	/**
	 * Scanning device using the method
	 * 
	 * @param enable
	 *            True start scanning, false stop scanning
	 */
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private void scanLeDevice(boolean enable) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			if (enable) {
				scanner.startScan(scanCallback);
			} else {
				scanner.stopScan(scanCallback);
			}
		} else {
			if (enable) {
				mBluetoothAdapter.startLeScan(mLeScanCallback);
			} else {
				mBluetoothAdapter.stopLeScan(mLeScanCallback);
			}
		}
	}

	/**
	 * Less than android 5.0 using the scan callback
	 */
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
			final Eddystone eddystone = EddystoneClass.fromScanData(device, rssi, scanRecord, DeviceScanActivity.this);
			if (eddystone == null) {
				return;
			}
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					adapter.addDevice(eddystone);
				}
			});
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
		// Request to open Bluetooth
		if (!mBluetoothAdapter.isEnabled()) {
			if (!mBluetoothAdapter.isEnabled()) {
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
		}
		scanLeDevice(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_start) {
			adapter.clear();
			scanLeDevice(true); 
			return true;
		} else if (id == R.id.action_stop) {
			scanLeDevice(false); 
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
			finish();
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// stop scanning
		scanLeDevice(false);
	}
}
