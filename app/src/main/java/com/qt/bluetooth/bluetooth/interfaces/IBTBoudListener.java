package com.qt.bluetooth.bluetooth.interfaces;

import android.bluetooth.BluetoothDevice;

/**
 *@date 2019/7/23
 *@desc 蓝牙配对监听
 *
 */

public interface IBTBoudListener {

    /**
     * 设备配对状态改变
     * int BOND_NONE = 10; //配对没有成功
     * int BOND_BONDING = 11; //配对中
     * int BOND_BONDED = 12; //配对成功
     */
    void onBondStateChange(BluetoothDevice dev);
}
