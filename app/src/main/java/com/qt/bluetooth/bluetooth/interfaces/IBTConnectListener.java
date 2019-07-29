package com.qt.bluetooth.bluetooth.interfaces;

import android.bluetooth.BluetoothDevice;

import java.util.List;

/**
 *@date 2019/7/23
 *@desc 蓝牙连接监听
 *
 */

public interface IBTConnectListener {
    void onConnecting(BluetoothDevice bluetoothDevice);//连接中
    void onConnected(BluetoothDevice bluetoothDevice);//连接成功
    void onDisConnecting(BluetoothDevice bluetoothDevice);//断开中
    void onDisConnect(BluetoothDevice bluetoothDevice);//断开
    void onConnectedDevice(List<BluetoothDevice> devices);//已连接的设备
}
