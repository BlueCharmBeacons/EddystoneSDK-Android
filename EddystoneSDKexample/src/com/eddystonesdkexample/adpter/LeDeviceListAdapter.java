package com.eddystonesdkexample.adpter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import com.axaet.device.EddystoneClass.Eddystone;
import com.axaet.eddystonesdkexample.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class LeDeviceListAdapter extends BaseAdapter {

	public ArrayList<Eddystone> mLeDevices;
	private LayoutInflater mInflator;

	public LeDeviceListAdapter(Activity mContext) {
		super();
		mLeDevices = new ArrayList<Eddystone>();
		mInflator = mContext.getLayoutInflater();
	}

	/**
	 * add data
	 * 
	 */
	public synchronized void addDevice(Eddystone device) {
		boolean b = false;
		for (Eddystone eddystone : mLeDevices) {
			b = eddystone.deviceAddress.equals(device.deviceAddress);
			if (b) {
				mLeDevices.remove(eddystone);
				mLeDevices.add(device);
				break;
			}
		}
		if (!b) {
			mLeDevices.add(device);
		}
		// Sort according to Rssi value
		Collections.sort(this.mLeDevices, comparator);
		notifyDataSetChanged();
	}

	Comparator<Eddystone> comparator = new Comparator<Eddystone>() {
		@Override
		public int compare(Eddystone h1, Eddystone h2) {
			return h2.rssi - h1.rssi;
		}
	};

	public Eddystone getDevice(int position) {
		return mLeDevices.get(position);
	}

	public void clear() {
		mLeDevices.clear();
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mLeDevices == null ? 0 : mLeDevices.size();
	}

	@Override
	public Eddystone getItem(int i) {
		return mLeDevices.get(i);
	}

	@Override
	public long getItemId(int i) {
		return i;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		ViewHolder viewHolder;
		if (view == null) {
			view = mInflator.inflate(R.layout.item_scan, null);
			viewHolder = new ViewHolder();
			viewHolder.deviceName = (TextView) view.findViewById(R.id.deviceName);
			viewHolder.deviceAddress = (TextView) view.findViewById(R.id.deviceAddress);
			viewHolder.ID2 = (TextView) view.findViewById(R.id.ID2);
			viewHolder.ID3 = (TextView) view.findViewById(R.id.ID3);
			viewHolder.RSSI = (TextView) view.findViewById(R.id.rssi);
			viewHolder.txpower = (TextView) view.findViewById(R.id.txpower);
			viewHolder.type = (TextView) view.findViewById(R.id.type);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}
		Eddystone device = mLeDevices.get(i);
		final String deviceName = device.deviceName;
		if (deviceName != null && deviceName.length() > 0)
			viewHolder.deviceName.setText("DeviceName: " + deviceName);
		else
			viewHolder.deviceName.setText("DeviceName: unknown");
		viewHolder.deviceAddress.setText("Address: " + device.deviceAddress);

		// Display different data according to different types
		if (device.type == 1) {
			// This is the type of iBeacon,Please use the IBeacon type with some
			// field properties,Do not use device.nameSpaceID or device.url
			viewHolder.txpower.setVisibility(View.GONE);
			viewHolder.type.setText(device.deviceUuid);
			viewHolder.ID2.setVisibility(View.VISIBLE);
			viewHolder.ID3.setVisibility(View.VISIBLE);
			viewHolder.RSSI.setVisibility(View.VISIBLE);
			viewHolder.ID2.setText("ID2: " + device.major);
			viewHolder.ID3.setText("ID3: " + device.minor);
			viewHolder.RSSI.setText("Rssi: " + device.rssi);

		} else if (device.type == 2) {
			// This is the type of UID,Do not use device.major ,
			// device.major,device.deviceUuid or device.url
			viewHolder.txpower.setVisibility(View.GONE);
			viewHolder.type.setText(device.nameSpaceID);
			viewHolder.RSSI.setText("Rssi: " + device.rssi);
			viewHolder.ID2.setText("ID2: " + device.instanceID);
			viewHolder.ID3.setVisibility(View.GONE);
		} else if (device.type == 3) {
			// This is the type of URl,Do not use device.major ,
			// device.major,device.deviceUuid , device.nameSpaceID or
			// device.instanceID
			viewHolder.type.setText(device.url);
			viewHolder.txpower.setText("Rssi: " + device.rssi);
			viewHolder.ID3.setVisibility(View.GONE);
			viewHolder.ID2.setVisibility(View.GONE);
			viewHolder.RSSI.setVisibility(View.GONE);
			viewHolder.txpower.setVisibility(View.VISIBLE);
		}
		return view;
	}

	static class ViewHolder {
		TextView deviceName;
		TextView deviceAddress;
		TextView type;
		TextView ID2;
		TextView ID3;
		TextView txpower;
		TextView RSSI;
	}

}
