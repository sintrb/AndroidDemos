package com.sin.listusbdevice;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;

/**
 * UsbEndpointItem 端点通信类，用于和USB端点通信
 * 
 * @author RobinTang
 * 
 *         2014-01-24
 */
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

	public int send(byte[] data) {
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

	public int recv_buf(byte[] buf, int len) {
		return usbDeviceConnection.bulkTransfer(usbEndpoint, buf, len, 1000);
	}
}
