package com.sin.listusbdevice;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.sin.android.sinlibs.activities.BaseActivity;
import com.sin.android.sinlibs.base.Callable;
import com.sin.android.sinlibs.utils.IntervalRunner;

/**
 * DetailActivity USB设备详细界面，列出设备接口和端点信息
 * 
 * @author RobinTang
 * 
 *         2014-01-24
 */
public class DetailActivity extends BaseActivity implements OnClickListener {
	private static final String TAG = "DetailActivity";
	private static final String ACTION_FOR_PERMISSION = DetailActivity.class.getName() + ".ACTION_FOR_PERMISSION";

	private EditText et_console = null;
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
		findViewById(R.id.btn_recv).setOnClickListener(this);
		findViewById(R.id.btn_looprecv).setOnClickListener(this);
		findViewById(R.id.btn_loopsend).setOnClickListener(this);

		et_console = (EditText) findViewById(R.id.et_console);
		et_console.setText(getPreferences(MODE_PRIVATE).getString("et", ""));
		et_console.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View arg0) {
				et_console.setText(getPreferences(MODE_PRIVATE).getString("et", ""));
				return false;
			}
		});
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
				((TextView) convertView.findViewById(R.id.tv_item_endpointdirection)).setText(String.format("%s", uei.getDirection()==0x80?"IN":"OUT"));
				((TextView) convertView.findViewById(R.id.tv_item_endpointinterval)).setText(String.format("%02X", uei.getInterval()));
				((TextView) convertView.findViewById(R.id.tv_item_endpointmaxpacketsize)).setText(String.format("%02X", uei.getMaxPacketSize()));
				((TextView) convertView.findViewById(R.id.tv_item_endpointtype)).setText(String.format("%02X", uei.getType()));
				if (selInterfaceIndex == groupPosition && selEndpointIndex == childPosition) {
					convertView.setBackgroundColor(Color.WHITE);
				} else {
					convertView.setBackgroundColor(0xffEEEEEE);
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
					if (usbDeviceConnection.claimInterface(usbDevice.getInterface(groupPosition), true) == false) {
						appendLog("claimInterface fail~~");
					}
					usbEndpointItem = new UsbEndpointItem(groupPosition, childPosition, usbDeviceConnection, usbDevice.getInterface(groupPosition).getEndpoint(childPosition));
					expandableListAdapter.notifyDataSetChanged();
				}
				return false;
			}
		});
		elv_interfaces.setAdapter(expandableListAdapter);

		tv_log = (TextView) findViewById(R.id.tv_log);
		tv_log.setOnLongClickListener(new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				tv_log.setText("");
				logix = 0;
				return false;
			}
		});
	}

	private TextView tv_log = null;

	public void safeLog(String log) {
		safeCall(new Callable() {
			@Override
			public void call(Object... args) {
				appendLog((String) args[0]);
			}
		}, log);
	}

	private int logix = 1;

	public void appendLog(String log) {
		tv_log.append(logix + " ");
		++logix;
		tv_log.append(log);
		tv_log.append("\r\n");
		scrollToBottom((ScrollView) tv_log.getParent());
	}

	private void scrollToBottom(final ScrollView sv) {
		sv.post(new Runnable() {
			@Override
			public void run() {
				sv.fullScroll(ScrollView.FOCUS_DOWN);
			}
		});
	}

	private IntervalRunner recvRunner = null;
	private IntervalRunner sendRunner = null;

	@Override
	protected void onDestroy() {
		if (usbDeviceConnection != null)
			usbDeviceConnection.close();
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

	private byte[] hexToBytes(String hexStr) {
		String[] hexs = hexStr.replaceAll("[\\,,\r,\n,; ]", " ").replaceAll(" +", " ").split(" ");
		List<Byte> data = new ArrayList<Byte>();
		for (String hex : hexs) {
			try {
				data.add(Byte.parseByte(hex, 16));
			} catch (Exception e) {

			}
		}
		byte[] rets = new byte[data.size()];
		for (int i = 0; i < data.size(); ++i) {
			rets[i] = data.get(i).byteValue();
		}
		return rets;
	}

	private String bytesToHex(byte[] dt) {
		StringBuffer sb = new StringBuffer();
		if (dt == null)
			return "";
		for (int i = 0; i < dt.length; ++i) {
			sb.append(String.format("%02X ", dt[i]));
		}
		sb.append(":");
		try {
			sb.append(new String(dt, "utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	@Override
	public void onClick(View view) {
		if (usbEndpointItem == null)
			return;
		switch (view.getId()) {
		case R.id.btn_send:
			String hs = et_console.getText().toString() + " ";
			getPreferences(MODE_PRIVATE).edit().putString("et", hs).commit();
			byte[] dt = hexToBytes(hs);
			String s = "send:" + usbEndpointItem.send(dt) + "/" + dt.length;
			Log.i(TAG, s);
			appendLog(s);
			break;
		case R.id.btn_recv:
			byte[] dats = usbEndpointItem.recv();
			if (dats != null) {
				appendLog("recv(" + dats.length + "):" + bytesToHex(dats));
			} else {
				appendLog("recv failed");
			}
			break;
		case R.id.btn_looprecv:
			if (recvRunner == null) {
				rcount = 0;
				final UsbEndpointItem ri = usbEndpointItem;
				rsttime = System.currentTimeMillis();
				recvRunner = IntervalRunner.run(new Callable() {
					@Override
					public void call(Object... args) {
						int len = ri.recv_buf(rbuf, rbuf.length);
						if (len > 0)
							rcount += len;
						else
							safeLog("recv failed");
					}
				}, 0);
				((Button) view).setText(R.string.stoprecv);
			} else {
				long ct = System.currentTimeMillis() - rsttime;
				safeLog(String.format("recv %d bytes, cost %d ms, speed %dKB/s", rcount, ct, rcount / ct));
				recvRunner.stop();
				recvRunner = null;
				((Button) view).setText(R.string.looprecv);
			}
			break;
		case R.id.btn_loopsend:
			if (sendRunner == null) {
				scount = 0;
				for (int i = 0; i < sbuf.length; ++i) {
					sbuf[i] = (byte) i;
				}
				final UsbEndpointItem si = usbEndpointItem;
				ssttime = System.currentTimeMillis();
				sendRunner = IntervalRunner.run(new Callable() {
					@Override
					public void call(Object... args) {
						int len = si.send(sbuf);
						if (len > 0)
							scount += len;
						else
							safeLog("send failed");
					}
				}, 0);
				((Button) view).setText(R.string.stopsend);
			} else {
				long ct = System.currentTimeMillis() - ssttime;
				safeLog(String.format("send %d bytes, cost %d ms, speed %dKB/s", scount, ct, scount / ct));
				sendRunner.stop();
				sendRunner = null;
				((Button) view).setText(R.string.loopsend);
			}
			break;
		default:
			break;
		}
	}

	byte[] rbuf = new byte[1024];
	int rcount = 0;
	long rsttime = 0;

	byte[] sbuf = new byte[64];
	int scount = 0;
	long ssttime = 0;
}
