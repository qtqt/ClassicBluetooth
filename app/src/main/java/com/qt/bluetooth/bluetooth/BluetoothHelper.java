package com.qt.bluetooth.bluetooth;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;

import com.qt.bluetooth.bluetooth.interfaces.IBTBoudListener;
import com.qt.bluetooth.bluetooth.interfaces.IBTConnectListener;
import com.qt.bluetooth.bluetooth.interfaces.IBTScanListener;
import com.qt.bluetooth.bluetooth.interfaces.IBTStateListener;
import com.qt.bluetooth.bluetooth.interfaces.IBluetoothHelper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *@date 2019/7/23
 *@desc 蓝牙辅助类
 *
 */

public class BluetoothHelper implements IBluetoothHelper {
    private final String TAG="BluetoothHelper";
    private Context mContext;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothadapter;
    private BluetoothA2dp mBluetoothA2dp;
    private BluetoothHeadset mBluetoothHeadset;
//    private BluetoothHealth mBluetoothHealth;
    private IntentFilter mFilter;

    private IBTStateListener mBTStateListener;//蓝牙状态监听
    private IBTScanListener mBTScanListener;//蓝牙搜索监听
    private IBTBoudListener mBTBoudListener;//蓝牙绑定监听
    private IBTConnectListener mBTConnectListener;//连接监听
    private boolean isBackConDev;//是否返回已连接的设备
    private boolean isA2dpComplete,isHeadsetComplete;

    @Override
    public void init(Context context) {
        mContext=context.getApplicationContext();
        mBluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothadapter = mBluetoothManager.getAdapter();
        isA2dpComplete=false;
        isHeadsetComplete=false;
        mBluetoothadapter.getProfileProxy(mContext,mProfileServiceListener, BluetoothProfile.A2DP);
        mBluetoothadapter.getProfileProxy(mContext,mProfileServiceListener, BluetoothProfile.HEADSET);
//        mBluetoothadapter.getProfileProxy(mContext,mProfileServiceListener, BluetoothProfile.HEALTH);
        if(mFilter==null){
            mContext.registerReceiver(mBluetoothReceiver,makeFilter());
        }
    }


    private IntentFilter makeFilter() {
        if(mFilter==null){
            mFilter = new IntentFilter();
            mFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);//状态改变
            mFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED); //蓝牙开关状态
            mFilter.addAction(BluetoothDevice.ACTION_FOUND);//蓝牙发现新设备(未配对)
            mFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED); //蓝牙开始搜索
            mFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED); //蓝牙搜索结束
            mFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED); //设备配对状态改变
            mFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);//设备建立连接
            mFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED); //设备断开连接
            mFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED); //BluetoothAdapter连接状态
            mFilter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED); //BluetoothHeadset连接状态
            mFilter.addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED); //BluetoothA2dp连接状态
        }
        return mFilter;
    }


    /**
     * 连接A2dp 与 HeadSet
     * @param device
     * @param device
     */
    private boolean connectA2dpAndHeadSet(Class btClass,BluetoothProfile bluetoothProfile,BluetoothDevice device){
        setPriority(device, 100); //设置priority
        try {
            //通过反射获取BluetoothA2dp中connect方法（hide的），进行连接。
            Method connectMethod =btClass.getMethod("connect",
                    BluetoothDevice.class);
            connectMethod.setAccessible(true);
            connectMethod.invoke(bluetoothProfile, device);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 断开A2dp 与 HeadSet
     * @param device
     */
    private boolean disConnectA2dpAndHeadSet(Class btClass,BluetoothProfile bluetoothProfile,BluetoothDevice device){
        setPriority(device, 0);
        try {
            //通过反射获取BluetoothA2dp中connect方法（hide的），断开连接。
            Method connectMethod =btClass.getMethod("disconnect",
                    BluetoothDevice.class);
            connectMethod.setAccessible(true);
            connectMethod.invoke(bluetoothProfile, device);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 设置优先级
     * 优先级是必要的，否则可能导致连接或断开连接失败等问题
     * @param device
     * @param priority
     */
    private void setPriority(BluetoothDevice device, int priority) {
        if (mBluetoothA2dp == null) return;
        try {//通过反射获取BluetoothA2dp中setPriority方法（hide的），设置优先级
            Method connectMethod =BluetoothA2dp.class.getMethod("setPriority",
                    BluetoothDevice.class,int.class);
            connectMethod.setAccessible(true);
            connectMethod.invoke(mBluetoothA2dp, device, priority);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    @Override
    public boolean open() {//打开蓝牙 ture-打开成功
        if(mBluetoothadapter==null){
            return false;
        }
        return mBluetoothadapter.enable();
    }

    @Override
    public boolean close() {//关闭蓝牙
        if(mBluetoothadapter==null){
            return true;
        }
        return mBluetoothadapter.disable();
    }

    @Override
    public boolean startDiscovery() {//搜索蓝牙
        if(mBluetoothadapter==null){
            return false;
        }
        if (mBluetoothadapter.isDiscovering()) {
            mBluetoothadapter.cancelDiscovery();
        }

        return mBluetoothadapter.startDiscovery();
    }

    @Override
    public boolean stopDiscovery() {//停止搜索蓝牙
        if(mBluetoothadapter==null||!mBluetoothadapter.isDiscovering()){
            return true;
        }
        return mBluetoothadapter.cancelDiscovery();
    }

    @Override
    public String getName() {//获取本地蓝牙名称
        if(mBluetoothadapter==null){
            return null;
        }
        return mBluetoothadapter.getName();
    }

    @Override
    public boolean setName(String name) {//设置蓝牙的名称
        if (mBluetoothadapter == null) {
            return false;
        }
        return mBluetoothadapter.setName(name);
    }

    @Override
    public String getAddress() {//获取本地蓝牙地址
        if(mBluetoothadapter==null){
            return null;
        }
        return mBluetoothadapter.getAddress();
    }

    @Override
    public boolean isEnable() {//蓝牙是否可用，即是否打开
        if(mBluetoothadapter==null){
            return false;
        }
        return mBluetoothadapter.isEnabled();
    }

    @Override
    public boolean isSupport() {//是否支持蓝牙
        return mBluetoothadapter==null?false:true;
    }

    @Override
    public Set<BluetoothDevice> getBondedDevices() {//获取以配对设备
        if(mBluetoothadapter==null){
            return null;
        }
        return mBluetoothadapter.getBondedDevices();
    }

    @Override
    public boolean createBond(BluetoothDevice device) {//配对
        if(device==null){
            return false;
        }
        return device.createBond();
    }

    @Override
    public boolean removeBond(BluetoothDevice device) {//取消配对
        Class btDeviceCls = BluetoothDevice.class;
        Method removeBond = null;
        try {
            removeBond = btDeviceCls.getMethod("removeBond");
            removeBond.setAccessible(true);
            return (boolean) removeBond.invoke(device);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean connect(BluetoothDevice device) {
        int styleMajor = device.getBluetoothClass().getMajorDeviceClass();
        boolean isConnect=false;
        switch (styleMajor) {
            case BluetoothClass.Device.Major.AUDIO_VIDEO://音频设备
                if(connectA2dpAndHeadSet(BluetoothHeadset.class,mBluetoothHeadset,device)){
                    isConnect=true;
                }
                if(connectA2dpAndHeadSet(BluetoothA2dp.class,mBluetoothA2dp,device)){
                    isConnect=true;
                }
                return isConnect;
//            case BluetoothClass.Device.Major.COMPUTER://电脑
//                break;
//            case BluetoothClass.Device.Major.HEALTH://健康状况
//                break;
//            case BluetoothClass.Device.Major.IMAGING://镜像，映像
//                break;
            case BluetoothClass.Device.Major.MISC://麦克风
                if(connectA2dpAndHeadSet(BluetoothHeadset.class,mBluetoothHeadset,device)){
                    isConnect=true;
                }
                if(connectA2dpAndHeadSet(BluetoothA2dp.class,mBluetoothA2dp,device)){
                    isConnect=true;
                }
                return isConnect;
//            case BluetoothClass.Device.Major.NETWORKING://网络
//                break;
//            case BluetoothClass.Device.Major.PERIPHERAL://外部设备
//                break;
            case BluetoothClass.Device.Major.PHONE://电话
                if(connectA2dpAndHeadSet(BluetoothHeadset.class,mBluetoothHeadset,device)){
                    isConnect=true;
                }
                if(connectA2dpAndHeadSet(BluetoothA2dp.class,mBluetoothA2dp,device)){
                    isConnect=true;
                }
                return isConnect;
//            case BluetoothClass.Device.Major.TOY://玩具
//                break;
//            case BluetoothClass.Device.Major.UNCATEGORIZED://未知的
//                break;
//            case BluetoothClass.Device.Major.WEARABLE://穿戴设备
//                break;
        }
        if(connectA2dpAndHeadSet(BluetoothHeadset.class,mBluetoothHeadset,device)){
            isConnect=true;
        }
        if(connectA2dpAndHeadSet(BluetoothA2dp.class,mBluetoothA2dp,device)){
            isConnect=true;
        }
        return isConnect;
    }

    @Override
    public boolean disconnect(BluetoothDevice device) {
        boolean isDisconnect=false;
        if(mBluetoothA2dp!=null){
            List<BluetoothDevice> devices=mBluetoothA2dp.getConnectedDevices();
            if(devices!=null&&devices.contains(device)){
                Log.d(TAG,"disconnect A2dp");
                isDisconnect=disConnectA2dpAndHeadSet(BluetoothA2dp.class,mBluetoothA2dp,device);
            }
        }
        if(mBluetoothHeadset!=null){
            List<BluetoothDevice> devices=mBluetoothHeadset.getConnectedDevices();
            if(devices!=null&&devices.contains(device)){
                Log.d(TAG,"disconnect Headset");
                isDisconnect=disConnectA2dpAndHeadSet(BluetoothHeadset.class,mBluetoothHeadset,device);
            }
        }
        return isDisconnect;
//        int styleMajor = device.getBluetoothClass().getMajorDeviceClass();
//        switch (styleMajor) {
//            case BluetoothClass.Device.Major.AUDIO_VIDEO://音频设备
//                return disConnectA2dpAndHeadSet(BluetoothA2dp.class,device);
//            case BluetoothClass.Device.Major.COMPUTER://电脑
//                break;
//            case BluetoothClass.Device.Major.HEALTH://健康状况
//                return disConnectA2dpAndHeadSet(BluetoothHealth.class,device);
//            case BluetoothClass.Device.Major.IMAGING://镜像，映像
//                break;
//            case BluetoothClass.Device.Major.MISC://麦克风
//                break;
//            case BluetoothClass.Device.Major.NETWORKING://网络
//                break;
//            case BluetoothClass.Device.Major.PERIPHERAL://外部设备
//                break;
//            case BluetoothClass.Device.Major.PHONE://电话
//                return disConnectA2dpAndHeadSet(BluetoothHeadset.class,device);
//            case BluetoothClass.Device.Major.TOY://玩具
//                break;
//            case BluetoothClass.Device.Major.UNCATEGORIZED://未知的
//                break;
//            case BluetoothClass.Device.Major.WEARABLE://穿戴设备
//                break;
//        }
//        return disConnectA2dpAndHeadSet(BluetoothA2dp.class,device);
    }

    @Override
    public void destroy() {
        if(mFilter!=null){
            mFilter=null;
            mContext.unregisterReceiver(mBluetoothReceiver);
        }
        isA2dpComplete=false;
        isHeadsetComplete=false;
        mBluetoothadapter.closeProfileProxy(BluetoothProfile.A2DP,mBluetoothA2dp);
        mBluetoothadapter.closeProfileProxy(BluetoothProfile.HEADSET,mBluetoothHeadset);

    }

    @Override
    public void getConnectedDevices() {
        if(isBackConDev){
            return;
        }
        isBackConDev=true;
        if(isA2dpComplete&&isHeadsetComplete){
            List<BluetoothDevice> devices=new ArrayList<>();
            if(mBluetoothA2dp!=null){
//                removeA2dpMacEqual();
                List<BluetoothDevice> deviceList=mBluetoothA2dp.getConnectedDevices();
                if(deviceList!=null&&deviceList.size()>0){

                    devices.addAll(deviceList);
                }
            }
            if(mBluetoothHeadset!=null){
//                removeHeadsetMacEqual();
                List<BluetoothDevice> deviceList=mBluetoothHeadset.getConnectedDevices();
                if(deviceList!=null&&deviceList.size()>0){
                    devices.addAll(deviceList);
                }
            }
            mBTConnectListener.onConnectedDevice(devices);
            isBackConDev=false;
        }

    }

//    /**
//     * 移除A2dp mac相等设备
//     */
//    private void removeA2dpMacEqual(){
//        if(mBluetoothA2dp==null){
//            return;
//        }
//        List<BluetoothDevice> deviceList=mBluetoothA2dp.getConnectedDevices();
//        if(deviceList==null||deviceList.size()<1){
//            return;
//        }
//        for(int i=0;i<deviceList.size();){
//            BluetoothDevice bluetoothDevice=deviceList.get(i);
//            boolean isSkip=false;
//            for(int j=i+1;j<deviceList.size();){
//                BluetoothDevice device=deviceList.get(j);
//                if(!TextUtils.isEmpty(device.getAddress())&&device.getAddress().equals(bluetoothDevice.getAddress())){
//                    isSkip=true;
//                    if(mBluetoothA2dp.getConnectionState(bluetoothDevice) == BluetoothA2dp.STATE_CONNECTED){
//                        deviceList.remove(device);
//                    }else if(mBluetoothA2dp.getConnectionState(device) == BluetoothA2dp.STATE_CONNECTED){
//                        deviceList.remove(bluetoothDevice);
//                    }else{
//                        deviceList.remove(bluetoothDevice);
//                    }
//                    break;
//                }
//                j++;
//            }
//            if(isSkip){
//                continue;
//            }
//            i++;
//        }
//    }

//    /**
//     * 移除Headset mac相等设备
//     */
//    private void removeHeadsetMacEqual(){
//        if(mBluetoothHeadset==null){
//            return;
//        }
//        List<BluetoothDevice> deviceList=mBluetoothHeadset.getConnectedDevices();
//        if(deviceList==null||deviceList.size()<1){
//            return;
//        }
//        for(int i=0;i<deviceList.size();){
//            BluetoothDevice bluetoothDevice=deviceList.get(i);
//            boolean isSkip=false;
//            for(int j=i+1;j<deviceList.size();){
//                BluetoothDevice device=deviceList.get(j);
//                if(!TextUtils.isEmpty(device.getAddress())&&device.getAddress().equals(bluetoothDevice.getAddress())){
//                    isSkip=true;
//                    if(mBluetoothHeadset.getConnectionState(bluetoothDevice) == BluetoothHeadset.STATE_CONNECTED){
//                        deviceList.remove(device);
//                    }else if(mBluetoothHeadset.getConnectionState(device) == BluetoothHeadset.STATE_CONNECTED){
//                        deviceList.remove(bluetoothDevice);
//                    }else{
//                        deviceList.remove(bluetoothDevice);
//                    }
//                    break;
//                }
//                j++;
//            }
//            if(isSkip){
//                continue;
//            }
//            i++;
//        }
//    }

    @Override
    public boolean isConnected(BluetoothDevice device) {//是否连接
        if(mBluetoothA2dp!=null&&mBluetoothA2dp.getConnectionState(device) == BluetoothA2dp.STATE_CONNECTED){
            Log.d(TAG,"isConnected name="+device.getName());
//            removeA2dpMacEqual();
            List<BluetoothDevice> bluetoothDeviceList=mBluetoothA2dp.getConnectedDevices();
            if(bluetoothDeviceList!=null&&bluetoothDeviceList.size()>0){
                for(BluetoothDevice bluetoothDevice:bluetoothDeviceList){
                    if(!TextUtils.isEmpty(device.getAddress())&&device.getAddress().equals(bluetoothDevice.getAddress())){
                        return true;
                    }
                }
            }

        }
        if(mBluetoothHeadset!=null&&mBluetoothHeadset.getConnectionState(device) == BluetoothHeadset.STATE_CONNECTED){
            Log.d(TAG,"isConnected name="+device.getName());
//            removeHeadsetMacEqual();
            List<BluetoothDevice> bluetoothDeviceList=mBluetoothHeadset.getConnectedDevices();
            if(bluetoothDeviceList!=null&&bluetoothDeviceList.size()>0){
                for(BluetoothDevice bluetoothDevice:bluetoothDeviceList){
                    if(!TextUtils.isEmpty(device.getAddress())&&device.getAddress().equals(bluetoothDevice.getAddress())){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean setDiscoverableTimeout(int timeout) {
        try {//得到指定的类中的方法
//            Method method = BluetoothAdapter.class.getMethod("setDiscoverableTimeout", int.class);
//            method.setAccessible(true);
//            method.invoke(mBluetoothadapter, timeout);//根据测试，发现这一函数的参数无论传递什么值，都是永久可见的

            Method setDiscoverableTimeout = BluetoothAdapter.class.getMethod("setDiscoverableTimeout",int.class);
            setDiscoverableTimeout.setAccessible(true);
            Method setScanMode =BluetoothAdapter.class.getMethod("setScanMode",int.class,int.class);
            setScanMode.setAccessible(true);setDiscoverableTimeout.invoke(mBluetoothadapter,timeout);
            setScanMode.invoke(mBluetoothadapter,BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE,timeout);
            return true;
        } catch (Exception e) {
            Log.d(TAG,"setDiscoverableTimeout msg="+e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void setBTStateListener(IBTStateListener btStateListener) {
        mBTStateListener=btStateListener;
    }

    @Override
    public void setBTScanListener(IBTScanListener btScanListener) {
        mBTScanListener=btScanListener;

    }

    @Override
    public void setBTBoudListener(IBTBoudListener btBoudListener) {
        mBTBoudListener=btBoudListener;
    }

    @Override
    public void setBTConnectListener(IBTConnectListener btConnectListener) {
        mBTConnectListener=btConnectListener;
    }

    //A2dp
     private BluetoothProfile.ServiceListener mProfileServiceListener=new BluetoothProfile.ServiceListener() {
         @Override
         public void onServiceConnected(int profile, BluetoothProfile proxy) {
             Log.i(TAG, "onServiceConnected profile="+profile);
             if(profile == BluetoothProfile.A2DP){//播放音乐
                 mBluetoothA2dp = (BluetoothA2dp) proxy; //转换
                 isA2dpComplete=true;
             }else if(profile == BluetoothProfile.HEADSET){//打电话
                 mBluetoothHeadset = (BluetoothHeadset) proxy;
                 isHeadsetComplete=true;
             }
             if(isA2dpComplete&&isHeadsetComplete&&isBackConDev&&mBTConnectListener!=null){
                 List<BluetoothDevice> devices=new ArrayList<>();
                 if(mBluetoothA2dp!=null){
//                     removeA2dpMacEqual();
                     List<BluetoothDevice> deviceList=mBluetoothA2dp.getConnectedDevices();
                     if(deviceList!=null&&deviceList.size()>0){
                         devices.addAll(deviceList);
                     }
                 }
                 if(mBluetoothHeadset!=null){
//                     removeHeadsetMacEqual();
                     List<BluetoothDevice> deviceList=mBluetoothHeadset.getConnectedDevices();
                     if(deviceList!=null&&deviceList.size()>0){
                         devices.addAll(deviceList);
                     }
                 }
                 mBTConnectListener.onConnectedDevice(devices);
             }
//             else if(profile == BluetoothProfile.HEALTH){//健康
//                 mBluetoothHealth = (BluetoothHealth) proxy;
//             }
         }

         @Override
         public void onServiceDisconnected(int profile) {
             Log.i(TAG, "onServiceDisconnected profile="+profile);
             if(profile == BluetoothProfile.A2DP){
                 mBluetoothA2dp = null;
             }else if(profile == BluetoothProfile.HEADSET){
                 mBluetoothHeadset = null;
             }
//             else if(profile == BluetoothProfile.HEALTH) {
//                 mBluetoothHealth = null;
//             }

         }
     };



    private BroadcastReceiver mBluetoothReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice dev;
            int state;
            if (action == null) {
                return;
            }
            switch (action) {
                /**
                 * 蓝牙开关状态
                 * int STATE_OFF = 10; //蓝牙关闭
                 * int STATE_ON = 12; //蓝牙打开
                 * int STATE_TURNING_OFF = 13; //蓝牙正在关闭
                 * int STATE_TURNING_ON = 11; //蓝牙正在打开
                 */
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    if(mBTStateListener!=null){
                        mBTStateListener.onStateChange(state);
                    }
                    break;
                /**
                 * 蓝牙开始搜索
                 */
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    Log.i(TAG, "蓝牙开始搜索");
                    if(mBTScanListener!=null){
                        mBTScanListener.onScanStart();
                    }
                    break;
                /**
                 * 蓝牙搜索结束
                 */
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    Log.i(TAG, "蓝牙扫描结束");
                    if(mBTScanListener!=null){
                        mBTScanListener.onScanStop(null);
                    }
                    break;
                /**
                 * 发现新设备
                 */
                case BluetoothDevice.ACTION_FOUND:
                    dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                    short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,(short)0);//信号强度
                    if(mBTScanListener!=null){
                        mBTScanListener.onFindDevice(dev);
                    }
                    break;
                /**
                 * 设备配对状态改变
                 * int BOND_NONE = 10; //配对没有成功
                 * int BOND_BONDING = 11; //配对中
                 * int BOND_BONDED = 12; //配对成功
                 */
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                    dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if(mBTBoudListener!=null){
                        mBTBoudListener.onBondStateChange(dev);
                    }
                    Log.i(TAG, "设备配对状态改变：" + dev.getBondState());
                    break;
                /**
                 * 设备建立连接
                 * int STATE_DISCONNECTED = 0; //未连接
                 * int STATE_CONNECTING = 1; //连接中
                 * int STATE_CONNECTED = 2; //连接成功
                 */
                case BluetoothDevice.ACTION_ACL_CONNECTED:
                    dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    Log.i(TAG, "设备建立连接：" + dev.getBondState());
//                    mCallback.onConnect(dev);
                    break;
                /**
                 * 设备断开连接
                 */
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // mCallback.onConnect(dev.getBondState(), dev);
                    break;
                /**
                 * 本地蓝牙适配器
                 * BluetoothAdapter连接状态
                 */
                case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED:
                    dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    Log.i(TAG, "Adapter STATE: " + intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, 0));
                    Log.i(TAG, "BluetoothDevice: " + dev.getName() + ", " + dev.getAddress());
                    break;
                /**
                 * 提供用于手机的蓝牙耳机支持
                 * BluetoothHeadset连接状态
                 */
                case BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED:
                    dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    Log.i(TAG, "Headset STATE: " + intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, 0));
                    Log.i(TAG, "BluetoothDevice: " + dev.getName() + ", " + dev.getAddress());
                    switch (intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, -1)) {
                        case BluetoothHeadset.STATE_CONNECTING://连接中
                            if(mBTConnectListener!=null){
                                mBTConnectListener.onConnecting(dev);
                            }
                            break;
                        case BluetoothHeadset.STATE_CONNECTED://已连接
                            if(mBTConnectListener!=null){
                                mBTConnectListener.onConnected(dev);
                            }
                            break;
                        case BluetoothHeadset.STATE_DISCONNECTED://断开
                            if(mBTConnectListener!=null){
                                mBTConnectListener.onDisConnect(dev);
                            }
                            break;
                        case BluetoothHeadset.STATE_DISCONNECTING://断开中
                            if(mBTConnectListener!=null){
                                mBTConnectListener.onDisConnecting(dev);
                            }
                            break;
                    }
                    break;
                /**
                 * 定义高质量音频可以从一个设备通过蓝牙连接传输到另一个设备
                 * BluetoothA2dp连接状态
                 */
                case BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED:
                    dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    switch (intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, -1)) {
                        case BluetoothA2dp.STATE_CONNECTING:
                            Log.i(TAG,"A2dp device: " + dev.getName() + " connecting");
                            if(mBTConnectListener!=null){
                                mBTConnectListener.onConnecting(dev);
                            }
                            break;
                        case BluetoothA2dp.STATE_CONNECTED:
                            Log.i(TAG,"A2dp device: " + dev.getName() + " connected");
                            if(mBTConnectListener!=null){
                                mBTConnectListener.onConnected(dev);
                            }
                            break;
                        case BluetoothA2dp.STATE_DISCONNECTING:
                            Log.i(TAG,"A2dp device: " + dev.getName() + " disconnecting");
                            if(mBTConnectListener!=null){
                                mBTConnectListener.onDisConnecting(dev);
                            }
                            break;
                        case BluetoothA2dp.STATE_DISCONNECTED:
                            Log.i(TAG,"A2dp device: " + dev.getName() + " disconnected");
                            if(mBTConnectListener!=null){
                                mBTConnectListener.onDisConnect(dev);
                            }

                            break;
                        default:
                            break;
                    }
                default:
                    break;
            }
        }
    };
}
