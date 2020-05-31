package com.axaet.device;

import java.io.Serializable;
import java.util.List;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;


/**
 * Specific data analysis, you can see here
 * https://github.com/google/eddystone/blob/master/protocol-specification.md
 *
 */
@SuppressLint("DefaultLocale")
public class EddystoneClass {

	// Eddystone-UID
	// 0201060303aafe1716aafe00 e9(txpower,Need to turn back to 10 binary data)
	// 001122334455667788ff(namespace ID) aabbccddee99(instance ID) 0000(Retain)
	
	// Eddystone-URL Broadcast data has been converted into 16 binary data.
	// http://www.jerry@163.com
	// 020106 0303aafe 10(length,From the right of the 16 to the last 07) 16aafe
	// 10 e9(txpower,Need to turn back to 10 binary data) 00(http://www.) 4a(j)
	// 65(e) 72(r) 72(r) 79(y) 40(@) 31(1) 36(6) 33(3) 07(.com)
	/**
	 * Different modes of transmission of the broadcast data are different, so
	 * the following constants are used to distinguish different patterns
	 */
	private static String iBeaconType = "0215";
	private static String UIDType = "00";
	private static String other = "0303aafe";
	private static String URLType = "10";

	@SuppressLint("DefaultLocale")
	public static Eddystone fromScanData(BluetoothDevice device, int rssi, byte[] scanData, Context context) {
		String scanDataHex = EddystoneSDK.bytesToHexString(scanData);
		String tempiBeacon = scanDataHex.substring(14, 18);
		String tempOther = scanDataHex.substring(6, 14);
		if (iBeaconType.equals(tempiBeacon)) {
			int startByte = 2;
			boolean patternFound = false;
			while (startByte <= 5) {
				if (((int) scanData[startByte + 2] & 0xff) == 0x02 && ((int) scanData[startByte + 3] & 0xff) == 0x15) {
					patternFound = true;
					break;
				}
				startByte++;
			}
			if (patternFound == false) {
				return null;
			}
			Eddystone iBeacon = new Eddystone();
			iBeacon.major = (scanData[startByte + 20] & 0xff) * 0x100 + (scanData[startByte + 21] & 0xff);
			iBeacon.minor = (scanData[startByte + 22] & 0xff) * 0x100 + (scanData[startByte + 23] & 0xff);
			iBeacon.rssi = rssi;
			byte[] proximityUuidBytes = new byte[16];
			System.arraycopy(scanData, startByte + 4, proximityUuidBytes, 0, 16);
			String hexString = EddystoneSDK.bytesToHexString(proximityUuidBytes);
			StringBuilder sb = new StringBuilder();
			sb.append(hexString.substring(0, 8));
			sb.append("-");
			sb.append(hexString.substring(8, 12));
			sb.append("-");
			sb.append(hexString.substring(12, 16));
			sb.append("-");
			sb.append(hexString.substring(16, 20));
			sb.append("-");
			sb.append(hexString.substring(20, 32));
			iBeacon.deviceUuid = sb.toString().toUpperCase();
			if (device != null) {
				iBeacon.deviceAddress = device.getAddress();
				iBeacon.deviceName = device.getName();
			}
			iBeacon.type = 1;
			iBeacon.context = context;
			iBeacon.bluetoothDevice = device;
			return iBeacon;
		} else if (other.equals(tempOther)) {
			try {
				String UID = scanDataHex.substring(22, 24);
				if (UIDType.equals(UID)) {
					Eddystone UidInstance = new Eddystone();
					if (device != null) {
						UidInstance.deviceName = device.getName();
						UidInstance.deviceAddress = device.getAddress();
					}
					UidInstance.instanceID = scanDataHex.substring(46, 58).toUpperCase();
					UidInstance.rssi = rssi;
					UidInstance.nameSpaceID = scanDataHex.substring(26, 46).toUpperCase();
					UidInstance.bluetoothDevice = device;
					UidInstance.context = context;
					UidInstance.type = 2;
					return UidInstance;
				} else if (URLType.equals(UID)) {
					Eddystone UrlInstance = new Eddystone();
					if (device != null) {
						UrlInstance.deviceName = device.getName();
						UrlInstance.deviceAddress = device.getAddress();
					}
					UrlInstance.rssi = rssi;
					UrlInstance.url = EddystoneSDK
							.getUrlPrefix(
									scanDataHex
											.substring(26,
													28))
							+ EddystoneSDK.getUrlSuffix(scanDataHex.substring(28,
									28 + (Integer.parseInt(scanDataHex.substring(14, 16), 16) - 6) * 2));
					UrlInstance.bluetoothDevice = device;
					UrlInstance.context = context;
					UrlInstance.type = 3;
					return UrlInstance;
				}
			} catch (Exception e) {
				return null;
			}
		}
		return null;
	}

	public static class Eddystone implements Comparable<Eddystone>, Serializable {

		/**
		 * Three types of parameters
		 */
		private static final long serialVersionUID = 1L;
		public String deviceName = "";
		public String deviceAddress = "";
		public int rssi;
		/**
		 * Used to distinguish between different types
		 */
		public int type;
		/**
		 * IBeacon unique parameters
		 */
		public int major;
		public int minor;
		public String deviceUuid = "";
		/**
		 * UID unique parameters
		 */
		public String nameSpaceID = "";
		public String instanceID = "";
		/**
		 * url unique parameters
		 */
		public String url = "";
		/**
		 * the device has been disconnected
		 */
		public final static int DEVICE_STATUS_DISCONNECTED = 0;
		/**
		 * the device has been connected
		 */
		public final static int DEVICE_STATUS_CONNECTED = 1;

		private int deviceStatus;

		private BluetoothDevice bluetoothDevice;
		private BluetoothGatt mBluetoothGatt;

		private DeviceListener deviceListener;
		private Context context;

		@Override
		public int compareTo(Eddystone another) {
			if (this.rssi < another.rssi)
				return 1;
			if (this.rssi > another.rssi)
				return -1;
			return 0;
		}

		public void setDeviceListener(DeviceListener deviceListener) {
			this.deviceListener = deviceListener;
		}

		/**
		 * get the device deviceAddress
		 * 
		 * @return the device deviceAddress
		 */
		public String getdeviceAddress() {
			return this.deviceAddress;
		}

		/**
		 * 
		 * @return the BluetoothDevice connected
		 */
		public BluetoothDevice getBluetoothDevice() {
			return bluetoothDevice;
		}

		/**
		 * start connect the device connectGatt and mBluetoothGatt.connect();
		 */
		public void connect() {

			if (this.mBluetoothGatt != null) {
				this.mBluetoothGatt.connect();
				return;
			}
			if (this.bluetoothDevice != null) {
				this.mBluetoothGatt = this.bluetoothDevice.connectGatt(this.context, false, this.gattCallback);
			}
		}

		/**
		 * remove the connection with the device
		 */
		public void disConnect() {
			if (mBluetoothGatt != null) {
				mBluetoothGatt.disconnect();
				mBluetoothGatt.close();
				mBluetoothGatt = null;
			}
			deviceStatus = DEVICE_STATUS_DISCONNECTED;
		}

		/**
		 * Descriptor value to read from the remote device
		 * 
		 * @return true, if the read operation was initiated successfully
		 */
		public boolean readRssi() {
			if (mBluetoothGatt != null && isConnected()) {
				return mBluetoothGatt.readRemoteRssi();
			}
			return false;
		}

		public boolean isConnected() {
			if (deviceStatus == 1) {
				return true;
			} else if (deviceStatus == 0) {
				return false;
			}
			return false;
		}

		private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

			@Override
			public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
				super.onCharacteristicChanged(gatt, characteristic);
				byte[] value = characteristic.getValue();
				if (deviceListener != null) {
					deviceListener.onGetValue(deviceAddress, value, bluetoothDevice);
				}

			}

			@Override
			public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
					int status) {
				super.onCharacteristicWrite(gatt, characteristic, status);
				byte[] value = characteristic.getValue();
				if (status == BluetoothGatt.GATT_SUCCESS && deviceListener != null) {
					deviceListener.onSendSuccess(deviceAddress, value, bluetoothDevice);
				}
			}

			@Override
			public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
				super.onConnectionStateChange(gatt, status, newState);
				if (deviceListener != null) {
					if (newState == BluetoothGatt.STATE_CONNECTED) {
						gatt.discoverServices();
					} else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
						deviceStatus = DEVICE_STATUS_DISCONNECTED;
						deviceListener.onDisconnected(deviceAddress, bluetoothDevice);
					}
					if (status != BluetoothGatt.GATT_SUCCESS) {
						disConnect();
						deviceListener.onDisconnected(deviceAddress, bluetoothDevice);
					}
				}
			}

			@Override
			public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
				super.onReadRemoteRssi(gatt, rssi, status);
				if (status == BluetoothGatt.GATT_SUCCESS && deviceListener != null) {
					deviceListener.onGetRssi(deviceAddress, rssi, bluetoothDevice);
				}
			}

			@Override
			public void onServicesDiscovered(BluetoothGatt gatt, int status) {
				super.onServicesDiscovered(gatt, status);
				if (status == BluetoothGatt.GATT_SUCCESS && deviceListener != null) {
					deviceStatus = DEVICE_STATUS_CONNECTED;
					enableNotication();
					deviceListener.onConnected(deviceAddress, bluetoothDevice);
				}
			}
		};

		
		public void sendDatatoDevice(byte[] bs) {
			if (mBluetoothGatt==null) return;
			BluetoothGattService alertService = mBluetoothGatt.getService(UUIDUtils.UUID_LOST_SERVICE);
			if (alertService == null) {
				return;
			}
			BluetoothGattCharacteristic gattCharacteristic = alertService.getCharacteristic(UUIDUtils.UUID_LOST_WRITE);
			if (gattCharacteristic == null) {
				return;
			}
			gattCharacteristic.setValue(bs);
			gattCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
			mBluetoothGatt.writeCharacteristic(gattCharacteristic);
		}

		/**
		 * notification
		 */
		public void enableNotication() {
			BluetoothGattService nableService = mBluetoothGatt.getService(UUIDUtils.UUID_LOST_SERVICE);
			if (nableService == null) {
				return;
			}
			BluetoothGattCharacteristic gattCharacteristic = nableService.getCharacteristic(UUIDUtils.UUID_LOST_ENABLE);
			if (gattCharacteristic == null) {
				return;
			}
			setCharacteristicNotification(gattCharacteristic, true);
		}

		/**
		 * Enables or disables notification on a give characteristic.
		 * 
		 * @param characteristic
		 *            Characteristic to act on.
		 * @param enabled
		 *            If true, enable notification. False otherwise.
		 */
		private void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
			if (mBluetoothGatt == null) {
				return;
			}
			mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
			if (UUIDUtils.UUID_LOST_ENABLE.equals(characteristic.getUuid())) {
				BluetoothGattDescriptor descriptor = characteristic
						.getDescriptor(UUIDUtils.CLIENT_CHARACTERISTIC_CONFIG);
				descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
				mBluetoothGatt.writeDescriptor(descriptor);
			}
		}

		/**
		 * start on air download
		 * 
		 * @param imageInfo
		 *            the information you want to update to
		 */
		public List<BluetoothGattService> getServices() {
			if (mBluetoothGatt != null) {
				return mBluetoothGatt.getServices();
			}
			return null;
		}
	}
}