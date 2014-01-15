package com.sin.listusbdevice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.sin.android.sinlibs.activities.BaseActivity;
import com.sin.android.sinlibs.base.Callable;

public class MainActivity extends BaseActivity {
	public final String VERSION	= "1.0";
	public final int VERSION_INT = 10;
	private UsbManager usbManager;
	private ListView lv_devices;
	private ArrayList<HashMap<String, Object>> devices = new ArrayList<HashMap<String, Object>>();
	private SimpleAdapter adapter = null;
	private final BroadcastReceiver receiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			safeToast(action);
			refreshDevices();
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
		lv_devices = (ListView) findViewById(R.id.lv_devices);
		adapter = new SimpleAdapter(MainActivity.this, devices, R.layout.item_device, new String[] { "devicename", "deviceid", "vendorid", "productid" }, new int[] { R.id.tv_item_devicename, R.id.tv_item_deviceid, R.id.tv_item_vendorid, R.id.tv_item_productid });
		lv_devices.setAdapter(adapter);
		
		IntentFilter filter = new IntentFilter();            
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        this.registerReceiver(receiver, filter); 
	}

	@Override
	protected void onResume() {
		super.onResume();
		refreshDevices();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_refresh:
			refreshDevices();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	// ˢ���豸�б�
	private void refreshDevices() {
		asynCall(new Callable() {

			@Override
			public void call(Object... arg0) {
				HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
				Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
				devices.clear();
				while (deviceIterator.hasNext()) {
					UsbDevice device = deviceIterator.next();
					HashMap<String, Object> map = new HashMap<String, Object>();
					map.put("devicename", device.getDeviceName());
					map.put("deviceid", String.format("%04X", device.getDeviceId()));
					map.put("vendorid", String.format("%04X", device.getVendorId()));
					map.put("productid", String.format("%04X", device.getProductId()));
					devices.add(map);
				}
				safeCall(new Callable() {
					@Override
					public void call(Object... arg0) {
						adapter.notifyDataSetChanged();
					}
				});
			}
		});
	}
}
