package com.qt.bluetooth.bluetooth.interfaces;


import android.bluetooth.BluetoothDevice;

import java.util.List;

/**
 *@date 2019/7/23
 *@desc 蓝牙搜索监听
 *
 */

public interface IBTScanListener {


    /**
     * 搜索开始
     */
    void onScanStart();

    /**
     * 搜索结束
     *
     * @param deviceList
     */
    void onScanStop(List<BluetoothDevice> deviceList);

    /**
     * 发现新设备
     * @param device
     */
    void onFindDevice(BluetoothDevice device);
}
