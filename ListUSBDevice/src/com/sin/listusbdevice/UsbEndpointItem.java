package com.sin.listusbdevice;

import android.annotation.SuppressLint;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;

public class UsbEndpointItem {
	public int intefaceIndex;
	public int endpointIndex;
	public UsbDeviceConnection usbDeviceConnection;
	public UsbEndpoint usbEndpoint;

	public UsbEndpointItem(int intefaceIndex, int endpointIndex, UsbDeviceConnection usbDeviceConnection, UsbEndpoint usbEndpoint) {
		super();
		this.intefaceIndex = intefaceIndex;
		this.endpointIndex = endpointIndex;
		this.usbDeviceConnection = usbDeviceConnection;
		this.usbEndpoint = usbEndpoint;
	}

	@SuppressLint("DefaultLocale")
	public int send(byte[] data) {
		// byte [] dt = new byte[]{1,1,1,1,1,1};
		return usbDeviceConnection.bulkTransfer(usbEndpoint, data, data.length, 1000);
	}

	private byte[] rbuf = new byte[1024];

	public byte[] recv() {
		int l = usbDeviceConnection.bulkTransfer(usbEndpoint, rbuf, rbuf.length, 1000);
		if (l < 0) {
			return null;
		} else {
			byte[] rt = new byte[l];
			System.arraycopy(rbuf, 0, rt, 0, l);
			return rt;
		}
	}
}
