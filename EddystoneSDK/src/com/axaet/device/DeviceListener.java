package com.axaet.device;

import android.bluetooth.BluetoothDevice;

public interface DeviceListener {
	/**
	 * Connect successfully
	 * 
	 * @param tag
	 *            - the SWDevice's tag
	 * @param device
	 *            - the connected BluetoothDevice
	 */
	public void onConnected(String tag, BluetoothDevice device);

	/**
	 * connection failed
	 * 
	 * @param tag
	 *            - the SWDevice's tag
	 * @param device
	 *            - the disconnected BluetoothDevice
	 */
	public void onDisconnected(String tag, BluetoothDevice device);

	/**
	 * read rssi
	 * 
	 * @param tag
	 *            - the SWDevice's tag
	 * @param rssi
	 * @param device
	 */
	public void onGetRssi(String tag, int rssi, BluetoothDevice device);

	/**
	 * device response data
	 * 
	 * @param tag
	 * @param value
	 *            - the SWDevice's command; see SWDevice's final int Command
	 * @param device
	 */
	public void onGetValue(String tag, byte[] value, BluetoothDevice device);

	/**
	 * Data sent back to success
	 * 
	 * @param tag
	 * @param value
	 *            - the setting successfully sent
	 * @param device
	 */
	public void onSendSuccess(String tag, byte[] value, BluetoothDevice device);

}
