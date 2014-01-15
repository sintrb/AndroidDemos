package com.sin.listusbdevice;

import java.util.HashMap;
import java.util.Iterator;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sin.android.sinlibs.activities.BaseActivity;

public class DetailActivity extends BaseActivity implements OnClickListener {
	private static final String TAG = "DetailActivity";
	private static final String ACTION_FOR_PERMISSION = DetailActivity.class.getName() + ".ACTION_FOR_PERMISSION";

	private ExpandableListView elv_interfaces = null;
	private BaseExpandableListAdapter expandableListAdapter = null;

	private int deviceid = 0;
	private UsbManager usbManager = null;
	private UsbDevice usbDevice = null;
	private UsbDeviceConnection usbDeviceConnection;
	private UsbEndpointItem usbEndpointItem = null;
	private int selInterfaceIndex = -1;
	private int selEndpointIndex = -1;
	final BroadcastReceiver permissionReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ACTION_FOR_PERMISSION.equals(action)) {
				if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
					Toast.makeText(context, "授权成功", Toast.LENGTH_SHORT).show();
					whenHadPermission();
				} else {
					Toast.makeText(context, "授权失败", Toast.LENGTH_SHORT).show();
					finish();
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_detail);
		deviceid = getIntent().getIntExtra("deviceid", 0);
		setTitle(getIntent().getStringExtra("devicename"));

		usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
		HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
		Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
		while (deviceIterator.hasNext()) {
			UsbDevice device = deviceIterator.next();
			if (device.getDeviceId() == deviceid) {
				this.usbDevice = device;
				break;
			}
		}

		IntentFilter filter = new IntentFilter(ACTION_FOR_PERMISSION);
		filter.addAction(ACTION_FOR_PERMISSION);

		this.registerReceiver(permissionReceiver, filter);
		readyOpenDevice();

		findViewById(R.id.btn_send).setOnClickListener(this);

		//
		elv_interfaces = (ExpandableListView) findViewById(R.id.elv_interfaces);
		elv_interfaces.setGroupIndicator(null);
		expandableListAdapter = new BaseExpandableListAdapter() {

			// 重写ExpandableListAdapter中的各个方法
			@Override
			public int getGroupCount() {
				return usbDevice.getInterfaceCount();
			}

			@Override
			public Object getGroup(int groupPosition) {
				return null;
			}

			@Override
			public long getGroupId(int groupPosition) {
				return groupPosition;
			}

			@Override
			public int getChildrenCount(int groupPosition) {
				return usbDevice.getInterface(groupPosition).getEndpointCount();
			}


			@Override
			public Object getChild(int groupPosition, int childPosition) {
				return null;
			}

			@Override
			public long getChildId(int groupPosition, int childPosition) {
				return childPosition;
			}

			@Override
			public boolean hasStableIds() {
				return false;
			}

			@Override
			public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
				if (convertView == null)
					convertView = LinearLayout.inflate(DetailActivity.this, R.layout.item_interface, null);
				if (isExpanded) {
					((ImageView) convertView.findViewById(R.id.iv_icon)).setImageResource(R.drawable.ico_expanded);
				} else {
					((ImageView) convertView.findViewById(R.id.iv_icon)).setImageResource(R.drawable.ico_unexpand);
				}
				UsbInterface usbInterface = usbDevice.getInterface(groupPosition);
				((TextView) convertView.findViewById(R.id.tv_item_interfaceid)).setText(String.format("%02X", usbInterface.getId()));
				((TextView) convertView.findViewById(R.id.tv_item_interfaceclass)).setText(String.format("%02X", usbInterface.getInterfaceClass()));
				((TextView) convertView.findViewById(R.id.tv_item_interfaceprotocol)).setText(String.format("%02X", usbInterface.getInterfaceProtocol()));
				((TextView) convertView.findViewById(R.id.tv_item_interfacesubclass)).setText(String.format("%02X", usbInterface.getInterfaceSubclass()));
				((TextView) convertView.findViewById(R.id.tv_item_endpointcount)).setText(String.format("%02X", usbInterface.getEndpointCount()));
				return convertView;
			}

			@Override
			public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
				if (convertView == null)
					convertView = LinearLayout.inflate(DetailActivity.this, R.layout.item_endpoint, null);
				UsbEndpoint uei = usbDevice.getInterface(groupPosition).getEndpoint(childPosition);
				((TextView) convertView.findViewById(R.id.tv_item_endpointnumber)).setText(String.format("%02X", uei.getEndpointNumber()));
				((TextView) convertView.findViewById(R.id.tv_item_endpointaddress)).setText(String.format("%02X", uei.getAddress()));
				((TextView) convertView.findViewById(R.id.tv_item_endpointattributes)).setText(String.format("%02X", uei.getAttributes()));
				((TextView) convertView.findViewById(R.id.tv_item_endpointdirection)).setText(String.format("%02X", uei.getDirection()));
				((TextView) convertView.findViewById(R.id.tv_item_endpointinterval)).setText(String.format("%02X", uei.getInterval()));
				((TextView) convertView.findViewById(R.id.tv_item_endpointmaxpacketsize)).setText(String.format("%02X", uei.getMaxPacketSize()));
				((TextView) convertView.findViewById(R.id.tv_item_endpointtype)).setText(String.format("%02X", uei.getType()));
				if (selInterfaceIndex == groupPosition && selEndpointIndex == childPosition) {
					convertView.setBackgroundColor(0xffEEEEEE);
				} else {
					convertView.setBackgroundColor(Color.WHITE);
				}
				return convertView;
			}

			@Override
			public boolean isChildSelectable(int groupPosition, int childPosition) {
				return true;
			}
		};
		elv_interfaces.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				if (selInterfaceIndex != groupPosition || selEndpointIndex != childPosition) {
					selInterfaceIndex = groupPosition;
					selEndpointIndex = childPosition;
					usbEndpointItem = new UsbEndpointItem(groupPosition, childPosition, usbDeviceConnection, usbDevice.getInterface(groupPosition).getEndpoint(childPosition));
					expandableListAdapter.notifyDataSetChanged();
				}
				return false;
			}
		});
		elv_interfaces.setAdapter(expandableListAdapter);
	}

	@Override
	protected void onDestroy() {
		this.unregisterReceiver(permissionReceiver);
		super.onDestroy();
	}

	private void whenHadPermission() {
		usbDeviceConnection = usbManager.openDevice(usbDevice);
	}

	private void readyOpenDevice() {
		if (this.usbDevice == null) {
			return;
		}
		if (usbManager.hasPermission(usbDevice)) {
			whenHadPermission();
		} else {
			PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_FOR_PERMISSION), 0);
			usbManager.requestPermission(usbDevice, mPermissionIntent);
		}
	}

	@Override
	public void onClick(View view) {
		if (usbEndpointItem == null)
			return;
		switch (view.getId()) {
		case R.id.btn_send:
			Log.i(TAG, "send:" + usbEndpointItem.send());
			break;

		default:
			break;
		}
	}
}
