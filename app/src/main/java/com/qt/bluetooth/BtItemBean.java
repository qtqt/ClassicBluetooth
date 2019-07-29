package com.qt.bluetooth;

import android.bluetooth.BluetoothDevice;

/**
 *@date 2019/7/22
 *@desc 蓝牙bean
 *
 */

public class BtItemBean{

    public static final int STATE_BOND_NONE=-1;//未配对
    public static final int STATE_UNCONNECT=0;//未连接
    public static final int STATE_BONDING=1;//配对中
    public static final int STATE_BONDED=2;//已配对
    public static final int STATE_CONNECTING=3;//连接中
    public static final int STATE_CONNECTED=4;//已连接
    public static final int STATE_DISCONNECTING=5;//断开中
    public static final int STATE_DISCONNECTED=6;//已断开(但还保存)
    private int state;

    private BluetoothDevice bluetoothDevice;
    public BtItemBean(){

    }
    public BtItemBean(BluetoothDevice bluetoothDevice){
        this.bluetoothDevice=bluetoothDevice;
    }

    public BtItemBean(int state, BluetoothDevice bluetoothDevice){
        this.state=state;
        this.bluetoothDevice=bluetoothDevice;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }
}
