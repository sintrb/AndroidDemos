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
	@Override
	public String toString() {
		return String.format("IF:%d  A:%02X AT:%02X D:%02X EN:%02X IV:%02X MPS:%02X T:%02X", intefaceIndex, 
				usbEndpoint.getAddress(), 
				usbEndpoint.getAttributes(), 
				usbEndpoint.getDirection(), 
				usbEndpoint.getEndpointNumber(), 
				usbEndpoint.getInterval(), 
				usbEndpoint.getMaxPacketSize(), 
				usbEndpoint.getType());
	}
	
	
	public int send(){
		byte [] dt = new byte[]{1,2,3,4};
		return usbDeviceConnection.bulkTransfer(usbEndpoint, dt, dt.length, 1000);
	}
}
